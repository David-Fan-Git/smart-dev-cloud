package com.develop.mvp.pk.module.pay.controller.app.order;

import com.develop.mvp.pk.framework.common.enums.UserTypeEnum;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.web.core.util.WebFrameworkUtils;
import com.develop.mvp.pk.module.pay.api.order.dto.PayOrderCreateReqDTO;
import com.develop.mvp.pk.module.pay.application.wallet.PayWalletApplicationService;
import com.develop.mvp.pk.module.pay.controller.admin.order.vo.PayOrderExportReqVO;
import com.develop.mvp.pk.module.pay.controller.admin.order.vo.PayOrderPageReqVO;
import com.develop.mvp.pk.module.pay.controller.admin.order.vo.PayOrderRespVO;
import com.develop.mvp.pk.module.pay.controller.admin.order.vo.PayOrderSubmitReqVO;
import com.develop.mvp.pk.module.pay.controller.admin.order.vo.PayOrderSubmitRespVO;
import com.develop.mvp.pk.module.pay.controller.app.order.vo.AppPayOrderSubmitReqVO;
import com.develop.mvp.pk.module.pay.controller.app.order.vo.AppPayOrderSubmitRespVO;
import com.develop.mvp.pk.module.pay.dal.dataobject.order.PayOrderDO;
import com.develop.mvp.pk.module.pay.dal.dataobject.order.PayOrderExtensionDO;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWallet;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletFactory;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletTransaction;
import com.develop.mvp.pk.module.pay.domain.wallet.repository.PayWalletRepository;
import com.develop.mvp.pk.module.pay.domain.wallet.repository.PayWalletTransactionRepository;
import com.develop.mvp.pk.module.pay.domain.wallet.service.PayWalletLock;
import com.develop.mvp.pk.module.pay.enums.PayChannelEnum;
import com.develop.mvp.pk.module.pay.framework.pay.core.client.dto.order.PayOrderRespDTO;
import com.develop.mvp.pk.module.pay.framework.pay.core.client.impl.wallet.WalletPayClient;
import com.develop.mvp.pk.module.pay.service.order.PayOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppPayOrderControllerTest {

    @Test
    void submitPayOrder_putsDomainWalletIdIntoWalletChannelExtras() throws Exception {
        AppPayOrderController controller = new AppPayOrderController();
        RecordingOrderService orderService = new RecordingOrderService();
        RecordingWalletRepository walletRepository = new RecordingWalletRepository();
        inject(controller, "payOrderService", orderService);
        inject(controller, "payWalletApplicationService", new PayWalletApplicationService(
                walletRepository, new StubTransactionRepository(), new DirectWalletLock()));
        MockHttpServletRequest request = new MockHttpServletRequest();
        WebFrameworkUtils.setLoginUserId(request, 1L);
        WebFrameworkUtils.setLoginUserType(request, UserTypeEnum.MEMBER.getValue());
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        AppPayOrderSubmitReqVO reqVO = new AppPayOrderSubmitReqVO();
        reqVO.setId(10L);
        reqVO.setChannelCode(PayChannelEnum.WALLET.getCode());

        try {
            CommonResult<AppPayOrderSubmitRespVO> result = controller.submitPayOrder(reqVO);

            assertEquals(0, result.getCode());
            assertEquals("100", orderService.reqVO.getChannelExtras().get(WalletPayClient.WALLET_ID_KEY));
            assertEquals(1L, walletRepository.userId);
            assertEquals(UserTypeEnum.MEMBER.getValue(), walletRepository.userType);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    private static void inject(AppPayOrderController controller, String fieldName, Object value) throws Exception {
        Field field = AppPayOrderController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controller, value);
    }

    private static final class RecordingWalletRepository implements PayWalletRepository {
        private Long userId;
        private Integer userType;

        @Override
        public PayWallet save(PayWallet wallet) {
            return PayWalletFactory.restore(100L, wallet.userId(), wallet.userType(), 0, 0, 0, 0);
        }

        @Override
        public PayWallet findById(Long id) { return null; }

        @Override
        public Optional<PayWallet> findByUserIdAndType(Long userId, Integer userType) {
            this.userId = userId;
            this.userType = userType;
            return Optional.of(PayWalletFactory.restore(100L, userId, userType, 0, 0, 0, 0));
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
        public PayWalletTransaction save(PayWalletTransaction transaction) { return transaction; }

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

    private static final class RecordingOrderService implements PayOrderService {
        private PayOrderSubmitReqVO reqVO;

        @Override
        public PayOrderSubmitRespVO submitOrder(PayOrderSubmitReqVO reqVO, String userIp) {
            this.reqVO = reqVO;
            return new PayOrderSubmitRespVO();
        }

        @Override
        public PayOrderDO getOrder(Long id) { throw new UnsupportedOperationException(); }

        @Override
        public PayOrderDO getOrder(String no) { throw new UnsupportedOperationException(); }

        @Override
        public PayOrderDO getOrder(Long appId, String merchantOrderId) { throw new UnsupportedOperationException(); }

        @Override
        public List<PayOrderDO> getOrderList(Collection<Long> ids) { throw new UnsupportedOperationException(); }

        @Override
        public Long getOrderCountByAppId(Long appId) { throw new UnsupportedOperationException(); }

        @Override
        public PageResult<PayOrderDO> getOrderPage(PayOrderPageReqVO pageReqVO) { throw new UnsupportedOperationException(); }

        @Override
        public List<PayOrderDO> getOrderList(PayOrderExportReqVO exportReqVO) { throw new UnsupportedOperationException(); }

        @Override
        public Long createOrder(PayOrderCreateReqDTO reqDTO) { throw new UnsupportedOperationException(); }

        @Override
        public void notifyOrder(Long channelId, PayOrderRespDTO notify) { throw new UnsupportedOperationException(); }

        @Override
        public void updateOrderRefundPrice(Long id, Integer incrRefundPrice) { throw new UnsupportedOperationException(); }

        @Override
        public void updatePayOrderPrice(Long id, Integer payPrice) { throw new UnsupportedOperationException(); }

        @Override
        public PayOrderExtensionDO getOrderExtension(Long id) { throw new UnsupportedOperationException(); }

        @Override
        public PayOrderExtensionDO getOrderExtensionByNo(String no) { throw new UnsupportedOperationException(); }

        @Override
        public int syncOrder(LocalDateTime minCreateTime) { throw new UnsupportedOperationException(); }

        @Override
        public void syncOrderQuietly(Long id) { throw new UnsupportedOperationException(); }

        @Override
        public int expireOrder() { throw new UnsupportedOperationException(); }
    }
}
