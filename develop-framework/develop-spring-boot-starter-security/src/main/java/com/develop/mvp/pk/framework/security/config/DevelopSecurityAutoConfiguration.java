package com.develop.mvp.pk.framework.security.config;

import com.develop.mvp.pk.framework.common.biz.system.permission.PermissionCommonApi;
import com.develop.mvp.pk.framework.security.core.context.TransmittableThreadLocalSecurityContextHolderStrategy;
import com.develop.mvp.pk.framework.security.core.filter.TokenAuthenticationFilter;
import com.develop.mvp.pk.framework.security.core.handler.AccessDeniedHandlerImpl;
import com.develop.mvp.pk.framework.security.core.handler.AuthenticationEntryPointImpl;
import com.develop.mvp.pk.framework.security.core.service.SecurityFrameworkService;
import com.develop.mvp.pk.framework.security.core.service.SecurityFrameworkServiceImpl;
import com.develop.mvp.pk.framework.web.core.handler.GlobalExceptionHandler;
import com.develop.mvp.pk.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * Spring Security 自动配置类，负责把安全链路中的核心组件交给 Spring 容器管理。
 *
 * <p>一次 HTTP 请求进入系统后，安全链路大致分为：客户端携带 token 或网关透传的登录用户信息；
 * {@link TokenAuthenticationFilter} 在过滤器链中解析并校验身份；校验通过后把 {@code LoginUser}
 * 写入 {@link SecurityContextHolder}；业务代码再通过安全工具类或 {@code @PreAuthorize} 等能力读取当前登录用户、
 * 执行权限判断。本类只负责装配这些公共 Bean，不直接处理某个具体请求。</p>
 *
 * <p>注意，不能和 {@link DevelopWebSecurityConfigurerAdapter} 放在同一个配置类，原因是会导致初始化报错。
 * 参见 https://stackoverflow.com/questions/53847050/spring-boot-delegatebuilder-cannot-be-null-on-autowiring-authenticationmanager 文档。</p>
 *
 * @author David
 */
@AutoConfiguration
@AutoConfigureOrder(-1) // 目的：先于 Spring Security 自动配置，避免一键改包后，org.* 基础包无法生效
@EnableConfigurationProperties(SecurityProperties.class)
public class DevelopSecurityAutoConfiguration {

    @Resource
    private SecurityProperties securityProperties;

    /**
     * 注册认证失败处理器。
     *
     * <p>当请求没有有效登录态、需要登录却未登录时，Spring Security 会使用该 Bean 生成统一的未认证响应。</p>
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new AuthenticationEntryPointImpl();
    }

    /**
     * 注册授权失败处理器。
     *
     * <p>当请求已经识别出登录用户，但该用户没有访问目标资源所需权限时，Spring Security 会使用该 Bean
     * 生成统一的无权限响应。</p>
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new AccessDeniedHandlerImpl();
    }

    /**
     * 注册 Spring Security 使用的密码编码器。
     *
     * <p>用户登录或修改密码时，认证逻辑会通过该 Bean 完成密码哈希与比对。这里使用 BCrypt，
     * 并从 {@link SecurityProperties} 读取强度参数。</p>
     *
     * @see <a href="http://stackabuse.com/password-encoding-with-spring-security/">Password Encoding with Spring Security</a>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(securityProperties.getPasswordEncoderLength());
    }

    /**
     * 注册 token 认证过滤器。
     *
     * <p>该过滤器会在请求过滤链中尝试从请求头、请求参数或网关透传头中恢复登录用户。
     * 如果 token 校验过程抛出异常，会交给全局异常处理器转换为统一响应。</p>
     */
    @Bean
    public TokenAuthenticationFilter authenticationTokenFilter(GlobalExceptionHandler globalExceptionHandler,
                                                               OAuth2TokenCommonApi oauth2TokenApi) {
        return new TokenAuthenticationFilter(securityProperties, globalExceptionHandler, oauth2TokenApi);
    }

    /**
     * 注册安全框架服务，并暴露为 SpEL 中常用的 {@code ss} Bean。
     *
     * <p>控制器或业务入口上的权限表达式可以通过该服务读取当前登录用户的权限信息，
     * 例如判断菜单权限、角色权限或数据访问条件。</p>
     */
    @Bean("ss") // 使用 Spring Security 的缩写，方便使用
    public SecurityFrameworkService securityFrameworkService(PermissionCommonApi permissionApi) {
        return new SecurityFrameworkServiceImpl(permissionApi);
    }

    /**
     * 注册 SecurityContextHolder 策略切换 Bean。
     *
     * <p>容器初始化该 Bean 时，会调用 {@link SecurityContextHolder#setStrategyName(String)}，
     * 将 Spring Security 默认的上下文持有策略替换为 {@link TransmittableThreadLocalSecurityContextHolderStrategy}。
     * 这样请求线程中保存的登录态，在支持 TTL 的异步任务中也能被传递。</p>
     */
    @Bean
    public MethodInvokingFactoryBean securityContextHolderMethodInvokingFactoryBean() {
        MethodInvokingFactoryBean methodInvokingFactoryBean = new MethodInvokingFactoryBean();
        methodInvokingFactoryBean.setTargetClass(SecurityContextHolder.class);
        methodInvokingFactoryBean.setTargetMethod("setStrategyName");
        methodInvokingFactoryBean.setArguments(TransmittableThreadLocalSecurityContextHolderStrategy.class.getName());
        return methodInvokingFactoryBean;
    }

}
