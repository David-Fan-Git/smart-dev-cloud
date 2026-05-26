package com.develop.mvp.pk.module.system.api.sms.remote;

import com.develop.mvp.pk.module.system.api.sms.SmsCodeApi;
import com.develop.mvp.pk.module.system.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = ApiConstants.NAME, contextId = "systemSmsCodeRemoteClient")
public interface SmsCodeRemoteClient extends SmsCodeApi {
}
