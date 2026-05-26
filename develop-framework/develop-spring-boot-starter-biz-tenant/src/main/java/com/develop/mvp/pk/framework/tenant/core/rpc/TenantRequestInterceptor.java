package com.develop.mvp.pk.framework.tenant.core.rpc;

import com.develop.mvp.pk.framework.tenant.core.context.TenantContextHolder;
import com.develop.mvp.pk.framework.web.core.util.WebFrameworkUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;

import static com.develop.mvp.pk.framework.web.core.util.WebFrameworkUtils.HEADER_TENANT_ID;

/**
 * Tenant 的 Feign {@link RequestInterceptor} 实现类。
 *
 * <p>当当前服务通过 OpenFeign 调用其他服务时，本拦截器从 {@link TenantContextHolder} 读取当前租户编号，
 * 并写入出站请求 Header 的 {@code tenant-id}，让被调用服务的 Web 入口能够按同一租户继续处理。
 * 它只负责透传已经存在的上下文，不负责识别租户、校验租户，也不会在缺少 tenantId 时伪造默认值。</p>
 *
 * @author David
 */
public class TenantRequestInterceptor implements RequestInterceptor {

    /**
     * 将当前租户编号写入 Feign 出站请求头。
     *
     * <p>tenantId 来自当前线程的 {@link TenantContextHolder}，通常由入站 HTTP 请求、消息消费或任务入口建立。
     * 只有存在租户编号时才写入 {@link WebFrameworkUtils#HEADER_TENANT_ID}；缺失时保持请求头不变，
     * 由被调用方按自己的入口规则处理 missing tenant 或 ignore tenant。</p>
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId != null) {
            requestTemplate.header(HEADER_TENANT_ID, String.valueOf(tenantId));
        }
    }

}
