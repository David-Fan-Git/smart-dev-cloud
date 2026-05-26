package com.develop.mvp.pk.module.promotion.domain.seckill.repository;

// Skill: AggregateRoot_SeckillActivity_Validation_Skill — 仓储接口

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.promotion.domain.seckill.SeckillActivity;
import com.develop.mvp.pk.module.promotion.domain.seckill.valueobject.SeckillActivityId;

import java.util.List;

public interface SeckillActivityRepository {
    SeckillActivity save(SeckillActivity activity);
    void delete(SeckillActivityId id);
    SeckillActivity findById(SeckillActivityId id);
    List<SeckillActivity> findByStatus(Integer status);
    List<SeckillActivity> findActiveActivities();
    PageResult<SeckillActivity> findPage(String name, Integer status, Long spuId, Integer pageNo, Integer pageSize);
    long count();
}
