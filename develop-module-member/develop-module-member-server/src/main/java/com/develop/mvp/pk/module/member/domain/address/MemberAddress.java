package com.develop.mvp.pk.module.member.domain.address;

// Skill: AggregateRoot_MemberAddress_Skill — 聚合根 MemberAddress
// 验收标准 AC01：无 MyBatis/Spring 注解

import java.util.Objects;

public final class MemberAddress {
    private final Long id;
    private final Long userId;
    private String name;
    private String mobile;
    private Long areaId;
    private String detailAddress;
    private boolean defaultStatus;

    private MemberAddress(Long id, Long userId, String name, String mobile,
                          Long areaId, String detailAddress, boolean defaultStatus) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId);
        this.name = Objects.requireNonNull(name);
        this.mobile = Objects.requireNonNull(mobile);
        this.areaId = areaId;
        this.detailAddress = Objects.requireNonNull(detailAddress);
        this.defaultStatus = defaultStatus;
    }

    // ── 工厂方法 ──
    public static MemberAddress create(Long userId, String name, String mobile,
                                        Long areaId, String detailAddress, boolean defaultStatus) {
        return new MemberAddress(null, userId, name, mobile, areaId, detailAddress, defaultStatus);
    }

    public static MemberAddress reconstitute(Long id, Long userId, String name, String mobile,
                                              Long areaId, String detailAddress, boolean defaultStatus) {
        return new MemberAddress(id, userId, name, mobile, areaId, detailAddress, defaultStatus);
    }

    // ── 业务方法 ──
    public void updateInfo(String name, String mobile, Long areaId, String detailAddress) {
        this.name = Objects.requireNonNull(name);
        this.mobile = Objects.requireNonNull(mobile);
        this.areaId = areaId;
        this.detailAddress = Objects.requireNonNull(detailAddress);
    }

    public void markDefault() { this.defaultStatus = true; }

    public void unmarkDefault() { this.defaultStatus = false; }

    // ── 访问器 ──
    public Long id() { return id; }
    public Long userId() { return userId; }
    public String name() { return name; }
    public String mobile() { return mobile; }
    public Long areaId() { return areaId; }
    public String detailAddress() { return detailAddress; }
    public boolean defaultStatus() { return defaultStatus; }

    @Override public boolean equals(Object o) { return o instanceof MemberAddress a && Objects.equals(id, a.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
