package com.develop.mvp.pk.framework.web.config;

import cn.hutool.core.util.StrUtil;
import com.develop.mvp.pk.framework.common.biz.infra.logger.ApiErrorLogCommonApi;
import com.develop.mvp.pk.framework.common.enums.WebFilterOrderEnum;
import com.develop.mvp.pk.framework.web.core.filter.CacheRequestBodyFilter;
import com.develop.mvp.pk.framework.web.core.filter.DemoFilter;
import com.develop.mvp.pk.framework.web.core.handler.GlobalExceptionHandler;
import com.develop.mvp.pk.framework.web.core.handler.GlobalResponseBodyHandler;
import com.develop.mvp.pk.framework.web.core.util.WebFrameworkUtils;
import com.google.common.collect.Maps;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Web Starter 的基础自动配置入口。
 *
 * <p>它解决的是每个 HTTP 服务都要重复处理的通用问题：为 Controller 统一增加接口前缀，注册全局异常处理、统一响应处理、
 * 请求工具类、跨域过滤器、请求体缓存过滤器、演示模式过滤器以及 RestTemplate。Spring Boot 启动时会加载本类，并把这些
 * Bean 放入容器，随后 Servlet Filter 链和 Spring MVC 调用链在请求进入 Controller 前后使用它们。</p>
 *
 * <p>在请求链路中，本类提供最底层的 Web 基础设施：CorsFilter 最早处理浏览器跨域预检，CacheRequestBodyFilter 在业务
 * 过滤器之前包装请求体，Spring MVC 的 HandlerMapping 负责把请求路由到带有统一前缀的 Controller，全局异常和响应处理器
 * 则在 Controller 执行后统一兜底。初学者阅读时建议先看 {@link #webMvcRegistrations(WebProperties)} 理解路径前缀，
 * 再看过滤器注册方法理解请求进入 Controller 前会被哪些基础设施处理。</p>
 */
@AutoConfiguration(beforeName = {
        "com.fhs.trans.config.TransServiceConfig" // cloud 独有：避免一键改包后，RestTemplate 初始化的冲突。可见 https://t.zsxq.com/T4yj7 帖子
})
@EnableConfigurationProperties(WebProperties.class)
public class DevelopWebAutoConfiguration {

    /**
     * 应用名
     */
    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * 定制 Spring MVC 的 HandlerMapping，在 Controller 注册到路由表时自动追加 admin/app 等统一接口前缀。
     *
     * <p>这个 Bean 发生在应用启动阶段，而不是单次请求阶段；它会影响后续所有请求如何匹配到 Controller。前缀只作用于
     * 指定包路径下的 {@link RestController}，避免误改非接口类或第三方组件的映射。</p>
     */
    @Bean
    public WebMvcRegistrations webMvcRegistrations(WebProperties webProperties) {
        return new WebMvcRegistrations() {

            @Override
            public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
                RequestMappingHandlerMapping mapping = new RequestMappingHandlerMapping();
                // 实例化时就带上前缀
                mapping.setPathPrefixes(buildPathPrefixes(webProperties));
                return mapping;
            }

            /**
             * 构建 prefix → 匹配条件的映射
             */
            private Map<String, Predicate<Class<?>>> buildPathPrefixes(WebProperties webProperties) {
                AntPathMatcher antPathMatcher = new AntPathMatcher(".");
                Map<String, Predicate<Class<?>>> pathPrefixes = Maps.newLinkedHashMapWithExpectedSize(2);
                putPathPrefix(pathPrefixes, webProperties.getAdminApi(), antPathMatcher);
                putPathPrefix(pathPrefixes, webProperties.getAppApi(), antPathMatcher);
                return pathPrefixes;
            }

            /**
             * 设置 API 前缀，仅仅匹配 controller 包下的
             */
            private void putPathPrefix(Map<String, Predicate<Class<?>>> pathPrefixes, WebProperties.Api api, AntPathMatcher matcher) {
                if (api == null || StrUtil.isEmpty(api.getPrefix())) {
                    return;
                }
                pathPrefixes.put(api.getPrefix(), // api 前缀
                        clazz -> clazz.isAnnotationPresent(RestController.class)
                                && matcher.match(api.getController(), clazz.getPackage().getName()));
            }

        };
    }

    /**
     * 注册全局异常处理器，在 Controller 或后续业务组件抛出异常后统一转换错误响应并记录异常日志。
     */
    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public GlobalExceptionHandler globalExceptionHandler(ApiErrorLogCommonApi apiErrorLogApi) {
        return new GlobalExceptionHandler(applicationName, apiErrorLogApi);
    }

    /**
     * 注册响应结果观察器，在 Controller 正常返回后记录 CommonResult，供访问日志等后续组件读取处理结果。
     */
    @Bean
    public GlobalResponseBodyHandler globalResponseBodyHandler() {
        return new GlobalResponseBodyHandler();
    }

    @Bean
    @SuppressWarnings("InstantiationOfUtilityClass")
    public WebFrameworkUtils webFrameworkUtils(WebProperties webProperties) {
        // 由于 WebFrameworkUtils 需要使用到 webProperties 属性，所以注册为一个 Bean
        return new WebFrameworkUtils(webProperties);
    }

    // ========== Filter 相关 ==========

    /**
     * 注册跨域过滤器，优先处理浏览器的 CORS 预检请求和跨域响应头。
     *
     * <p>它必须排在过滤器链最前面，否则后续安全、日志或业务过滤器可能先拦截预检请求，导致前端看到跨域失败。</p>
     */
    @Bean
    @Order(value = WebFilterOrderEnum.CORS_FILTER) // 特殊：修复因执行顺序影响到跨域配置不生效问题
    public FilterRegistrationBean<CorsFilter> corsFilterBean() {
        // 创建 CorsConfiguration 对象
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*"); // 设置访问源地址
        config.addAllowedHeader("*"); // 设置访问源请求头
        config.addAllowedMethod("*"); // 设置访问源请求方法
        // 创建 UrlBasedCorsConfigurationSource 对象
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 对接口配置跨域设置
        return createFilterBean(new CorsFilter(source), WebFilterOrderEnum.CORS_FILTER);
    }

    /**
     * 注册请求体缓存过滤器，在 Controller 读取请求体之前把原始 Body 包装为可重复读取的请求。
     *
     * <p>Servlet 请求体默认只能读取一次；日志、XSS、加解密和 Controller 都可能需要读取 Body，因此该过滤器顺序必须早于这些
     * 依赖请求内容的组件。</p>
     */
    @Bean
    public FilterRegistrationBean<CacheRequestBodyFilter> requestBodyCacheFilter() {
        return createFilterBean(new CacheRequestBodyFilter(), WebFilterOrderEnum.REQUEST_BODY_CACHE_FILTER);
    }

    /**
     * 在演示模式开启时注册保护过滤器，在请求进入 Controller 前拦截不允许的写操作。
     */
    @Bean
    @ConditionalOnProperty(value = "develop.demo", havingValue = "true")
    public FilterRegistrationBean<DemoFilter> demoFilter() {
        return createFilterBean(new DemoFilter(), WebFilterOrderEnum.DEMO_FILTER);
    }

    /**
     * 统一创建过滤器注册对象，并显式写入顺序，避免不同 Starter 的过滤器因默认顺序变化而破坏请求链路。
     */
    public static <T extends Filter> FilterRegistrationBean<T> createFilterBean(T filter, Integer order) {
        FilterRegistrationBean<T> bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(order);
        return bean;
    }

    /**
     * 创建 RestTemplate 实例
     *
     * @param restTemplateBuilder {@link RestTemplateAutoConfiguration#restTemplateBuilder}
     */
    @Bean
    @ConditionalOnMissingBean
    @Primary
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    /**
     * 创建 RestTemplate 实例（支持负载均衡）
     *
     * @param restTemplateBuilder {@link RestTemplateAutoConfiguration#restTemplateBuilder}
     */
    @Bean
    @LoadBalanced
    public RestTemplate loadBalancedRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

}
