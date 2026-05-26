package com.develop.mvp.pk.module.promotion.domain.banner.valueobject;

// Skill: AggregateRoot_Banner_Validation_Skill — 值对象 BannerId
// DDD 角色：Banner 聚合根标识
// 验收标准 AC04：final 字段，无 setter

import java.util.Objects;

public final class BannerId {
    private final Long value;

    private BannerId(Long value) {
        this.value = Objects.requireNonNull(value, "Banner编号不能为空");
    }

    public static BannerId of(Long value) { return new BannerId(value); }

    public Long value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BannerId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return "BannerId{" + value + '}'; }
}
