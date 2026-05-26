package com.develop.mvp.pk.module.system.domain.mail;

// DDD 角色：邮件模板聚合根
import java.util.Objects;

/**
 * Mail Template 领域模型。
 */
public final class MailTemplate {
    private final Long id; private final String code, name; private Long accountId;
    private String nickname, title, content; private Integer status; private String remark;
    /**
     * 创建 MailTemplate 实例。
     *
     * @param id id 参数
     * @param code code 参数
     * @param name name 参数
     */
    private MailTemplate(Long id, String code, String name) { this.id = Objects.requireNonNull(id); this.code = Objects.requireNonNull(code); this.name = Objects.requireNonNull(name); }
    /**
     * 执行 of 对应的业务操作。
     *
     * @param id id 参数
     * @param code code 参数
     * @param name name 参数
     * @return 处理结果
     */
    public static MailTemplate of(Long id, String code, String name) { return new MailTemplate(id, code, name); }
    /**
     * 执行 id 对应的业务操作。
     *
     * @return 处理结果
     */
    public Long id() { return id; } public String code() { return code; } public String name() { return name; }
    /**
     * 执行 account Id 对应的业务操作。
     *
     * @return 处理结果
     */
    public Long accountId() { return accountId; } public String nickname() { return nickname; }
    /**
     * 执行 title 对应的业务操作。
     *
     * @return 处理结果
     */
    public String title() { return title; } public String content() { return content; }
    /**
     * 执行 status 对应的业务操作。
     *
     * @return 处理结果
     */
    public Integer status() { return status; } public String remark() { return remark; }
    /**
     * 执行 account Id 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public MailTemplate accountId(Long v) { this.accountId = v; return this; }
    /**
     * 执行 nickname 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public MailTemplate nickname(String v) { this.nickname = v; return this; }
    /**
     * 执行 title 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public MailTemplate title(String v) { this.title = v; return this; }
    /**
     * 执行 content 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public MailTemplate content(String v) { this.content = v; return this; }
    /**
     * 执行 status 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public MailTemplate status(Integer v) { this.status = v; return this; }
    /**
     * 执行 remark 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public MailTemplate remark(String v) { this.remark = v; return this; }
    @Override public boolean equals(Object o) { return o instanceof MailTemplate t && id.equals(t.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
