package com.develop.mvp.pk.framework.translate.config;

import com.develop.mvp.pk.framework.translate.core.TranslateUtils;
import com.fhs.trans.service.impl.TransService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 数据翻译自动配置入口。
 * <p>
 * 数据翻译用于把持久化对象或接口返回对象中的编码类字段，转换为前端更容易理解的展示文本。
 * 例如业务数据里保存的是用户编号、部门编号或字典值，展示层需要的是用户名称、部门名称或字典标签。
 * 本类将 easy-trans 的 {@link TransService} 注入到框架工具类 {@link TranslateUtils}，
 * 让应用代码可以通过统一工具触发翻译能力，而不需要直接感知底层翻译服务实现。
 */
@AutoConfiguration
public class DevelopTranslateAutoConfiguration {

    /**
     * 初始化框架数据翻译工具。
     * <p>
     * {@link TranslateUtils} 是静态工具入口，创建 Bean 时先绑定 easy-trans 的 {@link TransService}。
     * 这样接口返回对象需要补齐展示文本时，可以复用同一套翻译服务完成批量翻译，避免在业务代码里手写多次查询和字段拼装。
     */
    @Bean
    @SuppressWarnings({"InstantiationOfUtilityClass", "SpringJavaInjectionPointsAutowiringInspection"})
    public TranslateUtils translateUtils(TransService transService) {
        TranslateUtils.init(transService);
        return new TranslateUtils();
    }

}
