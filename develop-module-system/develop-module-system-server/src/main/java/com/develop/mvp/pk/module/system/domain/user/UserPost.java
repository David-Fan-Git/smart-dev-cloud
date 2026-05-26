package com.develop.mvp.pk.module.system.domain.user;

// Skill: AggregateRoot_User_Validation_Skill — 聚合内部实体 UserPost
// DDD 角色：User 聚合内部实体，表示用户-岗位关联
// 不变式 I07：删除用户时，其所有岗位关联必须同时删除

import java.util.Objects;

/**
 * User Post 领域模型。
 */
public final class UserPost {

    private final Long userId;
    private final Long postId;

    /**
     * 创建 UserPost 实例。
     *
     * @param userId userId 参数
     * @param postId postId 参数
     */
    public UserPost(Long userId, Long postId) {
        this.userId = Objects.requireNonNull(userId, "userId 不能为空");
        this.postId = Objects.requireNonNull(postId, "postId 不能为空");
    }

    /**
     * 执行 user Id 对应的业务操作。
     *
     * @return 处理结果
     */
    public Long userId() { return userId; }
    /**
     * 执行 post Id 对应的业务操作。
     *
     * @return 处理结果
     */
    public Long postId() { return postId; }

    /**
     * 执行 equals 对应的业务操作。
     *
     * @param o o 参数
     * @return 处理结果
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserPost that)) return false;
        return userId.equals(that.userId) && postId.equals(that.postId);
    }

    /**
     * 判断 hash Code 对应的条件是否成立。
     *
     * @return 处理结果
     */
    @Override
    public int hashCode() { return Objects.hash(userId, postId); }
}
