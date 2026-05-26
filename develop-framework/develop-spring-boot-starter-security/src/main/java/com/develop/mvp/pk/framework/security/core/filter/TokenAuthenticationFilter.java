package com.develop.mvp.pk.framework.security.core.filter;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.develop.mvp.pk.framework.common.exception.ServiceException;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.util.json.JsonUtils;
import com.develop.mvp.pk.framework.common.util.servlet.ServletUtils;
import com.develop.mvp.pk.framework.security.config.SecurityProperties;
import com.develop.mvp.pk.framework.security.core.LoginUser;
import com.develop.mvp.pk.framework.security.core.util.SecurityFrameworkUtils;
import com.develop.mvp.pk.framework.web.core.handler.GlobalExceptionHandler;
import com.develop.mvp.pk.framework.web.core.util.WebFrameworkUtils;
import com.develop.mvp.pk.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import com.develop.mvp.pk.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenCheckRespDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Token 认证过滤器，负责在每次请求进入业务代码前恢复当前登录用户。
 *
 * <p>请求可以通过两种方式携带身份信息：一种是网关或其它服务在请求头中透传序列化后的 {@link LoginUser}；
 * 另一种是客户端直接携带访问 token。本过滤器优先读取透传头，读取不到时再解析 token，
 * 并通过 OAuth2 token API 校验有效性。校验通过后，会把 {@link LoginUser} 写入 Spring Security 上下文，
 * 后续控制器、权限表达式和业务代码即可读取当前登录用户。</p>
 *
 * <p>该类继承 {@link OncePerRequestFilter}，因此同一次请求只执行一次过滤逻辑。</p>
 *
 * @author David
 */
@RequiredArgsConstructor
@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;

    private final GlobalExceptionHandler globalExceptionHandler;

    private final OAuth2TokenCommonApi oauth2TokenApi;

    /**
     * 在请求进入后续过滤器和业务处理前解析登录态。
     *
     * <p>触发时机是 Servlet 过滤器链执行到本过滤器时：先尝试从 {@code login-user} 请求头恢复网关或服务间透传的用户；
     * 如果没有透传用户，再从配置的 token 请求头或请求参数读取 token 并调用认证服务校验。
     * 只有解析到有效用户或开发调试用的模拟用户时才写入 SecurityContext；没有恢复出登录用户时，
     * 后续是否要求登录由 Spring Security 的访问规则决定。</p>
     */
    @Override
    @SuppressWarnings("NullableProblems")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // 情况一，基于 header[login-user] 获得用户，例如说来自 Gateway 或者其它服务透传
        LoginUser loginUser = buildLoginUserByHeader(request);

        // 情况二，基于 Token 获得用户
        // 注意，这里主要满足直接使用 Nginx 直接转发到 Spring Cloud 服务的场景。
        if (loginUser == null) {
            String token = SecurityFrameworkUtils.obtainAuthorization(request,
                    securityProperties.getTokenHeader(), securityProperties.getTokenParameter());
            if (StrUtil.isNotEmpty(token)) {
                Integer userType = WebFrameworkUtils.getLoginUserType(request);
                try {
                    // 1.1 基于 token 构建登录用户
                    loginUser = buildLoginUserByToken(token, userType);
                    // 1.2 模拟 Login 功能，方便日常开发调试
                    if (loginUser == null) {
                        loginUser = mockLoginUser(request, token, userType);
                    }
                } catch (Throwable ex) {
                    CommonResult<?> result = globalExceptionHandler.allExceptionHandler(request, ex);
                    ServletUtils.writeJSON(response, result);
                    return;
                }
            }
        }

        // 设置当前用户
        if (loginUser != null) {
            SecurityFrameworkUtils.setLoginUser(loginUser, request);
        }
        // 继续过滤链
        chain.doFilter(request, response);
    }

    /**
     * 根据访问 token 构建登录用户。
     *
     * <p>该方法只在请求未携带网关透传用户、但携带 token 时执行。它会调用 OAuth2 token API 校验 token，
     * 并在 admin/app 等能识别用户类型的入口上比对用户类型。校验失败的 {@link ServiceException} 会被转换为
     * {@code null}，表示当前请求没有恢复出登录用户。</p>
     */
    private LoginUser buildLoginUserByToken(String token, Integer userType) {
        try {
            // 校验访问令牌
            OAuth2AccessTokenCheckRespDTO accessToken = oauth2TokenApi.checkAccessToken(token).getCheckedData();
            if (accessToken == null) {
                return null;
            }
            // 用户类型不匹配，无权限
            // 注意：只有 /admin-api/* 和 /app-api/* 有 userType，才需要比对用户类型
            // 类似 WebSocket 的 /ws/* 连接地址，是不需要比对用户类型的
            if (userType != null
                    && ObjectUtil.notEqual(accessToken.getUserType(), userType)) {
                throw new AccessDeniedException("错误的用户类型");
            }
            // 构建登录用户
            return new LoginUser().setId(accessToken.getUserId()).setUserType(accessToken.getUserType())
                    .setInfo(accessToken.getUserInfo()) // 额外的用户信息
                    .setTenantId(accessToken.getTenantId()).setScopes(accessToken.getScopes())
                    .setExpiresTime(accessToken.getExpiresTime());
        } catch (ServiceException serviceException) {
            // 校验 Token 不通过时，考虑到一些接口是无需登录的，所以直接返回 null 即可
            return null;
        }
    }

    /**
     * 模拟登录用户，方便日常开发调试
     *
     * 注意，在线上环境下，一定要关闭该功能！！！
     *
     * @param request 请求
     * @param token 模拟的 token，格式为 {@link SecurityProperties#getMockSecret()} + 用户编号
     * @param userType 用户类型
     * @return 模拟的 LoginUser
     */
    private LoginUser mockLoginUser(HttpServletRequest request, String token, Integer userType) {
        if (!securityProperties.getMockEnable()) {
            return null;
        }
        // 必须以 mockSecret 开头
        if (!token.startsWith(securityProperties.getMockSecret())) {
            return null;
        }
        // 构建模拟用户
        Long userId = Long.valueOf(token.substring(securityProperties.getMockSecret().length()));
        return new LoginUser().setId(userId).setUserType(userType)
                .setTenantId(WebFrameworkUtils.getTenantId(request));
    }

    /**
     * 从请求头读取服务间透传的登录用户。
     *
     * <p>网关或上游服务已经完成认证时，会把 {@link LoginUser} 序列化后放入约定请求头。
     * 当前服务在这里反序列化该用户，并按当前请求入口的用户类型做一致性校验。解析失败会继续抛出异常，
     * 让调用链按 Spring Web/Security 的异常处理规则结束请求。</p>
     */
    private LoginUser buildLoginUserByHeader(HttpServletRequest request) {
        String loginUserStr = request.getHeader(SecurityFrameworkUtils.LOGIN_USER_HEADER);
        if (StrUtil.isEmpty(loginUserStr)) {
            return null;
        }
        try {
            loginUserStr = URLDecoder.decode(loginUserStr, StandardCharsets.UTF_8); // 解码，解决中文乱码问题
            LoginUser loginUser = JsonUtils.parseObject(loginUserStr, LoginUser.class);
            // 用户类型不匹配，无权限
            // 注意：只有 /admin-api/* 和 /app-api/* 有 userType，才需要比对用户类型
            // 类似 WebSocket 的 /ws/* 连接地址，是不需要比对用户类型的
            Integer userType = WebFrameworkUtils.getLoginUserType(request);
            if (userType != null
                    && loginUser != null
                    && ObjectUtil.notEqual(loginUser.getUserType(), userType)) {
                throw new AccessDeniedException("错误的用户类型");
            }
            return loginUser;
        } catch (Exception ex) {
            log.error("[buildLoginUserByHeader][解析 LoginUser({}) 发生异常]", loginUserStr, ex);  ;
            throw ex;
        }
    }

}
