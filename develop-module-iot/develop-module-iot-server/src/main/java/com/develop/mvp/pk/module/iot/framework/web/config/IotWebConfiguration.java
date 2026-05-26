package com.develop.mvp.pk.module.iot.framework.web.config;

import com.develop.mvp.pk.framework.swagger.config.DevelopSwaggerAutoConfiguration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * iot 模块的 web 组件的 Configuration
 *
 * @author David
 */
@Configuration(proxyBeanMethods = false)
public class IotWebConfiguration {

    /**
     * iot 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi iotGroupedOpenApi() {
        return DevelopSwaggerAutoConfiguration.buildGroupedOpenApi("iot");
    }

}
