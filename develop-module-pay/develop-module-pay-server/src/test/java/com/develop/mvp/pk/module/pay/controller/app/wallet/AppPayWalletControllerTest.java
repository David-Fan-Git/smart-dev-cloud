package com.develop.mvp.pk.module.pay.controller.app.wallet;

import com.develop.mvp.pk.framework.common.enums.UserTypeEnum;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.security.core.LoginUser;
import com.develop.mvp.pk.module.pay.application.wallet.PayWalletApplicationService;
import com.develop.mvp.pk.module.pay.controller.app.wallet.vo.wallet.AppPayWalletRespVO;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWallet;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletFactory;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletTransaction;
import com.develop.mvp.pk.module.pay.domain.wallet.repository.PayWalletRepository;
import com.develop.mvp.pk.module.pay.domain.wallet.repository.PayWalletTransactionRepository;
import com.develop.mvp.pk.module.pay.domain.wallet.service.PayWalletLock;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppPayWalletControllerTest {

    @Test
    void getPayWallet_returnsMemberDomainWallet() throws Exception {
        AppPayWalletController controller = new AppPayWalletController();
        RecordingWalletRepository walletRepository = new RecordingWalletRepository();
        inject(controller, new PayWalletApplicationService(walletRepository, new StubTransactionRepository(), new DirectWalletLock()));
        LoginUser loginUser = new LoginUser();
        loginUser.setId(1L);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(loginUser, ""));

        try {
            CommonResult<AppPayWalletRespVO> result = controller.getPayWallet();

            assertTrue(result.isSuccess());
            assertEquals(150, result.getData().getBalance());
            assertEquals(50, result.getData().getTotalExpense());
            assertEquals(300, result.getData().getTotalRecharge());
            assertEquals(1L, walletRepository.userId);
            assertEquals(UserTypeEnum.MEMBER.getValue(), walletRepository.userType);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private static void inject(AppPayWalletController controller, PayWalletApplicationService applicationService) throws Exception {
        Field field = AppPayWalletController.class.getDeclaredField("payWalletApplicationService");
        field.setAccessible(true);
        field.set(controller, applicationService);
    }

    private static final class RecordingWalletRepository implements PayWalletRepository {
        private Long userId;
        private Integer userType;

        @Override
        public PayWallet save(PayWallet wallet) {
            return PayWalletFactory.restore(100L, wallet.userId(), wallet.userType(), 150, 10, 300, 50);
        }

        @Override
        public PayWallet findById(Long id) {
            return PayWalletFactory.restore(id, 1L, 1, 150, 10, 300, 50);
        }

        @Override
        public Optional<PayWallet> findByUserIdAndType(Long userId, Integer userType) {
            this.userId = userId;
            this.userType = userType;
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
        public int updateWhenAdd(Long id, Integer price) { return 0; }

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
            return transaction;
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
