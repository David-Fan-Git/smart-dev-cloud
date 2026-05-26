package com.develop.mvp.pk.module.report.framework.jmreport.config;

import com.develop.mvp.pk.framework.common.biz.system.permission.PermissionCommonApi;
import com.develop.mvp.pk.framework.security.config.SecurityProperties;
import com.develop.mvp.pk.module.report.framework.jmreport.core.service.JmOnlDragExternalServiceImpl;
import com.develop.mvp.pk.module.report.framework.jmreport.core.service.JmReportTokenServiceImpl;
import com.develop.mvp.pk.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import com.develop.mvp.pk.module.system.api.permission.PermissionApi;
import org.jeecg.modules.jmreport.api.JmReportTokenServiceI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 积木报表的配置类
 *
 * @author David
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan(basePackages = "org.jeecg.modules.jmreport") // 扫描积木报表的包
public class JmReportConfiguration {

    @Bean
    public JmReportTokenServiceI jmReportTokenService(OAuth2TokenCommonApi oAuth2TokenApi,
                                                      PermissionCommonApi permissionApi,
                                                      SecurityProperties securityProperties) {
        return new JmReportTokenServiceImpl(oAuth2TokenApi, permissionApi, securityProperties);
    }

    @Bean // 暂时注释：可以按需实现后打开
    @Primary
    public JmOnlDragExternalServiceImpl jmOnlDragExternalService2() {
        return new JmOnlDragExternalServiceImpl();
    }

}
