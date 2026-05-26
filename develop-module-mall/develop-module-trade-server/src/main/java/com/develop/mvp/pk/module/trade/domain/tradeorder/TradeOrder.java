package com.develop.mvp.pk.module.trade.domain.tradeorder;

// Skill: AggregateRoot_TradeOrder_Validation_Skill — 聚合根 TradeOrder
// DDD 角色：交易订单聚合根，封装订单完整生命周期和状态流转
// 验收标准 AC01/AC02：无 MyBatis/Spring 注解

import com.develop.mvp.pk.module.trade.domain.tradeorder.event.TradeOrderCreatedEvent;
import com.develop.mvp.pk.module.trade.domain.tradeorder.event.TradeOrderStatusChangedEvent;
import com.develop.mvp.pk.module.trade.domain.tradeorder.valueobject.OrderItem;
import com.develop.mvp.pk.module.trade.domain.tradeorder.valueobject.TradeOrderId;
import com.develop.mvp.pk.module.trade.enums.order.TradeOrderCancelTypeEnum;
import com.develop.mvp.pk.module.trade.enums.order.TradeOrderRefundStatusEnum;
import com.develop.mvp.pk.module.trade.enums.order.TradeOrderStatusEnum;
import com.develop.mvp.pk.module.trade.domain.event.DomainEvent;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public final class TradeOrder {

    public static final Long LOGISTICS_ID_NULL = 0L;

    private final TradeOrderId id;
    private String no;
    private Integer type;
    private Integer terminal;
    private Long userId;
    private String userIp;
    private String userRemark;
    private Integer status;
    private Integer productCount;
    private LocalDateTime finishTime;
    private LocalDateTime cancelTime;
    private Integer cancelType;
    private String remark;
    private Boolean commentStatus;
    private Long brokerageUserId;

    // 价格
    private Long payOrderId;
    private Boolean payStatus;
    private LocalDateTime payTime;
    private String payChannelCode;
    private Integer totalPrice;
    private Integer discountPrice;
    private Integer deliveryPrice;
    private Integer adjustPrice;
    private Integer payPrice;

    // 物流
    private Integer deliveryType;
    private Long logisticsId;
    private String logisticsNo;
    private LocalDateTime deliveryTime;
    private LocalDateTime receiveTime;
    private String receiverName;
    private String receiverMobile;
    private Integer receiverAreaId;
    private String receiverDetailAddress;
    private Long pickUpStoreId;
    private String pickUpVerifyCode;

    // 售后
    private Integer refundStatus;
    private Integer refundPrice;

    // 营销
    private Long couponId;
    private Integer couponPrice;
    private Integer usePoint;
    private Integer pointPrice;
    private Integer givePoint;
    private Integer refundPoint;
    private Integer vipPrice;
    private Map<Long, Integer> giveCouponTemplateCounts;
    private List<Long> giveCouponIds;
    private Long seckillActivityId;
    private Long bargainActivityId;
    private Long bargainRecordId;
    private Long combinationActivityId;
    private Long combinationHeadId;
    private Long combinationRecordId;
    private Long pointActivityId;

    // 订单项
    private List<OrderItem> items;

    private final List<DomainEvent> events = new ArrayList<>();

    // Package-private constructor - use Factory
    TradeOrder(TradeOrderId id, String no, Integer type, Integer terminal, Long userId,
               String userIp, String userRemark, Integer status, Integer productCount,
               LocalDateTime finishTime, LocalDateTime cancelTime, Integer cancelType,
               String remark, Boolean commentStatus, Long brokerageUserId,
               Long payOrderId, Boolean payStatus, LocalDateTime payTime, String payChannelCode,
               Integer totalPrice, Integer discountPrice, Integer deliveryPrice, Integer adjustPrice,
               Integer payPrice, Integer deliveryType, Long logisticsId, String logisticsNo,
               LocalDateTime deliveryTime, LocalDateTime receiveTime,
               String receiverName, String receiverMobile, Integer receiverAreaId,
               String receiverDetailAddress, Long pickUpStoreId, String pickUpVerifyCode,
               Integer refundStatus, Integer refundPrice,
               Long couponId, Integer couponPrice, Integer usePoint, Integer pointPrice,
               Integer givePoint, Integer refundPoint, Integer vipPrice,
               Map<Long, Integer> giveCouponTemplateCounts, List<Long> giveCouponIds,
               Long seckillActivityId, Long bargainActivityId, Long bargainRecordId,
               Long combinationActivityId, Long combinationHeadId, Long combinationRecordId,
               Long pointActivityId, List<OrderItem> items) {
        this.id = id;
        this.no = no; this.type = type; this.terminal = terminal;
        this.userId = Objects.requireNonNull(userId);
        this.userIp = userIp; this.userRemark = userRemark;
        this.status = status != null ? status : TradeOrderStatusEnum.UNPAID.getStatus();
        this.productCount = productCount; this.finishTime = finishTime;
        this.cancelTime = cancelTime; this.cancelType = cancelType; this.remark = remark;
        this.commentStatus = commentStatus; this.brokerageUserId = brokerageUserId;
        this.payOrderId = payOrderId; this.payStatus = payStatus; this.payTime = payTime;
        this.payChannelCode = payChannelCode;
        this.totalPrice = totalPrice; this.discountPrice = discountPrice;
        this.deliveryPrice = deliveryPrice; this.adjustPrice = adjustPrice;
        this.payPrice = Objects.requireNonNull(payPrice, "支付金额不能为空");
        this.deliveryType = deliveryType; this.logisticsId = logisticsId; this.logisticsNo = logisticsNo;
        this.deliveryTime = deliveryTime; this.receiveTime = receiveTime;
        this.receiverName = receiverName; this.receiverMobile = receiverMobile;
        this.receiverAreaId = receiverAreaId; this.receiverDetailAddress = receiverDetailAddress;
        this.pickUpStoreId = pickUpStoreId; this.pickUpVerifyCode = pickUpVerifyCode;
        this.refundStatus = refundStatus != null ? refundStatus : TradeOrderRefundStatusEnum.NONE.getStatus();
        this.refundPrice = refundPrice != null ? refundPrice : 0;
        this.couponId = couponId; this.couponPrice = couponPrice;
        this.usePoint = usePoint; this.pointPrice = pointPrice; this.givePoint = givePoint;
        this.refundPoint = refundPoint; this.vipPrice = vipPrice;
        this.giveCouponTemplateCounts = giveCouponTemplateCounts;
        this.giveCouponIds = giveCouponIds;
        this.seckillActivityId = seckillActivityId; this.bargainActivityId = bargainActivityId;
        this.bargainRecordId = bargainRecordId; this.combinationActivityId = combinationActivityId;
        this.combinationHeadId = combinationHeadId; this.combinationRecordId = combinationRecordId;
        this.pointActivityId = pointActivityId;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
    }

    // ── 业务方法 ──

    /** 支付成功 */
    public void paySuccess(Long payOrderId, String payChannelCode, LocalDateTime payTime) {
        if (!TradeOrderStatusEnum.UNPAID.getStatus().equals(this.status)) {
            throw new IllegalStateException("只有未支付订单才能支付");
        }
        Integer oldStatus = this.status;
        this.payOrderId = payOrderId;
        this.payStatus = true;
        this.payChannelCode = payChannelCode;
        this.payTime = payTime;
        this.status = TradeOrderStatusEnum.UNDELIVERED.getStatus();
        events.add(new TradeOrderStatusChangedEvent(this.id.value(), this.no, oldStatus, this.status));
    }

    /** 发货 */
    public void deliver(Long logisticsId, String logisticsNo, LocalDateTime deliveryTime) {
        if (!TradeOrderStatusEnum.UNDELIVERED.getStatus().equals(this.status)) {
            throw new IllegalStateException("只有待发货订单才能发货");
        }
        Integer oldStatus = this.status;
        this.logisticsId = logisticsId;
        this.logisticsNo = logisticsNo;
        this.deliveryTime = deliveryTime;
        this.status = TradeOrderStatusEnum.DELIVERED.getStatus();
        events.add(new TradeOrderStatusChangedEvent(this.id.value(), this.no, oldStatus, this.status));
    }

    /** 确认收货 */
    public void receive(LocalDateTime receiveTime) {
        if (!TradeOrderStatusEnum.DELIVERED.getStatus().equals(this.status)) {
            throw new IllegalStateException("只有已发货订单才能确认收货");
        }
        Integer oldStatus = this.status;
        this.receiveTime = receiveTime;
        this.status = TradeOrderStatusEnum.COMPLETED.getStatus();
        this.finishTime = receiveTime;
        events.add(new TradeOrderStatusChangedEvent(this.id.value(), this.no, oldStatus, this.status));
    }

    /** 取消订单 */
    public void cancel(Integer cancelType, LocalDateTime cancelTime) {
        if (TradeOrderStatusEnum.COMPLETED.getStatus().equals(this.status) ||
            TradeOrderStatusEnum.CANCELED.getStatus().equals(this.status)) {
            throw new IllegalStateException("已完成或已取消的订单不能取消");
        }
        Integer oldStatus = this.status;
        this.cancelType = cancelType;
        this.cancelTime = cancelTime;
        this.status = TradeOrderStatusEnum.CANCELED.getStatus();
        events.add(new TradeOrderStatusChangedEvent(this.id.value(), this.no, oldStatus, this.status));
    }

    /** 更新收货地址 */
    public void updateReceiver(String receiverName, String receiverMobile, Integer receiverAreaId,
                                String receiverDetailAddress) {
        if (!TradeOrderStatusEnum.UNDELIVERED.getStatus().equals(this.status)) {
            throw new IllegalStateException("只有待发货订单才能修改收货地址");
        }
        this.receiverName = receiverName;
        this.receiverMobile = receiverMobile;
        this.receiverAreaId = receiverAreaId;
        this.receiverDetailAddress = receiverDetailAddress;
    }

    /** 更新备注 */
    public void updateRemark(String remark) {
        this.remark = remark;
    }

    /** 设置推广人 */
    public void setBrokerageUserId(Long brokerageUserId) {
        this.brokerageUserId = brokerageUserId;
    }

    // ── 查询方法 ──

    public TradeOrderId id() { return id; }
    public String no() { return no; }
    public Integer type() { return type; }
    public Integer terminal() { return terminal; }
    public Long userId() { return userId; }
    public String userIp() { return userIp; }
    public String userRemark() { return userRemark; }
    public Integer status() { return status; }
    public Integer productCount() { return productCount; }
    public LocalDateTime finishTime() { return finishTime; }
    public LocalDateTime cancelTime() { return cancelTime; }
    public Integer cancelType() { return cancelType; }
    public String remark() { return remark; }
    public Boolean commentStatus() { return commentStatus; }
    public Long brokerageUserId() { return brokerageUserId; }
    public Long payOrderId() { return payOrderId; }
    public Boolean payStatus() { return payStatus; }
    public LocalDateTime payTime() { return payTime; }
    public String payChannelCode() { return payChannelCode; }
    public Integer totalPrice() { return totalPrice; }
    public Integer discountPrice() { return discountPrice; }
    public Integer deliveryPrice() { return deliveryPrice; }
    public Integer adjustPrice() { return adjustPrice; }
    public Integer payPrice() { return payPrice; }
    public Integer deliveryType() { return deliveryType; }
    public Long logisticsId() { return logisticsId; }
    public String logisticsNo() { return logisticsNo; }
    public LocalDateTime deliveryTime() { return deliveryTime; }
    public LocalDateTime receiveTime() { return receiveTime; }
    public String receiverName() { return receiverName; }
    public String receiverMobile() { return receiverMobile; }
    public Integer receiverAreaId() { return receiverAreaId; }
    public String receiverDetailAddress() { return receiverDetailAddress; }
    public Long pickUpStoreId() { return pickUpStoreId; }
    public String pickUpVerifyCode() { return pickUpVerifyCode; }
    public Integer refundStatus() { return refundStatus; }
    public Integer refundPrice() { return refundPrice; }
    public Long couponId() { return couponId; }
    public Integer couponPrice() { return couponPrice; }
    public Integer usePoint() { return usePoint; }
    public Integer pointPrice() { return pointPrice; }
    public Integer givePoint() { return givePoint; }
    public Integer refundPoint() { return refundPoint; }
    public Integer vipPrice() { return vipPrice; }
    public Map<Long, Integer> giveCouponTemplateCounts() { return giveCouponTemplateCounts; }
    public List<Long> giveCouponIds() { return giveCouponIds; }
    public Long seckillActivityId() { return seckillActivityId; }
    public Long bargainActivityId() { return bargainActivityId; }
    public Long bargainRecordId() { return bargainRecordId; }
    public Long combinationActivityId() { return combinationActivityId; }
    public Long combinationHeadId() { return combinationHeadId; }
    public Long combinationRecordId() { return combinationRecordId; }
    public Long pointActivityId() { return pointActivityId; }
    public List<OrderItem> items() { return Collections.unmodifiableList(items); }

    public boolean isUnpaid() { return TradeOrderStatusEnum.UNPAID.getStatus().equals(status); }
    public boolean isUndelivered() { return TradeOrderStatusEnum.UNDELIVERED.getStatus().equals(status); }
    public boolean isDelivered() { return TradeOrderStatusEnum.DELIVERED.getStatus().equals(status); }
    public boolean isCompleted() { return TradeOrderStatusEnum.COMPLETED.getStatus().equals(status); }
    public boolean isCanceled() { return TradeOrderStatusEnum.CANCELED.getStatus().equals(status); }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> result = new ArrayList<>(events);
        events.clear();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TradeOrder that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() { return "TradeOrder{id=" + id + ", no='" + no + "'}"; }
}
