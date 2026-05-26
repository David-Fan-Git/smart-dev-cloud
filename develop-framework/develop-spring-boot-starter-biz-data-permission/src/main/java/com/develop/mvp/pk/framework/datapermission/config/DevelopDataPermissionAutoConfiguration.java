package com.develop.mvp.pk.framework.datapermission.config;

import com.develop.mvp.pk.framework.datapermission.core.aop.DataPermissionAnnotationAdvisor;
import com.develop.mvp.pk.framework.datapermission.core.db.DataPermissionRuleHandler;
import com.develop.mvp.pk.framework.datapermission.core.rule.DataPermissionRule;
import com.develop.mvp.pk.framework.datapermission.core.rule.DataPermissionRuleFactory;
import com.develop.mvp.pk.framework.datapermission.core.rule.DataPermissionRuleFactoryImpl;
import com.develop.mvp.pk.framework.mybatis.core.util.MyBatisUtils;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * 数据权限 Starter 的自动配置入口。
 *
 * <p>这个配置类负责把数据权限流程中的三个关键环节串起来：
 * <ol>
 *     <li>收集容器中的 {@link DataPermissionRule}，交给 {@link DataPermissionRuleFactory} 统一选择本次查询要使用的规则；</li>
 *     <li>注册 {@link DataPermissionAnnotationAdvisor}，让带有数据权限注解的类或方法在执行前后维护线程上下文；</li>
 *     <li>把 MyBatis Plus 的 {@link DataPermissionInterceptor} 插入拦截器链，由 {@link DataPermissionRuleHandler} 在 SQL 执行前生成权限条件。</li>
 * </ol>
 *
 * <p>本类只负责装配 Bean 和拦截器顺序，不直接判断用户权限，也不直接改写业务代码中的 Mapper 方法。
 *
 * @author David
 */
@AutoConfiguration
public class DevelopDataPermissionAutoConfiguration {

    /**
     * 创建数据权限规则工厂。
     *
     * <p>Spring 会把所有 {@link DataPermissionRule} 实现收集到 {@code rules} 中。工厂后续会结合
     * {@link com.develop.mvp.pk.framework.datapermission.core.aop.DataPermissionContextHolder} 中的当前注解，决定本次 Mapper 查询实际启用哪些规则。
     */
    @Bean
    public DataPermissionRuleFactory dataPermissionRuleFactory(List<DataPermissionRule> rules) {
        return new DataPermissionRuleFactoryImpl(rules);
    }

    /**
     * 创建 MyBatis Plus 数据权限处理器，并把它放到 MyBatis Plus 内部拦截器链的最前面。
     *
     * <p>SQL 即将执行时，{@link DataPermissionInterceptor} 会回调 {@link DataPermissionRuleHandler}，
     * 由处理器根据表名、当前上下文和规则工厂返回的规则生成权限表达式。这里把它插在索引 0，
     * 是为了让数据权限条件先于分页等后续插件生效，避免分页插件先改写 SQL 后再追加权限条件。
     */
    @Bean
    public DataPermissionRuleHandler dataPermissionRuleHandler(MybatisPlusInterceptor interceptor,
                                                               DataPermissionRuleFactory ruleFactory) {
        // 创建 DataPermissionInterceptor 拦截器
        DataPermissionRuleHandler handler = new DataPermissionRuleHandler(ruleFactory);
        DataPermissionInterceptor inner = new DataPermissionInterceptor(handler);
        // 添加到 interceptor 中
        // 需要加在首个，主要是为了在分页插件前面。这个是 MyBatis Plus 的规定
        MyBatisUtils.addInterceptor(interceptor, inner, 0);
        return handler;
    }

    /**
     * 创建注解 AOP Advisor。
     *
     * <p>它不负责生成 SQL 条件，只负责识别 {@code @DataPermission} 标注的类或方法，
     * 并在业务方法执行期间通过拦截器维护当前线程的数据权限上下文。
     */
    @Bean
    public DataPermissionAnnotationAdvisor dataPermissionAnnotationAdvisor() {
        return new DataPermissionAnnotationAdvisor();
    }

}
