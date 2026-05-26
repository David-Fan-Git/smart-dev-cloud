package com.develop.mvp.pk.framework.tenant.core.context;

import cn.hutool.core.util.StrUtil;
import com.develop.mvp.pk.framework.common.enums.DocumentEnum;
import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 多租户上下文 Holder。
 *
 * <p>它保存的是“当前执行链路”的租户编号和忽略租户标记：Web 请求入口会从 {@code tenant-id} 请求头写入租户编号，
 * MQ、Job、RPC 等入口也可以在各自流程中写入或读取该上下文。DB 拦截器、Redis 缓存、Feign 透传等组件再从这里读取，
 * 从而避免每一层方法都显式传递 tenantId。</p>
 *
 * <p>底层使用 {@link TransmittableThreadLocal}，适合“请求线程内同步调用”以及接入 TTL 的异步任务上下文传递。
 * 只要线程会被复用，入口流程结束时就必须调用 {@link #clear()}；否则旧租户编号或 ignore 标记可能污染下一次请求、消息或任务。</p>
 *
 * @author David
 */
public class TenantContextHolder {

    /**
     * 当前租户编号
     */
    private static final ThreadLocal<Long> TENANT_ID = new TransmittableThreadLocal<>();

    /**
     * 是否忽略租户
     */
    private static final ThreadLocal<Boolean> IGNORE = new TransmittableThreadLocal<>();

    /**
     * 获得当前链路中的租户编号。
     *
     * <p>在 HTTP 请求中，该值通常来自 {@code tenant-id} 请求头，并由
     * {@link com.develop.mvp.pk.framework.tenant.core.web.TenantContextWebFilter} 写入；在消息、任务或内部工具调用中，
     * 则由对应入口自行写入。返回 {@code null} 只表示当前上下文没有租户编号，不等价于已经开启 ignore tenant。</p>
     *
     * @return 租户编号
     */
    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    /**
     * 获得必须存在的租户编号。
     *
     * <p>DB 租户拦截器在需要给 SQL 拼接租户条件时会调用本方法。此时如果上下文缺少 tenantId，说明入口没有提供
     * {@code tenant-id} 或没有正确建立上下文，应当立即失败；它不会自动当作 ignore tenant 处理，避免无意间查询到跨租户数据。</p>
     *
     * @return 租户编号
     */
    public static Long getRequiredTenantId() {
        Long tenantId = getTenantId();
        if (tenantId == null) {
            throw new NullPointerException("TenantContextHolder 不存在租户编号！可参考文档："
                + DocumentEnum.TENANT.getUrl());
        }
        return tenantId;
    }

    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static void setIgnore(Boolean ignore) {
        IGNORE.set(ignore);
    }

    /**
     * 当前是否显式忽略租户。
     *
     * <p>ignore tenant 是由 {@code @TenantIgnore}、{@code TenantUtils.executeIgnore(...)} 或安全过滤器等入口显式设置的绕过标记，
     * 语义是“这段极小范围的逻辑不拼租户条件”。它和 {@link #getTenantId()} 返回 {@code null} 不同：
     * 后者只是缺少租户编号，不能被当作允许跨租户访问。</p>
     *
     * @return 是否忽略
     */
    public static boolean isIgnore() {
        return Boolean.TRUE.equals(IGNORE.get());
    }

    /**
     * 清理当前线程保存的租户编号和忽略标记。
     *
     * <p>Web 容器线程、MQ 消费线程、任务线程通常都会复用。入口流程结束后必须清理 ThreadLocal，
     * 否则下一次复用同一线程时可能读到旧 tenantId 或旧 ignore 状态。只有在明确的入口边界结束处清理才安全，
     * 业务中途不要随意清理，以免后续 DB、Redis、RPC 无法读取当前租户。</p>
     */
    public static void clear() {
        TENANT_ID.remove();
        IGNORE.remove();
    }

}
