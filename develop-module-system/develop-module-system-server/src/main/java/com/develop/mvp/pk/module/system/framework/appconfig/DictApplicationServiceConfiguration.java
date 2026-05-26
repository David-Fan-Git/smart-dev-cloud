package com.develop.mvp.pk.module.system.framework.appconfig;

import com.develop.mvp.pk.module.system.application.dict.service.DictApplicationService;
import com.develop.mvp.pk.module.system.domain.dict.repository.DictDataRepository;
import com.develop.mvp.pk.module.system.domain.dict.repository.DictTypeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Dict Application Service Configuration 配置类。
 */
@Configuration
public class DictApplicationServiceConfiguration {

    /**
     * 执行 dict Application Service 对应的业务操作。
     *
     * @param dictTypeRepository dictTypeRepository 参数
     * @param dictDataRepository dictDataRepository 参数
     * @return 处理结果
     */
    @Bean
    public DictApplicationService dictApplicationService(
            DictTypeRepository dictTypeRepository,
            DictDataRepository dictDataRepository) {
        return new DictApplicationService(dictTypeRepository, dictDataRepository);
    }
}
