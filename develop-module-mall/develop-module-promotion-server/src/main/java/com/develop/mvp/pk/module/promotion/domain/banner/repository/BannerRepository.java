package com.develop.mvp.pk.module.promotion.domain.banner.repository;

// Skill: AggregateRoot_Banner_Validation_Skill — 仓储接口 BannerRepository
// DDD 角色：领域层定义的仓储接口，不依赖任何基础设施

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.promotion.domain.banner.Banner;
import com.develop.mvp.pk.module.promotion.domain.banner.valueobject.BannerId;

import java.util.List;

public interface BannerRepository {
    Banner save(Banner banner);
    void delete(BannerId id);
    Banner findById(BannerId id);
    List<Banner> findByStatus(Integer status);
    List<Banner> findByPosition(Integer position);
    List<Banner> findAll();
    PageResult<Banner> findPage(String title, Integer pageNo, Integer pageSize);
}
