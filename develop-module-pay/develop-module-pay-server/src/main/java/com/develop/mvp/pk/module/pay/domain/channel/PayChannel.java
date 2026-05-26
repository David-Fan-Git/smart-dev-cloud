package com.develop.mvp.pk.module.pay.domain.channel;
// DDD 角色：支付渠道聚合根 - AggregateRoot_Pay_Skill
import java.util.Objects;
import lombok.Getter;
@Getter
public final class PayChannel {
    private final Long id;
    private final String code;
    private final Long appId;
    private Integer status;
    private Double feeRate;
    private String remark;
    private Object config;
    private Long tenantId;
    public PayChannel(Long id, String code, Long appId) {
        this.id = id;
        this.code = Objects.requireNonNull(code);
        this.appId = Objects.requireNonNull(appId);
    }
    public static PayChannel of(Long id, String code, Long appId) { return new PayChannel(id, code, appId); }
    public Long id() { return id; }
    public String code() { return code; }
    public Long appId() { return appId; }
    public Integer status() { return status; }
    public Double feeRate() { return feeRate; }
    public String remark() { return remark; }
    public Object config() { return config; }
    public Long tenantId() { return tenantId; }
    public PayChannel status(Integer v) { status = v; return this; }
    public PayChannel feeRate(Double v) { feeRate = v; return this; }
    public PayChannel remark(String v) { remark = v; return this; }
    public PayChannel config(Object v) { config = v; return this; }
    public PayChannel tenantId(Long v) { tenantId = v; return this; }
    public boolean isEnabled() { return Objects.equals(status, 0); }
    public void enable() { status = 0; }
    public void disable() { status = 1; }
    @Override public boolean equals(Object o) { return o instanceof PayChannel c && id.equals(c.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
