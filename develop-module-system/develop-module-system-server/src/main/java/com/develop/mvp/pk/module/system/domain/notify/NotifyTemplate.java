package com.develop.mvp.pk.module.system.domain.notify;

// DDD 角色：站内信模板聚合根
import java.util.Objects;

/**
 * Notify Template 领域模型。
 */
public final class NotifyTemplate {
    private final Long id; private final String code, name; private String nickname, content;
    private Integer type, status; private String remark;
    /**
     * 创建 NotifyTemplate 实例。
     *
     * @param id id 参数
     * @param code code 参数
     * @param name name 参数
     */
    private NotifyTemplate(Long id, String code, String name) { this.id = Objects.requireNonNull(id); this.code = Objects.requireNonNull(code); this.name = Objects.requireNonNull(name); }
    /**
     * 执行 of 对应的业务操作。
     *
     * @param id id 参数
     * @param code code 参数
     * @param name name 参数
     * @return 处理结果
     */
    public static NotifyTemplate of(Long id, String code, String name) { return new NotifyTemplate(id, code, name); }
    /**
     * 执行 id 对应的业务操作。
     *
     * @return 处理结果
     */
    public Long id() { return id; } public String code() { return code; } public String name() { return name; }
    /**
     * 执行 nickname 对应的业务操作。
     *
     * @return 处理结果
     */
    public String nickname() { return nickname; } public String content() { return content; }
    /**
     * 执行 type 对应的业务操作。
     *
     * @return 处理结果
     */
    public Integer type() { return type; } public Integer status() { return status; } public String remark() { return remark; }
    /**
     * 执行 nickname 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public NotifyTemplate nickname(String v) { this.nickname = v; return this; }
    /**
     * 执行 content 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public NotifyTemplate content(String v) { this.content = v; return this; }
    /**
     * 执行 type 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public NotifyTemplate type(Integer v) { this.type = v; return this; }
    /**
     * 执行 status 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public NotifyTemplate status(Integer v) { this.status = v; return this; }
    /**
     * 执行 remark 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public NotifyTemplate remark(String v) { this.remark = v; return this; }
    @Override public boolean equals(Object o) { return o instanceof NotifyTemplate t && id.equals(t.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
