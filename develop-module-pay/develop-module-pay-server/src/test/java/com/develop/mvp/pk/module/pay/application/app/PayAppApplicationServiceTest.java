package com.develop.mvp.pk.module.pay.application.app;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.test.core.ut.BaseDbUnitTest;
import com.develop.mvp.pk.module.pay.dal.dataobject.app.PayAppDO;
import com.develop.mvp.pk.module.pay.dal.mysql.app.PayAppMapper;
import com.develop.mvp.pk.module.pay.domain.app.PayApp;
import com.develop.mvp.pk.module.pay.infrastructure.app.PayAppRepositoryImpl;
import com.develop.mvp.pk.module.pay.application.order.PayOrderApplicationService;
import com.develop.mvp.pk.module.pay.application.refund.PayRefundApplicationService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.develop.mvp.pk.framework.test.core.util.AssertUtils.assertServiceException;
import static com.develop.mvp.pk.module.pay.enums.ErrorCodeConstants.APP_EXIST_ORDER_CANT_DELETE;
import static com.develop.mvp.pk.module.pay.enums.ErrorCodeConstants.APP_EXIST_REFUND_CANT_DELETE;
import static com.develop.mvp.pk.module.pay.enums.ErrorCodeConstants.APP_KEY_EXISTS;
import static com.develop.mvp.pk.module.pay.enums.ErrorCodeConstants.APP_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Import({PayAppApplicationService.class, PayAppRepositoryImpl.class})
class PayAppApplicationServiceTest extends BaseDbUnitTest {

    @Resource
    private PayAppApplicationService appApplicationService;
    @Resource
    private PayAppMapper appMapper;

    @MockitoBean
    private PayOrderApplicationService orderApplicationService;
    @MockitoBean
    private PayRefundApplicationService refundApplicationService;

    @Test
    void create_preservesAllRequestFieldsAndReturnsGeneratedId() {
        PayApp app = appApplicationService.create("商城支付", "mall-pay", CommonStatusEnum.DISABLE.getStatus(),
                "remark", "http://127.0.0.1/order", "http://127.0.0.1/refund", "http://127.0.0.1/transfer");

        assertNotNull(app.id());
        PayAppDO saved = appMapper.selectById(app.id());
        assertEquals("商城支付", saved.getName());
        assertEquals("mall-pay", saved.getAppKey());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), saved.getStatus());
        assertEquals("remark", saved.getRemark());
        assertEquals("http://127.0.0.1/order", saved.getOrderNotifyUrl());
        assertEquals("http://127.0.0.1/refund", saved.getRefundNotifyUrl());
        assertEquals("http://127.0.0.1/transfer", saved.getTransferNotifyUrl());
    }

    @Test
    void create_rejectsDuplicateAppKey() {
        appMapper.insert(PayAppDO.builder().name("旧应用").appKey("duplicate-key")
                .status(CommonStatusEnum.ENABLE.getStatus())
                .orderNotifyUrl("http://127.0.0.1/order")
                .refundNotifyUrl("http://127.0.0.1/refund")
                .build());

        assertServiceException(() -> appApplicationService.create("新应用", "duplicate-key", CommonStatusEnum.ENABLE.getStatus(),
                null, "http://127.0.0.1/order", "http://127.0.0.1/refund", null), APP_KEY_EXISTS);
    }

    @Test
    void update_rejectsMissingAppWithServiceErrorCode() {
        assertServiceException(() -> appApplicationService.update(999L, "应用", "app-key", CommonStatusEnum.ENABLE.getStatus(),
                null, "http://127.0.0.1/order", "http://127.0.0.1/refund", null), APP_NOT_FOUND);
    }

    @Test
    void delete_rejectsAppWithOrders() {
        PayApp app = appApplicationService.create("应用", "order-app", CommonStatusEnum.ENABLE.getStatus(),
                null, "http://127.0.0.1/order", "http://127.0.0.1/refund", null);
        when(orderApplicationService.countByAppId(eq(app.id()))).thenReturn(1L);

        assertServiceException(() -> appApplicationService.delete(app.id()), APP_EXIST_ORDER_CANT_DELETE);
    }

    @Test
    void delete_rejectsAppWithRefunds() {
        PayApp app = appApplicationService.create("应用", "refund-app", CommonStatusEnum.ENABLE.getStatus(),
                null, "http://127.0.0.1/order", "http://127.0.0.1/refund", null);
        when(refundApplicationService.countByAppId(eq(app.id()))).thenReturn(1L);

        assertServiceException(() -> appApplicationService.delete(app.id()), APP_EXIST_REFUND_CANT_DELETE);
    }
}
