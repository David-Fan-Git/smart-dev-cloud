package com.develop.mvp.pk.module.infra.domain.file.repository;

// DDD 角色：文件分页查询对象

import java.time.LocalDateTime;

public record FilePageQuery(
        String path,
        String type,
        LocalDateTime[] createTime,
        Integer pageNo,
        Integer pageSize
) {}
