package com.develop.mvp.pk.module.iot.framework.rpc.config;

import com.develop.mvp.pk.module.system.api.mail.remote.MailSendRemoteClient;
import com.develop.mvp.pk.module.system.api.notify.remote.NotifyMessageSendRemoteClient;
import com.develop.mvp.pk.module.system.api.sms.remote.SmsSendRemoteClient;
import com.develop.mvp.pk.module.system.api.user.remote.AdminUserRemoteClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "iotRpcConfiguration", proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "develop.rpc.remote.system", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableFeignClients(clients = {
        AdminUserRemoteClient.class, SmsSendRemoteClient.class, MailSendRemoteClient.class, NotifyMessageSendRemoteClient.class
})
public class RpcConfiguration {
}
