package com.develop.mvp.pk.module.infra.api.file.remote;

import com.develop.mvp.pk.module.infra.api.file.FileApi;
import com.develop.mvp.pk.module.infra.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = ApiConstants.NAME, contextId = "infraFileRemoteClient")
public interface FileRemoteClient extends FileApi {
}
