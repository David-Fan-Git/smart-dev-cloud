package com.develop.mvp.pk.module.system.api.mail.remote;

import com.develop.mvp.pk.module.system.api.mail.MailSendApi;
import com.develop.mvp.pk.module.system.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = ApiConstants.NAME, contextId = "systemMailSendRemoteClient")
public interface MailSendRemoteClient extends MailSendApi {
}
