package com.develop.mvp.pk.module.promotion.domain.banner;

// Skill: AggregateRoot_Banner_Validation_Skill — 工厂 BannerFactory
// DDD 角色：工厂，负责创建和重建 Banner 聚合

import com.develop.mvp.pk.module.promotion.domain.banner.valueobject.BannerId;

public final class BannerFactory {

    private BannerFactory() {}

    public static Banner create(Long id, String title, String url, String picUrl,
                                 Integer sort, Integer status, Integer position, String memo) {
        return new Banner(id != null ? BannerId.of(id) : null, title, url, picUrl, sort, status, position, memo);
    }

    public static Banner reconstitute(Long id, String title, String url, String picUrl,
                                       Integer sort, Integer status, Integer position, String memo) {
        return new Banner(BannerId.of(id), title, url, picUrl, sort, status, position, memo);
    }
}
