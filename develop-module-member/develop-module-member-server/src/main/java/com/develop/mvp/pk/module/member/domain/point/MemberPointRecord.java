package com.develop.mvp.pk.module.member.domain.point;

// Skill: AggregateRoot_MemberPointRecord_Skill — 聚合根 MemberPointRecord
// 验收标准 AC01：无 MyBatis/Spring 注解

import java.time.LocalDateTime;
import java.util.Objects;

public final class MemberPointRecord {
    private final Long id;
    private final Long userId;
    private String bizId;
    private Integer bizType;
    private String title;
    private String description;
    private Integer point;
    private Integer totalPoint;
    private LocalDateTime createTime;

    private MemberPointRecord(Long id, Long userId) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId);
    }

    // ── 工厂方法 ──
    public static MemberPointRecord create(Long userId, Integer point, String bizId, Integer bizType,
                                            String title, String description, Integer totalPoint) {
        MemberPointRecord r = new MemberPointRecord(null, userId);
        r.point = point;
        r.bizId = bizId;
        r.bizType = bizType;
        r.title = title;
        r.description = description;
        r.totalPoint = totalPoint;
        return r;
    }

    public static MemberPointRecord reconstitute(Long id, Long userId, String bizId, Integer bizType,
                                                  String title, String description, Integer point,
                                                  Integer totalPoint, LocalDateTime createTime) {
        MemberPointRecord r = new MemberPointRecord(id, userId);
        r.bizId = bizId;
        r.bizType = bizType;
        r.title = title;
        r.description = description;
        r.point = point;
        r.totalPoint = totalPoint;
        r.createTime = createTime;
        return r;
    }

    // ── 访问器 ──
    public Long id() { return id; }
    public Long userId() { return userId; }
    public String bizId() { return bizId; }
    public Integer bizType() { return bizType; }
    public String title() { return title; }
    public String description() { return description; }
    public Integer point() { return point; }
    public Integer totalPoint() { return totalPoint; }
    public LocalDateTime createTime() { return createTime; }

    @Override public boolean equals(Object o) { return o instanceof MemberPointRecord r && Objects.equals(id, r.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
