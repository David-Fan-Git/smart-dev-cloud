package com.develop.mvp.pk.module.system.framework.appconfig;

import com.develop.mvp.pk.module.system.application.dept.port.inbound.DeptUseCase;
import com.develop.mvp.pk.module.system.application.user.service.UserApplicationService;
import com.develop.mvp.pk.module.system.domain.user.event.DomainEventPublisher;
import com.develop.mvp.pk.module.system.domain.user.repository.UserRepository;
import com.develop.mvp.pk.module.system.domain.user.service.PasswordEncoder;
import com.develop.mvp.pk.module.system.domain.user.service.UserUniquenessChecker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * User Application Service Configuration 配置类。
 */
@Configuration
public class UserApplicationServiceConfiguration {

    /**
     * 执行 user Application Service 对应的业务操作。
     *
     * @param userRepository userRepository 参数
     * @param passwordEncoder passwordEncoder 参数
     * @param uniquenessChecker uniquenessChecker 参数
     * @param eventPublisher eventPublisher 参数
     * @param deptUseCase deptUseCase 参数
     * @param postUseCase postUseCase 参数
     * @return 处理结果
     */
    @Bean
    public UserApplicationService userApplicationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserUniquenessChecker uniquenessChecker,
            DomainEventPublisher eventPublisher,
            DeptUseCase deptUseCase,
            DeptUseCase postUseCase) {
        return new UserApplicationService(userRepository, passwordEncoder, uniquenessChecker,
                eventPublisher, deptUseCase, postUseCase);
    }
}
