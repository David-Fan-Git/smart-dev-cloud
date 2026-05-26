package com.develop.mvp.pk.module.member.domain.signin;

// Skill: AggregateRoot_MemberSignInConfig_Skill — 聚合根 MemberSignInConfig
// 验收标准 AC01：无 MyBatis/Spring 注解

import java.util.Objects;

public final class MemberSignInConfig {
    private final Long id;
    private Integer day;
    private Integer point;
    private Integer experience;
    private Integer status;

    private MemberSignInConfig(Long id) {
        this.id = id;
    }

    // ── 工厂方法 ──
    public static MemberSignInConfig create(Integer day, Integer point, Integer experience) {
        MemberSignInConfig c = new MemberSignInConfig(null);
        c.day = Objects.requireNonNull(day);
        c.point = point;
        c.experience = experience;
        return c;
    }

    public static MemberSignInConfig reconstitute(Long id, Integer day, Integer point, Integer experience, Integer status) {
        MemberSignInConfig c = new MemberSignInConfig(id);
        c.day = day;
        c.point = point;
        c.experience = experience;
        c.status = status;
        return c;
    }

    // ── 业务方法 ──
    public void update(Integer day, Integer point, Integer experience, Integer status) {
        this.day = Objects.requireNonNull(day);
        this.point = point;
        this.experience = experience;
        this.status = status;
    }

    public void enable() { this.status = 0; }
    public void disable() { this.status = 1; }

    // ── 访问器 ──
    public Long id() { return id; }
    public Integer day() { return day; }
    public Integer point() { return point; }
    public Integer experience() { return experience; }
    public Integer status() { return status; }

    @Override public boolean equals(Object o) { return o instanceof MemberSignInConfig c && Objects.equals(id, c.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
