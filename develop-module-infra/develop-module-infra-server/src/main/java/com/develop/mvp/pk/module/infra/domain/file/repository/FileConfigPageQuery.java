package com.develop.mvp.pk.module.infra.domain.file.repository;

// DDD 角色：文件配置分页查询对象

import java.time.LocalDateTime;

public record FileConfigPageQuery(
        String name,
        Integer storage,
        LocalDateTime[] createTime,
        Integer pageNo,
        Integer pageSize
) {}
