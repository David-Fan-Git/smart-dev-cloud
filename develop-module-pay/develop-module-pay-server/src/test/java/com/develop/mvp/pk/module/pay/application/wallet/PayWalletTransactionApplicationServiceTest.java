package com.develop.mvp.pk.module.pay.application.wallet;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.controller.app.wallet.vo.transaction.AppPayWalletTransactionSummaryRespVO;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWallet;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletFactory;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletTransaction;
import com.develop.mvp.pk.module.pay.domain.wallet.repository.PayWalletRepository;
import com.develop.mvp.pk.module.pay.domain.wallet.repository.PayWalletTransactionRepository;
import com.develop.mvp.pk.module.pay.domain.wallet.service.PayWalletLock;
import com.develop.mvp.pk.module.pay.enums.wallet.PayWalletBizTypeEnum;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import static com.develop.mvp.pk.module.pay.controller.app.wallet.vo.transaction.AppPayWalletTransactionPageReqVO.TYPE_EXPENSE;
import static com.develop.mvp.pk.module.pay.controller.app.wallet.vo.transaction.AppPayWalletTransactionPageReqVO.TYPE_INCOME;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PayWalletTransactionApplicationServiceTest {

    @Test
    void getAppPage_resolvesWalletAndQueriesTransactionPage() {
        RecordingWalletRepository walletRepository = new RecordingWalletRepository();
        RecordingTransactionRepository transactionRepository = new RecordingTransactionRepository();
        PayWalletTransactionApplicationService applicationService = new PayWalletTransactionApplicationService(
                new PayWalletApplicationService(walletRepository, transactionRepository, new DirectWalletLock()), transactionRepository);
        LocalDateTime[] createTime = {LocalDateTime.of(2026, 5, 24, 1, 0), LocalDateTime.of(2026, 5, 24, 2, 0)};

        PageResult<PayWalletTransaction> page = applicationService.getAppPage(1L, 1, TYPE_INCOME, createTime, 2, 30);

        assertEquals(1L, walletRepository.userId);
        assertEquals(1, walletRepository.userType);
        assertEquals(100L, transactionRepository.walletId);
        assertEquals(TYPE_INCOME, transactionRepository.type);
        assertEquals(createTime, transactionRepository.createTime);
        assertEquals(2, transactionRepository.pageNo);
        assertEquals(30, transactionRepository.pageSize);
        assertEquals(1L, page.getTotal());
        assertEquals(200L, page.getList().get(0).id());
    }

    @Test
    void getAdminPage_resolvesWalletWhenUserFiltersPresent() {
        RecordingWalletRepository walletRepository = new RecordingWalletRepository();
        RecordingTransactionRepository transactionRepository = new RecordingTransactionRepository();
        PayWalletTransactionApplicationService applicationService = new PayWalletTransactionApplicationService(
                new PayWalletApplicationService(walletRepository, transactionRepository, new DirectWalletLock()), transactionRepository);

        PageResult<PayWalletTransaction> page = applicationService.getAdminPage(null, 1L, 1, 2, 30);

        assertEquals(1L, walletRepository.userId);
        assertEquals(1, walletRepository.userType);
        assertEquals(100L, transactionRepository.walletId);
        assertEquals(2, transactionRepository.pageNo);
        assertEquals(30, transactionRepository.pageSize);
        assertEquals(1L, page.getTotal());
    }

    @Test
    void getSummary_resolvesWalletAndSumsIncomeAndExpense() {
        RecordingWalletRepository walletRepository = new RecordingWalletRepository();
        RecordingTransactionRepository transactionRepository = new RecordingTransactionRepository();
        PayWalletTransactionApplicationService applicationService = new PayWalletTransactionApplicationService(
                new PayWalletApplicationService(walletRepository, transactionRepository, new DirectWalletLock()), transactionRepository);
        LocalDateTime[] createTime = {LocalDateTime.of(2026, 5, 24, 1, 0), LocalDateTime.of(2026, 5, 24, 2, 0)};

        AppPayWalletTransactionSummaryRespVO summary = applicationService.getSummary(1L, 1, createTime);

        assertEquals(1L, walletRepository.userId);
        assertEquals(1, walletRepository.userType);
        assertEquals(100L, transactionRepository.walletId);
        assertEquals(TYPE_INCOME, transactionRepository.lastSumType);
        assertEquals(30, summary.getTotalIncome());
        assertEquals(-20, summary.getTotalExpense());
    }

    @Test
    void getByBiz_returnsTransactionByBizType() {
        RecordingTransactionRepository transactionRepository = new RecordingTransactionRepository();
        PayWalletTransactionApplicationService applicationService = new PayWalletTransactionApplicationService(
                new PayWalletApplicationService(new RecordingWalletRepository(), transactionRepository, new DirectWalletLock()),
                transactionRepository);

        PayWalletTransaction transaction = applicationService.getByBiz("300", PayWalletBizTypeEnum.PAYMENT);

        assertEquals("300", transactionRepository.bizId);
        assertEquals(PayWalletBizTypeEnum.PAYMENT.getType(), transactionRepository.bizType);
        assertEquals(200L, transaction.id());
    }

    private static final class DirectWalletLock implements PayWalletLock {
        @Override
        public <V> V lock(Long walletId, Callable<V> callable) {
            try {
                return callable.call();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static final class RecordingWalletRepository implements PayWalletRepository {
        private Long userId;
        private Integer userType;

        @Override
        public PayWallet save(PayWallet wallet) { return wallet; }

        @Override
        public PayWallet findById(Long id) { return PayWalletFactory.restore(id, 1L, 1, 100, 0, 0, 0); }

        @Override
        public Optional<PayWallet> findByUserIdAndType(Long userId, Integer userType) {
            this.userId = userId;
            this.userType = userType;
            return Optional.of(PayWalletFactory.restore(100L, userId, userType, 100, 0, 0, 0));
        }

        @Override
        public PageResult<PayWallet> findPage(Long userId, Integer userType, Integer pageNo, Integer pageSize) {
            return PageResult.empty();
        }

        @Override
        public int updateBalance(Long id, int balanceDelta) { return 0; }

        @Override
        public int updateWhenConsumption(Long id, Integer price) { return 0; }

        @Override
        public int updateWhenConsumptionRefund(Long id, Integer price) { return 0; }

        @Override
        public int updateWhenRecharge(Long id, Integer price) { return 0; }

        @Override
        public int updateWhenAdd(Long id, Integer price) { return 0; }

        @Override
        public int freezePrice(Long id, Integer price) { return 0; }

        @Override
        public int unFreezePrice(Long id, Integer price) { return 0; }

        @Override
        public int updateWhenRechargeRefund(Long id, Integer price) { return 0; }
    }

    private static final class RecordingTransactionRepository implements PayWalletTransactionRepository {
        private Long walletId;
        private Integer type;
        private Integer pageNo;
        private Integer pageSize;
        private LocalDateTime[] createTime;
        private Integer lastSumType;
        private String bizId;
        private Integer bizType;

        @Override
        public PayWalletTransaction save(PayWalletTransaction transaction) { return transaction; }

        @Override
        public Optional<PayWalletTransaction> findByNo(String no) { return Optional.empty(); }

        @Override
        public Optional<PayWalletTransaction> findByBiz(String bizId, Integer bizType) {
            this.bizId = bizId;
            this.bizType = bizType;
            return Optional.of(new PayWalletTransaction(200L).bizId(bizId).bizType(bizType));
        }

        @Override
        public PageResult<PayWalletTransaction> findPage(Long walletId, Integer type, Integer pageNo,
                                                         Integer pageSize, LocalDateTime[] createTime) {
            this.walletId = walletId;
            this.type = type;
            this.pageNo = pageNo;
            this.pageSize = pageSize;
            this.createTime = createTime;
            return new PageResult<>(List.of(new PayWalletTransaction(200L).walletId(walletId)), 1L);
        }

        @Override
        public Integer sumPriceByType(Long walletId, Integer type, LocalDateTime[] createTime) {
            this.walletId = walletId;
            lastSumType = type;
            return TYPE_INCOME.equals(type) ? 30 : -20;
        }
    }
}
