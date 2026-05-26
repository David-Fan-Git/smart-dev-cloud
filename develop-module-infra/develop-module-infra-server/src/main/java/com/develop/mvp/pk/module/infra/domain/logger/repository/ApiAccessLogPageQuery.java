package com.develop.mvp.pk.module.infra.domain.logger.repository;

// DDD 角色：API 访问日志分页查询对象

import java.time.LocalDateTime;

public record ApiAccessLogPageQuery(
        Long userId,
        Integer userType,
        String applicationName,
        String requestUrl,
        LocalDateTime[] beginTime,
        Integer duration,
        Integer resultCode,
        Integer pageNo,
        Integer pageSize
) {}
