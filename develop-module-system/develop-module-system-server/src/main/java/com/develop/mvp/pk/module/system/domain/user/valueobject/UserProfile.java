package com.develop.mvp.pk.module.system.domain.user.valueobject;

// Skill: AggregateRoot_User_Validation_Skill — 值对象 UserProfile
// DDD 角色：不可变值对象（整体替换），封装用户个人资料
// 验收标准 AC05：提供 withXxx() 方法而非 setter

import com.develop.mvp.pk.module.system.enums.common.SexEnum;

import java.util.Objects;

/**
 * User Profile 值对象。
 */
public final class UserProfile {

    private final String nickname;
    private final String avatar;
    private final Integer sex;    // SexEnum code
    private final String remark;

    /**
     * 创建 UserProfile 实例。
     *
     * @param nickname nickname 参数
     * @param avatar avatar 参数
     * @param sex sex 参数
     * @param remark remark 参数
     */
    private UserProfile(String nickname, String avatar, Integer sex, String remark) {
        this.nickname = nickname;
        this.avatar = avatar;
        this.sex = sex;
        this.remark = remark;
    }

    /**
     * 执行 of 对应的业务操作。
     *
     * @param nickname nickname 参数
     * @param avatar avatar 参数
     * @param sex sex 参数
     * @param remark remark 参数
     * @return 处理结果
     */
    public static UserProfile of(String nickname, String avatar, Integer sex, String remark) {
        return new UserProfile(nickname, avatar, sex, remark);
    }

    // withXxx 方法 — 整体替换语义
    /**
     * 执行 with Nickname 对应的业务操作。
     *
     * @param nickname nickname 参数
     * @return 处理结果
     */
    public UserProfile withNickname(String nickname) {
        return new UserProfile(nickname, this.avatar, this.sex, this.remark);
    }

    /**
     * 执行 with Avatar 对应的业务操作。
     *
     * @param avatar avatar 参数
     * @return 处理结果
     */
    public UserProfile withAvatar(String avatar) {
        return new UserProfile(this.nickname, avatar, this.sex, this.remark);
    }

    /**
     * 执行 with Sex 对应的业务操作。
     *
     * @param sex sex 参数
     * @return 处理结果
     */
    public UserProfile withSex(Integer sex) {
        return new UserProfile(this.nickname, this.avatar, sex, this.remark);
    }

    /**
     * 执行 with Remark 对应的业务操作。
     *
     * @param remark remark 参数
     * @return 处理结果
     */
    public UserProfile withRemark(String remark) {
        return new UserProfile(this.nickname, this.avatar, this.sex, remark);
    }

    /**
     * 执行 nickname 对应的业务操作。
     *
     * @return 处理结果
     */
    public String nickname() { return nickname; }
    /**
     * 执行 avatar 对应的业务操作。
     *
     * @return 处理结果
     */
    public String avatar() { return avatar; }
    /**
     * 执行 sex 对应的业务操作。
     *
     * @return 处理结果
     */
    public Integer sex() { return sex; }
    /**
     * 执行 remark 对应的业务操作。
     *
     * @return 处理结果
     */
    public String remark() { return remark; }

    /**
     * 执行 equals 对应的业务操作。
     *
     * @param o o 参数
     * @return 处理结果
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserProfile that)) return false;
        return Objects.equals(nickname, that.nickname)
            && Objects.equals(avatar, that.avatar)
            && Objects.equals(sex, that.sex)
            && Objects.equals(remark, that.remark);
    }

    /**
     * 判断 hash Code 对应的条件是否成立。
     *
     * @return 处理结果
     */
    @Override
    public int hashCode() {
        return Objects.hash(nickname, avatar, sex, remark);
    }
}
