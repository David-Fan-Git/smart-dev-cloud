package com.develop.mvp.pk.module.pay.domain.refund;
// DDD 角色：退款单聚合根 - AggregateRoot_Pay_Skill
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
@Getter
public final class PayRefund {
    private final Long id; private final String no;
    private Long appId; private Long channelId; private String channelCode;
    private Long orderId; private String orderNo;
    private Long userId; private Integer userType;
    private String merchantOrderId; private String merchantRefundId; private String notifyUrl;
    private Integer status; private Integer payPrice; private Integer refundPrice;
    private String reason; private String userIp;
    private String channelOrderNo; private String channelRefundNo;
    private LocalDateTime successTime;
    private String channelErrorCode; private String channelErrorMsg; private String channelNotifyData;
    public PayRefund(Long id, String no) { this.id = id; this.no = Objects.requireNonNull(no); }
    public static PayRefund of(Long id, String no) { return new PayRefund(id, no); }
    public Long id() { return id; } public String no() { return no; }
    public Long appId() { return appId; } public Long channelId() { return channelId; }
    public String channelCode() { return channelCode; } public Long orderId() { return orderId; }
    public String orderNo() { return orderNo; } public Long userId() { return userId; }
    public Integer userType() { return userType; }
    public String merchantOrderId() { return merchantOrderId; }
    public String merchantRefundId() { return merchantRefundId; }
    public String notifyUrl() { return notifyUrl; }
    public Integer status() { return status; } public Integer payPrice() { return payPrice; }
    public Integer refundPrice() { return refundPrice; }
    public String reason() { return reason; } public String userIp() { return userIp; }
    public String channelOrderNo() { return channelOrderNo; }
    public String channelRefundNo() { return channelRefundNo; }
    public LocalDateTime successTime() { return successTime; }
    public String channelErrorCode() { return channelErrorCode; }
    public String channelErrorMsg() { return channelErrorMsg; }
    public PayRefund appId(Long v) { appId = v; return this; } public PayRefund channelId(Long v) { channelId = v; return this; }
    public PayRefund channelCode(String v) { channelCode = v; return this; } public PayRefund orderId(Long v) { orderId = v; return this; }
    public PayRefund orderNo(String v) { orderNo = v; return this; } public PayRefund userId(Long v) { userId = v; return this; }
    public PayRefund userType(Integer v) { userType = v; return this; }
    public PayRefund merchantOrderId(String v) { merchantOrderId = v; return this; }
    public PayRefund merchantRefundId(String v) { merchantRefundId = v; return this; }
    public PayRefund notifyUrl(String v) { notifyUrl = v; return this; }
    public PayRefund status(Integer v) { status = v; return this; } public PayRefund payPrice(Integer v) { payPrice = v; return this; }
    public PayRefund refundPrice(Integer v) { refundPrice = v; return this; }
    public PayRefund reason(String v) { reason = v; return this; } public PayRefund userIp(String v) { userIp = v; return this; }
    public PayRefund channelOrderNo(String v) { channelOrderNo = v; return this; }
    public PayRefund channelRefundNo(String v) { channelRefundNo = v; return this; }
    public PayRefund successTime(LocalDateTime v) { successTime = v; return this; }
    public PayRefund channelErrorCode(String v) { channelErrorCode = v; return this; }
    public PayRefund channelErrorMsg(String v) { channelErrorMsg = v; return this; }
    public PayRefund channelNotifyData(String v) { channelNotifyData = v; return this; }
    public boolean isWaiting() { return Objects.equals(status, 0); }
    public boolean isSuccess() { return Objects.equals(status, 10); }
    public boolean isFailure() { return Objects.equals(status, 20); }
    public void markSuccess(LocalDateTime time, String channelRefundNo) {
        status = 10; successTime = time; this.channelRefundNo = channelRefundNo;
    }
    public void markFailure(String errorCode, String errorMsg) {
        status = 20; channelErrorCode = errorCode; channelErrorMsg = errorMsg;
    }
    @Override public boolean equals(Object o) { return o instanceof PayRefund r && id.equals(r.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
