package com.develop.mvp.pk.module.pay.api.wallet;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.api.wallet.dto.PayWalletAddBalanceReqDTO;
import com.develop.mvp.pk.module.pay.api.wallet.dto.PayWalletRespDTO;
import com.develop.mvp.pk.module.pay.application.wallet.PayWalletApplicationService;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWallet;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletFactory;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletTransaction;
import com.develop.mvp.pk.module.pay.domain.wallet.repository.PayWalletRepository;
import com.develop.mvp.pk.module.pay.domain.wallet.repository.PayWalletTransactionRepository;
import com.develop.mvp.pk.module.pay.domain.wallet.service.PayWalletLock;
import com.develop.mvp.pk.module.pay.enums.wallet.PayWalletBizTypeEnum;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayWalletApiImplTest {

    @Test
    void getOrCreateWallet_returnsDomainWalletDto() throws Exception {
        PayWalletApiImpl api = new PayWalletApiImpl();
        RecordingWalletRepository walletRepository = new RecordingWalletRepository();
        inject(api, new PayWalletApplicationService(walletRepository, new StubTransactionRepository(), new DirectWalletLock()));

        CommonResult<PayWalletRespDTO> result = api.getOrCreateWallet(1L, 2);

        assertTrue(result.isSuccess());
        assertEquals(100L, result.getData().getId());
        assertEquals(1L, result.getData().getUserId());
        assertEquals(2, result.getData().getUserType());
        assertEquals(150, result.getData().getBalance());
        assertEquals(10, result.getData().getFreezePrice());
        assertEquals(300, result.getData().getTotalRecharge());
        assertEquals(50, result.getData().getTotalExpense());
    }

    @Test
    void addWalletBalance_usesDomainApplicationService() throws Exception {
        PayWalletApiImpl api = new PayWalletApiImpl();
        RecordingWalletRepository walletRepository = new RecordingWalletRepository();
        inject(api, new PayWalletApplicationService(walletRepository, new StubTransactionRepository(), new DirectWalletLock()));
        PayWalletAddBalanceReqDTO reqDTO = new PayWalletAddBalanceReqDTO();
        reqDTO.setUserId(1L);
        reqDTO.setUserType(2);
        reqDTO.setBizType(PayWalletBizTypeEnum.UPDATE_BALANCE.getType());
        reqDTO.setBizId("biz-1");
        reqDTO.setPrice(50);

        CommonResult<Boolean> result = api.addWalletBalance(reqDTO);

        assertTrue(result.isSuccess());
        assertEquals(Boolean.TRUE, result.getData());
        assertEquals(1, walletRepository.updateWhenAddCount);
    }

    private static void inject(PayWalletApiImpl api, PayWalletApplicationService applicationService) throws Exception {
        Field field = PayWalletApiImpl.class.getDeclaredField("payWalletApplicationService");
        field.setAccessible(true);
        field.set(api, applicationService);
    }

    private static final class RecordingWalletRepository implements PayWalletRepository {
        private int updateWhenAddCount;

        @Override
        public PayWallet save(PayWallet wallet) {
            return PayWalletFactory.restore(100L, wallet.userId(), wallet.userType(), 150, 10, 300, 50);
        }

        @Override
        public PayWallet findById(Long id) {
            return PayWalletFactory.restore(id, 1L, 2, 150, 10, 300, 50);
        }

        @Override
        public Optional<PayWallet> findByUserIdAndType(Long userId, Integer userType) {
            return Optional.of(PayWalletFactory.restore(100L, userId, userType, 150, 10, 300, 50));
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
        public int updateWhenAdd(Long id, Integer price) {
            updateWhenAddCount++;
            return 1;
        }

        @Override
        public int freezePrice(Long id, Integer price) { return 0; }

        @Override
        public int unFreezePrice(Long id, Integer price) { return 0; }

        @Override
        public int updateWhenRechargeRefund(Long id, Integer price) { return 0; }
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
