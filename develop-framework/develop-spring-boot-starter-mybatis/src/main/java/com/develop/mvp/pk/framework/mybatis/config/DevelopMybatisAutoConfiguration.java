package com.develop.mvp.pk.framework.mybatis.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.develop.mvp.pk.framework.common.util.json.JsonUtils;
import com.develop.mvp.pk.framework.mybatis.core.handler.DefaultDBFieldHandler;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.core.handlers.IJsonTypeHandler;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.incrementer.IKeyGenerator;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.baomidou.mybatisplus.extension.incrementer.*;
import com.baomidou.mybatisplus.extension.parser.JsqlParserGlobal;
import com.baomidou.mybatisplus.extension.parser.cache.JdkSerialCaffeineJsqlParseCache;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * MyBatis Plus 自动配置入口。
 * <p>
 * 本类处在“业务 Mapper 调用”和“数据库执行 SQL”之间：Controller 或 ApplicationService 调用 Mapper 后，
 * MyBatis 会创建 SQL 会话并执行语句，MyBatis Plus 的插件链会在 SQL 真正发送到数据库前进行增强。
 * 当前框架在这里注册 Mapper 扫描、分页插件、公共字段自动填充、部分数据库主键生成器，以及 JSON 类型处理器。
 * <p>
 * 需要注意：租户、数据权限等插件如果由其它 Starter 注册到同一个 MyBatis Plus 拦截器体系，顺序会影响最终 SQL。
 * 通常应先追加租户和数据权限等“缩小数据范围”的条件，再进行分页改写，避免分页统计或查询范围不符合预期。
 * 本类当前只显式加入分页插件，不在注释中假设不存在的插件已经启用。
 *
 * @author David
 */
@AutoConfiguration(before = MybatisPlusAutoConfiguration.class) // 先于 MyBatis Plus 自动配置，确保 Mapper 扫描配置尽早生效，减少扫描不到 Mapper 的 warn 日志
@MapperScan(value = "${develop.info.base-package}", annotationClass = Mapper.class,
        lazyInitialization = "${mybatis.lazy-initialization:false}") // Mapper 懒加载目前主要服务于单元测试场景，默认关闭
public class DevelopMybatisAutoConfiguration {

    static {
        // 为 JSQLParser 开启短生命周期本地缓存，降低分页等 SQL 改写场景反复解析动态 SQL 的成本。
        JsqlParserGlobal.setJsqlParseCache(new JdkSerialCaffeineJsqlParseCache(
                (cache) -> cache.maximumSize(1024)
                        .expireAfterWrite(5, TimeUnit.SECONDS))
        );
    }

    /**
     * 创建 MyBatis Plus 插件链。
     * <p>
     * 业务 Mapper 方法执行时，MyBatis Plus 会让 SQL 依次经过这里注册的 InnerInterceptor。
     * 当前只注册 {@link PaginationInnerInterceptor}，用于把分页查询改写为数据库可识别的分页 SQL，
     * 并配合 MyBatis Plus 完成分页总数统计等能力。
     * <p>
     * 插件顺序非常重要：如果后续接入租户、数据权限、防全表更新等插件，应按“先限制数据范围，再分页/统计”的思路设计顺序，
     * 否则可能出现分页总数、查询结果或拦截范围与业务预期不一致的问题。
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor()); // 分页插件：拦截分页查询并改写 SQL
        // ↓↓↓ 按需开启，可能会影响到 updateBatch 的地方：例如说文件配置管理 ↓↓↓
        // mybatisPlusInterceptor.addInnerInterceptor(new BlockAttackInnerInterceptor()); // 拦截没有指定条件的 update 和 delete 语句
        return mybatisPlusInterceptor;
    }

    /**
     * 注册公共字段自动填充处理器。
     * <p>
     * Mapper 执行 insert 或 update 时，MyBatis Plus 会回调 {@link MetaObjectHandler}，
     * 由 {@link DefaultDBFieldHandler} 统一填充创建时间、更新时间、创建人等通用审计字段，
     * 避免每个业务表在 Service 中重复编写相同赋值逻辑。
     */
    @Bean
    public MetaObjectHandler defaultMetaObjectHandler() {
        return new DefaultDBFieldHandler(); // 自动填充参数类
    }

    /**
     * 根据当前数据库类型选择 MyBatis Plus 主键生成器。
     * <p>
     * 只有当 {@code mybatis-plus.global-config.db-config.id-type=INPUT} 时才创建该 Bean，
     * 用于 Oracle、PostgreSQL、H2、Kingbase、DM 等需要框架侧配合生成主键的数据库。
     */
    @Bean
    @ConditionalOnProperty(prefix = "mybatis-plus.global-config.db-config", name = "id-type", havingValue = "INPUT")
    public IKeyGenerator keyGenerator(ConfigurableEnvironment environment) {
        DbType dbType = IdTypeEnvironmentPostProcessor.getDbType(environment);
        if (dbType != null) {
            switch (dbType) {
                case POSTGRE_SQL:
                    return new PostgreKeyGenerator();
                case ORACLE:
                case ORACLE_12C:
                    return new OracleKeyGenerator();
                case H2:
                    return new H2KeyGenerator();
                case KINGBASE_ES:
                    return new KingbaseKeyGenerator();
                case DM:
                    return new DmKeyGenerator();
            }
        }
        // 找不到合适的 IKeyGenerator 实现类
        throw new IllegalArgumentException(StrUtil.format("DbType{} 找不到合适的 IKeyGenerator 实现类", dbType));
    }

    /**
     * 配置 MyBatis Plus 的 Jackson JSON 类型处理器。
     * <p>
     * 当实体字段通过 MyBatis Plus JSON TypeHandler 映射到数据库 JSON/文本字段时，
     * 这里把框架统一的 {@link ObjectMapper} 注入给 {@link JacksonTypeHandler}，
     * 保证数据库字段中的 JSON 与项目其它 Web/Redis 序列化场景尽量保持一致。
     * <p>
     * 返回类型刻意声明为 {@link Object}，避免 {@link JacksonTypeHandler} 被 MyBatis 作为全局类型处理器套用到所有字段上。
     */
    @Bean // 特殊：返回结果使用 Object 而不用 JacksonTypeHandler 的原因，避免因为 JacksonTypeHandler 被 mybatis 全局使用！
    public Object jacksonTypeHandler(List<ObjectMapper> objectMappers) {
        // 优先复用 Spring 容器中的 ObjectMapper；不存在时使用框架 JSON 工具的默认 ObjectMapper。
        ObjectMapper objectMapper = CollUtil.getFirst(objectMappers);
        if (objectMapper == null) {
            objectMapper = JsonUtils.getObjectMapper();
        }
        JacksonTypeHandler.setObjectMapper(objectMapper);
        return new JacksonTypeHandler(Object.class);
    }

}
