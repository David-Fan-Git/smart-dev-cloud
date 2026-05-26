package com.develop.mvp.pk.framework.datapermission.core.aop;

import com.develop.mvp.pk.framework.datapermission.core.annotation.DataPermission;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;

/**
 * {@link DataPermission} 注解的 Advisor 实现类。
 *
 * <p>Advisor 是 Spring AOP 的入口描述对象，由它告诉 Spring：哪些方法需要进入数据权限注解流程，
 * 以及进入流程后应该执行哪个 Advice。本实现同时匹配类级和方法级 {@link DataPermission} 注解：
 * 只要目标类或目标方法上存在该注解，就会交给 {@link DataPermissionAnnotationInterceptor} 处理。
 *
 * <p>需要注意，Advisor 只决定“是否进入 AOP 流程”，不判断具体规则是否启用，也不直接参与 MyBatis SQL 条件生成。
 *
 * @author David
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class DataPermissionAnnotationAdvisor extends AbstractPointcutAdvisor {

    private final Advice advice;

    private final Pointcut pointcut;

    public DataPermissionAnnotationAdvisor() {
        this.advice = new DataPermissionAnnotationInterceptor();
        this.pointcut = this.buildPointcut();
    }

    /**
     * 构建数据权限注解的切点。
     *
     * <p>类级切点用于匹配整个类都需要数据权限上下文的方法，方法级切点用于匹配只在单个方法上声明的数据权限。
     * 两个切点取并集，因此任一位置存在 {@link DataPermission} 注解都会触发后续拦截器。
     */
    protected Pointcut buildPointcut() {
        Pointcut classPointcut = new AnnotationMatchingPointcut(DataPermission.class, true);
        Pointcut methodPointcut = new AnnotationMatchingPointcut(null, DataPermission.class, true);
        return new ComposablePointcut(classPointcut).union(methodPointcut);
    }

}
