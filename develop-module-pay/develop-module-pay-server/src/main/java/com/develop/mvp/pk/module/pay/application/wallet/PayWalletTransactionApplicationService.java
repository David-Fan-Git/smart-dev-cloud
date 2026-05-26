package com.develop.mvp.pk.module.pay.application.wallet;

import cn.hutool.core.util.ObjectUtil;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.controller.app.wallet.vo.transaction.AppPayWalletTransactionSummaryRespVO;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWallet;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletTransaction;
import com.develop.mvp.pk.module.pay.domain.wallet.repository.PayWalletTransactionRepository;
import com.develop.mvp.pk.module.pay.enums.wallet.PayWalletBizTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.develop.mvp.pk.module.pay.controller.app.wallet.vo.transaction.AppPayWalletTransactionPageReqVO.TYPE_EXPENSE;
import static com.develop.mvp.pk.module.pay.controller.app.wallet.vo.transaction.AppPayWalletTransactionPageReqVO.TYPE_INCOME;

@Service
@RequiredArgsConstructor
public class PayWalletTransactionApplicationService {

    private final PayWalletApplicationService payWalletApplicationService;

    private final PayWalletTransactionRepository transactionRepository;

    public PageResult<PayWalletTransaction> getAppPage(Long userId, Integer userType, Integer type,
                                                       LocalDateTime[] createTime, Integer pageNo, Integer pageSize) {
        PayWallet wallet = getOrCreateWallet(userId, userType);
        return transactionRepository.findPage(wallet.id(), type, pageNo, pageSize, createTime);
    }

    public PageResult<PayWalletTransaction> getAdminPage(Long walletId, Long userId, Integer userType,
                                                         Integer pageNo, Integer pageSize) {
        Long queryWalletId = walletId;
        if (queryWalletId == null && ObjectUtil.isAllNotEmpty(userId, userType)) {
            PayWallet wallet = getOrCreateWallet(userId, userType);
            if (wallet != null) {
                queryWalletId = wallet.id();
            }
        }
        return transactionRepository.findPage(queryWalletId, null, pageNo, pageSize, null);
    }

    public AppPayWalletTransactionSummaryRespVO getSummary(Long userId, Integer userType, LocalDateTime[] createTime) {
        PayWallet wallet = getOrCreateWallet(userId, userType);
        return new AppPayWalletTransactionSummaryRespVO()
                .setTotalExpense(transactionRepository.sumPriceByType(wallet.id(), TYPE_EXPENSE, createTime))
                .setTotalIncome(transactionRepository.sumPriceByType(wallet.id(), TYPE_INCOME, createTime));
    }

    public PayWalletTransaction getByBiz(String bizId, PayWalletBizTypeEnum type) {
        return transactionRepository.findByBiz(bizId, type.getType()).orElse(null);
    }

    public PayWalletTransaction getByNo(String no) {
        return transactionRepository.findByNo(no).orElse(null);
    }

    private PayWallet getOrCreateWallet(Long userId, Integer userType) {
        return payWalletApplicationService.getOrCreate(userId, userType);
    }
}
