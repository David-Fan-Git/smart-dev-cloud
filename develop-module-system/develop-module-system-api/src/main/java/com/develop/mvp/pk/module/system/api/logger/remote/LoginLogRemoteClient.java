package com.develop.mvp.pk.module.system.api.logger.remote;

import com.develop.mvp.pk.module.system.api.logger.LoginLogApi;
import com.develop.mvp.pk.module.system.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = ApiConstants.NAME, contextId = "systemLoginLogRemoteClient")
public interface LoginLogRemoteClient extends LoginLogApi {
}
