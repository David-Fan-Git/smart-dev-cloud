package com.develop.mvp.pk.module.system.domain.user;

// Skill: AggregateRoot_User_Validation_Skill — 工厂 UserFactory
// DDD 角色：工厂，负责创建复杂的 User 聚合（与 User 同包，可访问包级构造器）

import com.develop.mvp.pk.module.system.domain.user.valueobject.*;

import java.util.Set;

/**
 * User Factory 工厂。
 */
public final class UserFactory {

    /**
     * 创建 UserFactory 实例。
     */
    private UserFactory() {}

    /** 创建新用户 */
    public static User create(Long id, String username, EncodedPassword encodedPassword,
                               Long tenantId, Long deptId,
                               String email, String mobile,
                               String nickname, String avatar, Integer sex, String remark,
                               Set<Long> postIds) {
        User user = new User(
                UserId.of(id),
                Username.of(username),
                encodedPassword,
                tenantId,
                deptId,
                email != null ? Email.of(email) : Email.empty(),
                mobile != null ? Mobile.of(mobile) : Mobile.empty(),
                UserProfile.of(nickname, avatar, sex, remark),
                UserStatus.ENABLED,
                postIds
        );
        user.recordCreated();
        return user;
    }

    /** 从持久化数据重建 User 聚合（供仓储实现调用） */
    public static User reconstitute(Long id, String username, String encodedPassword,
                                     Long tenantId, Long deptId,
                                     String email, String mobile,
                                     String nickname, String avatar, Integer sex, String remark,
                                     Integer status, Set<Long> postIds,
                                     String loginIp, java.time.LocalDateTime loginDate) {
        User user = new User(
                UserId.of(id),
                Username.of(username),
                EncodedPassword.of(encodedPassword),
                tenantId,
                deptId,
                email != null ? Email.of(email) : Email.empty(),
                mobile != null ? Mobile.of(mobile) : Mobile.empty(),
                UserProfile.of(nickname, avatar, sex, remark),
                UserStatus.of(status),
                postIds
        );
        if (loginDate != null) {
            user.recordLogin(LoginRecord.of(loginIp, loginDate));
            user.pullEvents(); // 清除重建时产生的登录事件
        }
        return user;
    }
}
