package com.develop.mvp.pk.module.infra.framework.file.config;

import com.develop.mvp.pk.module.infra.framework.file.core.client.FileClientFactory;
import com.develop.mvp.pk.module.infra.framework.file.core.client.FileClientFactoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件配置类
 *
 * @author David
 */
@Configuration(proxyBeanMethods = false)
public class DevelopFileAutoConfiguration {

    @Bean
    public FileClientFactory fileClientFactory() {
        return new FileClientFactoryImpl();
    }

}
