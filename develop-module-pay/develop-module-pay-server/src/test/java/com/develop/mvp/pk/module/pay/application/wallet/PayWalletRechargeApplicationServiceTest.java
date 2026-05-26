package com.develop.mvp.pk.module.pay.application.wallet;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.api.order.dto.PayOrderCreateReqDTO;
import com.develop.mvp.pk.module.pay.api.refund.PayRefundApi;
import com.develop.mvp.pk.module.pay.api.refund.dto.PayRefundCreateReqDTO;
import com.develop.mvp.pk.module.pay.api.refund.dto.PayRefundRespDTO;
import com.develop.mvp.pk.module.pay.dal.dataobject.order.PayOrderDO;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWallet;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletFactory;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletRecharge;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletRechargePackage;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletTransaction;
import com.develop.mvp.pk.module.pay.domain.wallet.repository.PayWalletRechargePackageRepository;
import com.develop.mvp.pk.module.pay.domain.wallet.repository.PayWalletRechargeRepository;
import com.develop.mvp.pk.module.pay.domain.wallet.repository.PayWalletRepository;
import com.develop.mvp.pk.module.pay.domain.wallet.repository.PayWalletTransactionRepository;
import com.develop.mvp.pk.module.pay.domain.wallet.service.PayWalletLock;
import com.develop.mvp.pk.module.pay.enums.order.PayOrderStatusEnum;
import com.develop.mvp.pk.module.pay.enums.refund.PayRefundStatusEnum;
import com.develop.mvp.pk.module.pay.enums.wallet.PayWalletBizTypeEnum;
import com.develop.mvp.pk.module.pay.framework.pay.config.PayProperties;
import com.develop.mvp.pk.module.pay.service.order.PayOrderService;
import com.develop.mvp.pk.module.system.api.social.SocialClientApi;
import com.develop.mvp.pk.module.system.api.social.dto.SocialWxJsapiSignatureRespDTO;
import com.develop.mvp.pk.module.system.api.social.dto.SocialWxPhoneNumberInfoRespDTO;
import com.develop.mvp.pk.module.system.api.social.dto.SocialWxQrcodeReqDTO;
import com.develop.mvp.pk.module.system.api.social.dto.SocialWxaOrderNotifyConfirmReceiveReqDTO;
import com.develop.mvp.pk.module.system.api.social.dto.SocialWxaOrderUploadShippingInfoReqDTO;
import com.develop.mvp.pk.module.system.api.social.dto.SocialWxaSubscribeMessageSendReqDTO;
import com.develop.mvp.pk.module.system.api.social.dto.SocialWxaSubscribeTemplateRespDTO;
import com.develop.mvp.pk.module.pay.controller.admin.order.vo.PayOrderExportReqVO;
import com.develop.mvp.pk.module.pay.controller.admin.order.vo.PayOrderPageReqVO;
import com.develop.mvp.pk.module.pay.controller.admin.order.vo.PayOrderSubmitReqVO;
import com.develop.mvp.pk.module.pay.controller.admin.order.vo.PayOrderSubmitRespVO;
import com.develop.mvp.pk.module.pay.dal.dataobject.order.PayOrderExtensionDO;
import com.develop.mvp.pk.module.pay.framework.pay.core.client.dto.order.PayOrderRespDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayWalletRechargeApplicationServiceTest {

    @Test
    void create_resolvesWalletAndPackageThenCreatesPayOrder() {
        RecordingWalletRepository walletRepository = new RecordingWalletRepository();
        RecordingRechargeRepository rechargeRepository = new RecordingRechargeRepository();
        RecordingRechargePackageRepository packageRepository = new RecordingRechargePackageRepository();
        RecordingOrderService orderService = new RecordingOrderService();
        PayWalletRechargeApplicationService service = newService(
                walletRepository, rechargeRepository, packageRepository, orderService, new StubRefundApi());

        PayWalletRecharge recharge = service.create(1L, 1, "127.0.0.1", null, 500L);

        assertEquals(1L, walletRepository.userId);
        assertEquals(1, walletRepository.userType);
        assertEquals(500L, packageRepository.findById);
        assertEquals(100L, rechargeRepository.saved.walletId());
        assertEquals(80, rechargeRepository.saved.payPrice());
        assertEquals(20, rechargeRepository.saved.bonusPrice());
        assertEquals(100, rechargeRepository.saved.totalPrice());
        assertEquals(500L, rechargeRepository.saved.packageId());
        assertEquals("300", orderService.reqDTO.getMerchantOrderId());
        assertEquals("钱包余额充值", orderService.reqDTO.getSubject());
        assertEquals(80, orderService.reqDTO.getPrice());
        assertEquals(200L, rechargeRepository.payOrderId);
        assertEquals(200L, recharge.payOrderId());
    }

    @Test
    void updatePaid_validatesPayOrderAndAddsRechargeBalance() {
        RecordingWalletRepository walletRepository = new RecordingWalletRepository();
        RecordingRechargeRepository rechargeRepository = new RecordingRechargeRepository();
        rechargeRepository.recharge = new PayWalletRecharge(300L).walletId(100L).payPrice(80).bonusPrice(20)
                .totalPrice(100).payStatus(false).payOrderId(200L);
        RecordingOrderService orderService = new RecordingOrderService();
        StubTransactionRepository transactionRepository = new StubTransactionRepository();
        PayWalletRechargeApplicationService service = newService(
                walletRepository, transactionRepository, rechargeRepository, new RecordingRechargePackageRepository(),
                orderService, new StubRefundApi());

        service.updatePaid(300L, 200L);

        assertTrue(rechargeRepository.markPaidCalled);
        assertEquals("wx_lite", rechargeRepository.payChannelCode);
        assertEquals(100L, walletRepository.rechargeWalletId);
        assertEquals(100, walletRepository.rechargePrice);
        assertEquals("300", transactionRepository.saved.bizId());
        assertEquals(PayWalletBizTypeEnum.RECHARGE.getType(), transactionRepository.saved.bizType());
    }

    @Test
    void refund_freezesRechargeTotalPriceAndCreatesRefund() {
        RecordingWalletRepository walletRepository = new RecordingWalletRepository();
        walletRepository.wallet = PayWalletFactory.restore(100L, 1L, 1, 200, 0, 0, 0);
        RecordingRechargeRepository rechargeRepository = new RecordingRechargeRepository();
        rechargeRepository.recharge = new PayWalletRecharge(300L).walletId(100L).payPrice(80).bonusPrice(20)
                .totalPrice(100).payStatus(true);
        StubRefundApi refundApi = new StubRefundApi();
        PayWalletRechargeApplicationService service = newService(
                walletRepository, rechargeRepository, new RecordingRechargePackageRepository(), new RecordingOrderService(), refundApi);

        service.refund(300L, "127.0.0.1");

        assertEquals(100L, walletRepository.freezeWalletId);
        assertEquals(100, walletRepository.freezePrice);
        assertEquals("300", refundApi.createReqDTO.getMerchantOrderId());
        assertEquals("300-refund", refundApi.createReqDTO.getMerchantRefundId());
        assertEquals(80, refundApi.createReqDTO.getPrice());
        assertEquals(900L, rechargeRepository.payRefundId);
        assertEquals(PayRefundStatusEnum.WAITING.getStatus(), rechargeRepository.refundStatus);
    }

    private static PayWalletRechargeApplicationService newService(RecordingWalletRepository walletRepository,
                                                                  RecordingRechargeRepository rechargeRepository,
                                                                  RecordingRechargePackageRepository packageRepository,
                                                                  RecordingOrderService orderService,
                                                                  PayRefundApi refundApi) {
        return newService(walletRepository, new StubTransactionRepository(), rechargeRepository, packageRepository,
                orderService, refundApi);
    }

    private static PayWalletRechargeApplicationService newService(RecordingWalletRepository walletRepository,
                                                                  StubTransactionRepository transactionRepository,
                                                                  RecordingRechargeRepository rechargeRepository,
                                                                  RecordingRechargePackageRepository packageRepository,
                                                                  RecordingOrderService orderService,
                                                                  PayRefundApi refundApi) {
        PayProperties properties = new PayProperties();
        properties.setWalletPayAppKey("wallet-app");
        return new PayWalletRechargeApplicationService(rechargeRepository,
                new PayWalletApplicationService(walletRepository, transactionRepository, new DirectWalletLock()),
                new PayWalletRechargePackageApplicationService(packageRepository), orderService, refundApi,
                new StubSocialClientApi(), properties);
    }

    private static final class RecordingRechargeRepository implements PayWalletRechargeRepository {
        private PayWalletRecharge saved;
        private PayWalletRecharge recharge;
        private Long payOrderId;
        private boolean markPaidCalled;
        private String payChannelCode;
        private Long payRefundId;
        private Integer refundStatus;

        @Override
        public PayWalletRecharge save(PayWalletRecharge recharge) {
            saved = recharge;
            return new PayWalletRecharge(300L).walletId(recharge.walletId()).payPrice(recharge.payPrice())
                    .bonusPrice(recharge.bonusPrice()).totalPrice(recharge.totalPrice()).packageId(recharge.packageId())
                    .payStatus(recharge.payStatus());
        }

        @Override
        public PayWalletRecharge findById(Long id) {
            return recharge;
        }

        @Override
        public PageResult<PayWalletRecharge> findPage(Long walletId, Boolean payStatus, Integer pageNo, Integer pageSize) {
            return PageResult.empty();
        }

        @Override
        public void updatePayOrderId(Long id, Long payOrderId) {
            this.payOrderId = payOrderId;
        }

        @Override
        public int markPaid(Long id, String payChannelCode) {
            markPaidCalled = true;
            this.payChannelCode = payChannelCode;
            return 1;
        }

        @Override
        public void markRefundWaiting(Long id, Long payRefundId) {
            this.payRefundId = payRefundId;
            refundStatus = PayRefundStatusEnum.WAITING.getStatus();
        }

        @Override
        public int markRefundSuccess(Long id, LocalDateTime refundTime, Integer refundTotalPrice,
                                     Integer refundPayPrice, Integer refundBonusPrice) {
            return 1;
        }

        @Override
        public int markRefundFailure(Long id) {
            return 1;
        }

        @Override
        public int updateByIdAndPaid(Long id, boolean wherePayStatus, PayWalletRecharge recharge) {
            return 1;
        }

        @Override
        public int updateByIdAndRefunded(Long id, Integer whereRefundStatus, PayWalletRecharge recharge) {
            return 1;
        }
    }

    private static final class RecordingRechargePackageRepository implements PayWalletRechargePackageRepository {
        private Long findById;

        @Override
        public PayWalletRechargePackage save(PayWalletRechargePackage rechargePackage) { return rechargePackage; }

        @Override
        public PayWalletRechargePackage findById(Long id) {
            findById = id;
            return new PayWalletRechargePackage(id).name("套餐A").payPrice(80).bonusPrice(20)
                    .status(CommonStatusEnum.ENABLE.getStatus());
        }

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
        public List<PayWalletRechargePackage> findListByStatus(Integer status) { return List.of(); }
    }

    private static final class RecordingWalletRepository implements PayWalletRepository {
        private Long userId;
        private Integer userType;
        private Long rechargeWalletId;
        private Integer rechargePrice;
        private Long freezeWalletId;
        private Integer freezePrice;
        private PayWallet wallet = PayWalletFactory.restore(100L, 1L, 1, 0, 0, 0, 0);

        @Override
        public PayWallet save(PayWallet wallet) {
            return PayWalletFactory.restore(100L, wallet.userId(), wallet.userType(), 0, 0, 0, 0);
        }

        @Override
        public PayWallet findById(Long id) {
            return wallet;
        }

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
        public int updateWhenRecharge(Long id, Integer price) {
            rechargeWalletId = id;
            rechargePrice = price;
            return 1;
        }

        @Override
        public int updateWhenAdd(Long id, Integer price) { return 0; }

        @Override
        public int freezePrice(Long id, Integer price) {
            freezeWalletId = id;
            freezePrice = price;
            return 1;
        }

        @Override
        public int unFreezePrice(Long id, Integer price) { return 0; }

        @Override
        public int updateWhenRechargeRefund(Long id, Integer price) { return 0; }
    }

    private static final class StubTransactionRepository implements PayWalletTransactionRepository {
        private PayWalletTransaction saved;

        @Override
        public PayWalletTransaction save(PayWalletTransaction transaction) {
            saved = transaction;
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

    private static final class RecordingOrderService implements PayOrderService {
        private PayOrderCreateReqDTO reqDTO;

        @Override
        public Long createOrder(PayOrderCreateReqDTO reqDTO) {
            this.reqDTO = reqDTO;
            return 200L;
        }

        @Override
        public PayOrderDO getOrder(Long id) {
            return PayOrderDO.builder().id(id).status(PayOrderStatusEnum.SUCCESS.getStatus())
                    .price(80).merchantOrderId("300").channelCode("wx_lite").build();
        }

        @Override
        public PayOrderDO getOrder(String no) { throw new UnsupportedOperationException(); }

        @Override
        public PayOrderDO getOrder(Long appId, String merchantOrderId) { throw new UnsupportedOperationException(); }

        @Override
        public List<PayOrderDO> getOrderList(Collection<Long> ids) { return List.of(); }

        @Override
        public Long getOrderCountByAppId(Long appId) { throw new UnsupportedOperationException(); }

        @Override
        public PageResult<PayOrderDO> getOrderPage(PayOrderPageReqVO pageReqVO) { throw new UnsupportedOperationException(); }

        @Override
        public List<PayOrderDO> getOrderList(PayOrderExportReqVO exportReqVO) { throw new UnsupportedOperationException(); }

        @Override
        public PayOrderSubmitRespVO submitOrder(PayOrderSubmitReqVO reqVO, String userIp) { throw new UnsupportedOperationException(); }

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

    private static final class StubSocialClientApi implements SocialClientApi {
        @Override
        public CommonResult<String> getAuthorizeUrl(Integer socialType, Integer userType, String redirectUri) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CommonResult<SocialWxJsapiSignatureRespDTO> createWxMpJsapiSignature(Integer userType, String url) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CommonResult<SocialWxPhoneNumberInfoRespDTO> getWxMaPhoneNumberInfo(Integer userType, String phoneCode) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CommonResult<byte[]> getWxaQrcode(SocialWxQrcodeReqDTO reqVO) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CommonResult<List<SocialWxaSubscribeTemplateRespDTO>> getWxaSubscribeTemplateList(Integer userType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CommonResult<Boolean> sendWxaSubscribeMessage(SocialWxaSubscribeMessageSendReqDTO reqDTO) {
            return CommonResult.success(true);
        }

        @Override
        public CommonResult<Boolean> uploadWxaOrderShippingInfo(Integer userType, SocialWxaOrderUploadShippingInfoReqDTO reqDTO) {
            return CommonResult.success(true);
        }

        @Override
        public CommonResult<Boolean> notifyWxaOrderConfirmReceive(Integer userType, SocialWxaOrderNotifyConfirmReceiveReqDTO reqDTO) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class StubRefundApi implements PayRefundApi {
        private PayRefundCreateReqDTO createReqDTO;
        private PayRefundRespDTO refund = new PayRefundRespDTO().setId(900L).setRefundPrice(80)
                .setMerchantRefundId("300-refund").setStatus(PayRefundStatusEnum.SUCCESS.getStatus())
                .setSuccessTime(LocalDateTime.now());

        @Override
        public CommonResult<Long> createRefund(PayRefundCreateReqDTO reqDTO) {
            createReqDTO = reqDTO;
            return CommonResult.success(900L);
        }

        @Override
        public CommonResult<PayRefundRespDTO> getRefund(Long id) {
            return CommonResult.success(refund);
        }
    }
}
