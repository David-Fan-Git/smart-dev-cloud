package com.develop.mvp.pk.module.pay.application.wallet;

import com.develop.mvp.pk.framework.common.exception.ServiceException;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWallet;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletFactory;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletTransaction;
import com.develop.mvp.pk.module.pay.domain.wallet.repository.PayWalletRepository;
import com.develop.mvp.pk.module.pay.domain.wallet.repository.PayWalletTransactionRepository;
import com.develop.mvp.pk.module.pay.domain.wallet.service.PayWalletLock;
import com.develop.mvp.pk.module.pay.enums.wallet.PayWalletBizTypeEnum;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.Callable;

import static com.develop.mvp.pk.module.pay.enums.ErrorCodeConstants.WALLET_BALANCE_NOT_ENOUGH;
import static com.develop.mvp.pk.module.pay.enums.ErrorCodeConstants.WALLET_FREEZE_PRICE_NOT_ENOUGH;
import static com.develop.mvp.pk.module.pay.enums.ErrorCodeConstants.WALLET_REFUND_EXIST;
import static com.develop.mvp.pk.module.pay.enums.ErrorCodeConstants.WALLET_TRANSACTION_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PayWalletApplicationServiceTest {

    @Test
    void getOrCreate_returnsPersistedWallet() {
        PayWalletApplicationService applicationService = new PayWalletApplicationService(
                new StubWalletRepository(), new StubTransactionRepository(), new RecordingWalletLock());

        PayWallet wallet = applicationService.getOrCreate(1L, 1);

        assertEquals(100L, wallet.id());
    }

    @Test
    void addBalance_createsTransactionAfterGuardedAdd() {
        RecordingAddWalletRepository walletRepository = new RecordingAddWalletRepository();
        RecordingWalletLock walletLock = new RecordingWalletLock();
        PayWalletApplicationService applicationService = new PayWalletApplicationService(
                walletRepository, new StubTransactionRepository(), walletLock);

        PayWalletTransaction transaction = applicationService.addBalance(
                100L, "biz-1", PayWalletBizTypeEnum.UPDATE_BALANCE.getType(), 50, "后台余额调整");

        assertEquals(1, walletLock.lockCount);
        assertEquals(100L, walletLock.lockedWalletId);
        assertEquals(1, walletRepository.updateWhenAddCount);
        assertEquals(0, walletRepository.saveCount);
        assertEquals(200L, transaction.id());
        assertEquals(100L, transaction.walletId());
        assertEquals("biz-1", transaction.bizId());
        assertEquals(PayWalletBizTypeEnum.UPDATE_BALANCE.getType(), transaction.bizType());
        assertEquals(PayWalletBizTypeEnum.UPDATE_BALANCE.getDescription(), transaction.title());
        assertEquals(50, transaction.price());
        assertEquals(150, transaction.balance());
    }

    @Test
    void freezePrice_throwsServiceExceptionWhenBalanceNotEnough() {
        PayWalletApplicationService applicationService = new PayWalletApplicationService(
                new FailingFreezeWalletRepository(), new StubTransactionRepository(), new RecordingWalletLock());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> applicationService.freezePrice(100L, 50));

        assertEquals(WALLET_BALANCE_NOT_ENOUGH.getCode(), exception.getCode());
        assertEquals(WALLET_BALANCE_NOT_ENOUGH.getMsg(), exception.getMessage());
    }

    @Test
    void deductBalance_createsPaymentTransactionAfterGuardedConsumption() {
        StubTransactionRepository transactionRepository = new StubTransactionRepository();
        PayWalletApplicationService applicationService = new PayWalletApplicationService(
                new SuccessfulConsumptionWalletRepository(), transactionRepository, new RecordingWalletLock());

        PayWalletTransaction transaction = applicationService.deductBalance(
                100L, 300L, PayWalletBizTypeEnum.PAYMENT.getType(), 40);

        assertEquals(200L, transaction.id());
        assertEquals(100L, transaction.walletId());
        assertEquals("300", transaction.bizId());
        assertEquals(PayWalletBizTypeEnum.PAYMENT.getType(), transaction.bizType());
        assertEquals(PayWalletBizTypeEnum.PAYMENT.getDescription(), transaction.title());
        assertEquals(-40, transaction.price());
        assertEquals(60, transaction.balance());
    }

    @Test
    void deductBalance_throwsServiceExceptionWhenBalanceNotEnough() {
        PayWalletApplicationService applicationService = new PayWalletApplicationService(
                new FailingConsumptionWalletRepository(), new StubTransactionRepository(), new RecordingWalletLock());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> applicationService.deductBalance(100L, 300L, PayWalletBizTypeEnum.PAYMENT.getType(), 40));

        assertEquals(WALLET_BALANCE_NOT_ENOUGH.getCode(), exception.getCode());
        assertEquals(WALLET_BALANCE_NOT_ENOUGH.getMsg(), exception.getMessage());
    }

    @Test
    void freezePrice_throwsBalanceNotEnoughWhenWalletMissing() {
        PayWalletApplicationService applicationService = new PayWalletApplicationService(
                new MissingWalletRepository(), new StubTransactionRepository(), new RecordingWalletLock());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> applicationService.freezePrice(100L, 50));

        assertEquals(WALLET_BALANCE_NOT_ENOUGH.getCode(), exception.getCode());
        assertEquals(WALLET_BALANCE_NOT_ENOUGH.getMsg(), exception.getMessage());
    }

    @Test
    void unfreezePrice_throwsFreezePriceNotEnoughWhenWalletMissing() {
        PayWalletApplicationService applicationService = new PayWalletApplicationService(
                new MissingWalletRepository(), new StubTransactionRepository(), new RecordingWalletLock());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> applicationService.unfreezePrice(100L, 50));

        assertEquals(WALLET_FREEZE_PRICE_NOT_ENOUGH.getCode(), exception.getCode());
        assertEquals(WALLET_FREEZE_PRICE_NOT_ENOUGH.getMsg(), exception.getMessage());
    }

    @Test
    void unfreezePrice_throwsServiceExceptionWhenFreezePriceNotEnough() {
        PayWalletApplicationService applicationService = new PayWalletApplicationService(
                new FailingUnfreezeWalletRepository(), new StubTransactionRepository(), new RecordingWalletLock());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> applicationService.unfreezePrice(100L, 50));

        assertEquals(WALLET_FREEZE_PRICE_NOT_ENOUGH.getCode(), exception.getCode());
        assertEquals(WALLET_FREEZE_PRICE_NOT_ENOUGH.getMsg(), exception.getMessage());
    }

    @Test
    void refundPayment_addsBalanceToOriginalWallet() {
        PaymentRefundTransactionRepository transactionRepository = new PaymentRefundTransactionRepository();
        RecordingAddWalletRepository walletRepository = new RecordingAddWalletRepository();
        PayWalletApplicationService applicationService = new PayWalletApplicationService(
                walletRepository, transactionRepository, new RecordingWalletLock());

        PayWalletTransaction transaction = applicationService.refundPayment(500L, "W100", 40);

        assertEquals(100L, transaction.walletId());
        assertEquals("500", transaction.bizId());
        assertEquals(PayWalletBizTypeEnum.PAYMENT_REFUND.getType(), transaction.bizType());
        assertEquals(PayWalletBizTypeEnum.PAYMENT_REFUND.getDescription(), transaction.title());
        assertEquals(40, transaction.price());
        assertEquals(140, transaction.balance());
        assertEquals(1, walletRepository.updateWhenConsumptionRefundCount);
    }

    @Test
    void refundPayment_throwsTransactionNotFoundWhenPaymentMissing() {
        PayWalletApplicationService applicationService = new PayWalletApplicationService(
                new StubWalletRepository(), new StubTransactionRepository(), new RecordingWalletLock());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> applicationService.refundPayment(500L, "missing", 40));

        assertEquals(WALLET_TRANSACTION_NOT_FOUND.getCode(), exception.getCode());
        assertEquals(WALLET_TRANSACTION_NOT_FOUND.getMsg(), exception.getMessage());
    }

    @Test
    void refundPayment_throwsRefundExistWhenRefundAlreadyCreated() {
        PayWalletApplicationService applicationService = new PayWalletApplicationService(
                new StubWalletRepository(), new ExistingRefundTransactionRepository(), new RecordingWalletLock());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> applicationService.refundPayment(500L, "W100", 40));

        assertEquals(WALLET_REFUND_EXIST.getCode(), exception.getCode());
        assertEquals(WALLET_REFUND_EXIST.getMsg(), exception.getMessage());
    }

    private static class StubWalletRepository implements PayWalletRepository {

        @Override
        public PayWallet save(PayWallet wallet) {
            return PayWalletFactory.restore(100L, wallet.userId(), wallet.userType(), wallet.balance(),
                    wallet.freezePrice(), wallet.totalRecharge(), wallet.totalExpense());
        }

        @Override
        public PayWallet findById(Long id) {
            return PayWalletFactory.restore(id, 1L, 1, 100, 0, 0, 0);
        }

        @Override
        public Optional<PayWallet> findByUserIdAndType(Long userId, Integer userType) { return Optional.empty(); }

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

    private static final class FailingFreezeWalletRepository extends StubWalletRepository {

        @Override
        public int freezePrice(Long id, Integer price) { return 0; }
    }

    private static final class MissingWalletRepository extends StubWalletRepository {

        @Override
        public PayWallet findById(Long id) { return null; }
    }

    private static final class RecordingAddWalletRepository extends StubWalletRepository {
        private int updateWhenAddCount;
        private int saveCount;

        @Override
        public PayWallet save(PayWallet wallet) {
            saveCount++;
            return super.save(wallet);
        }

        private int updateWhenConsumptionRefundCount;

        @Override
        public int updateWhenAdd(Long id, Integer price) {
            updateWhenAddCount++;
            return 1;
        }

        @Override
        public int updateWhenConsumptionRefund(Long id, Integer price) {
            updateWhenConsumptionRefundCount++;
            return 1;
        }
    }

    private static final class SuccessfulConsumptionWalletRepository extends StubWalletRepository {

        @Override
        public int updateWhenConsumption(Long id, Integer price) { return 1; }
    }

    private static final class FailingConsumptionWalletRepository extends StubWalletRepository {

        @Override
        public int updateWhenConsumption(Long id, Integer price) { return 0; }
    }

    private static final class FailingUnfreezeWalletRepository extends StubWalletRepository {

        @Override
        public int unFreezePrice(Long id, Integer price) { return 0; }
    }

    private static final class RecordingWalletLock implements PayWalletLock {
        private int lockCount;
        private Long lockedWalletId;

        @Override
        public <V> V lock(Long walletId, Callable<V> callable) {
            lockCount++;
            lockedWalletId = walletId;
            try {
                return callable.call();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static final class PaymentRefundTransactionRepository implements PayWalletTransactionRepository {

        @Override
        public PayWalletTransaction save(PayWalletTransaction transaction) {
            return new PayWalletTransaction(201L)
                    .walletId(transaction.walletId()).bizType(transaction.bizType()).bizId(transaction.bizId())
                    .title(transaction.title()).price(transaction.price()).balance(transaction.balance());
        }

        @Override
        public Optional<PayWalletTransaction> findByNo(String no) {
            return Optional.of(new PayWalletTransaction(200L)
                    .no(no).walletId(100L).bizId("300").bizType(PayWalletBizTypeEnum.PAYMENT.getType())
                    .title(PayWalletBizTypeEnum.PAYMENT.getDescription()).price(-40).balance(60));
        }

        @Override
        public Optional<PayWalletTransaction> findByBiz(String bizId, Integer bizType) { return Optional.empty(); }

        @Override
        public PageResult<PayWalletTransaction> findPage(Long walletId, Integer type, Integer pageNo,
                                                         Integer pageSize, LocalDateTime[] createTime) {
            return PageResult.empty();
        }

        @Override
        public Integer sumPriceByType(Long walletId, Integer type, LocalDateTime[] createTime) { return 0; }
    }

    private static final class ExistingRefundTransactionRepository implements PayWalletTransactionRepository {

        @Override
        public PayWalletTransaction save(PayWalletTransaction transaction) { return transaction; }

        @Override
        public Optional<PayWalletTransaction> findByNo(String no) {
            return Optional.of(new PayWalletTransaction(200L).no(no).walletId(100L));
        }

        @Override
        public Optional<PayWalletTransaction> findByBiz(String bizId, Integer bizType) {
            return Optional.of(new PayWalletTransaction(201L).bizId(bizId).bizType(bizType));
        }

        @Override
        public PageResult<PayWalletTransaction> findPage(Long walletId, Integer type, Integer pageNo,
                                                         Integer pageSize, LocalDateTime[] createTime) {
            return PageResult.empty();
        }

        @Override
        public Integer sumPriceByType(Long walletId, Integer type, LocalDateTime[] createTime) { return 0; }
    }

    private static final class StubTransactionRepository implements PayWalletTransactionRepository {

        @Override
        public PayWalletTransaction save(PayWalletTransaction transaction) {
            return new PayWalletTransaction(200L)
                    .walletId(transaction.walletId()).bizType(transaction.bizType()).bizId(transaction.bizId())
                    .title(transaction.title()).price(transaction.price()).balance(transaction.balance());
        }

        @Override
        public Optional<PayWalletTransaction> findByNo(String no) { return Optional.empty(); }

        @Override
        public Optional<PayWalletTransaction> findByBiz(String bizId, Integer bizType) { return Optional.empty(); }

        @Override
        public PageResult<PayWalletTransaction> findPage(Long walletId, Integer type, Integer pageNo,
                                                         Integer pageSize, LocalDateTime[] createTime) {
            return PageResult.empty();
        }

        @Override
        public Integer sumPriceByType(Long walletId, Integer type, LocalDateTime[] createTime) { return 0; }
    }
}
