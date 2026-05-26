package com.develop.mvp.pk.module.system.domain.logger;

import java.time.LocalDateTime;

/**
 * Login Log 领域模型。
 */
public final class LoginLog {

    private final Long id;
    private final Integer logType;
    private final String traceId;
    private final Long userId;
    private final Integer userType;
    private final String username;
    private final Integer result;
    private final String userIp;
    private final String userAgent;
    private final LocalDateTime createTime;

    /**
     * 创建 LoginLog 实例。
     *
     * @param builder builder 参数
     */
    private LoginLog(Builder builder) {
        this.id = builder.id;
        this.logType = builder.logType;
        this.traceId = builder.traceId;
        this.userId = builder.userId;
        this.userType = builder.userType;
        this.username = builder.username;
        this.result = builder.result;
        this.userIp = builder.userIp;
        this.userAgent = builder.userAgent;
        this.createTime = builder.createTime;
    }

    /**
     * 构建 builder 对应的数据对象。
     *
     * @return 处理结果
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 执行 id 对应的业务操作。
     *
     * @return 处理结果
     */
    public Long id() {
        return id;
    }

    /**
     * 执行 log Type 对应的业务操作。
     *
     * @return 处理结果
     */
    public Integer logType() {
        return logType;
    }

    /**
     * 执行 trace Id 对应的业务操作。
     *
     * @return 处理结果
     */
    public String traceId() {
        return traceId;
    }

    /**
     * 执行 user Id 对应的业务操作。
     *
     * @return 处理结果
     */
    public Long userId() {
        return userId;
    }

    /**
     * 执行 user Type 对应的业务操作。
     *
     * @return 处理结果
     */
    public Integer userType() {
        return userType;
    }

    /**
     * 执行 username 对应的业务操作。
     *
     * @return 处理结果
     */
    public String username() {
        return username;
    }

    /**
     * 执行 result 对应的业务操作。
     *
     * @return 处理结果
     */
    public Integer result() {
        return result;
    }

    /**
     * 执行 user Ip 对应的业务操作。
     *
     * @return 处理结果
     */
    public String userIp() {
        return userIp;
    }

    /**
     * 执行 user Agent 对应的业务操作。
     *
     * @return 处理结果
     */
    public String userAgent() {
        return userAgent;
    }

    /**
     * 返回登录日志创建时间。
     *
     * @return 登录日志创建时间
     */
    public LocalDateTime createTime() {
        return createTime;
    }

    /**
     * Builder 领域模型。
     */
    public static class Builder {
        private Long id;
        private Integer logType;
        private String traceId;
        private Long userId;
        private Integer userType;
        private String username;
        private Integer result;
        private String userIp;
        private String userAgent;
        private LocalDateTime createTime;

        /**
         * 执行 id 对应的业务操作。
         *
         * @param id id 参数
         * @return 处理结果
         */
        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        /**
         * 执行 log Type 对应的业务操作。
         *
         * @param logType logType 参数
         * @return 处理结果
         */
        public Builder logType(Integer logType) {
            this.logType = logType;
            return this;
        }

        /**
         * 执行 trace Id 对应的业务操作。
         *
         * @param traceId traceId 参数
         * @return 处理结果
         */
        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        /**
         * 执行 user Id 对应的业务操作。
         *
         * @param userId userId 参数
         * @return 处理结果
         */
        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        /**
         * 执行 user Type 对应的业务操作。
         *
         * @param userType userType 参数
         * @return 处理结果
         */
        public Builder userType(Integer userType) {
            this.userType = userType;
            return this;
        }

        /**
         * 执行 username 对应的业务操作。
         *
         * @param username username 参数
         * @return 处理结果
         */
        public Builder username(String username) {
            this.username = username;
            return this;
        }

        /**
         * 执行 result 对应的业务操作。
         *
         * @param result result 参数
         * @return 处理结果
         */
        public Builder result(Integer result) {
            this.result = result;
            return this;
        }

        /**
         * 执行 user Ip 对应的业务操作。
         *
         * @param userIp userIp 参数
         * @return 处理结果
         */
        public Builder userIp(String userIp) {
            this.userIp = userIp;
            return this;
        }

        /**
         * 执行 user Agent 对应的业务操作。
         *
         * @param userAgent userAgent 参数
         * @return 处理结果
         */
        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        /**
         * 设置登录日志创建时间。
         *
         * @param createTime 登录日志创建时间
         * @return 当前构建器
         */
        public Builder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        /**
         * 构建 build 对应的数据对象。
         *
         * @return 处理结果
         */
        public LoginLog build() {
            return new LoginLog(this);
        }
    }
}
