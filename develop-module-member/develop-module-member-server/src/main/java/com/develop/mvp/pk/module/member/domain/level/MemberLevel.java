package com.develop.mvp.pk.module.member.domain.level;

// Skill: AggregateRoot_MemberLevel_Skill — 聚合根 MemberLevel
// 验收标准 AC01：无 MyBatis/Spring 注解

import java.util.Objects;

public final class MemberLevel {
    private final Long id;
    private String name;
    private Integer level;
    private Integer experience;
    private Integer discountPercent;
    private String icon;
    private String backgroundUrl;
    private Integer status;

    private MemberLevel(Long id, String name) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
    }

    // ── 工厂方法 ──
    public static MemberLevel create(String name) {
        return new MemberLevel(null, name);
    }

    public static MemberLevel reconstitute(Long id, String name, Integer level, Integer experience,
                                            Integer discountPercent, String icon, String backgroundUrl, Integer status) {
        MemberLevel l = new MemberLevel(id, name);
        l.level = level;
        l.experience = experience;
        l.discountPercent = discountPercent;
        l.icon = icon;
        l.backgroundUrl = backgroundUrl;
        l.status = status;
        return l;
    }

    // ── 业务方法 ──
    public void updateConfig(String name, Integer level, Integer experience, Integer discountPercent,
                              String icon, String backgroundUrl, Integer status) {
        this.name = Objects.requireNonNull(name);
        this.level = level;
        this.experience = experience;
        this.discountPercent = discountPercent;
        this.icon = icon;
        this.backgroundUrl = backgroundUrl;
        this.status = status;
    }

    public void enable() { this.status = 0; }
    public void disable() { this.status = 1; }

    // ── 访问器 ──
    public Long id() { return id; }
    public String name() { return name; }
    public Integer level() { return level; }
    public Integer experience() { return experience; }
    public Integer discountPercent() { return discountPercent; }
    public String icon() { return icon; }
    public String backgroundUrl() { return backgroundUrl; }
    public Integer status() { return status; }

    @Override public boolean equals(Object o) { return o instanceof MemberLevel l && Objects.equals(id, l.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
