package com.develop.mvp.pk.module.pay.domain.transfer;
// DDD 角色：转账单聚合根 - AggregateRoot_Pay_Skill
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
@Getter
public final class PayTransfer {
    private final Long id; private final String no;
    private Long appId; private Long channelId; private String channelCode;
    private Long userId; private Integer userType;
    private String merchantTransferId; private String subject;
    private Integer price; private String userAccount; private String userName;
    private Integer status; private LocalDateTime successTime;
    private String notifyUrl; private String userIp;
    private Map<String, String> channelExtras;
    private String channelTransferNo;
    private String channelErrorCode; private String channelErrorMsg;
    private String channelNotifyData; private String channelPackageInfo;
    public PayTransfer(Long id, String no) { this.id = id; this.no = Objects.requireNonNull(no); }
    public static PayTransfer of(Long id, String no) { return new PayTransfer(id, no); }
    public Long id() { return id; } public String no() { return no; }
    public Long appId() { return appId; } public Long channelId() { return channelId; }
    public String channelCode() { return channelCode; } public Long userId() { return userId; }
    public Integer userType() { return userType; }
    public String merchantTransferId() { return merchantTransferId; } public String subject() { return subject; }
    public Integer price() { return price; } public String userAccount() { return userAccount; }
    public String userName() { return userName; } public Integer status() { return status; }
    public LocalDateTime successTime() { return successTime; }
    public String notifyUrl() { return notifyUrl; } public String userIp() { return userIp; }
    public Map<String, String> channelExtras() { return channelExtras; }
    public String channelTransferNo() { return channelTransferNo; }
    public String channelErrorCode() { return channelErrorCode; }
    public String channelErrorMsg() { return channelErrorMsg; }
    public String channelPackageInfo() { return channelPackageInfo; }
    public PayTransfer appId(Long v) { appId = v; return this; } public PayTransfer channelId(Long v) { channelId = v; return this; }
    public PayTransfer channelCode(String v) { channelCode = v; return this; } public PayTransfer userId(Long v) { userId = v; return this; }
    public PayTransfer userType(Integer v) { userType = v; return this; }
    public PayTransfer merchantTransferId(String v) { merchantTransferId = v; return this; }
    public PayTransfer subject(String v) { subject = v; return this; }
    public PayTransfer price(Integer v) { price = v; return this; }
    public PayTransfer userAccount(String v) { userAccount = v; return this; }
    public PayTransfer userName(String v) { userName = v; return this; }
    public PayTransfer status(Integer v) { status = v; return this; }
    public PayTransfer successTime(LocalDateTime v) { successTime = v; return this; }
    public PayTransfer notifyUrl(String v) { notifyUrl = v; return this; }
    public PayTransfer userIp(String v) { userIp = v; return this; }
    public PayTransfer channelExtras(Map<String, String> v) { channelExtras = v; return this; }
    public PayTransfer channelTransferNo(String v) { channelTransferNo = v; return this; }
    public PayTransfer channelErrorCode(String v) { channelErrorCode = v; return this; }
    public PayTransfer channelErrorMsg(String v) { channelErrorMsg = v; return this; }
    public PayTransfer channelNotifyData(String v) { channelNotifyData = v; return this; }
    public PayTransfer channelPackageInfo(String v) { channelPackageInfo = v; return this; }
    public boolean isWaiting() { return Objects.equals(status, 0); }
    public boolean isProcessing() { return Objects.equals(status, 5); }
    public boolean isSuccess() { return Objects.equals(status, 10); }
    public boolean isClosed() { return Objects.equals(status, 20); }
    public boolean isWaitingOrProcessing() { return isWaiting() || isProcessing(); }
    public void markProcessing(String packageInfo) { status = 5; channelPackageInfo = packageInfo; }
    public void markSuccess(LocalDateTime time, String channelTransferNo) {
        status = 10; successTime = time; this.channelTransferNo = channelTransferNo;
    }
    public void markClosed(String errorCode, String errorMsg) {
        status = 20; channelErrorCode = errorCode; channelErrorMsg = errorMsg;
    }
    @Override public boolean equals(Object o) { return o instanceof PayTransfer t && id.equals(t.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
