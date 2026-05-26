package com.develop.mvp.pk.module.erp.domain.product.repository;

public record ErpProductPageQuery(
        String name,
        String barCode,
        Long categoryId,
        Integer status,
        Integer pageNo,
        Integer pageSize
) {}
