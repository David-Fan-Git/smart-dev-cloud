package com.develop.mvp.pk.module.infra.api.config.remote;

import com.develop.mvp.pk.module.infra.api.config.ConfigApi;
import com.develop.mvp.pk.module.infra.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = ApiConstants.NAME, contextId = "infraConfigRemoteClient")
public interface ConfigRemoteClient extends ConfigApi {
}
