package com.develop.mvp.pk.framework.tenant.core.aop;

import com.develop.mvp.pk.framework.common.util.spring.SpringExpressionUtils;
import com.develop.mvp.pk.framework.tenant.core.context.TenantContextHolder;
import com.develop.mvp.pk.framework.tenant.core.util.TenantUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * 忽略多租户的 Aspect，基于 {@link TenantIgnore} 注解实现，用于少量明确需要跨租户执行的全局逻辑。
 * 例如说，一个定时任务，读取所有数据，进行处理。
 * 又例如说，读取所有数据，进行缓存。
 *
 * <p>{@code @TenantIgnore} 的作用不是提供默认租户编号，而是在被注解方法执行期间临时把
 * {@link TenantContextHolder} 的 ignore 标记置为 {@code true}。DB 拦截器等组件看到该标记后会跳过租户条件。
 * 因此它应当只包住最小必要范围，避免把普通业务调用意外扩大成跨租户查询。</p>
 *
 * <p>整体逻辑的实现，和 {@link TenantUtils#executeIgnore(Runnable)} 需要保持一致。</p>
 *
 * @author David
 */
@Aspect
@Slf4j
public class TenantIgnoreAspect {

    /**
     * 在被 {@link TenantIgnore} 标记的方法周围临时切换 ignore tenant 状态。
     *
     * <p>{@link TenantIgnore#enable()} 支持 Spring 表达式；只有表达式结果为 {@code true} 时才会设置 ignore。
     * 方法执行前先记录旧值，执行后无论成功或异常都恢复旧值，保证嵌套调用和外层上下文不被破坏。
     * 这里不会清理 tenantId，因为它不是完整请求或任务入口，只负责一个窄范围方法调用的绕过语义。</p>
     */
    @Around("@annotation(tenantIgnore)")
    public Object around(ProceedingJoinPoint joinPoint, TenantIgnore tenantIgnore) throws Throwable {
        Boolean oldIgnore = TenantContextHolder.isIgnore();
        try {
            // 计算条件，满足的情况下，才进行忽略
            Object enable = SpringExpressionUtils.parseExpression(tenantIgnore.enable());
            if (Boolean.TRUE.equals(enable)) {
                TenantContextHolder.setIgnore(true);
            }

            // 执行逻辑
            return joinPoint.proceed();
        } finally {
            TenantContextHolder.setIgnore(oldIgnore);
        }
    }

}
