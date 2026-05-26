package com.develop.mvp.pk.module.pay.application.wallet;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.develop.mvp.pk.framework.common.enums.UserTypeEnum;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.api.order.dto.PayOrderCreateReqDTO;
import com.develop.mvp.pk.module.pay.api.refund.PayRefundApi;
import com.develop.mvp.pk.module.pay.api.refund.dto.PayRefundCreateReqDTO;
import com.develop.mvp.pk.module.pay.api.refund.dto.PayRefundRespDTO;
import com.develop.mvp.pk.module.pay.dal.dataobject.order.PayOrderDO;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWallet;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletRecharge;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletRechargePackage;
import com.develop.mvp.pk.module.pay.domain.wallet.repository.PayWalletRechargeRepository;
import com.develop.mvp.pk.module.pay.enums.PayChannelEnum;
import com.develop.mvp.pk.module.pay.enums.order.PayOrderStatusEnum;
import com.develop.mvp.pk.module.pay.enums.refund.PayRefundStatusEnum;
import com.develop.mvp.pk.module.pay.enums.wallet.PayWalletBizTypeEnum;
import com.develop.mvp.pk.module.pay.framework.pay.config.PayProperties;
import com.develop.mvp.pk.module.pay.service.order.PayOrderService;
import com.develop.mvp.pk.module.system.api.social.SocialClientApi;
import com.develop.mvp.pk.module.system.api.social.dto.SocialWxaOrderUploadShippingInfoReqDTO;
import com.develop.mvp.pk.module.system.api.social.dto.SocialWxaSubscribeMessageSendReqDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.framework.common.util.date.LocalDateTimeUtils.addTime;
import static com.develop.mvp.pk.framework.common.util.number.MoneyUtils.fenToYuanStr;
import static com.develop.mvp.pk.module.pay.enums.ErrorCodeConstants.*;
import static com.develop.mvp.pk.module.pay.enums.MessageTemplateConstants.WXA_WALLET_RECHARGER_PAID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayWalletRechargeApplicationService {

    private static final String WALLET_RECHARGE_ORDER_SUBJECT = "钱包余额充值";

    private final PayWalletRechargeRepository rechargeRepository;
    private final PayWalletApplicationService payWalletApplicationService;
    private final PayWalletRechargePackageApplicationService rechargePackageApplicationService;
    private final PayOrderService payOrderService;
    private final PayRefundApi payRefundApi;
    private final SocialClientApi socialClientApi;
    private final PayProperties payProperties;

    @Transactional(rollbackFor = Exception.class)
    public PayWalletRecharge create(Long userId, Integer userType, String userIp, Integer payPrice, Long packageId) {
        int actualPayPrice;
        int bonusPrice = 0;
        if (packageId != null) {
            PayWalletRechargePackage rechargePackage = rechargePackageApplicationService.valid(packageId);
            actualPayPrice = rechargePackage.payPrice();
            bonusPrice = rechargePackage.bonusPrice();
        } else {
            actualPayPrice = payPrice;
        }
        PayWallet wallet = payWalletApplicationService.getOrCreate(userId, userType);
        PayWalletRecharge recharge = rechargeRepository.save(new PayWalletRecharge(null).walletId(wallet.id())
                .payPrice(actualPayPrice).bonusPrice(bonusPrice).totalPrice(actualPayPrice + bonusPrice)
                .packageId(packageId).payStatus(false));
        Long payOrderId = payOrderService.createOrder(new PayOrderCreateReqDTO()
                .setAppKey(payProperties.getWalletPayAppKey()).setUserIp(userIp)
                .setUserId(userId).setUserType(userType)
                .setMerchantOrderId(recharge.id().toString())
                .setSubject(WALLET_RECHARGE_ORDER_SUBJECT).setBody("")
                .setPrice(recharge.payPrice())
                .setExpireTime(addTime(Duration.ofHours(2L))));
        rechargeRepository.updatePayOrderId(recharge.id(), payOrderId);
        return recharge.payOrderId(payOrderId);
    }

    public PageResult<PayWalletRecharge> getPage(Long userId, Integer userType, Integer pageNo, Integer pageSize,
                                                 Boolean payStatus) {
        PayWallet wallet = payWalletApplicationService.getOrCreate(userId, userType);
        return rechargeRepository.findPage(wallet.id(), payStatus, pageNo, pageSize);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updatePaid(Long id, Long payOrderId) {
        PayWalletRecharge recharge = rechargeRepository.findById(id);
        if (recharge == null) {
            log.error("[updatePaid][recharge({}) payOrder({}) 不存在充值订单，请进行处理！]", id, payOrderId);
            throw exception(WALLET_RECHARGE_NOT_FOUND);
        }
        if (recharge.isPaid()) {
            if (ObjectUtil.equals(recharge.payOrderId(), payOrderId)) {
                log.warn("[updatePaid][recharge({}) 已支付，且支付单号相同({})，直接返回]", recharge, payOrderId);
                return;
            }
            log.error("[updatePaid][recharge({}) 已支付，但是支付单号不同({})，请进行处理！]", recharge, payOrderId);
            throw exception(WALLET_RECHARGE_UPDATE_PAID_PAY_ORDER_ID_ERROR);
        }
        PayOrderDO payOrder = validatePayOrderPaid(recharge, payOrderId);
        int updateCount = rechargeRepository.markPaid(id, payOrder.getChannelCode());
        if (updateCount == 0) {
            throw exception(WALLET_RECHARGE_UPDATE_PAID_STATUS_NOT_UNPAID);
        }
        payWalletApplicationService.addBalance(recharge.walletId(), String.valueOf(id),
                PayWalletBizTypeEnum.RECHARGE.getType(), recharge.totalPrice(), null);
        getSelf().sendPaidMessage(payOrderId, recharge);
    }

    @Async
    public void sendPaidMessage(Long payOrderId, PayWalletRecharge recharge) {
        PayWallet wallet = payWalletApplicationService.get(recharge.walletId());
        socialClientApi.sendWxaSubscribeMessage(new SocialWxaSubscribeMessageSendReqDTO()
                .setUserId(wallet.userId()).setUserType(wallet.userType())
                .setTemplateTitle(WXA_WALLET_RECHARGER_PAID)
                .setPage("pages/user/wallet/money")
                .addMessage("character_string1", String.valueOf(payOrderId))
                .addMessage("amount2", fenToYuanStr(recharge.totalPrice()))
                .addMessage("time3", LocalDateTimeUtil.formatNormal(recharge.createTime()))
                .addMessage("phrase4", "充值成功")).checkError();
        PayOrderDO payOrder = payOrderService.getOrder(payOrderId);
        if (ObjUtil.notEqual(payOrder.getChannelCode(), PayChannelEnum.WX_LITE.getCode())) {
            return;
        }
        SocialWxaOrderUploadShippingInfoReqDTO reqDTO = new SocialWxaOrderUploadShippingInfoReqDTO()
                .setTransactionId(payOrder.getChannelOrderNo())
                .setOpenid(payOrder.getChannelUserId())
                .setItemDesc(payOrder.getSubject())
                .setLogisticsType(SocialWxaOrderUploadShippingInfoReqDTO.LOGISTICS_TYPE_VIRTUAL);
        try {
            socialClientApi.uploadWxaOrderShippingInfo(UserTypeEnum.MEMBER.getValue(), reqDTO).checkError();
        } catch (Exception ex) {
            log.error("[sendPaidMessage][订单({}) 上传订单物流信息到微信小程序失败]", payOrder, ex);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void refund(Long id, String userIp) {
        PayWalletRecharge recharge = rechargeRepository.findById(id);
        if (recharge == null) {
            log.error("[refund][钱包充值记录不存在，钱包充值记录 id({})]", id);
            throw exception(WALLET_RECHARGE_NOT_FOUND);
        }
        PayWallet wallet = validateCanRefund(recharge);
        payWalletApplicationService.freezePrice(wallet.id(), recharge.totalPrice());
        String walletRechargeId = String.valueOf(id);
        Long payRefundId = payRefundApi.createRefund(new PayRefundCreateReqDTO()
                .setAppKey(payProperties.getWalletPayAppKey()).setUserIp(userIp)
                .setUserId(wallet.userId()).setUserType(wallet.userType())
                .setMerchantOrderId(walletRechargeId).setMerchantRefundId(walletRechargeId + "-refund")
                .setReason("想退钱").setPrice(recharge.payPrice())).getCheckedData();
        rechargeRepository.markRefundWaiting(id, payRefundId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateRefunded(Long id, String refundId, Long payRefundId) {
        PayWalletRecharge recharge = rechargeRepository.findById(id);
        if (recharge == null) {
            log.error("[updateRefunded][钱包充值记录不存在，钱包充值记录 id({})]", id);
            throw exception(WALLET_RECHARGE_NOT_FOUND);
        }
        PayRefundRespDTO payRefund = validateCanRefunded(recharge, payRefundId);
        if (PayRefundStatusEnum.isSuccess(payRefund.getStatus())) {
            payWalletApplicationService.deductBalance(recharge.walletId(), id,
                    PayWalletBizTypeEnum.RECHARGE_REFUND.getType(), recharge.totalPrice());
            rechargeRepository.markRefundSuccess(id, payRefund.getSuccessTime(), recharge.totalPrice(),
                    recharge.payPrice(), recharge.bonusPrice());
        } else if (PayRefundStatusEnum.isFailure(payRefund.getStatus())) {
            payWalletApplicationService.unfreezePrice(recharge.walletId(), recharge.totalPrice());
            rechargeRepository.markRefundFailure(id);
        }
    }

    private PayWallet validateCanRefund(PayWalletRecharge recharge) {
        if (!recharge.isPaid()) {
            throw exception(WALLET_RECHARGE_REFUND_FAIL_NOT_PAID);
        }
        if (recharge.payRefundId() != null) {
            throw exception(WALLET_RECHARGE_REFUND_FAIL_REFUNDED);
        }
        PayWallet wallet = payWalletApplicationService.get(recharge.walletId());
        if (wallet.balance() < recharge.totalPrice()) {
            throw exception(WALLET_RECHARGE_REFUND_BALANCE_NOT_ENOUGH);
        }
        return wallet;
    }

    private PayRefundRespDTO validateCanRefunded(PayWalletRecharge recharge, Long payRefundId) {
        if (ObjectUtil.notEqual(recharge.payRefundId(), payRefundId)) {
            throw exception(WALLET_RECHARGE_REFUND_FAIL_REFUND_ORDER_ID_ERROR);
        }
        PayRefundRespDTO payRefund = payRefundApi.getRefund(payRefundId).getCheckedData();
        if (payRefund == null) {
            throw exception(WALLET_RECHARGE_REFUND_FAIL_REFUND_NOT_FOUND);
        }
        if (ObjectUtil.notEqual(payRefund.getRefundPrice(), recharge.payPrice())) {
            throw exception(WALLET_RECHARGE_REFUND_FAIL_REFUND_PRICE_NOT_MATCH);
        }
        if (ObjectUtil.notEqual(payRefund.getMerchantRefundId(), recharge.id() + "-refund")) {
            throw exception(WALLET_RECHARGE_REFUND_FAIL_REFUND_ORDER_ID_ERROR);
        }
        return payRefund;
    }

    private PayWalletRechargeApplicationService getSelf() {
        try {
            return SpringUtil.getBean(getClass());
        } catch (Exception ignored) {
            return this;
        }
    }

    private PayOrderDO validatePayOrderPaid(PayWalletRecharge recharge, Long payOrderId) {
        PayOrderDO payOrder = payOrderService.getOrder(payOrderId);
        if (payOrder == null) {
            throw exception(PAY_ORDER_NOT_FOUND);
        }
        if (!PayOrderStatusEnum.isSuccess(payOrder.getStatus())) {
            throw exception(WALLET_RECHARGE_UPDATE_PAID_PAY_ORDER_STATUS_NOT_SUCCESS);
        }
        if (ObjectUtil.notEqual(payOrder.getPrice(), recharge.payPrice())) {
            throw exception(WALLET_RECHARGE_UPDATE_PAID_PAY_PRICE_NOT_MATCH);
        }
        if (ObjectUtil.notEqual(payOrder.getMerchantOrderId(), recharge.id().toString())) {
            throw exception(WALLET_RECHARGE_UPDATE_PAID_PAY_ORDER_ID_ERROR);
        }
        return payOrder;
    }
}
