package com.develop.mvp.pk.module.wms.framework.web.config;

import com.develop.mvp.pk.framework.swagger.config.DevelopSwaggerAutoConfiguration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * WMS 模块的 web 组件的 Configuration
 *
 * @author David
 */
@Configuration(proxyBeanMethods = false)
public class WmsWebConfiguration {

    /**
     * WMS 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi wmsGroupedOpenApi() {
        return DevelopSwaggerAutoConfiguration.buildGroupedOpenApi("wms");
    }

}
