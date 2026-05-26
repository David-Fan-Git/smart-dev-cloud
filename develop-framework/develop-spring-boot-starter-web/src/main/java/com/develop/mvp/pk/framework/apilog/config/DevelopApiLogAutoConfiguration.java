package com.develop.mvp.pk.framework.apilog.config;

import com.develop.mvp.pk.framework.apilog.core.filter.ApiAccessLogFilter;
import com.develop.mvp.pk.framework.apilog.core.interceptor.ApiAccessLogInterceptor;
import com.develop.mvp.pk.framework.common.biz.infra.logger.ApiAccessLogCommonApi;
import com.develop.mvp.pk.framework.common.enums.WebFilterOrderEnum;
import com.develop.mvp.pk.framework.web.config.WebProperties;
import com.develop.mvp.pk.framework.web.config.DevelopWebAutoConfiguration;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * API 访问日志的自动配置入口。
 *
 * <p>它解决的是“每个接口请求都要留下可追踪访问记录”的问题：通过 Servlet Filter 记录请求开始、响应完成、耗时和结果，
 * 再通过 Spring MVC Interceptor 补充 Controller 匹配后的处理信息。该配置在 {@link DevelopWebAutoConfiguration} 之后加载，
 * 复用其中的 WebProperties、请求体缓存顺序和通用过滤器注册方式。</p>
 *
 * <p>在 HTTP 请求链路中，{@link ApiAccessLogFilter} 位于 Controller 之前和响应返回之后，适合记录完整请求生命周期；
 * {@link ApiAccessLogInterceptor} 位于 Spring MVC 内部，能感知 Handler 执行过程。初学者可先看过滤器理解“全链路日志”，
 * 再看拦截器理解“Controller 执行阶段日志补充”。</p>
 */
@AutoConfiguration(after = DevelopWebAutoConfiguration.class)
public class DevelopApiLogAutoConfiguration implements WebMvcConfigurer {

    /**
     * 注册访问日志过滤器，在请求进入 Controller 前开始计时，并在响应写回前后汇总请求、响应和异常信息。
     *
     * <p>它依赖请求体可重复读取能力，顺序必须晚于请求体缓存和 API 加解密过滤器；同时要早于 XSS 等更靠近业务的处理，
     * 按当前链路先保存进入 XSS 清理前的请求信息。配置项允许关闭访问日志，便于在特殊环境降低日志量。</p>
     */
    @Bean
    @ConditionalOnProperty(prefix = "develop.access-log", value = "enable", matchIfMissing = true) // 允许使用 develop.access-log.enable=false 禁用访问日志
    public FilterRegistrationBean<ApiAccessLogFilter> apiAccessLogFilter(WebProperties webProperties,
                                                                         @Value("${spring.application.name}") String applicationName,
                                                                         ApiAccessLogCommonApi apiAccessLogApi) {
        ApiAccessLogFilter filter = new ApiAccessLogFilter(webProperties, applicationName, apiAccessLogApi);
        return createFilterBean(filter, WebFilterOrderEnum.API_ACCESS_LOG_FILTER);
    }

    private static <T extends Filter> FilterRegistrationBean<T> createFilterBean(T filter, Integer order) {
        FilterRegistrationBean<T> bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(order);
        return bean;
    }

    /**
     * 注册 MVC 拦截器，在 Spring MVC 已匹配到 Handler 后参与 preHandle/afterCompletion 等阶段。
     *
     * <p>过滤器负责 Servlet 层全链路包围，拦截器负责 Controller 调用阶段的补充信息；两者配合可以同时覆盖原始请求和业务处理结果。</p>
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ApiAccessLogInterceptor());
    }

}
