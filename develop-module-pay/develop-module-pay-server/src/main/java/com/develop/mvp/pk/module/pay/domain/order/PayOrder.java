package com.develop.mvp.pk.module.pay.domain.order;
// DDD 角色：支付订单聚合根 - AggregateRoot_Pay_Skill
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
@Getter
public final class PayOrder {
    private final Long id; private final String no;
    private Long appId; private Long channelId; private String channelCode;
    private Long userId; private Integer userType;
    private String merchantOrderId; private String subject; private String body; private String notifyUrl;
    private String userIp; private Integer price;
    private Double channelFeeRate; private Integer channelFeePrice;
    private Integer status; private LocalDateTime expireTime; private LocalDateTime successTime;
    private Long extensionId; private Integer refundPrice;
    private String channelUserId; private String channelOrderNo;
    public PayOrder(Long id, String no) { this.id = id; this.no = Objects.requireNonNull(no); }
    public static PayOrder of(Long id, String no) { return new PayOrder(id, no); }
    public Long id() { return id; } public String no() { return no; }
    public Long appId() { return appId; } public Long channelId() { return channelId; }
    public String channelCode() { return channelCode; } public Long userId() { return userId; }
    public Integer userType() { return userType; }
    public String merchantOrderId() { return merchantOrderId; } public String subject() { return subject; }
    public String body() { return body; } public String notifyUrl() { return notifyUrl; }
    public String userIp() { return userIp; } public Integer price() { return price; }
    public Double channelFeeRate() { return channelFeeRate; } public Integer channelFeePrice() { return channelFeePrice; }
    public Integer status() { return status; } public LocalDateTime expireTime() { return expireTime; }
    public LocalDateTime successTime() { return successTime; } public Long extensionId() { return extensionId; }
    public Integer refundPrice() { return refundPrice; } public String channelUserId() { return channelUserId; }
    public String channelOrderNo() { return channelOrderNo; }
    public PayOrder appId(Long v) { appId = v; return this; } public PayOrder channelId(Long v) { channelId = v; return this; }
    public PayOrder channelCode(String v) { channelCode = v; return this; } public PayOrder userId(Long v) { userId = v; return this; }
    public PayOrder userType(Integer v) { userType = v; return this; }
    public PayOrder merchantOrderId(String v) { merchantOrderId = v; return this; } public PayOrder subject(String v) { subject = v; return this; }
    public PayOrder body(String v) { body = v; return this; } public PayOrder notifyUrl(String v) { notifyUrl = v; return this; }
    public PayOrder userIp(String v) { userIp = v; return this; } public PayOrder price(Integer v) { price = v; return this; }
    public PayOrder channelFeeRate(Double v) { channelFeeRate = v; return this; } public PayOrder channelFeePrice(Integer v) { channelFeePrice = v; return this; }
    public PayOrder status(Integer v) { status = v; return this; } public PayOrder expireTime(LocalDateTime v) { expireTime = v; return this; }
    public PayOrder successTime(LocalDateTime v) { successTime = v; return this; } public PayOrder extensionId(Long v) { extensionId = v; return this; }
    public PayOrder refundPrice(Integer v) { refundPrice = v; return this; } public PayOrder channelUserId(String v) { channelUserId = v; return this; }
    public PayOrder channelOrderNo(String v) { channelOrderNo = v; return this; }
    public boolean isWaiting() { return Objects.equals(status, 0); }
    public boolean isPaid() { return Objects.equals(status, 10); }
    public boolean isRefunded() { return Objects.equals(status, 20); }
    public boolean isClosed() { return Objects.equals(status, 30); }
    public void markPaid() { status = 10; }
    public void markClosed() { status = 30; }
    public void markRefund(Integer incrRefund) { refundPrice = (refundPrice == null ? 0 : refundPrice) + incrRefund; status = 20; }
    @Override public boolean equals(Object o) { return o instanceof PayOrder or && id.equals(or.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
