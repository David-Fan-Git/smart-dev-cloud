package com.develop.mvp.pk.framework.security.core.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.util.Assert;

/**
 * 基于 TransmittableThreadLocal 实现的 Spring Security 上下文持有策略。
 *
 * <p>Spring Security 会把当前请求的认证信息保存在 {@link SecurityContext} 中，业务代码读取登录用户时，
 * 实际上就是从 {@code SecurityContextHolder} 当前策略管理的上下文中取值。默认 ThreadLocal 只能在当前线程内可见，
 * 本策略使用 {@link TransmittableThreadLocal}，用于支持 {@code @Async} 等异步执行场景下传递登录态。</p>
 *
 * <p>该类只定义上下文的创建、读取、替换和清理方式；具体何时写入登录用户由认证过滤器和 Spring Security 过滤器链决定。</p>
 *
 * @author David
 */
public class TransmittableThreadLocalSecurityContextHolderStrategy implements SecurityContextHolderStrategy {

    /**
     * 使用 TransmittableThreadLocal 作为上下文
     */
    private static final ThreadLocal<SecurityContext> CONTEXT_HOLDER = new TransmittableThreadLocal<>();

    /**
     * 清理当前线程绑定的安全上下文。
     *
     * <p>调用该方法时，会移除当前线程变量中的认证信息，避免旧登录态继续留在当前上下文中。</p>
     */
    @Override
    public void clearContext() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * 获取当前线程的安全上下文。
     *
     * <p>如果当前线程还没有上下文，会立即创建一个空的 {@link SecurityContext} 并保存下来，
     * 这样后续过滤器或业务代码可以在同一个上下文对象上写入、读取认证信息。</p>
     */
    @Override
    public SecurityContext getContext() {
        SecurityContext ctx = CONTEXT_HOLDER.get();
        if (ctx == null) {
            ctx = createEmptyContext();
            CONTEXT_HOLDER.set(ctx);
        }
        return ctx;
    }

    /**
     * 替换当前线程的安全上下文。
     *
     * <p>认证成功、测试准备或框架内部需要整体替换上下文时会调用该方法。
     * 传入值必须非空，避免业务代码后续读取上下文时出现不可预期的空引用。</p>
     */
    @Override
    public void setContext(SecurityContext context) {
        Assert.notNull(context, "Only non-null SecurityContext instances are permitted");
        CONTEXT_HOLDER.set(context);
    }

    /**
     * 创建一个不包含认证信息的空安全上下文。
     *
     * <p>首次读取上下文但当前线程尚未绑定上下文时，会使用该方法创建容器对象；
     * 后续认证流程可以再把登录用户写入这个上下文。</p>
     */
    @Override
    public SecurityContext createEmptyContext() {
        return new SecurityContextImpl();
    }

}
