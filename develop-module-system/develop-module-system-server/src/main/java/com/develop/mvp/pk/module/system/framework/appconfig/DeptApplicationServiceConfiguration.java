package com.develop.mvp.pk.module.system.framework.appconfig;

import com.develop.mvp.pk.module.system.application.dept.service.DeptApplicationService;
import com.develop.mvp.pk.module.system.dal.mysql.dept.DeptMapper;
import com.develop.mvp.pk.module.system.dal.mysql.dept.PostMapper;
import com.develop.mvp.pk.module.system.domain.dept.repository.DeptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Dept Application Service Configuration 配置类。
 */
@Configuration
public class DeptApplicationServiceConfiguration {

    /**
     * 执行 dept Application Service 对应的业务操作。
     *
     * @param deptRepository deptRepository 参数
     * @param eventPublisher eventPublisher 参数
     * @param deptMapper deptMapper 参数
     * @param postMapper postMapper 参数
     * @return 处理结果
     */
    @Bean
    public DeptApplicationService deptApplicationService(
            @Autowired(required = false) DeptRepository deptRepository,
            @Autowired(required = false) ApplicationEventPublisher eventPublisher,
            DeptMapper deptMapper,
            PostMapper postMapper) {
        return new DeptApplicationService(deptRepository, eventPublisher, deptMapper, postMapper);
    }
}
