package com.develop.mvp.pk.framework.xss.config;

import com.develop.mvp.pk.framework.common.enums.WebFilterOrderEnum;
import com.develop.mvp.pk.framework.xss.core.clean.JsoupXssCleaner;
import com.develop.mvp.pk.framework.xss.core.clean.XssCleaner;
import com.develop.mvp.pk.framework.xss.core.filter.XssFilter;
import com.develop.mvp.pk.framework.xss.core.json.XssStringJsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static com.develop.mvp.pk.framework.web.config.DevelopWebAutoConfiguration.createFilterBean;

/**
 * XSS 防护的自动配置入口。
 *
 * <p>它解决的是外部请求中可能携带脚本片段、危险 HTML 等内容的问题：通过 {@link XssCleaner} 提供清理规则，通过 Jackson
 * 反序列化扩展清理 JSON 字符串参数，通过 {@link XssFilter} 包装请求并清理表单、查询参数等 Servlet 参数。配置项关闭时，
 * 整个 XSS 防护链路不会启用。</p>
 *
 * <p>在 HTTP 请求链路中，XSS 处理发生在 Controller 读取参数之前，确保业务代码拿到的是已经清理过的输入；同时它晚于请求体缓存，
 * 避免清理过程消耗原始 Body 后影响访问日志、加解密或 Controller 继续读取。初学者建议先看 {@link #xssCleaner()} 理解清理能力，
 * 再看 {@link #xssJacksonCustomizer(XssProperties, PathMatcher, XssCleaner)} 和 {@link #xssFilter(XssProperties, PathMatcher, XssCleaner)}
 * 理解 JSON 与 Servlet 参数两条入口。</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(XssProperties.class)
@ConditionalOnProperty(prefix = "develop.xss", name = "enable", havingValue = "true", matchIfMissing = true) // 设置为 false 时，禁用
public class DevelopXssAutoConfiguration implements WebMvcConfigurer {

    /**
     * 提供默认的 XSS 清理器，负责把危险 HTML、脚本片段等输入转换为安全内容。
     *
     * <p>使用 {@link ConditionalOnMissingBean} 是为了允许业务系统替换自己的清理策略，而不改变后续过滤器和 Jackson 扩展的接入方式。</p>
     */
    @Bean
    @ConditionalOnMissingBean(XssCleaner.class)
    public XssCleaner xssCleaner() {
        return new JsoupXssCleaner();
    }

    /**
     * 注册 Jackson 反序列化扩展，在 JSON 请求体绑定到 Controller 参数之前清理字符串字段。
     *
     * <p>这条链路处理的是 application/json 等请求体参数，发生在 Spring MVC 参数绑定阶段；与 XssFilter 处理 Servlet 参数互补，
     * 共同保证 Controller 接收到的输入已经过 XSS 清理。</p>
     */
    @Bean
    @ConditionalOnMissingBean(name = "xssJacksonCustomizer")
    @ConditionalOnProperty(value = "develop.xss.enable", havingValue = "true")
    public Jackson2ObjectMapperBuilderCustomizer xssJacksonCustomizer(XssProperties properties,
                                                                      PathMatcher pathMatcher,
                                                                      XssCleaner xssCleaner) {
        // 在反序列化时进行 xss 过滤，可以替换使用 XssStringJsonSerializer，在序列化时进行处理
        return builder ->
                builder.deserializerByType(String.class, new XssStringJsonDeserializer(properties, pathMatcher, xssCleaner));
    }

    /**
     * 注册 XSS 过滤器，在请求进入 Controller 前包装 HttpServletRequest 并清理查询、表单等参数。
     *
     * <p>顺序上它依赖请求体缓存过滤器先执行，同时晚于访问日志过滤器，使日志能按当前实现保留清理前的原始请求信息。</p>
     */
    @Bean
    @ConditionalOnBean(XssCleaner.class)
    public FilterRegistrationBean<XssFilter> xssFilter(XssProperties properties, PathMatcher pathMatcher, XssCleaner xssCleaner) {
        return createFilterBean(new XssFilter(properties, pathMatcher, xssCleaner), WebFilterOrderEnum.XSS_FILTER);
    }

}
