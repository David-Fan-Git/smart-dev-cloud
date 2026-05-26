package com.develop.mvp.pk.module.ai.domain.model.repository;

public record AiModelPageQuery(
        String name,
        String model,
        String platform,
        Integer type,
        Integer status,
        Integer pageNo,
        Integer pageSize
) {}
