package com.develop.mvp.pk.module.pay.controller.app.wallet;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.application.wallet.PayWalletRechargePackageApplicationService;
import com.develop.mvp.pk.module.pay.controller.app.wallet.vo.recharge.AppPayWalletPackageRespVO;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletRechargePackage;
import com.develop.mvp.pk.module.pay.domain.wallet.repository.PayWalletRechargePackageRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppPayWalletRechargePackageControllerTest {

    @Test
    void getWalletRechargePackageList_usesApplicationServiceAndSortsByPayPrice() throws Exception {
        AppPayWalletRechargePackageController controller = new AppPayWalletRechargePackageController();
        RecordingRepository repository = new RecordingRepository();
        inject(controller, new PayWalletRechargePackageApplicationService(repository));

        CommonResult<List<AppPayWalletPackageRespVO>> result = controller.getWalletRechargePackageList();

        assertEquals(0, result.getCode());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), repository.status);
        assertEquals(2, result.getData().size());
        assertEquals(50L, result.getData().get(0).getId());
        assertEquals(100L, result.getData().get(1).getId());
    }

    private static void inject(AppPayWalletRechargePackageController controller,
                               PayWalletRechargePackageApplicationService applicationService) throws Exception {
        Field field = AppPayWalletRechargePackageController.class.getDeclaredField("rechargePackageApplicationService");
        field.setAccessible(true);
        field.set(controller, applicationService);
    }

    private static final class RecordingRepository implements PayWalletRechargePackageRepository {
        private Integer status;

        @Override
        public PayWalletRechargePackage save(PayWalletRechargePackage rechargePackage) { return rechargePackage; }

        @Override
        public PayWalletRechargePackage findById(Long id) { return null; }

        @Override
        public PayWalletRechargePackage findByName(String name) { return null; }

        @Override
        public void deleteById(Long id) { }

        @Override
        public PageResult<PayWalletRechargePackage> findPage(String name, Integer status, LocalDateTime[] createTime,
                                                             Integer pageNo, Integer pageSize) {
            return PageResult.empty();
        }

        @Override
        public List<PayWalletRechargePackage> findListByStatus(Integer status) {
            this.status = status;
            return List.of(
                    new PayWalletRechargePackage(100L).name("套餐B").payPrice(100).bonusPrice(20).status(status),
                    new PayWalletRechargePackage(50L).name("套餐A").payPrice(50).bonusPrice(10).status(status));
        }
    }
}
