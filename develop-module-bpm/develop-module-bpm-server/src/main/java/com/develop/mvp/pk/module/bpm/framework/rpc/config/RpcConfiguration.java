package com.develop.mvp.pk.module.bpm.framework.rpc.config;

import com.develop.mvp.pk.module.bpm.api.event.CrmContractStatusListener;
import com.develop.mvp.pk.module.bpm.api.event.CrmReceivableStatusListener;
import com.develop.mvp.pk.module.system.api.dept.remote.DeptRemoteClient;
import com.develop.mvp.pk.module.system.api.dept.remote.PostRemoteClient;
import com.develop.mvp.pk.module.system.api.dict.remote.DictDataRemoteClient;
import com.develop.mvp.pk.module.system.api.permission.remote.PermissionRemoteClient;
import com.develop.mvp.pk.module.system.api.permission.remote.RoleRemoteClient;
import com.develop.mvp.pk.module.system.api.sms.remote.SmsSendRemoteClient;
import com.develop.mvp.pk.module.system.api.user.remote.AdminUserRemoteClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "bpmRpcConfiguration", proxyBeanMethods = false)
public class RpcConfiguration {

    // ========== 特殊：解决微 develop-cloud 微服务场景下，跨服务（进程）无法 Listener 的问题 ==========

    @Bean
    @ConditionalOnMissingBean(name = "crmReceivableStatusListener")
    public CrmReceivableStatusListener crmReceivableStatusListener() {
        return new CrmReceivableStatusListener();
    }

    @Bean
    @ConditionalOnMissingBean(name = "crmContractStatusListener")
    public CrmContractStatusListener crmContractStatusListener() {
        return new CrmContractStatusListener();
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "develop.rpc.remote.system", name = "enabled", havingValue = "true", matchIfMissing = true)
    @EnableFeignClients(clients = {RoleRemoteClient.class, DeptRemoteClient.class, PostRemoteClient.class,
            AdminUserRemoteClient.class, SmsSendRemoteClient.class, DictDataRemoteClient.class,
            PermissionRemoteClient.class})
    public static class SystemRemoteRpcConfiguration {
    }

}
