package com.develop.mvp.pk.module.system.api.permission.remote;

import com.develop.mvp.pk.module.system.api.permission.RoleApi;
import com.develop.mvp.pk.module.system.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = ApiConstants.NAME, contextId = "systemRoleRemoteClient")
public interface RoleRemoteClient extends RoleApi {
}
