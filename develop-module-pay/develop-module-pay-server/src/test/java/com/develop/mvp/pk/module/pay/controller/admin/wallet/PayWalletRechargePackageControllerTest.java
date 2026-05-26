package com.develop.mvp.pk.module.pay.controller.admin.wallet;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.application.wallet.PayWalletRechargePackageApplicationService;
import com.develop.mvp.pk.module.pay.controller.admin.wallet.vo.rechargepackage.WalletRechargePackageCreateReqVO;
import com.develop.mvp.pk.module.pay.controller.admin.wallet.vo.rechargepackage.WalletRechargePackagePageReqVO;
import com.develop.mvp.pk.module.pay.controller.admin.wallet.vo.rechargepackage.WalletRechargePackageRespVO;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletRechargePackage;
import com.develop.mvp.pk.module.pay.domain.wallet.repository.PayWalletRechargePackageRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayWalletRechargePackageControllerTest {

    @Test
    void createWalletRechargePackage_usesApplicationService() throws Exception {
        PayWalletRechargePackageController controller = new PayWalletRechargePackageController();
        RecordingRepository repository = new RecordingRepository();
        inject(controller, new PayWalletRechargePackageApplicationService(repository));
        WalletRechargePackageCreateReqVO reqVO = new WalletRechargePackageCreateReqVO();
        reqVO.setName("套餐A");
        reqVO.setPayPrice(100);
        reqVO.setBonusPrice(20);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus().byteValue());

        CommonResult<Long> result = controller.createWalletRechargePackage(reqVO);

        assertEquals(0, result.getCode());
        assertEquals(100L, result.getData());
        assertEquals("套餐A", repository.saved.name());
        assertEquals(100, repository.saved.payPrice());
        assertEquals(20, repository.saved.bonusPrice());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), repository.saved.status());
    }

    @Test
    void getWalletRechargePackagePage_mapsDomainPageToResponseVo() throws Exception {
        PayWalletRechargePackageController controller = new PayWalletRechargePackageController();
        RecordingRepository repository = new RecordingRepository();
        inject(controller, new PayWalletRechargePackageApplicationService(repository));
        WalletRechargePackagePageReqVO pageReqVO = new WalletRechargePackagePageReqVO();
        pageReqVO.setName("套餐A");
        pageReqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        pageReqVO.setPageNo(2);
        pageReqVO.setPageSize(30);
        LocalDateTime[] createTime = {LocalDateTime.of(2026, 5, 24, 1, 0), LocalDateTime.of(2026, 5, 24, 2, 0)};
        pageReqVO.setCreateTime(createTime);

        CommonResult<PageResult<WalletRechargePackageRespVO>> result = controller.getWalletRechargePackagePage(pageReqVO);

        assertEquals(0, result.getCode());
        assertEquals(1L, result.getData().getTotal());
        assertEquals(200L, result.getData().getList().get(0).getId());
        assertEquals("套餐A", result.getData().getList().get(0).getName());
        assertEquals(100, result.getData().getList().get(0).getPayPrice());
        assertEquals(20, result.getData().getList().get(0).getBonusPrice());
        assertEquals("套餐A", repository.pageName);
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), repository.pageStatus);
        assertEquals(createTime, repository.pageCreateTime);
        assertEquals(2, repository.pageNo);
        assertEquals(30, repository.pageSize);
    }

    private static void inject(PayWalletRechargePackageController controller,
                               PayWalletRechargePackageApplicationService applicationService) throws Exception {
        Field field = PayWalletRechargePackageController.class.getDeclaredField("rechargePackageApplicationService");
        field.setAccessible(true);
        field.set(controller, applicationService);
    }

    private static final class RecordingRepository implements PayWalletRechargePackageRepository {
        private PayWalletRechargePackage saved;
        private String pageName;
        private Integer pageStatus;
        private LocalDateTime[] pageCreateTime;
        private Integer pageNo;
        private Integer pageSize;

        @Override
        public PayWalletRechargePackage save(PayWalletRechargePackage rechargePackage) {
            saved = rechargePackage;
            return new PayWalletRechargePackage(100L).name(rechargePackage.name()).payPrice(rechargePackage.payPrice())
                    .bonusPrice(rechargePackage.bonusPrice()).status(rechargePackage.status());
        }

        @Override
        public PayWalletRechargePackage findById(Long id) {
            return new PayWalletRechargePackage(id).name("套餐A").payPrice(100).bonusPrice(20)
                    .status(CommonStatusEnum.ENABLE.getStatus());
        }

        @Override
        public PayWalletRechargePackage findByName(String name) { return null; }

        @Override
        public void deleteById(Long id) { }

        @Override
        public PageResult<PayWalletRechargePackage> findPage(String name, Integer status, LocalDateTime[] createTime,
                                                             Integer pageNo, Integer pageSize) {
            this.pageName = name;
            this.pageStatus = status;
            this.pageCreateTime = createTime;
            this.pageNo = pageNo;
            this.pageSize = pageSize;
            return new PageResult<>(List.of(new PayWalletRechargePackage(200L).name("套餐A").payPrice(100)
                    .bonusPrice(20).status(CommonStatusEnum.ENABLE.getStatus())), 1L);
        }

        @Override
        public List<PayWalletRechargePackage> findListByStatus(Integer status) { return List.of(); }
    }
}
