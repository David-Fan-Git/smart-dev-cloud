package com.develop.mvp.pk.module.infra.domain.logger.repository;

// DDD 角色：仓储接口，定义在领域层，不依赖任何基础设施

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.infra.domain.logger.ApiAccessLog;

import java.time.LocalDateTime;

public interface ApiAccessLogRepository {
    ApiAccessLog save(ApiAccessLog log);
    ApiAccessLog findById(Long id);
    PageResult<ApiAccessLog> findPage(ApiAccessLogPageQuery query);
    Integer deleteByCreateTimeLt(LocalDateTime expireDate, Integer deleteLimit);
}
