package com.develop.mvp.pk.module.system.api.dept.remote;

import com.develop.mvp.pk.module.system.api.dept.DeptApi;
import com.develop.mvp.pk.module.system.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = ApiConstants.NAME, contextId = "systemDeptRemoteClient")
public interface DeptRemoteClient extends DeptApi {
}
