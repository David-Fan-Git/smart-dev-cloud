package com.develop.mvp.pk.module.infra.domain.logger.repository;

// DDD 角色：仓储接口，定义在领域层，不依赖任何基础设施

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.infra.domain.logger.ApiErrorLog;

import java.time.LocalDateTime;

public interface ApiErrorLogRepository {
    ApiErrorLog save(ApiErrorLog log);
    ApiErrorLog findById(Long id);
    ApiErrorLog update(ApiErrorLog log);
    PageResult<ApiErrorLog> findPage(ApiErrorLogPageQuery query);
    Integer deleteByCreateTimeLt(LocalDateTime expireDate, Integer deleteLimit);
}
