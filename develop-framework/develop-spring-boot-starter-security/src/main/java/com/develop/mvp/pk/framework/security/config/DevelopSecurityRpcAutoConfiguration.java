package com.develop.mvp.pk.framework.security.config;

import com.develop.mvp.pk.framework.common.biz.system.permission.PermissionCommonApi;
import com.develop.mvp.pk.framework.security.core.rpc.LoginUserRequestInterceptor;
import com.develop.mvp.pk.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

/**
 * Security 在 RPC 场景下使用的自动配置。
 *
 * <p>单体聚合运行时，业务代码可以直接调用本地 Bean；拆分为微服务后，模块之间会通过 Feign 调用远程 API。
 * 安全链路需要在远程调用时继续传递当前 {@code LoginUser}，避免下游服务丢失登录用户信息。
 * 本配置负责启用安全相关的 Feign API 客户端，并注册请求拦截器完成身份透传。</p>
 *
 * @author David
 */
@AutoConfiguration
@EnableFeignClients(clients = {OAuth2TokenCommonApi.class, // 主要是引入相关的 API 服务
        PermissionCommonApi.class})
public class DevelopSecurityRpcAutoConfiguration {

    /**
     * 注册 Feign 请求拦截器。
     *
     * <p>发起远程调用前，拦截器会把当前登录用户写入 Feign 请求头，
     * 下游服务的 token 认证过滤器即可按约定请求头恢复登录态。</p>
     */
    @Bean
    public LoginUserRequestInterceptor loginUserRequestInterceptor() {
        return new LoginUserRequestInterceptor();
    }

}
