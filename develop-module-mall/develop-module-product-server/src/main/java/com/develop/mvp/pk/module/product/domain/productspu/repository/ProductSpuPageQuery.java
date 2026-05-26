package com.develop.mvp.pk.module.product.domain.productspu.repository;

// Skill: AggregateRoot_ProductSpu_Validation_Skill — 仓储查询对象 ProductSpuPageQuery
// DDD 角色：封装商品 SPU 分页查询条件

import java.time.LocalDateTime;

public record ProductSpuPageQuery(
        String name,
        Long categoryId,
        Integer status,
        Integer tabType,
        String keyword,
        LocalDateTime[] createTime,
        Integer pageNo,
        Integer pageSize
) {}
