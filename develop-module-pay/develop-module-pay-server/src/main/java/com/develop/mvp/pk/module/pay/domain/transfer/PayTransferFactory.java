package com.develop.mvp.pk.module.pay.domain.transfer;
// DDD 角色：转账单工厂 - AggregateRoot_Pay_Skill
import java.time.LocalDateTime;
import java.util.Map;
public class PayTransferFactory {
    public static PayTransfer create(Long id, String no, Long appId, Long channelId, String channelCode,
                                      String merchantTransferId, String subject, Integer price,
                                      String userAccount, String userName, String notifyUrl, String userIp) {
        PayTransfer transfer = new PayTransfer(id, no);
        transfer.appId(appId).channelId(channelId).channelCode(channelCode)
                .merchantTransferId(merchantTransferId).subject(subject)
                .price(price).userAccount(userAccount).userName(userName)
                .notifyUrl(notifyUrl).userIp(userIp)
                .status(0); // WAITING
        return transfer;
    }
    public static PayTransfer restore(Long id, String no, Long appId, Long channelId, String channelCode,
                                       Long userId, Integer userType, String merchantTransferId, String subject,
                                       Integer price, String userAccount, String userName, Integer status,
                                       LocalDateTime successTime, String notifyUrl, String userIp,
                                       Map<String, String> channelExtras, String channelTransferNo,
                                       String channelErrorCode, String channelErrorMsg, String channelPackageInfo) {
        PayTransfer transfer = new PayTransfer(id, no);
        transfer.appId(appId).channelId(channelId).channelCode(channelCode)
                .userId(userId).userType(userType)
                .merchantTransferId(merchantTransferId).subject(subject)
                .price(price).userAccount(userAccount).userName(userName)
                .status(status).successTime(successTime)
                .notifyUrl(notifyUrl).userIp(userIp)
                .channelExtras(channelExtras).channelTransferNo(channelTransferNo)
                .channelErrorCode(channelErrorCode).channelErrorMsg(channelErrorMsg)
                .channelPackageInfo(channelPackageInfo);
        return transfer;
    }
}
