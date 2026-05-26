package com.develop.mvp.pk.module.pay.domain.wallet.valueobject;
// DDD 角色：钱包余额值对象 - AggregateRoot_Pay_Skill
import java.util.Objects;
public final class WalletBalance {
    private final int balance;
    private final int freezePrice;
    public WalletBalance(int balance, int freezePrice) {
        this.balance = balance;
        this.freezePrice = freezePrice;
    }
    public int balance() { return balance; }
    public int freezePrice() { return freezePrice; }
    public int availableBalance() { return balance - freezePrice; }
    public boolean canDeduct(int amount) { return availableBalance() >= amount; }
    public WalletBalance deduct(int amount) {
        if (!canDeduct(amount)) throw new IllegalArgumentException("Insufficient available balance");
        return new WalletBalance(balance - amount, freezePrice);
    }
    public WalletBalance add(int amount) { return new WalletBalance(balance + amount, freezePrice); }
    public WalletBalance freeze(int amount) {
        if (amount > availableBalance()) throw new IllegalArgumentException("Insufficient available balance to freeze");
        return new WalletBalance(balance - amount, freezePrice + amount);
    }
    public WalletBalance unfreeze(int amount) {
        if (amount > freezePrice) throw new IllegalArgumentException("Insufficient frozen balance to unfreeze");
        return new WalletBalance(balance + amount, freezePrice - amount);
    }
    @Override public boolean equals(Object o) { return o instanceof WalletBalance wb && balance == wb.balance && freezePrice == wb.freezePrice; }
    @Override public int hashCode() { return Objects.hash(balance, freezePrice); }
    @Override public String toString() { return "WalletBalance(balance=" + balance + ", freeze=" + freezePrice + ")"; }
}
