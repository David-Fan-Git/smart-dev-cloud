package com.develop.mvp.pk.module.pay.domain.wallet.repository;
// DDD 角色：钱包仓储接口 - AggregateRoot_Pay_Skill
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWallet;
import java.util.Optional;
public interface PayWalletRepository {
    PayWallet save(PayWallet wallet);
    PayWallet findById(Long id);
    Optional<PayWallet> findByUserIdAndType(Long userId, Integer userType);
    PageResult<PayWallet> findPage(Long userId, Integer userType, Integer pageNo, Integer pageSize);
    int updateBalance(Long id, int balanceDelta);
    int updateWhenConsumption(Long id, Integer price);
    int updateWhenConsumptionRefund(Long id, Integer price);
    int updateWhenRecharge(Long id, Integer price);
    int updateWhenAdd(Long id, Integer price);
    int freezePrice(Long id, Integer price);
    int unFreezePrice(Long id, Integer price);
    int updateWhenRechargeRefund(Long id, Integer price);
}
