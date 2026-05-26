package com.develop.mvp.pk.module.crm.domain.customer.repository;

public record CrmCustomerPageQuery(
        String name,
        Long ownerUserId,
        Integer status,
        Integer pageNo,
        Integer pageSize
) {}
