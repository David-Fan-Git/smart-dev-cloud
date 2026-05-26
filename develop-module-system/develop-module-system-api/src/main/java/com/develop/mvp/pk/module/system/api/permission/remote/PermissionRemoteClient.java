package com.develop.mvp.pk.module.system.api.permission.remote;

import com.develop.mvp.pk.module.system.api.permission.PermissionApi;
import com.develop.mvp.pk.module.system.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = ApiConstants.NAME, contextId = "systemPermissionRemoteClient")
public interface PermissionRemoteClient extends PermissionApi {
}
