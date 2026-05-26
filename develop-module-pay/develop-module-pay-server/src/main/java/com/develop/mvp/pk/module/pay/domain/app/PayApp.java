package com.develop.mvp.pk.module.pay.domain.app;
// DDD 角色：支付应用聚合根 - AggregateRoot_Pay_Skill
import java.util.Objects;
import lombok.Getter;
@Getter
public final class PayApp {
    private final Long id; private String name;
    private String appKey; private Integer status; private String remark;
    private String orderNotifyUrl; private String refundNotifyUrl; private String transferNotifyUrl;
    public PayApp(Long id, String name) { this.id = id; this.name = Objects.requireNonNull(name); }
    public static PayApp of(Long id, String name) { return new PayApp(id, name); }
    public Long id() { return id; } public String name() { return name; }
    public PayApp name(String v) { name = Objects.requireNonNull(v); return this; }
    public String appKey() { return appKey; } public Integer status() { return status; }
    public String remark() { return remark; }
    public String orderNotifyUrl() { return orderNotifyUrl; }
    public String refundNotifyUrl() { return refundNotifyUrl; }
    public String transferNotifyUrl() { return transferNotifyUrl; }
    public PayApp appKey(String v) { appKey = v; return this; }
    public PayApp status(Integer v) { status = v; return this; }
    public PayApp remark(String v) { remark = v; return this; }
    public PayApp orderNotifyUrl(String v) { orderNotifyUrl = v; return this; }
    public PayApp refundNotifyUrl(String v) { refundNotifyUrl = v; return this; }
    public PayApp transferNotifyUrl(String v) { transferNotifyUrl = v; return this; }
    public boolean isEnabled() { return Objects.equals(status, 0); }
    public void enable() { status = 0; }
    public void disable() { status = 1; }
    @Override public boolean equals(Object o) { return o instanceof PayApp a && id.equals(a.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
