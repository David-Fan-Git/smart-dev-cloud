package com.develop.mvp.pk.module.pay.domain.wallet;
// DDD 角色：钱包充值套餐实体 - AggregateRoot_Pay_Skill
import java.util.Objects;
import lombok.Getter;
@Getter
public final class PayWalletRechargePackage {
    private final Long id;
    private String name; private Integer payPrice; private Integer bonusPrice; private Integer status;
    public PayWalletRechargePackage(Long id) { this.id = id; }
    public static PayWalletRechargePackage of(Long id) { return new PayWalletRechargePackage(id); }
    public Long id() { return id; } public String name() { return name; }
    public Integer payPrice() { return payPrice; } public Integer bonusPrice() { return bonusPrice; }
    public Integer status() { return status; }
    public PayWalletRechargePackage name(String v) { name = v; return this; }
    public PayWalletRechargePackage payPrice(Integer v) { payPrice = v; return this; }
    public PayWalletRechargePackage bonusPrice(Integer v) { bonusPrice = v; return this; }
    public PayWalletRechargePackage status(Integer v) { status = v; return this; }
    public boolean isEnabled() { return Objects.equals(status, 0); }
    @Override public boolean equals(Object o) { return o instanceof PayWalletRechargePackage p && id.equals(p.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
