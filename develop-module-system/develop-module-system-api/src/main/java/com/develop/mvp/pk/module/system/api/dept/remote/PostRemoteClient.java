package com.develop.mvp.pk.module.system.api.dept.remote;

import com.develop.mvp.pk.module.system.api.dept.PostApi;
import com.develop.mvp.pk.module.system.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = ApiConstants.NAME, contextId = "systemPostRemoteClient")
public interface PostRemoteClient extends PostApi {
}
