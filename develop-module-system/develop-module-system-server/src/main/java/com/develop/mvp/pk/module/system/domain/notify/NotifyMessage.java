package com.develop.mvp.pk.module.system.domain.notify;
// DDD 角色：站内信消息聚合根
import java.time.LocalDateTime; import java.util.Objects;
/**
 * Notify Message 领域模型。
 */
public final class NotifyMessage {
    private final Long id; private Long userId; private Integer userType, readStatus; private Long templateId; private String templateCode, templateType, templateContent, templateNickname; private Integer templateParams; private LocalDateTime readTime;
    /**
     * 创建 NotifyMessage 实例。
     *
     * @param id id 参数
     */
    private NotifyMessage(Long id) { this.id = Objects.requireNonNull(id); }
    /**
     * 执行 of 对应的业务操作。
     *
     * @param id id 参数
     * @return 处理结果
     */
    public static NotifyMessage of(Long id) { return new NotifyMessage(id); }
    /**
     * 执行 id 对应的业务操作。
     *
     * @return 处理结果
     */
    public Long id() { return id; } public Long userId() { return userId; } public Integer userType() { return userType; }
    /**
     * 执行 read Status 对应的业务操作。
     *
     * @return 处理结果
     */
    public Integer readStatus() { return readStatus; } public Long templateId() { return templateId; } public String templateCode() { return templateCode; }
    /**
     * 执行 template Content 对应的业务操作。
     *
     * @return 处理结果
     */
    public String templateContent() { return templateContent; } public String templateNickname() { return templateNickname; }
    /**
     * 执行 user Id 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public NotifyMessage userId(Long v) { userId = v; return this; } public NotifyMessage userType(Integer v) { userType = v; return this; }
    /**
     * 执行 read Status 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public NotifyMessage readStatus(Integer v) { readStatus = v; return this; } public NotifyMessage templateId(Long v) { templateId = v; return this; }
    /**
     * 执行 template Code 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public NotifyMessage templateCode(String v) { templateCode = v; return this; } public NotifyMessage templateType(String v) { templateType = v; return this; }
    /**
     * 执行 template Content 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public NotifyMessage templateContent(String v) { templateContent = v; return this; } public NotifyMessage templateNickname(String v) { templateNickname = v; return this; }
    /**
     * 执行 template Params 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public NotifyMessage templateParams(Integer v) { templateParams = v; return this; } public NotifyMessage readTime(LocalDateTime v) { readTime = v; return this; }
    @Override public boolean equals(Object o) { return o instanceof NotifyMessage m && id.equals(m.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
