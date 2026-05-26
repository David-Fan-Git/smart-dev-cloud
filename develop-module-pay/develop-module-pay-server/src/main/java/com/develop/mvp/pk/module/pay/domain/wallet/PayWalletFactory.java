package com.develop.mvp.pk.module.pay.domain.wallet;
// DDD 角色：钱包工厂 - AggregateRoot_Pay_Skill
public class PayWalletFactory {
    public static PayWallet create(Long id, Long userId, Integer userType) {
        PayWallet wallet = new PayWallet(id, userId);
        wallet.userType(userType).balance(0).freezePrice(0).totalExpense(0).totalRecharge(0);
        return wallet;
    }
    public static PayWallet restore(Long id, Long userId, Integer userType, Integer balance,
                                     Integer freezePrice, Integer totalRecharge, Integer totalExpense) {
        PayWallet wallet = new PayWallet(id, userId);
        wallet.userType(userType).balance(balance).freezePrice(freezePrice)
                .totalRecharge(totalRecharge).totalExpense(totalExpense);
        return wallet;
    }
}
