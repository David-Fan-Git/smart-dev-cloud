package com.develop.mvp.pk.framework.datapermission.core.aop;

import com.develop.mvp.pk.framework.datapermission.core.annotation.DataPermission;
import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.LinkedList;
import java.util.List;

/**
 * {@link DataPermission} 注解的线程上下文。
 *
 * <p>它保存当前线程正在执行的数据权限注解，供 {@link com.develop.mvp.pk.framework.datapermission.core.rule.DataPermissionRuleFactoryImpl}
 * 在 MyBatis 查询前读取。上下文的生命周期通常由 {@link DataPermissionAnnotationInterceptor} 控制：
 * 方法进入时写入，方法退出时移除，最后一层上下文移除后清理 ThreadLocal。
 *
 * <p>内部使用 {@link LinkedList} 保存多个注解，是为了支持 AOP 方法嵌套调用。当前生效值始终取最后写入的一项；
 * 因此嵌套调用必须严格成对执行 {@link #add(DataPermission)} 与 {@link #remove()}，否则可能错误恢复外层上下文。
 *
 * @author David
 */
public class DataPermissionContextHolder {

    /**
     * 使用列表保存上下文层级，原因是同一线程内可能出现带 {@link DataPermission} 注解的方法嵌套调用。
     *
     * <p>{@link TransmittableThreadLocal} 可以在部分异步执行场景中传递父线程上下文，但这里保存的仍然只是
     * 当前数据权限注解栈，不代表异步任务可以绕过自己的查询边界或权限规则。
     */
    private static final ThreadLocal<LinkedList<DataPermission>> DATA_PERMISSIONS =
            TransmittableThreadLocal.withInitial(LinkedList::new);

    /**
     * 获得当前生效的 {@link DataPermission} 注解。
     *
     * <p>当前值是列表最后一项，也就是最近进入的带注解方法。没有上下文时返回 {@code null}，规则工厂会按默认开启规则处理。
     *
     * @return 当前 DataPermission 注解；没有上下文时返回 {@code null}
     */
    public static DataPermission get() {
        return DATA_PERMISSIONS.get().peekLast();
    }

    /**
     * 写入一层 {@link DataPermission} 注解上下文。
     *
     * <p>通常由 AOP 拦截器在业务方法执行前调用。写入后，方法内部触发的 MyBatis 查询会看到这层上下文；
     * 如果内部又调用另一个带数据权限注解的方法，新的注解会追加到列表末尾并临时覆盖当前值。
     *
     * @param dataPermission DataPermission 注解
     */
    public static void add(DataPermission dataPermission) {
        DATA_PERMISSIONS.get().addLast(dataPermission);
    }

    /**
     * 移除最近写入的一层 {@link DataPermission} 注解上下文。
     *
     * <p>通常由 AOP 拦截器在 {@code finally} 中调用，用于恢复进入当前方法之前的上下文。嵌套调用时，
     * 这里只移除最内层注解；如果移除后列表为空，会清理 ThreadLocal，避免线程复用时发生上下文泄漏。
     *
     * @return 被移除的 DataPermission 注解
     */
    public static DataPermission remove() {
        DataPermission dataPermission = DATA_PERMISSIONS.get().removeLast();
        // 无元素时，清空 ThreadLocal
        if (DATA_PERMISSIONS.get().isEmpty()) {
            DATA_PERMISSIONS.remove();
        }
        return dataPermission;
    }

    /**
     * 获得当前线程内保存的全部 {@link DataPermission} 上下文层级。
     *
     * <p>返回的是当前 ThreadLocal 中的列表对象，调用方不要把它当作跨线程或跨请求的全局状态使用。
     *
     * @return DataPermission 队列
     */
    public static List<DataPermission> getAll() {
        return DATA_PERMISSIONS.get();
    }

    /**
     * 清空当前线程的数据权限上下文。
     *
     * <p>目前仅用于单测。生产流程应优先依赖 AOP 的成对写入和移除，不应在业务代码中随意清空，
     * 否则可能影响外层方法尚未结束的数据权限上下文。
     */
    public static void clear() {
        DATA_PERMISSIONS.remove();
    }

}
