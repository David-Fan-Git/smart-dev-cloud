package com.develop.mvp.pk.framework.datapermission.core.rpc;

import com.develop.mvp.pk.framework.datapermission.core.annotation.DataPermission;
import com.develop.mvp.pk.framework.datapermission.core.aop.DataPermissionContextHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * 数据权限标记的 Feign 请求拦截器。
 *
 * <p>当当前线程存在 {@link DataPermission} 上下文，并且注解显式设置 {@code enable = false} 时，
 * 本拦截器会在 Feign 请求头中写入 {@link #ENABLE_HEADER_NAME}，把“本次调用关闭数据权限”的标记透传给被调用服务。
 *
 * <p>注意：{@link DataPermission} 注解本身不能直接序列化和反序列化，因此这里仅透传 {@code enable} 为
 * {@code false} 的场景，不透传 {@code includeRules} 或 {@code excludeRules}。下游 WebFilter 收到该请求头后会建立
 * {@code enable = false} 的数据权限上下文，使该调用链默认不生成数据权限规则；如果下游方法重新声明数据权限注解，则以当前上下文最近一层为准。
 *
 * @author David
 */
public class DataPermissionRequestInterceptor implements RequestInterceptor {

    public static final String ENABLE_HEADER_NAME = "data-permission-enable";

    /**
     * 在发起 Feign 请求前按需写入数据权限请求头。
     *
     * <p>写入边界非常保守：只有当前上下文存在 {@link DataPermission}，并且 {@code enable()} 明确为
     * {@link Boolean#FALSE} 时，才写入 {@code data-permission-enable: false}。默认开启、未声明注解、
     * 只配置规则包含/排除的情况都不会在这里写请求头。
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        DataPermission dataPermission = DataPermissionContextHolder.get();
        if (dataPermission != null && Boolean.FALSE.equals(dataPermission.enable())) {
            requestTemplate.header(ENABLE_HEADER_NAME, "false");
        }
    }

}
