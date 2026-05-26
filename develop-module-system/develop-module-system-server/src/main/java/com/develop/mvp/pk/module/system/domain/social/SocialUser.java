package com.develop.mvp.pk.module.system.domain.social;

// DDD 角色：社交用户聚合根 — 封装第三方社交平台用户绑定
import java.util.Objects;

/**
 * Social User 领域模型。
 */
public final class SocialUser {
    private final Long id; private final Integer type; private final String openid;
    private Long userId; private Integer userType; private String token, rawUserInfo, nickname, avatar;
    /**
     * 创建 SocialUser 实例。
     *
     * @param id id 参数
     * @param type type 参数
     * @param openid openid 参数
     */
    private SocialUser(Long id, Integer type, String openid) { this.id = Objects.requireNonNull(id); this.type = Objects.requireNonNull(type); this.openid = Objects.requireNonNull(openid); }
    /**
     * 执行 of 对应的业务操作。
     *
     * @param id id 参数
     * @param type type 参数
     * @param openid openid 参数
     * @return 处理结果
     */
    public static SocialUser of(Long id, Integer type, String openid) { return new SocialUser(id, type, openid); }
    /**
     * 执行 id 对应的业务操作。
     *
     * @return 处理结果
     */
    public Long id() { return id; } public Integer type() { return type; } public String openid() { return openid; }
    /**
     * 执行 user Id 对应的业务操作。
     *
     * @return 处理结果
     */
    public Long userId() { return userId; } public Integer userType() { return userType; }
    /**
     * 执行 token 对应的业务操作。
     *
     * @return 处理结果
     */
    public String token() { return token; } public String rawUserInfo() { return rawUserInfo; }
    /**
     * 执行 nickname 对应的业务操作。
     *
     * @return 处理结果
     */
    public String nickname() { return nickname; } public String avatar() { return avatar; }
    /**
     * 执行 user Id 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public SocialUser userId(Long v) { this.userId = v; return this; }
    /**
     * 执行 user Type 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public SocialUser userType(Integer v) { this.userType = v; return this; }
    /**
     * 执行 token 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public SocialUser token(String v) { this.token = v; return this; }
    /**
     * 执行 raw User Info 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public SocialUser rawUserInfo(String v) { this.rawUserInfo = v; return this; }
    /**
     * 执行 nickname 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public SocialUser nickname(String v) { this.nickname = v; return this; }
    /**
     * 执行 avatar 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public SocialUser avatar(String v) { this.avatar = v; return this; }
    @Override public boolean equals(Object o) { return o instanceof SocialUser s && id.equals(s.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
