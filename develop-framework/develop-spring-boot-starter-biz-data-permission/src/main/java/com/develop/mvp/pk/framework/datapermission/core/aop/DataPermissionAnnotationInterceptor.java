package com.develop.mvp.pk.framework.datapermission.core.aop;

import com.develop.mvp.pk.framework.datapermission.core.annotation.DataPermission;
import lombok.Getter;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.MethodClassKey;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link DataPermission} 注解的 AOP 拦截器。
 *
 * <p>当 {@link DataPermissionAnnotationAdvisor} 判断目标类或方法命中数据权限切点后，
 * Spring AOP 会在业务方法执行前调用本拦截器。本拦截器的职责很窄：查找当前方法实际生效的
 * {@link DataPermission} 注解，并在方法执行期间写入 {@link DataPermissionContextHolder}，
 * 让后续 MyBatis 查询可以读取到当前上下文。
 *
 * <p>方法执行结束后，无论成功还是抛异常，都会在 {@code finally} 中恢复进入方法前的上下文层级。
 * 这里不生成 SQL 条件，也不判断表名，具体规则选择和 SQL 条件拼接由规则工厂与 MyBatis 处理器完成。
 *
 * @author David
 */
@DataPermission // 该注解，用于 {@link DATA_PERMISSION_NULL} 的空对象
public class DataPermissionAnnotationInterceptor implements MethodInterceptor {

    /**
     * DataPermission 空对象，用于方法无 {@link DataPermission} 注解时，使用 DATA_PERMISSION_NULL 进行占位
     */
    static final DataPermission DATA_PERMISSION_NULL = DataPermissionAnnotationInterceptor.class.getAnnotation(DataPermission.class);

    @Getter
    private final Map<MethodClassKey, DataPermission> dataPermissionCache = new ConcurrentHashMap<>();

    /**
     * 围绕业务方法维护数据权限上下文。
     *
     * <p>AOP 触发时，先解析当前方法或目标类上的 {@link DataPermission} 注解；如果存在注解，就把它追加到
     * {@link DataPermissionContextHolder} 的当前线程列表末尾。这样业务方法内部发起 Mapper 查询时，
     * 规则工厂可以读取到最近一次进入的数据权限注解。
     *
     * <p>使用 {@code finally} 移除上下文是为了处理异常场景：即使业务方法抛出异常，也必须恢复调用前状态，
     * 避免 ThreadLocal 中残留的注解影响同一线程后续请求。嵌套调用时，本方法只移除自己写入的最后一个注解，
     * 外层方法的上下文仍保留到外层拦截器退出时再恢复。
     */
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        // 入栈
        DataPermission dataPermission = this.findAnnotation(methodInvocation);
        if (dataPermission != null) {
            DataPermissionContextHolder.add(dataPermission);
        }
        try {
            // 执行逻辑
            return methodInvocation.proceed();
        } finally {
            // 出栈
            if (dataPermission != null) {
                DataPermissionContextHolder.remove();
            }
        }
    }

    /**
     * 查找当前调用实际使用的 {@link DataPermission} 注解。
     *
     * <p>优先读取方法上的注解，方法没有声明时再读取目标类上的注解。解析结果按
     * {@link MethodClassKey} 缓存，避免每次 AOP 调用都重复做反射查找。没有注解的方法使用
     * {@link #DATA_PERMISSION_NULL} 作为空对象占位，表示“已经查过但不存在注解”，避免缓存穿透。
     */
    private DataPermission findAnnotation(MethodInvocation methodInvocation) {
        // 1. 从缓存中获取
        Method method = methodInvocation.getMethod();
        Object targetObject = methodInvocation.getThis();
        Class<?> clazz = targetObject != null ? targetObject.getClass() : method.getDeclaringClass();
        MethodClassKey methodClassKey = new MethodClassKey(method, clazz);
        DataPermission dataPermission = dataPermissionCache.get(methodClassKey);
        if (dataPermission != null) {
            return dataPermission != DATA_PERMISSION_NULL ? dataPermission : null;
        }

        // 2.1 从方法中获取
        dataPermission = AnnotationUtils.findAnnotation(method, DataPermission.class);
        // 2.2 从类上获取
        if (dataPermission == null) {
            dataPermission = AnnotationUtils.findAnnotation(clazz, DataPermission.class);
        }
        // 2.3 添加到缓存中
        dataPermissionCache.put(methodClassKey, dataPermission != null ? dataPermission : DATA_PERMISSION_NULL);
        return dataPermission;
    }

}
