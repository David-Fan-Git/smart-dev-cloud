package com.develop.mvp.pk.module.member.domain.user;

// Skill: AggregateRoot_MemberUser_Skill — 聚合根 MemberUser
// 验收标准 AC01：无 MyBatis/Spring 注解
import com.develop.mvp.pk.module.member.domain.user.valueobject.*;

import java.time.LocalDateTime;
import java.util.*;

public final class MemberUser {
    private final Long id;
    private Nickname nickname;
    private Mobile mobile;
    private EncodedPassword password;
    private String email;
    private String avatar;
    private UserStatus status;
    private Long tenantId;
    // 注册信息
    private String registerIp;
    private Integer registerTerminal;
    // 登录信息
    private String loginIp;
    private LocalDateTime loginDate;
    // 等级/经验/积分/分组/标签
    private Long levelId;
    private Integer experience;
    private Integer point;
    private Long groupId;
    private List<Long> tagIds;

    private MemberUser(Long id, Nickname nickname, Mobile mobile, EncodedPassword password) {
        this.id = id;
        this.nickname = Objects.requireNonNull(nickname);
        this.mobile = mobile;
        this.password = password;
        this.status = UserStatus.enabled();
        this.point = 0;
        this.experience = 0;
    }

    // ── 工厂方法 ──
    static MemberUser create(Nickname nickname, Mobile mobile, EncodedPassword password) {
        return new MemberUser(null, nickname, mobile, password);
    }

    public static MemberUser reconstitute(Long id, Nickname nickname, Mobile mobile, EncodedPassword password,
                                          UserStatus status, String email, String avatar, Long tenantId,
                                          String loginIp, LocalDateTime loginDate, String registerIp, Integer registerTerminal,
                                          Long levelId, Integer experience, Integer point, Long groupId, List<Long> tagIds) {
        MemberUser u = new MemberUser(id, nickname, mobile, password);
        u.status = status;
        u.email = email;
        u.avatar = avatar;
        u.tenantId = tenantId;
        u.loginIp = loginIp;
        u.loginDate = loginDate;
        u.registerIp = registerIp;
        u.registerTerminal = registerTerminal;
        u.levelId = levelId;
        u.experience = experience;
        u.point = point;
        u.groupId = groupId;
        u.tagIds = tagIds;
        return u;
    }

    // ── 业务方法 ──
    public MemberUser recordRegisterInfo(String registerIp, Integer terminal) {
        this.registerIp = registerIp;
        this.registerTerminal = terminal;
        return this;
    }

    public void recordLogin(String loginIp) {
        this.loginIp = loginIp;
        this.loginDate = LocalDateTime.now();
    }

    public void changePassword(EncodedPassword newPassword) {
        this.password = Objects.requireNonNull(newPassword);
    }

    public void disable() { this.status = UserStatus.disabled(); }
    public void enable() { this.status = UserStatus.enabled(); }

    public void updateProfile(Nickname nickname, String avatar) {
        this.nickname = Objects.requireNonNull(nickname);
        this.avatar = avatar;
    }

    public void updateMobile(Mobile mobile) { this.mobile = Objects.requireNonNull(mobile); }

    public void updateLevel(Long levelId, Integer experience) {
        this.levelId = levelId;
        this.experience = experience;
    }

    public boolean addPoint(Integer delta) {
        if (delta == 0) return true;
        if (delta > 0) { this.point += delta; return true; }
        if (this.point + delta < 0) return false;
        this.point += delta;
        return true;
    }

    // ── 访问器 ──
    public Long id() { return id; }
    public Nickname nickname() { return nickname; }
    public Mobile mobile() { return mobile; }
    public EncodedPassword password() { return password; }
    public String email() { return email; }
    public String avatar() { return avatar; }
    public UserStatus status() { return status; }
    public Long tenantId() { return tenantId; }
    public String loginIp() { return loginIp; }
    public LocalDateTime loginDate() { return loginDate; }
    public String registerIp() { return registerIp; }
    public Integer registerTerminal() { return registerTerminal; }
    public Long levelId() { return levelId; }
    public Integer experience() { return experience != null ? experience : 0; }
    public Integer point() { return point != null ? point : 0; }
    public Long groupId() { return groupId; }
    public List<Long> tagIds() { return tagIds; }

    @Override public boolean equals(Object o) { return o instanceof MemberUser u && Objects.equals(id, u.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
