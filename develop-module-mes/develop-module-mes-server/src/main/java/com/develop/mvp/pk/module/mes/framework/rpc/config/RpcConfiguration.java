package com.develop.mvp.pk.module.mes.framework.rpc.config;

import com.develop.mvp.pk.module.system.api.dept.remote.PostRemoteClient;
import com.develop.mvp.pk.module.system.api.dict.remote.DictDataRemoteClient;
import com.develop.mvp.pk.module.system.api.permission.remote.RoleRemoteClient;
import com.develop.mvp.pk.module.system.api.user.remote.AdminUserRemoteClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "mesRpcConfiguration", proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "develop.rpc.remote.system", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableFeignClients(clients = {AdminUserRemoteClient.class, PostRemoteClient.class, RoleRemoteClient.class, DictDataRemoteClient.class})
public class RpcConfiguration {
}
