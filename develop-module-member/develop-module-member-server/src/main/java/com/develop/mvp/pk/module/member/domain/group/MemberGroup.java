package com.develop.mvp.pk.module.member.domain.group;

// Skill: AggregateRoot_MemberGroup_Skill — 聚合根 MemberGroup
// 验收标准 AC01：无 MyBatis/Spring 注解

import java.util.Objects;

public final class MemberGroup {
    private final Long id;
    private String name;
    private String remark;
    private Integer status;

    private MemberGroup(Long id, String name) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.status = 0;
    }

    // ── 工厂方法 ──
    public static MemberGroup create(String name) {
        return new MemberGroup(null, name);
    }

    public static MemberGroup reconstitute(Long id, String name, String remark, Integer status) {
        MemberGroup g = new MemberGroup(id, name);
        g.remark = remark;
        g.status = status;
        return g;
    }

    // ── 业务方法 ──
    public void updateInfo(String name, String remark) {
        this.name = Objects.requireNonNull(name);
        this.remark = remark;
    }

    public void enable() { this.status = 0; }
    public void disable() { this.status = 1; }

    // ── 访问器 ──
    public Long id() { return id; }
    public String name() { return name; }
    public String remark() { return remark; }
    public Integer status() { return status; }

    @Override public boolean equals(Object o) { return o instanceof MemberGroup g && Objects.equals(id, g.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
