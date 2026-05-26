package com.develop.mvp.pk.module.pay.domain.wallet;
// DDD 角色：钱包交易流水实体 - AggregateRoot_Pay_Skill
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
@Getter
public final class PayWalletTransaction {
    private final Long id;
    private String no; private Long walletId;
    private Integer bizType; private String bizId;
    private String title; private Integer price; private Integer balance;
    private String creator; private LocalDateTime createTime;
    public PayWalletTransaction(Long id) { this.id = id; }
    public static PayWalletTransaction of(Long id) { return new PayWalletTransaction(id); }
    public Long id() { return id; } public String no() { return no; }
    public Long walletId() { return walletId; } public Integer bizType() { return bizType; }
    public String bizId() { return bizId; } public String title() { return title; }
    public Integer price() { return price; } public Integer balance() { return balance; }
    public String creator() { return creator; } public LocalDateTime createTime() { return createTime; }
    public PayWalletTransaction no(String v) { no = v; return this; }
    public PayWalletTransaction walletId(Long v) { walletId = v; return this; }
    public PayWalletTransaction bizType(Integer v) { bizType = v; return this; }
    public PayWalletTransaction bizId(String v) { bizId = v; return this; }
    public PayWalletTransaction title(String v) { title = v; return this; }
    public PayWalletTransaction price(Integer v) { price = v; return this; }
    public PayWalletTransaction balance(Integer v) { balance = v; return this; }
    public PayWalletTransaction creator(String v) { creator = v; return this; }
    public PayWalletTransaction createTime(LocalDateTime v) { createTime = v; return this; }
    public boolean isIncome() { return price != null && price > 0; }
    public boolean isExpense() { return price != null && price < 0; }
    @Override public boolean equals(Object o) { return o instanceof PayWalletTransaction t && Objects.equals(id, t.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
