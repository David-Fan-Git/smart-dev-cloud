package com.develop.mvp.pk.module.member.domain.signin;

// Skill: AggregateRoot_MemberSignInRecord_Skill — 聚合根 MemberSignInRecord
// 验收标准 AC01：无 MyBatis/Spring 注解

import java.time.LocalDateTime;
import java.util.Objects;

public final class MemberSignInRecord {
    private final Long id;
    private final Long userId;
    private Integer day;
    private Integer point;
    private Integer experience;
    private LocalDateTime createTime;

    private MemberSignInRecord(Long id, Long userId) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId);
    }

    // ── 工厂方法 ──
    public static MemberSignInRecord create(Long userId, Integer day, Integer point, Integer experience) {
        MemberSignInRecord r = new MemberSignInRecord(null, userId);
        r.day = day;
        r.point = point;
        r.experience = experience;
        return r;
    }

    public static MemberSignInRecord reconstitute(Long id, Long userId, Integer day, Integer point,
                                                   Integer experience, LocalDateTime createTime) {
        MemberSignInRecord r = new MemberSignInRecord(id, userId);
        r.day = day;
        r.point = point;
        r.experience = experience;
        r.createTime = createTime;
        return r;
    }

    // ── 访问器 ──
    public Long id() { return id; }
    public Long userId() { return userId; }
    public Integer day() { return day; }
    public Integer point() { return point; }
    public Integer experience() { return experience; }
    public LocalDateTime createTime() { return createTime; }

    @Override public boolean equals(Object o) { return o instanceof MemberSignInRecord r && Objects.equals(id, r.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
