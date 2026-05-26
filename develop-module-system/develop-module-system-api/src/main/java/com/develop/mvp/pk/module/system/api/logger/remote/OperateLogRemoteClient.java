package com.develop.mvp.pk.module.system.api.logger.remote;

import com.develop.mvp.pk.module.system.api.logger.OperateLogApi;
import com.develop.mvp.pk.module.system.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = ApiConstants.NAME, contextId = "systemOperateLogRemoteClient")
public interface OperateLogRemoteClient extends OperateLogApi {
}
