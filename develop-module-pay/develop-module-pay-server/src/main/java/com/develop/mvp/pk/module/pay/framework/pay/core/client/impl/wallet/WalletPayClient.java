package com.develop.mvp.pk.module.pay.framework.pay.core.client.impl.wallet;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.develop.mvp.pk.framework.common.exception.ServiceException;
import com.develop.mvp.pk.module.pay.application.wallet.PayWalletApplicationService;
import com.develop.mvp.pk.module.pay.application.wallet.PayWalletTransactionApplicationService;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletTransaction;
import com.develop.mvp.pk.module.pay.enums.PayChannelEnum;
import com.develop.mvp.pk.module.pay.enums.refund.PayRefundStatusEnum;
import com.develop.mvp.pk.module.pay.enums.transfer.PayTransferStatusEnum;
import com.develop.mvp.pk.module.pay.framework.pay.core.client.dto.order.PayOrderRespDTO;
import com.develop.mvp.pk.module.pay.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import com.develop.mvp.pk.module.pay.framework.pay.core.client.dto.refund.PayRefundRespDTO;
import com.develop.mvp.pk.module.pay.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import com.develop.mvp.pk.module.pay.framework.pay.core.client.dto.transfer.PayTransferRespDTO;
import com.develop.mvp.pk.module.pay.framework.pay.core.client.dto.transfer.PayTransferUnifiedReqDTO;
import com.develop.mvp.pk.module.pay.framework.pay.core.client.impl.AbstractPayClient;
import com.develop.mvp.pk.module.pay.framework.pay.core.client.impl.NonePayClientConfig;
import com.develop.mvp.pk.module.pay.dal.dataobject.order.PayOrderExtensionDO;
import com.develop.mvp.pk.module.pay.dal.dataobject.refund.PayRefundDO;
import com.develop.mvp.pk.module.pay.dal.dataobject.transfer.PayTransferDO;
import com.develop.mvp.pk.module.pay.enums.order.PayOrderStatusEnum;
import com.develop.mvp.pk.module.pay.enums.wallet.PayWalletBizTypeEnum;
import com.develop.mvp.pk.module.pay.service.order.PayOrderService;
import com.develop.mvp.pk.module.pay.service.refund.PayRefundService;
import com.develop.mvp.pk.module.pay.service.transfer.PayTransferService;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.develop.mvp.pk.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;
import static com.develop.mvp.pk.module.pay.enums.ErrorCodeConstants.PAY_ORDER_EXTENSION_NOT_FOUND;
import static com.develop.mvp.pk.module.pay.enums.ErrorCodeConstants.REFUND_NOT_FOUND;

/**
 * 钱包支付的 PayClient 实现类
 *
 * @author David
 */
@Slf4j
public class WalletPayClient extends AbstractPayClient<NonePayClientConfig> {

    public static final String WALLET_ID_KEY = "walletId";

    private PayWalletApplicationService payWalletApplicationService;
    private PayWalletTransactionApplicationService transactionApplicationService;

    private PayOrderService orderService;
    private PayRefundService refundService;
    private PayTransferService transferService;

    public WalletPayClient(Long channelId, NonePayClientConfig config) {
        super(channelId, PayChannelEnum.WALLET.getCode(), config);
    }

    @Override
    protected void doInit() {
        if (payWalletApplicationService == null) {
            payWalletApplicationService = SpringUtil.getBean(PayWalletApplicationService.class);
        }
        if (transactionApplicationService == null) {
            transactionApplicationService = SpringUtil.getBean(PayWalletTransactionApplicationService.class);
        }
    }

    @Override
    @SuppressWarnings("PatternVariableCanBeUsed")
    protected PayOrderRespDTO doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO) {
        try {
            Long walletId = MapUtil.getLong(reqDTO.getChannelExtras(), WALLET_ID_KEY);
            Assert.notNull(walletId, "钱包编号");
            if (orderService == null) {
                orderService = SpringUtil.getBean(PayOrderService.class);
            }
            PayOrderExtensionDO orderExtension = orderService.getOrderExtensionByNo(reqDTO.getOutTradeNo());
            if (orderExtension == null) {
                throw com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception(PAY_ORDER_EXTENSION_NOT_FOUND);
            }
            PayWalletTransaction transaction = payWalletApplicationService.deductBalance(walletId,
                    orderExtension.getOrderId(), PayWalletBizTypeEnum.PAYMENT.getType(), reqDTO.getPrice());
            return PayOrderRespDTO.successOf(transaction.no(), transaction.creator(),
                    transaction.createTime(),
                    reqDTO.getOutTradeNo(), transaction);
        } catch (Throwable ex) {
            log.error("[doUnifiedOrder][reqDTO({}) 异常]", reqDTO, ex);
            Integer errorCode = INTERNAL_SERVER_ERROR.getCode();
            String errorMsg = INTERNAL_SERVER_ERROR.getMsg();
            if (ex instanceof ServiceException) {
                ServiceException serviceException = (ServiceException) ex;
                errorCode = serviceException.getCode();
                errorMsg = serviceException.getMessage();
            }
            return PayOrderRespDTO.closedOf(String.valueOf(errorCode), errorMsg,
                    reqDTO.getOutTradeNo(), "");
        }
    }

    @Override
    protected PayOrderRespDTO doParseOrderNotify(Map<String, String> params, String body, Map<String, String> headers) {
        throw new UnsupportedOperationException("钱包支付无支付回调");
    }

    @Override
    protected PayOrderRespDTO doGetOrder(String outTradeNo) {
        if (orderService == null) {
            orderService = SpringUtil.getBean(PayOrderService.class);
        }
        PayOrderExtensionDO orderExtension = orderService.getOrderExtensionByNo(outTradeNo);
        // 支付交易拓展单不存在， 返回关闭状态
        if (orderExtension == null) {
            return PayOrderRespDTO.closedOf(String.valueOf(PAY_ORDER_EXTENSION_NOT_FOUND.getCode()),
                    PAY_ORDER_EXTENSION_NOT_FOUND.getMsg(), outTradeNo, "");
        }
        // 关闭状态
        if (PayOrderStatusEnum.isClosed(orderExtension.getStatus())) {
            return PayOrderRespDTO.closedOf(orderExtension.getChannelErrorCode(),
                    orderExtension.getChannelErrorMsg(), outTradeNo, "");
        }
        // 成功状态
        if (PayOrderStatusEnum.isSuccess(orderExtension.getStatus())) {
            PayWalletTransaction walletTransaction = transactionApplicationService.getByBiz(
                    String.valueOf(orderExtension.getOrderId()), PayWalletBizTypeEnum.PAYMENT);
            Assert.notNull(walletTransaction, "支付单 {} 钱包流水不能为空", outTradeNo);
            return PayOrderRespDTO.successOf(walletTransaction.no(), walletTransaction.creator(),
                    walletTransaction.createTime(), outTradeNo, walletTransaction);
        }
        // 其它状态为无效状态
        log.error("[doGetOrder] 支付单 {} 的状态不正确", outTradeNo);
        throw new IllegalStateException(String.format("支付单[%s] 状态不正确", outTradeNo));
    }

    @Override
    @SuppressWarnings("PatternVariableCanBeUsed")
    protected PayRefundRespDTO doUnifiedRefund(PayRefundUnifiedReqDTO reqDTO) {
        try {
            if (refundService == null) {
                refundService = SpringUtil.getBean(PayRefundService.class);
            }
            PayRefundDO payRefund = refundService.getRefundByNo(reqDTO.getOutRefundNo());
            if (payRefund == null) {
                throw com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception(REFUND_NOT_FOUND);
            }
            PayWalletTransaction payWalletTransaction = payWalletApplicationService.refundPayment(payRefund.getId(),
                    payRefund.getChannelOrderNo(), reqDTO.getRefundPrice());
            return PayRefundRespDTO.successOf(payWalletTransaction.no(), payWalletTransaction.createTime(),
                    reqDTO.getOutRefundNo(), payWalletTransaction);
        } catch (Throwable ex) {
            log.error("[doUnifiedRefund][reqDOT({}) 异常]", reqDTO, ex);
            Integer errorCode = INTERNAL_SERVER_ERROR.getCode();
            String errorMsg = INTERNAL_SERVER_ERROR.getMsg();
            if (ex instanceof ServiceException) {
                ServiceException serviceException = (ServiceException) ex;
                errorCode =  serviceException.getCode();
                errorMsg = serviceException.getMessage();
            }
            return PayRefundRespDTO.failureOf(String.valueOf(errorCode), errorMsg,
                    reqDTO.getOutRefundNo(), "");
        }
    }

    @Override
    protected PayRefundRespDTO doParseRefundNotify(Map<String, String> params, String body, Map<String, String> headers) {
        throw new UnsupportedOperationException("钱包支付无退款回调");
    }

    @Override
    protected PayRefundRespDTO doGetRefund(String outTradeNo, String outRefundNo) {
        if (refundService == null) {
            refundService = SpringUtil.getBean(PayRefundService.class);
        }
        PayRefundDO payRefund = refundService.getRefundByNo(outRefundNo);
        // 支付退款单不存在， 返回退款失败状态
        if (payRefund == null) {
            return PayRefundRespDTO.failureOf(String.valueOf(REFUND_NOT_FOUND), REFUND_NOT_FOUND.getMsg(),
                    outRefundNo, "");
        }
        // 退款失败
        if (PayRefundStatusEnum.isFailure(payRefund.getStatus())) {
            return PayRefundRespDTO.failureOf(payRefund.getChannelErrorCode(), payRefund.getChannelErrorMsg(),
                    outRefundNo, "");
        }
        // 退款成功
        if (PayRefundStatusEnum.isSuccess(payRefund.getStatus())) {
            PayWalletTransaction walletTransaction = transactionApplicationService.getByBiz(
                    String.valueOf(payRefund.getId()), PayWalletBizTypeEnum.PAYMENT_REFUND);
            Assert.notNull(walletTransaction, "支付退款单 {} 钱包流水不能为空", outRefundNo);
            return PayRefundRespDTO.successOf(walletTransaction.no(), walletTransaction.createTime(),
                    outRefundNo, walletTransaction);
        }
        // 其它状态为无效状态
        log.error("[doGetRefund] 支付退款单 {} 的状态不正确", outRefundNo);
        throw new IllegalStateException(String.format("支付退款单[%s] 状态不正确", outRefundNo));
    }

    @Override
    @SuppressWarnings("PatternVariableCanBeUsed")
    public PayTransferRespDTO doUnifiedTransfer(PayTransferUnifiedReqDTO reqDTO) {
        try {
            Long walletId = Long.parseLong(reqDTO.getUserAccount());
            PayWalletTransaction transaction = payWalletApplicationService.addBalance(walletId, reqDTO.getOutTransferNo(),
                    PayWalletBizTypeEnum.TRANSFER.getType(), reqDTO.getPrice(), null);
            return PayTransferRespDTO.successOf(transaction.no(), transaction.createTime(),
                    reqDTO.getOutTransferNo(), transaction);
        } catch (Throwable ex) {
            log.error("[doUnifiedTransfer][reqDTO({}) 异常]", reqDTO, ex);
            Integer errorCode = INTERNAL_SERVER_ERROR.getCode();
            String errorMsg = INTERNAL_SERVER_ERROR.getMsg();
            if (ex instanceof ServiceException) {
                ServiceException serviceException = (ServiceException) ex;
                errorCode = serviceException.getCode();
                errorMsg = serviceException.getMessage();
            }
            return PayTransferRespDTO.closedOf(String.valueOf(errorCode), errorMsg,
                    reqDTO.getOutTransferNo(), "");
        }
    }

    @Override
    protected PayTransferRespDTO doParseTransferNotify(Map<String, String> params, String body, Map<String, String> headers) {
        throw new UnsupportedOperationException("钱包支付无转账回调");
    }

    @Override
    protected PayTransferRespDTO doGetTransfer(String outTradeNo) {
        if (transferService == null) {
            transferService = SpringUtil.getBean(PayTransferService.class);
        }
        // 获取转账单
        PayTransferDO transfer = transferService.getTransferByNo(outTradeNo);
        // 转账单不存在，返回关闭状态
        if (transfer == null) {
            return PayTransferRespDTO.closedOf(String.valueOf(PAY_ORDER_EXTENSION_NOT_FOUND.getCode()),
                    PAY_ORDER_EXTENSION_NOT_FOUND.getMsg(), outTradeNo, "");
        }
        // 关闭状态
        if (PayTransferStatusEnum.isClosed(transfer.getStatus())) {
            return PayTransferRespDTO.closedOf(transfer.getChannelErrorCode(),
                    transfer.getChannelErrorMsg(), outTradeNo, "");
        }
        // 成功状态
        if (PayTransferStatusEnum.isSuccess(transfer.getStatus())) {
            PayWalletTransaction walletTransaction = transactionApplicationService.getByBiz(
                    String.valueOf(transfer.getId()), PayWalletBizTypeEnum.TRANSFER);
            Assert.notNull(walletTransaction, "转账单 {} 钱包流水不能为空", outTradeNo);
            return PayTransferRespDTO.successOf(walletTransaction.no(), walletTransaction.createTime(),
                    outTradeNo, walletTransaction);
        }
        // 处理中状态
        if (PayTransferStatusEnum.isProcessing(transfer.getStatus())) {
            return PayTransferRespDTO.processingOf(transfer.getChannelTransferNo(),
                    outTradeNo, transfer);
        }
        // 等待状态
        if (PayTransferStatusEnum.isWaiting(transfer.getStatus())) {
            return PayTransferRespDTO.waitingOf(transfer.getChannelTransferNo(),
                    outTradeNo, transfer);
        }
        // 其它状态为无效状态
        log.error("[doGetTransfer] 转账单 {} 的状态不正确", outTradeNo);
        throw new IllegalStateException(String.format("转账单[%s] 状态不正确", outTradeNo));
    }

}
