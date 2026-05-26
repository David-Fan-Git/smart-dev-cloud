package com.develop.mvp.pk.module.product.framework.rpc.config;

import com.develop.mvp.pk.module.member.api.level.remote.MemberLevelRemoteClient;
import com.develop.mvp.pk.module.member.api.user.remote.MemberUserRemoteClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "productRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {MemberUserRemoteClient.class, MemberLevelRemoteClient.class})
public class RpcConfiguration {
}
