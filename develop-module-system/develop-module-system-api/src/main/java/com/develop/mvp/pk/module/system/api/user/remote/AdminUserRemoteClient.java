package com.develop.mvp.pk.module.system.api.user.remote;

import com.develop.mvp.pk.module.system.api.user.AdminUserApi;
import com.develop.mvp.pk.module.system.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = ApiConstants.NAME, contextId = "systemAdminUserRemoteClient")
public interface AdminUserRemoteClient extends AdminUserApi {
}
