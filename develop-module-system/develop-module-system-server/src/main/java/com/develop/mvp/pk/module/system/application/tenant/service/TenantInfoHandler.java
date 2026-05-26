package com.develop.mvp.pk.module.system.application.tenant.service;

import com.develop.mvp.pk.module.system.dal.dataobject.tenant.TenantDO;

/**
 * Tenant Info Handler 接口。
 */
public interface TenantInfoHandler {

    /**
     * 处理 handle 对应的业务逻辑。
     *
     * @param tenant tenant 参数
     */
    void handle(TenantDO tenant);
}
