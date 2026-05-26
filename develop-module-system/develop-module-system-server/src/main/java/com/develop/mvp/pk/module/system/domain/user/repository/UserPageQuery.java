package com.develop.mvp.pk.module.system.domain.user.repository;

// Skill: AggregateRoot_User_Validation_Skill — 查询对象 UserPageQuery
// DDD 角色：值对象，封装分页查询条件

import java.util.Collection;
import java.util.Collections;

/**
 * User Page Query 查询条件对象。
 */
public final class UserPageQuery {

    private final String username;
    private final String mobile;
    private final Integer status;
    private final Collection<Long> deptIds;
    private final Collection<Long> userIds;
    private final int pageNo;
    private final int pageSize;

    /**
     * 创建 UserPageQuery 实例。
     *
     * @param builder builder 参数
     */
    private UserPageQuery(Builder builder) {
        this.username = builder.username;
        this.mobile = builder.mobile;
        this.status = builder.status;
        this.deptIds = builder.deptIds != null ? builder.deptIds : Collections.emptySet();
        this.userIds = builder.userIds != null ? builder.userIds : Collections.emptySet();
        this.pageNo = builder.pageNo;
        this.pageSize = builder.pageSize;
    }

    /**
     * 构建 builder 对应的数据对象。
     *
     * @return 处理结果
     */
    public static Builder builder() { return new Builder(); }

    /**
     * 执行 username 对应的业务操作。
     *
     * @return 处理结果
     */
    public String username() { return username; }
    /**
     * 执行 mobile 对应的业务操作。
     *
     * @return 处理结果
     */
    public String mobile() { return mobile; }
    /**
     * 执行 status 对应的业务操作。
     *
     * @return 处理结果
     */
    public Integer status() { return status; }
    /**
     * 执行 dept Ids 对应的业务操作。
     *
     * @return 处理结果
     */
    public Collection<Long> deptIds() { return deptIds; }
    /**
     * 执行 user Ids 对应的业务操作。
     *
     * @return 处理结果
     */
    public Collection<Long> userIds() { return userIds; }
    /**
     * 查询 page No 对应的数据。
     *
     * @return 处理结果
     */
    public int pageNo() { return pageNo; }
    /**
     * 查询 page Size 对应的数据。
     *
     * @return 处理结果
     */
    public int pageSize() { return pageSize; }

    /**
     * Builder 领域模型。
     */
    public static class Builder {
        private String username;
        private String mobile;
        private Integer status;
        private Collection<Long> deptIds;
        private Collection<Long> userIds;
        private int pageNo = 1;
        private int pageSize = 10;

        /**
         * 执行 username 对应的业务操作。
         *
         * @param v v 参数
         * @return 处理结果
         */
        public Builder username(String v) { this.username = v; return this; }
        /**
         * 执行 mobile 对应的业务操作。
         *
         * @param v v 参数
         * @return 处理结果
         */
        public Builder mobile(String v) { this.mobile = v; return this; }
        /**
         * 执行 status 对应的业务操作。
         *
         * @param v v 参数
         * @return 处理结果
         */
        public Builder status(Integer v) { this.status = v; return this; }
        /**
         * 执行 dept Ids 对应的业务操作。
         *
         * @param v v 参数
         * @return 处理结果
         */
        public Builder deptIds(Collection<Long> v) { this.deptIds = v; return this; }
        /**
         * 执行 user Ids 对应的业务操作。
         *
         * @param v v 参数
         * @return 处理结果
         */
        public Builder userIds(Collection<Long> v) { this.userIds = v; return this; }
        /**
         * 查询 page No 对应的数据。
         *
         * @param v v 参数
         * @return 处理结果
         */
        public Builder pageNo(int v) { this.pageNo = v; return this; }
        /**
         * 查询 page Size 对应的数据。
         *
         * @param v v 参数
         * @return 处理结果
         */
        public Builder pageSize(int v) { this.pageSize = v; return this; }
        /**
         * 构建 build 对应的数据对象。
         *
         * @return 处理结果
         */
        public UserPageQuery build() { return new UserPageQuery(this); }
    }
}
