package com.develop.mvp.pk.framework.tenant.config;

import cn.hutool.extra.spring.SpringUtil;
import com.develop.mvp.pk.framework.common.biz.system.tenant.TenantCommonApi;
import com.develop.mvp.pk.framework.common.enums.WebFilterOrderEnum;
import com.develop.mvp.pk.framework.mybatis.core.util.MyBatisUtils;
import com.develop.mvp.pk.framework.redis.config.DevelopCacheProperties;
import com.develop.mvp.pk.framework.security.core.service.SecurityFrameworkService;
import com.develop.mvp.pk.framework.tenant.core.aop.TenantIgnore;
import com.develop.mvp.pk.framework.tenant.core.aop.TenantIgnoreAspect;
import com.develop.mvp.pk.framework.tenant.core.db.TenantDatabaseInterceptor;
import com.develop.mvp.pk.framework.tenant.core.job.TenantJobAspect;
import com.develop.mvp.pk.framework.tenant.core.mq.rabbitmq.TenantRabbitMQInitializer;
import com.develop.mvp.pk.framework.tenant.core.mq.redis.TenantRedisMessageInterceptor;
import com.develop.mvp.pk.framework.tenant.core.mq.rocketmq.TenantRocketMQInitializer;
import com.develop.mvp.pk.framework.tenant.core.redis.TenantRedisCacheManager;
import com.develop.mvp.pk.framework.tenant.core.security.TenantSecurityWebFilter;
import com.develop.mvp.pk.framework.tenant.core.service.TenantFrameworkService;
import com.develop.mvp.pk.framework.tenant.core.service.TenantFrameworkServiceImpl;
import com.develop.mvp.pk.framework.tenant.core.web.TenantContextWebFilter;
import com.develop.mvp.pk.framework.tenant.core.web.TenantVisitContextInterceptor;
import com.develop.mvp.pk.framework.web.config.WebProperties;
import com.develop.mvp.pk.framework.web.core.handler.GlobalExceptionHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.BatchStrategies;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertList;

/**
 * Tenant Starter 的总装配入口。
 *
 * <p>当 {@code develop.tenant.enable} 未配置或为 {@code true} 时，本自动配置会把多租户能力接入到应用的主流程中：
 * Web 过滤器负责从 HTTP 请求头建立 {@link com.develop.mvp.pk.framework.tenant.core.context.TenantContextHolder}；
 * Security 过滤器负责校验租户有效性和接口级忽略规则；DB 拦截器负责让 MyBatis Plus 在 SQL 解析阶段拼接租户条件；
 * MQ、Job、Redis 缓存相关 Bean 负责在各自技术入口延续或隔离租户信息；RPC 的 Feign 透传由
 * {@code DevelopTenantRpcAutoConfiguration} 中的 {@link com.develop.mvp.pk.framework.tenant.core.rpc.TenantRequestInterceptor}
 * 承担。</p>
 *
 * <p>这里的职责是“组装流程入口”，不直接实现业务隔离逻辑。初学者阅读时可以把它理解成多租户 Starter 的接线板：
 * 具体租户编号从请求、消息或任务上下文进入，随后被数据库、缓存、远程调用等组件按各自边界消费。</p>
 *
 * @author David
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "develop.tenant", value = "enable", matchIfMissing = true) // 允许使用 develop.tenant.enable=false 禁用多租户
@EnableConfigurationProperties(TenantProperties.class)
public class DevelopTenantAutoConfiguration {

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 创建租户框架服务，供 Web 安全校验、定时任务等框架组件查询租户状态。
     *
     * <p>参数中的 {@link TenantCommonApi} 可能来自本地模块实现，也可能来自远程 Feign 代理；
     * 这里优先尝试使用名为 {@code tenantApiImpl} 的本地 Bean，是为了在单体聚合启动时避免不必要的远程调用。
     * 该方法只决定“通过哪个 API 查询租户”，不负责写入或清理当前线程的租户上下文。</p>
     */
    @Bean
    public TenantFrameworkService tenantFrameworkService(TenantCommonApi tenantApi) {
        // 参见 https://gitee.com/zhijiantianya/develop-cloud/issues/IC6YZF
        try {
            TenantCommonApi tenantApiImpl = SpringUtil.getBean("tenantApiImpl", TenantCommonApi.class);
            if (tenantApiImpl != null) {
                tenantApi = tenantApiImpl;
            }
        } catch (Exception ignored) {}
        return new TenantFrameworkServiceImpl(tenantApi);
    }

    // ========== AOP ==========

    /**
     * 注册 {@link TenantIgnore} 的切面入口。
     *
     * <p>{@code @TenantIgnore} 只适合包住少量明确需要跨租户读取的逻辑，例如全局缓存预热或后台任务；
     * 它通过上下文中的 ignore 标记让后续 DB 等组件跳过租户条件，而不是伪造一个租户编号。</p>
     */
    @Bean
    public TenantIgnoreAspect tenantIgnoreAspect() {
        return new TenantIgnoreAspect();
    }

    // ========== DB ==========

    /**
     * 把租户 SQL 拦截器接入 MyBatis Plus 的插件链。
     *
     * <p>真正判断“当前表是否需要租户条件”和“租户编号表达式如何生成”的逻辑在
     * {@link TenantDatabaseInterceptor} 中。这里刻意把拦截器放在插件链第一个位置，遵循 MyBatis Plus
     * 对租户插件需早于分页等插件执行的要求，避免后续插件先改写 SQL 后再补租户条件。</p>
     */
    @Bean
    public TenantLineInnerInterceptor tenantLineInnerInterceptor(TenantProperties properties,
                                                                 MybatisPlusInterceptor interceptor) {
        TenantLineInnerInterceptor inner = new TenantLineInnerInterceptor(new TenantDatabaseInterceptor(properties));
        // 添加到 interceptor 中
        // 需要加在首个，主要是为了在分页插件前面。这个是 MyBatis Plus 的规定
        MyBatisUtils.addInterceptor(interceptor, inner, 0);
        return inner;
    }

    // ========== WEB ==========

    /**
     * 注册 HTTP 租户上下文过滤器。
     *
     * <p>该过滤器从请求头读取 {@code tenant-id}，写入当前线程上下文，供 Controller、Service、DB、RPC 等后续调用读取。
     * 请求结束后由过滤器清理上下文，避免 Web 容器线程复用时把上一个请求的租户编号带到下一个请求。</p>
     */
    @Bean
    public FilterRegistrationBean<TenantContextWebFilter> tenantContextWebFilter() {
        FilterRegistrationBean<TenantContextWebFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TenantContextWebFilter());
        registrationBean.setOrder(WebFilterOrderEnum.TENANT_CONTEXT_FILTER);
        return registrationBean;
    }

    @Bean
    public TenantVisitContextInterceptor tenantVisitContextInterceptor(TenantProperties tenantProperties,
                                                                       SecurityFrameworkService securityFrameworkService) {
        return new TenantVisitContextInterceptor(tenantProperties, securityFrameworkService);
    }

    @Bean
    public WebMvcConfigurer tenantWebMvcConfigurer(TenantProperties tenantProperties,
                                                   TenantVisitContextInterceptor tenantVisitContextInterceptor) {
        return new WebMvcConfigurer() {

            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(tenantVisitContextInterceptor)
                        .excludePathPatterns(tenantProperties.getIgnoreVisitUrls().toArray(new String[0]));
            }
        };
    }

    // ========== Security ==========

    @Bean
    public FilterRegistrationBean<TenantSecurityWebFilter> tenantSecurityWebFilter(TenantProperties tenantProperties,
                                                                                   WebProperties webProperties,
                                                                                   GlobalExceptionHandler globalExceptionHandler,
                                                                                   TenantFrameworkService tenantFrameworkService) {
        FilterRegistrationBean<TenantSecurityWebFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TenantSecurityWebFilter(webProperties, tenantProperties, getTenantIgnoreUrls(),
                globalExceptionHandler, tenantFrameworkService));
        registrationBean.setOrder(WebFilterOrderEnum.TENANT_SECURITY_FILTER);
        return registrationBean;
    }

    /**
     * 扫描 Controller 上声明了 {@link TenantIgnore} 的 URL，交给安全过滤器作为“接口级可忽略租户”的白名单。
     *
     * <p>这里的 ignore 语义是：某些接口本身允许不按租户校验访问，例如开放接口或全局能力入口；它不同于“请求缺少租户编号”。
     * 缺少租户编号表示上下文里没有 {@code tenant-id}，后续需要租户编号的 DB 拦截逻辑仍会按缺失处理；
     * 安全过滤器只会在请求命中忽略 URL 且当前上下文没有 tenantId 时写入 ignore 标记，有明确 tenantId 的请求仍按租户上下文继续处理。</p>
     *
     * @return 忽略租户的 URL 集合
     */
    private Set<String> getTenantIgnoreUrls() {
        Set<String> ignoreUrls = new HashSet<>();
        // 获得接口对应的 HandlerMethod 集合
        RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping)
                applicationContext.getBean("requestMappingHandlerMapping");
        Map<RequestMappingInfo, HandlerMethod> handlerMethodMap = requestMappingHandlerMapping.getHandlerMethods();
        // 获得有 @TenantIgnore 注解的接口
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethodMap.entrySet()) {
            HandlerMethod handlerMethod = entry.getValue();
            if (!handlerMethod.hasMethodAnnotation(TenantIgnore.class) // 方法级
                && !handlerMethod.getBeanType().isAnnotationPresent(TenantIgnore.class)) { // 接口级
                continue;
            }
            // 添加到忽略的 URL 中
            if (entry.getKey().getPatternsCondition() != null) {
                ignoreUrls.addAll(entry.getKey().getPatternsCondition().getPatterns());
            }
            if (entry.getKey().getPathPatternsCondition() != null) {
                ignoreUrls.addAll(
                        convertList(entry.getKey().getPathPatternsCondition().getPatterns(), PathPattern::getPatternString));
            }
        }
        return ignoreUrls;
    }

    // ========== MQ ==========

    /**
     * 多租户 Redis 消息队列的配置类
     *
     * 为什么要单独一个配置类呢？如果直接把 TenantRedisMessageInterceptor Bean 的初始化放外面，会报 RedisMessageInterceptor 类不存在的错误
     */
    @Configuration
    @ConditionalOnClass(name = "com.develop.mvp.pk.framework.mq.redis.core.RedisMQTemplate")
    public static class TenantRedisMQAutoConfiguration {

        @Bean
        public TenantRedisMessageInterceptor tenantRedisMessageInterceptor() {
            return new TenantRedisMessageInterceptor();
        }

    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.amqp.rabbit.core.RabbitTemplate")
    public TenantRabbitMQInitializer tenantRabbitMQInitializer() {
        return new TenantRabbitMQInitializer();
    }

    @Bean
    @ConditionalOnClass(name = "org.apache.rocketmq.spring.core.RocketMQTemplate")
    public TenantRocketMQInitializer tenantRocketMQInitializer() {
        return new TenantRocketMQInitializer();
    }

    // ========== Job ==========

    @Bean
    @ConditionalOnClass(name = "com.xxl.job.core.handler.annotation.XxlJob")
    public TenantJobAspect tenantJobAspect(TenantFrameworkService tenantFrameworkService) {
        return new TenantJobAspect(tenantFrameworkService);
    }

    // ========== Redis ==========

    /**
     * 创建带租户前缀处理能力的 Redis 缓存管理器。
     *
     * <p>Redis 缓存不是从请求头直接拿租户编号，而是在缓存读写发生时读取当前
     * {@link com.develop.mvp.pk.framework.tenant.core.context.TenantContextHolder}。
     * 因此它依赖 Web、MQ、Job 等入口先正确建立上下文；对配置在 ignoreCaches 中的缓存，则不追加租户维度。</p>
     */
    @Bean
    @Primary // 引入租户时，tenantRedisCacheManager 为主 Bean
    public RedisCacheManager tenantRedisCacheManager(RedisTemplate<String, Object> redisTemplate,
                                                     RedisCacheConfiguration redisCacheConfiguration,
                                                     DevelopCacheProperties developCacheProperties,
                                                     TenantProperties tenantProperties) {
        // 创建 RedisCacheWriter 对象
        RedisConnectionFactory connectionFactory = Objects.requireNonNull(redisTemplate.getConnectionFactory());
        RedisCacheWriter cacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory,
                BatchStrategies.scan(developCacheProperties.getRedisScanBatchSize()));
        // 创建 TenantRedisCacheManager 对象
        return new TenantRedisCacheManager(cacheWriter, redisCacheConfiguration, tenantProperties.getIgnoreCaches());
    }

}
