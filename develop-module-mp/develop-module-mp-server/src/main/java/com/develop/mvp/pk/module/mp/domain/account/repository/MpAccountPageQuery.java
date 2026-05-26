package com.develop.mvp.pk.module.mp.domain.account.repository;

public record MpAccountPageQuery(
        String name,
        String account,
        String appId,
        Integer pageNo,
        Integer pageSize
) {}
