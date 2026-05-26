package com.develop.mvp.pk.module.system.api.dict.remote;

import com.develop.mvp.pk.module.system.api.dict.DictDataApi;
import com.develop.mvp.pk.module.system.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = ApiConstants.NAME, contextId = "systemDictDataRemoteClient")
public interface DictDataRemoteClient extends DictDataApi {
}
