package com.develop.mvp.pk.module.pay.infrastructure.wallet;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.dal.dataobject.wallet.PayWalletRechargePackageDO;
import com.develop.mvp.pk.module.pay.dal.mysql.wallet.PayWalletRechargePackageMapper;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletRechargePackage;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayWalletRechargePackageRepositoryImplTest {

    @Test
    void save_insertsNewPackageAndReturnsGeneratedId() throws Exception {
        RecordingMapper mapper = new RecordingMapper();
        PayWalletRechargePackageRepositoryImpl repository = new PayWalletRechargePackageRepositoryImpl();
        inject(repository, mapper.proxy());

        PayWalletRechargePackage saved = repository.save(new PayWalletRechargePackage(null)
                .name("套餐A").payPrice(100).bonusPrice(20).status(CommonStatusEnum.ENABLE.getStatus()));

        assertEquals(100L, saved.id());
        assertEquals("套餐A", mapper.inserted.getName());
        assertEquals(100, mapper.inserted.getPayPrice());
        assertEquals(20, mapper.inserted.getBonusPrice());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), mapper.inserted.getStatus());
    }

    @Test
    void findPage_preservesLegacyQueryParameters() throws Exception {
        RecordingMapper mapper = new RecordingMapper();
        PayWalletRechargePackageRepositoryImpl repository = new PayWalletRechargePackageRepositoryImpl();
        inject(repository, mapper.proxy());
        LocalDateTime[] createTime = {LocalDateTime.of(2026, 5, 24, 1, 0), LocalDateTime.of(2026, 5, 24, 2, 0)};

        PageResult<PayWalletRechargePackage> page = repository.findPage("套餐A", CommonStatusEnum.ENABLE.getStatus(), createTime, 2, 30);

        assertEquals(1L, page.getTotal());
        assertEquals(200L, page.getList().get(0).id());
        assertEquals("套餐A", mapper.pageName);
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), mapper.pageStatus);
        assertEquals(createTime, mapper.pageCreateTime);
        assertEquals(2, mapper.pageNo);
        assertEquals(30, mapper.pageSize);
    }

    private static void inject(PayWalletRechargePackageRepositoryImpl repository, PayWalletRechargePackageMapper mapper) throws Exception {
        Field field = PayWalletRechargePackageRepositoryImpl.class.getDeclaredField("walletRechargePackageMapper");
        field.setAccessible(true);
        field.set(repository, mapper);
    }

    private static final class RecordingMapper {
        private PayWalletRechargePackageDO inserted;
        private String pageName;
        private Integer pageStatus;
        private LocalDateTime[] pageCreateTime;
        private Integer pageNo;
        private Integer pageSize;

        private PayWalletRechargePackageMapper proxy() {
            return (PayWalletRechargePackageMapper) Proxy.newProxyInstance(
                    PayWalletRechargePackageMapper.class.getClassLoader(),
                    new Class<?>[]{PayWalletRechargePackageMapper.class},
                    (proxy, method, args) -> {
                        if ("insert".equals(method.getName())) {
                            inserted = (PayWalletRechargePackageDO) args[0];
                            inserted.setId(100L);
                            return 1;
                        }
                        if ("selectPage".equals(method.getName())) {
                            Object pageReqVO = args[0];
                            pageName = (String) pageReqVO.getClass().getMethod("getName").invoke(pageReqVO);
                            pageStatus = (Integer) pageReqVO.getClass().getMethod("getStatus").invoke(pageReqVO);
                            pageCreateTime = (LocalDateTime[]) pageReqVO.getClass().getMethod("getCreateTime").invoke(pageReqVO);
                            pageNo = (Integer) pageReqVO.getClass().getMethod("getPageNo").invoke(pageReqVO);
                            pageSize = (Integer) pageReqVO.getClass().getMethod("getPageSize").invoke(pageReqVO);
                            return new PageResult<>(List.of(new PayWalletRechargePackageDO().setId(200L).setName("套餐A")
                                    .setPayPrice(100).setBonusPrice(20).setStatus(CommonStatusEnum.ENABLE.getStatus())), 1L);
                        }
                        if ("toString".equals(method.getName())) {
                            return "RecordingMapper";
                        }
                        throw new UnsupportedOperationException(method.getName());
                    });
        }
    }
}
