package com.develop.mvp.pk.module.system.framework.appconfig;

import com.develop.mvp.pk.module.system.application.logger.service.LoggerApplicationService;
import com.develop.mvp.pk.module.system.domain.logger.repository.LoginLogRepository;
import com.develop.mvp.pk.module.system.domain.logger.repository.OperateLogRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Logger Application Service Configuration 配置类。
 */
@Configuration
public class LoggerApplicationServiceConfiguration {

    /**
     * 执行 logger Application Service 对应的业务操作。
     *
     * @param loginLogRepository loginLogRepository 参数
     * @param operateLogRepository operateLogRepository 参数
     * @return 处理结果
     */
    @Bean
    public LoggerApplicationService loggerApplicationService(
            LoginLogRepository loginLogRepository,
            OperateLogRepository operateLogRepository) {
        return new LoggerApplicationService(loginLogRepository, operateLogRepository);
    }
}
