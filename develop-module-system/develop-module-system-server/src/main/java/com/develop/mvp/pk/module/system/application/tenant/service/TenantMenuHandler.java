package com.develop.mvp.pk.module.system.application.tenant.service;

import java.util.Set;

/**
 * Tenant Menu Handler 接口。
 */
public interface TenantMenuHandler {

    /**
     * 处理 handle 对应的业务逻辑。
     *
     * @param menuIds menuIds 参数
     */
    void handle(Set<Long> menuIds);
}
