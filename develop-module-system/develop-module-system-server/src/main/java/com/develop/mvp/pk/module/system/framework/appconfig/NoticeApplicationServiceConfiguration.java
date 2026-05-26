package com.develop.mvp.pk.module.system.framework.appconfig;

import com.develop.mvp.pk.module.system.application.notice.service.NoticeApplicationService;
import com.develop.mvp.pk.module.system.domain.notice.repository.NoticeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Notice Application Service Configuration 配置类。
 */
@Configuration
public class NoticeApplicationServiceConfiguration {

    /**
     * 执行 notice Application Service 对应的业务操作。
     *
     * @param noticeRepository noticeRepository 参数
     * @return 处理结果
     */
    @Bean
    public NoticeApplicationService noticeApplicationService(NoticeRepository noticeRepository) {
        return new NoticeApplicationService(noticeRepository);
    }
}
