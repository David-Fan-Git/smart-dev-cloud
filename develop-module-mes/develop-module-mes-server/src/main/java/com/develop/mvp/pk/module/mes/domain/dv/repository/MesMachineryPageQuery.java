package com.develop.mvp.pk.module.mes.domain.dv.repository;

public record MesMachineryPageQuery(
        String code,
        String name,
        String brand,
        Long machineryTypeId,
        Long workshopId,
        Integer status,
        Integer pageNo,
        Integer pageSize
) {}
