package com.develop.mvp.pk.framework.datasource.config;

import com.develop.mvp.pk.framework.datasource.core.filter.DruidAdRemoveFilter;
import com.alibaba.druid.spring.boot3.autoconfigure.properties.DruidStatProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 数据源自动配置入口。
 * <p>
 * 这个 Starter 不直接创建业务数据源，数据源本身由 Spring Boot、dynamic-datasource 和 Druid 的自动配置完成；
 * 本类负责补齐框架级数据库能力：开启基于 Spring AOP 的声明式事务，并接入 Druid 监控页相关的 Servlet Filter。
 * 初学者可以把它理解为“数据库基础设施总开关”：业务代码调用 Mapper 前，事务边界先由 Spring 管理，
 * SQL 执行和连接池监控再交给 MyBatis、数据源和 Druid 等组件协同完成。
 *
 * @author David
 */
@AutoConfiguration
@EnableTransactionManagement(proxyTargetClass = true) // 开启声明式事务管理，业务 Service 上的 @Transactional 才会生效
@EnableConfigurationProperties(DruidStatProperties.class)
public class DevelopDataSourceAutoConfiguration {

    /**
     * 注册 Druid 监控页广告移除过滤器。
     * <p>
     * 当 {@code spring.datasource.druid.stat-view-servlet.enabled=true} 时，Druid 会暴露连接池监控页面。
     * 该 Bean 根据 Druid 监控页配置推导 {@code js/common.js} 的访问路径，并只对这个静态资源挂载过滤器，
     * 用于处理 Druid 控制台前端资源中的广告内容，不影响业务接口、事务执行或 Mapper SQL 调用链。
     */
    @Bean
    @ConditionalOnProperty(name = "spring.datasource.druid.stat-view-servlet.enabled", havingValue = "true")
    public FilterRegistrationBean<DruidAdRemoveFilter> druidAdRemoveFilterFilter(DruidStatProperties properties) {
        // 读取 Druid StatViewServlet 的访问路径配置，例如默认的 /druid/*。
        DruidStatProperties.StatViewServlet config = properties.getStatViewServlet();
        // 将监控页路径定位到 common.js，过滤器只处理该资源，避免扩大影响范围。
        String pattern = config.getUrlPattern() != null ? config.getUrlPattern() : "/druid/*";
        String commonJsPattern = pattern.replaceAll("\\*", "js/common.js");
        // 创建并注册 Servlet Filter，具体过滤逻辑由 DruidAdRemoveFilter 实现。
        FilterRegistrationBean<DruidAdRemoveFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new DruidAdRemoveFilter());
        registrationBean.addUrlPatterns(commonJsPattern);
        return registrationBean;
    }

}
