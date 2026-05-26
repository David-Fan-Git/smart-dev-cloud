package com.develop.mvp.pk.module.system.api.notify.remote;

import com.develop.mvp.pk.module.system.api.notify.NotifyMessageSendApi;
import com.develop.mvp.pk.module.system.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = ApiConstants.NAME, contextId = "systemNotifyMessageSendRemoteClient")
public interface NotifyMessageSendRemoteClient extends NotifyMessageSendApi {
}
