package com.develop.mvp.pk.module.pay.domain.wallet.repository;
// DDD 角色：钱包交易流水仓储接口 - AggregateRoot_Pay_Skill
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletTransaction;
import java.time.LocalDateTime;
import java.util.Optional;
public interface PayWalletTransactionRepository {
    PayWalletTransaction save(PayWalletTransaction transaction);
    Optional<PayWalletTransaction> findByNo(String no);
    Optional<PayWalletTransaction> findByBiz(String bizId, Integer bizType);
    PageResult<PayWalletTransaction> findPage(Long walletId, Integer type, Integer pageNo, Integer pageSize, LocalDateTime[] createTime);
    Integer sumPriceByType(Long walletId, Integer type, LocalDateTime[] createTime);
}
