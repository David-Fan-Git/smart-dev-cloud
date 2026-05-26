package com.develop.mvp.pk.module.pay.domain.wallet.repository;
// DDD 角色：钱包充值仓储接口 - AggregateRoot_Pay_Skill
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletRecharge;
import java.time.LocalDateTime;
public interface PayWalletRechargeRepository {
    PayWalletRecharge save(PayWalletRecharge recharge);
    PayWalletRecharge findById(Long id);
    PageResult<PayWalletRecharge> findPage(Long walletId, Boolean payStatus, Integer pageNo, Integer pageSize);
    void updatePayOrderId(Long id, Long payOrderId);
    int markPaid(Long id, String payChannelCode);
    void markRefundWaiting(Long id, Long payRefundId);
    int markRefundSuccess(Long id, LocalDateTime refundTime, Integer refundTotalPrice, Integer refundPayPrice, Integer refundBonusPrice);
    int markRefundFailure(Long id);
    int updateByIdAndPaid(Long id, boolean wherePayStatus, PayWalletRecharge recharge);
    int updateByIdAndRefunded(Long id, Integer whereRefundStatus, PayWalletRecharge recharge);
}
