package com.develop.mvp.pk.module.member.domain.tag;

// Skill: AggregateRoot_MemberTag_Skill — 聚合根 MemberTag
// 验收标准 AC01：无 MyBatis/Spring 注解

import java.util.Objects;

public final class MemberTag {
    private final Long id;
    private String name;

    private MemberTag(Long id, String name) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
    }

    // ── 工厂方法 ──
    public static MemberTag create(String name) {
        return new MemberTag(null, name);
    }

    public static MemberTag reconstitute(Long id, String name) {
        return new MemberTag(id, name);
    }

    // ── 业务方法 ──
    public void rename(String name) {
        this.name = Objects.requireNonNull(name);
    }

    // ── 访问器 ──
    public Long id() { return id; }
    public String name() { return name; }

    @Override public boolean equals(Object o) { return o instanceof MemberTag t && Objects.equals(id, t.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
