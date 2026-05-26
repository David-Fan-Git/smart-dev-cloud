package com.develop.mvp.pk.module.system.domain.mail;

// DDD 角色：邮箱账号聚合根 — 封装邮箱配置属性
import java.util.Objects;

/**
 * Mail Account 领域模型。
 */
public final class MailAccount {
    private final Long id;
    private final String mail;
    private String username, password, host, port, nickname;
    private Boolean sslEnable, starttlsEnable;
    /**
     * 创建 MailAccount 实例。
     *
     * @param id id 参数
     * @param mail mail 参数
     */
    private MailAccount(Long id, String mail) { this.id = Objects.requireNonNull(id); this.mail = Objects.requireNonNull(mail); }
    /**
     * 执行 of 对应的业务操作。
     *
     * @param id id 参数
     * @param mail mail 参数
     * @return 处理结果
     */
    public static MailAccount of(Long id, String mail) { return new MailAccount(id, mail); }
    /**
     * 执行 id 对应的业务操作。
     *
     * @return 处理结果
     */
    public Long id() { return id; } public String mail() { return mail; }
    /**
     * 执行 username 对应的业务操作。
     *
     * @return 处理结果
     */
    public String username() { return username; } public String password() { return password; }
    /**
     * 执行 host 对应的业务操作。
     *
     * @return 处理结果
     */
    public String host() { return host; } public String port() { return port; }
    /**
     * 执行 nickname 对应的业务操作。
     *
     * @return 处理结果
     */
    public String nickname() { return nickname; }
    /**
     * 执行 ssl Enable 对应的业务操作。
     *
     * @return 处理结果
     */
    public Boolean sslEnable() { return sslEnable; } public Boolean starttlsEnable() { return starttlsEnable; }
    // Builder-style setters for reconstitution
    /**
     * 执行 username 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public MailAccount username(String v) { this.username = v; return this; }
    /**
     * 执行 password 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public MailAccount password(String v) { this.password = v; return this; }
    /**
     * 执行 host 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public MailAccount host(String v) { this.host = v; return this; }
    /**
     * 执行 port 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public MailAccount port(String v) { this.port = v; return this; }
    /**
     * 执行 nickname 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public MailAccount nickname(String v) { this.nickname = v; return this; }
    /**
     * 执行 ssl Enable 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public MailAccount sslEnable(Boolean v) { this.sslEnable = v; return this; }
    /**
     * 执行 starttls Enable 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public MailAccount starttlsEnable(Boolean v) { this.starttlsEnable = v; return this; }
    @Override public boolean equals(Object o) { return o instanceof MailAccount a && id.equals(a.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
