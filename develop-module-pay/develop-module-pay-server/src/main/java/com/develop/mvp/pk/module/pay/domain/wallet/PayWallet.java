package com.develop.mvp.pk.module.pay.domain.wallet;
// DDD 角色：支付钱包聚合根 - AggregateRoot_Pay_Skill
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
@Getter
public final class PayWallet {
    private final Long id; private final Long userId;
    private Integer userType; private Integer balance; private Integer freezePrice;
    private Integer totalRecharge; private Integer totalExpense;
    public PayWallet(Long id, Long userId) { this.id = id; this.userId = Objects.requireNonNull(userId); }
    public static PayWallet of(Long id, Long userId) { return new PayWallet(id, userId); }
    public Long id() { return id; } public Long userId() { return userId; }
    public Integer userType() { return userType; } public Integer balance() { return balance; }
    public Integer freezePrice() { return freezePrice; }
    public Integer totalRecharge() { return totalRecharge; } public Integer totalExpense() { return totalExpense; }
    public PayWallet userType(Integer v) { userType = v; return this; }
    public PayWallet balance(Integer v) { balance = v; return this; }
    public PayWallet freezePrice(Integer v) { freezePrice = v; return this; }
    public PayWallet totalRecharge(Integer v) { totalRecharge = v; return this; }
    public PayWallet totalExpense(Integer v) { totalExpense = v; return this; }
    public boolean hasSufficientBalance(Integer amount) { return balance != null && balance >= amount; }
    public void addBalance(Integer amount) { balance = (balance == null ? 0 : balance) + amount; }
    public void deductBalance(Integer amount) {
        if (!hasSufficientBalance(amount)) throw new IllegalStateException("Insufficient balance");
        balance -= amount;
    }
    public void addExpense(Integer amount) { totalExpense = (totalExpense == null ? 0 : totalExpense) + amount; }
    public void addRecharge(Integer amount) { totalRecharge = (totalRecharge == null ? 0 : totalRecharge) + amount; }
    public void freeze(Integer amount) {
        if (!hasSufficientBalance(amount)) throw new IllegalStateException("Insufficient balance to freeze");
        balance -= amount;
        freezePrice = (freezePrice == null ? 0 : freezePrice) + amount;
    }
    public void unfreeze(Integer amount) {
        if (freezePrice == null || freezePrice < amount) throw new IllegalStateException("Insufficient frozen balance");
        balance += amount;
        freezePrice -= amount;
    }
    @Override public boolean equals(Object o) { return o instanceof PayWallet w && id.equals(w.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
