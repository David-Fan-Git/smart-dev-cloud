package com.develop.mvp.pk.module.member.domain.config;

// Skill: AggregateRoot_MemberConfig_Skill — 聚合根 MemberConfig
// 验收标准 AC01：无 MyBatis/Spring 注解

import java.util.Objects;

public final class MemberConfig {
    private final Long id;
    private Boolean pointTradeDeductEnable;
    private Integer pointTradeDeductUnitPrice;
    private Integer pointTradeDeductMaxPrice;
    private Integer pointTradeGivePoint;

    private MemberConfig(Long id) {
        this.id = id;
    }

    // ── 工厂方法 ──
    public static MemberConfig create(Boolean pointTradeDeductEnable, Integer pointTradeDeductUnitPrice,
                                       Integer pointTradeDeductMaxPrice, Integer pointTradeGivePoint) {
        MemberConfig c = new MemberConfig(null);
        c.update(pointTradeDeductEnable, pointTradeDeductUnitPrice, pointTradeDeductMaxPrice, pointTradeGivePoint);
        return c;
    }

    public static MemberConfig reconstitute(Long id, Boolean pointTradeDeductEnable, Integer pointTradeDeductUnitPrice,
                                             Integer pointTradeDeductMaxPrice, Integer pointTradeGivePoint) {
        MemberConfig c = new MemberConfig(id);
        c.pointTradeDeductEnable = pointTradeDeductEnable;
        c.pointTradeDeductUnitPrice = pointTradeDeductUnitPrice;
        c.pointTradeDeductMaxPrice = pointTradeDeductMaxPrice;
        c.pointTradeGivePoint = pointTradeGivePoint;
        return c;
    }

    // ── 业务方法 ──
    public void update(Boolean pointTradeDeductEnable, Integer pointTradeDeductUnitPrice,
                       Integer pointTradeDeductMaxPrice, Integer pointTradeGivePoint) {
        this.pointTradeDeductEnable = pointTradeDeductEnable;
        this.pointTradeDeductUnitPrice = pointTradeDeductUnitPrice;
        this.pointTradeDeductMaxPrice = pointTradeDeductMaxPrice;
        this.pointTradeGivePoint = pointTradeGivePoint;
    }

    // ── 访问器 ──
    public Long id() { return id; }
    public Boolean pointTradeDeductEnable() { return pointTradeDeductEnable; }
    public Integer pointTradeDeductUnitPrice() { return pointTradeDeductUnitPrice; }
    public Integer pointTradeDeductMaxPrice() { return pointTradeDeductMaxPrice; }
    public Integer pointTradeGivePoint() { return pointTradeGivePoint; }

    @Override public boolean equals(Object o) { return o instanceof MemberConfig c && Objects.equals(id, c.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
