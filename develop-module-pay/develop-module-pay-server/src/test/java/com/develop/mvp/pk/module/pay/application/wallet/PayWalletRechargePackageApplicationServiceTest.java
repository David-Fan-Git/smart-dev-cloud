package com.develop.mvp.pk.module.pay.application.wallet;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.exception.ServiceException;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletRechargePackage;
import com.develop.mvp.pk.module.pay.domain.wallet.repository.PayWalletRechargePackageRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static com.develop.mvp.pk.module.pay.enums.ErrorCodeConstants.WALLET_RECHARGE_PACKAGE_IS_DISABLE;
import static com.develop.mvp.pk.module.pay.enums.ErrorCodeConstants.WALLET_RECHARGE_PACKAGE_NAME_EXISTS;
import static com.develop.mvp.pk.module.pay.enums.ErrorCodeConstants.WALLET_RECHARGE_PACKAGE_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PayWalletRechargePackageApplicationServiceTest {

    @Test
    void create_savesRechargePackageAfterNameUniqueCheck() {
        RecordingRepository repository = new RecordingRepository();
        PayWalletRechargePackageApplicationService applicationService = new PayWalletRechargePackageApplicationService(repository);

        Long id = applicationService.create("套餐A", 100, 20, CommonStatusEnum.ENABLE.getStatus());

        assertEquals(100L, id);
        assertEquals("套餐A", repository.saved.name());
        assertEquals(100, repository.saved.payPrice());
        assertEquals(20, repository.saved.bonusPrice());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), repository.saved.status());
    }

    @Test
    void create_throwsNameExistsWhenNameUsedByAnotherPackage() {
        RecordingRepository repository = new RecordingRepository();
        repository.namedPackage = new PayWalletRechargePackage(200L).name("套餐A");
        PayWalletRechargePackageApplicationService applicationService = new PayWalletRechargePackageApplicationService(repository);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> applicationService.create("套餐A", 100, 20, CommonStatusEnum.ENABLE.getStatus()));

        assertEquals(WALLET_RECHARGE_PACKAGE_NAME_EXISTS.getCode(), exception.getCode());
        assertEquals(WALLET_RECHARGE_PACKAGE_NAME_EXISTS.getMsg(), exception.getMessage());
    }

    @Test
    void update_checksExistsAndNameUniqueBeforeSaving() {
        RecordingRepository repository = new RecordingRepository();
        repository.packageById = new PayWalletRechargePackage(100L).name("旧套餐");
        PayWalletRechargePackageApplicationService applicationService = new PayWalletRechargePackageApplicationService(repository);

        applicationService.update(100L, "新套餐", 200, 30, CommonStatusEnum.DISABLE.getStatus());

        assertEquals(100L, repository.saved.id());
        assertEquals("新套餐", repository.saved.name());
        assertEquals(200, repository.saved.payPrice());
        assertEquals(30, repository.saved.bonusPrice());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), repository.saved.status());
    }

    @Test
    void delete_throwsNotFoundWhenPackageMissing() {
        PayWalletRechargePackageApplicationService applicationService = new PayWalletRechargePackageApplicationService(
                new RecordingRepository());

        ServiceException exception = assertThrows(ServiceException.class, () -> applicationService.delete(100L));

        assertEquals(WALLET_RECHARGE_PACKAGE_NOT_FOUND.getCode(), exception.getCode());
        assertEquals(WALLET_RECHARGE_PACKAGE_NOT_FOUND.getMsg(), exception.getMessage());
    }

    @Test
    void valid_throwsDisableWhenPackageDisabled() {
        RecordingRepository repository = new RecordingRepository();
        repository.packageById = new PayWalletRechargePackage(100L).name("套餐A")
                .status(CommonStatusEnum.DISABLE.getStatus());
        PayWalletRechargePackageApplicationService applicationService = new PayWalletRechargePackageApplicationService(repository);

        ServiceException exception = assertThrows(ServiceException.class, () -> applicationService.valid(100L));

        assertEquals(WALLET_RECHARGE_PACKAGE_IS_DISABLE.getCode(), exception.getCode());
        assertEquals(WALLET_RECHARGE_PACKAGE_IS_DISABLE.getMsg(), exception.getMessage());
    }

    private static final class RecordingRepository implements PayWalletRechargePackageRepository {
        private PayWalletRechargePackage packageById;
        private PayWalletRechargePackage namedPackage;
        private PayWalletRechargePackage saved;
        private Long deletedId;

        @Override
        public PayWalletRechargePackage save(PayWalletRechargePackage rechargePackage) {
            saved = rechargePackage;
            return rechargePackage.id() == null
                    ? new PayWalletRechargePackage(100L).name(rechargePackage.name()).payPrice(rechargePackage.payPrice())
                    .bonusPrice(rechargePackage.bonusPrice()).status(rechargePackage.status())
                    : rechargePackage;
        }

        @Override
        public PayWalletRechargePackage findById(Long id) { return packageById; }

        @Override
        public PayWalletRechargePackage findByName(String name) { return namedPackage; }

        @Override
        public void deleteById(Long id) { deletedId = id; }

        @Override
        public PageResult<PayWalletRechargePackage> findPage(String name, Integer status, LocalDateTime[] createTime,
                                                             Integer pageNo, Integer pageSize) {
            return PageResult.empty();
        }

        @Override
        public List<PayWalletRechargePackage> findListByStatus(Integer status) { return List.of(); }
    }
}
