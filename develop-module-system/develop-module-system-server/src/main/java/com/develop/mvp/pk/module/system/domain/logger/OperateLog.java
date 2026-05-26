package com.develop.mvp.pk.module.system.domain.logger;

import java.time.LocalDateTime;

/**
 * Operate Log 领域模型。
 */
public final class OperateLog {

    private final Long id;
    private final String traceId;
    private final Long userId;
    private final Integer userType;
    private final String type;
    private final String subType;
    private final Long bizId;
    private final String action;
    private final String extra;
    private final String requestMethod;
    private final String requestUrl;
    private final String userIp;
    private final String userAgent;
    private final LocalDateTime createTime;

    /**
     * 创建 OperateLog 实例。
     *
     * @param builder builder 参数
     */
    private OperateLog(Builder builder) {
        this.id = builder.id;
        this.traceId = builder.traceId;
        this.userId = builder.userId;
        this.userType = builder.userType;
        this.type = builder.type;
        this.subType = builder.subType;
        this.bizId = builder.bizId;
        this.action = builder.action;
        this.extra = builder.extra;
        this.requestMethod = builder.requestMethod;
        this.requestUrl = builder.requestUrl;
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
     * 执行 type 对应的业务操作。
     *
     * @return 处理结果
     */
    public String type() {
        return type;
    }

    /**
     * 执行 sub Type 对应的业务操作。
     *
     * @return 处理结果
     */
    public String subType() {
        return subType;
    }

    /**
     * 执行 biz Id 对应的业务操作。
     *
     * @return 处理结果
     */
    public Long bizId() {
        return bizId;
    }

    /**
     * 执行 action 对应的业务操作。
     *
     * @return 处理结果
     */
    public String action() {
        return action;
    }

    /**
     * 执行 extra 对应的业务操作。
     *
     * @return 处理结果
     */
    public String extra() {
        return extra;
    }

    /**
     * 执行 request Method 对应的业务操作。
     *
     * @return 处理结果
     */
    public String requestMethod() {
        return requestMethod;
    }

    /**
     * 执行 request Url 对应的业务操作。
     *
     * @return 处理结果
     */
    public String requestUrl() {
        return requestUrl;
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
     * 返回操作日志创建时间。
     *
     * @return 操作日志创建时间
     */
    public LocalDateTime createTime() {
        return createTime;
    }

    /**
     * Builder 领域模型。
     */
    public static class Builder {
        private Long id;
        private String traceId;
        private Long userId;
        private Integer userType;
        private String type;
        private String subType;
        private Long bizId;
        private String action;
        private String extra;
        private String requestMethod;
        private String requestUrl;
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
         * 执行 type 对应的业务操作。
         *
         * @param type type 参数
         * @return 处理结果
         */
        public Builder type(String type) {
            this.type = type;
            return this;
        }

        /**
         * 执行 sub Type 对应的业务操作。
         *
         * @param subType subType 参数
         * @return 处理结果
         */
        public Builder subType(String subType) {
            this.subType = subType;
            return this;
        }

        /**
         * 执行 biz Id 对应的业务操作。
         *
         * @param bizId bizId 参数
         * @return 处理结果
         */
        public Builder bizId(Long bizId) {
            this.bizId = bizId;
            return this;
        }

        /**
         * 执行 action 对应的业务操作。
         *
         * @param action action 参数
         * @return 处理结果
         */
        public Builder action(String action) {
            this.action = action;
            return this;
        }

        /**
         * 执行 extra 对应的业务操作。
         *
         * @param extra extra 参数
         * @return 处理结果
         */
        public Builder extra(String extra) {
            this.extra = extra;
            return this;
        }

        /**
         * 执行 request Method 对应的业务操作。
         *
         * @param requestMethod requestMethod 参数
         * @return 处理结果
         */
        public Builder requestMethod(String requestMethod) {
            this.requestMethod = requestMethod;
            return this;
        }

        /**
         * 执行 request Url 对应的业务操作。
         *
         * @param requestUrl requestUrl 参数
         * @return 处理结果
         */
        public Builder requestUrl(String requestUrl) {
            this.requestUrl = requestUrl;
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
         * 设置操作日志创建时间。
         *
         * @param createTime 操作日志创建时间
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
        public OperateLog build() {
            return new OperateLog(this);
        }
    }
}
