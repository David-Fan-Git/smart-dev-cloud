package com.develop.mvp.pk.module.infra.domain.config.repository;

// DDD 角色：配置分页查询对象

import java.time.LocalDateTime;

public record ConfigPageQuery(
        String name,
        String key,
        Integer type,
        LocalDateTime[] createTime,
        Integer pageNo,
        Integer pageSize
) {}
