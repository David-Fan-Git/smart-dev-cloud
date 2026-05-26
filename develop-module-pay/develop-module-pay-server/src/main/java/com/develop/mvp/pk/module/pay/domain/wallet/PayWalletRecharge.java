package com.develop.mvp.pk.module.pay.domain.wallet;
// DDD 角色：钱包充值记录实体 - AggregateRoot_Pay_Skill
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
@Getter
public final class PayWalletRecharge {
    private final Long id;
    private Long walletId; private Integer totalPrice; private Integer payPrice; private Integer bonusPrice;
    private Long packageId; private Boolean payStatus;
    private Long payOrderId; private String payChannelCode; private LocalDateTime payTime;
    private Long payRefundId; private LocalDateTime createTime;
    private Integer refundTotalPrice; private Integer refundPayPrice; private Integer refundBonusPrice;
    private LocalDateTime refundTime; private Integer refundStatus;
    public PayWalletRecharge(Long id) { this.id = id; }
    public static PayWalletRecharge of(Long id) { return new PayWalletRecharge(id); }
    public Long id() { return id; } public Long walletId() { return walletId; }
    public Integer totalPrice() { return totalPrice; } public Integer payPrice() { return payPrice; }
    public Integer bonusPrice() { return bonusPrice; } public Long packageId() { return packageId; }
    public Boolean payStatus() { return payStatus; } public Long payOrderId() { return payOrderId; }
    public String payChannelCode() { return payChannelCode; } public LocalDateTime payTime() { return payTime; }
    public Long payRefundId() { return payRefundId; } public LocalDateTime createTime() { return createTime; }
    public Integer refundTotalPrice() { return refundTotalPrice; }
    public Integer refundPayPrice() { return refundPayPrice; }
    public Integer refundBonusPrice() { return refundBonusPrice; }
    public LocalDateTime refundTime() { return refundTime; }
    public Integer refundStatus() { return refundStatus; }
    public PayWalletRecharge walletId(Long v) { walletId = v; return this; }
    public PayWalletRecharge totalPrice(Integer v) { totalPrice = v; return this; }
    public PayWalletRecharge payPrice(Integer v) { payPrice = v; return this; }
    public PayWalletRecharge bonusPrice(Integer v) { bonusPrice = v; return this; }
    public PayWalletRecharge packageId(Long v) { packageId = v; return this; }
    public PayWalletRecharge payStatus(Boolean v) { payStatus = v; return this; }
    public PayWalletRecharge payOrderId(Long v) { payOrderId = v; return this; }
    public PayWalletRecharge payChannelCode(String v) { payChannelCode = v; return this; }
    public PayWalletRecharge payTime(LocalDateTime v) { payTime = v; return this; }
    public PayWalletRecharge payRefundId(Long v) { payRefundId = v; return this; }
    public PayWalletRecharge createTime(LocalDateTime v) { createTime = v; return this; }
    public PayWalletRecharge refundTotalPrice(Integer v) { refundTotalPrice = v; return this; }
    public PayWalletRecharge refundPayPrice(Integer v) { refundPayPrice = v; return this; }
    public PayWalletRecharge refundBonusPrice(Integer v) { refundBonusPrice = v; return this; }
    public PayWalletRecharge refundTime(LocalDateTime v) { refundTime = v; return this; }
    public PayWalletRecharge refundStatus(Integer v) { refundStatus = v; return this; }
    public boolean isPaid() { return Boolean.TRUE.equals(payStatus); }
    public boolean isRefundWaiting() { return Objects.equals(refundStatus, 0); }
    @Override public boolean equals(Object o) { return o instanceof PayWalletRecharge r && id.equals(r.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
