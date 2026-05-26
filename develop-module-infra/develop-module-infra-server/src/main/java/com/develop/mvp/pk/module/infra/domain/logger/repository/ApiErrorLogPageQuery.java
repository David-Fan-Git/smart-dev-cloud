package com.develop.mvp.pk.module.infra.domain.logger.repository;

// DDD 角色：API 错误日志分页查询对象

import java.time.LocalDateTime;

public record ApiErrorLogPageQuery(
        Long userId,
        Integer userType,
        String applicationName,
        String requestUrl,
        LocalDateTime[] beginTime,
        Integer duration,
        Integer resultCode,
        Integer processStatus,
        Integer pageNo,
        Integer pageSize
) {}
