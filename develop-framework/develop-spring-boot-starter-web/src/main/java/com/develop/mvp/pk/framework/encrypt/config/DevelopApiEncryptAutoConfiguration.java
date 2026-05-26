package com.develop.mvp.pk.framework.encrypt.config;

import com.develop.mvp.pk.framework.common.enums.WebFilterOrderEnum;
import com.develop.mvp.pk.framework.encrypt.core.filter.ApiEncryptFilter;
import com.develop.mvp.pk.framework.web.config.WebProperties;
import com.develop.mvp.pk.framework.web.core.handler.GlobalExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static com.develop.mvp.pk.framework.web.config.DevelopWebAutoConfiguration.createFilterBean;

/**
 * API 加解密的自动配置入口。
 *
 * <p>它解决的是部分接口需要对请求内容解密、对响应内容加密的问题：启动时读取加密配置并注册 {@link ApiEncryptFilter}，
 * 运行时由过滤器根据 Controller 或方法上的加密注解决定是否处理当前请求。该配置只有在 develop.api-encrypt.enable=true 时启用，
 * 避免普通接口额外承担加解密成本。</p>
 *
 * <p>在 HTTP 请求链路中，加解密必须发生在 Controller 之前和响应写回之前：请求先被解密包装，Controller 才能读取明文参数；
 * Controller 返回后，响应内容再被包装并加密输出。它依赖 {@link RequestMappingHandlerMapping} 找到目标 Handler，依赖
 * {@link GlobalExceptionHandler} 把解密失败统一转换为平台错误响应。初学者应重点理解过滤器为何比 RequestBodyAdvice/
 * ResponseBodyAdvice 更早介入，以及它和请求体缓存、访问日志之间的顺序关系。</p>
 */
@AutoConfiguration
@Slf4j
@EnableConfigurationProperties(ApiEncryptProperties.class)
@ConditionalOnProperty(prefix = "develop.api-encrypt", name = "enable", havingValue = "true")
public class DevelopApiEncryptAutoConfiguration {

    /**
     * 注册 API 加解密过滤器，在 Controller 执行前解密请求，在 Controller 返回后加密响应。
     *
     * <p>过滤器需要先于访问日志和 XSS 执行，避免后续组件读到未解密的请求体；同时又要晚于请求体缓存，确保解密包装后的请求仍可被继续读取。
     * 当解密失败时，直接委托全局异常处理器生成统一错误响应，避免异常绕过平台响应格式。</p>
     */
    @Bean
    public FilterRegistrationBean<ApiEncryptFilter> apiEncryptFilter(WebProperties webProperties,
                                                                     ApiEncryptProperties apiEncryptProperties,
                                                                     RequestMappingHandlerMapping requestMappingHandlerMapping,
                                                                     GlobalExceptionHandler globalExceptionHandler) {
        ApiEncryptFilter filter = new ApiEncryptFilter(webProperties, apiEncryptProperties,
                requestMappingHandlerMapping, globalExceptionHandler);
        return createFilterBean(filter, WebFilterOrderEnum.API_ENCRYPT_FILTER);

    }

}
