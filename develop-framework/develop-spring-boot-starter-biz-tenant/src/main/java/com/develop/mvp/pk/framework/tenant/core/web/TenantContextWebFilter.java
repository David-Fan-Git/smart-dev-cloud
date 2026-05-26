package com.develop.mvp.pk.framework.tenant.core.web;

import com.develop.mvp.pk.framework.tenant.core.context.TenantContextHolder;
import com.develop.mvp.pk.framework.web.core.util.WebFrameworkUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 多租户 Context Web 过滤器。
 *
 * <p>这是 HTTP 请求进入应用后建立租户上下文的入口：它从请求 Header 的 {@code tenant-id} 读取租户编号，
 * 写入 {@link TenantContextHolder}，让后续 Controller、Service、DB 拦截器、Redis 缓存和 Feign RPC 都能从当前线程读取同一个租户编号。
 * 当前实现只接受可解析为数字的 header；没有传或格式非法时不会写入 tenantId，后续必须租户编号的组件会按缺失处理。</p>
 *
 * <p>过滤器继承 {@link OncePerRequestFilter}，保证同一次 Servlet 请求只执行一次。请求链结束后必须清理上下文，
 * 因为 Servlet 容器线程会复用，不清理会导致下一个请求误用上一个请求的租户编号或 ignore 标记。</p>
 *
 * @author David
 */
public class TenantContextWebFilter extends OncePerRequestFilter {

    /**
     * 从当前 HTTP 请求建立租户上下文，并在请求结束时清理。
     *
     * <p>本过滤器只从 {@link WebFrameworkUtils#getTenantId(HttpServletRequest)} 读取 {@code tenant-id} 请求头。
     * 当请求头不存在或不是数字时，这里不会设置租户编号；这只是 missing tenant，不代表开启了 ignore tenant。
     * 后续安全过滤器仍可能根据登录用户补充租户编号；完整链路结束后仍没有 tenantId 时，DB 拦截器才会按租户缺失处理。</p>
     *
     * <p>{@code finally} 中的 {@link TenantContextHolder#clear()} 是流程边界清理：无论 Controller 正常返回还是抛异常，
     * 都要移除 ThreadLocal 中的 tenantId 和 ignore 标记，防止容器线程复用造成租户串扰。</p>
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // 设置
        Long tenantId = WebFrameworkUtils.getTenantId(request);
        if (tenantId != null) {
            TenantContextHolder.setTenantId(tenantId);
        }
        try {
            chain.doFilter(request, response);
        } finally {
            // 清理
            TenantContextHolder.clear();
        }
    }

}
