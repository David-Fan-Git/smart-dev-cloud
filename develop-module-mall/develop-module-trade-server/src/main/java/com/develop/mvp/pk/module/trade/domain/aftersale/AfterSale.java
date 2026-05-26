package com.develop.mvp.pk.module.trade.domain.aftersale;

// Skill: AggregateRoot_AfterSale_Validation_Skill — 聚合根 AfterSale
// DDD 角色：售后聚合根，封装售后申请和处理生命周期
// 验收标准 AC01/AC02：无 MyBatis/Spring 注解

import com.develop.mvp.pk.module.trade.domain.aftersale.valueobject.AfterSaleId;
import com.develop.mvp.pk.module.trade.domain.event.DomainEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class AfterSale {

    private final AfterSaleId id;
    private String no;
    private Long userId;
    private Long orderId;
    private Long orderItemId;
    private Long spuId;
    private Long skuId;
    private Integer count;
    private Integer type;       // 售后类型：1-退款，2-退货退款
    private String reason;
    private String description;
    private String[] proofPictures;
    private Integer status;     // 售后状态
    private Integer refundPrice;
    private String rejectReason;
    private String payChannelCode;
    private Long payRefundId;
    private LocalDateTime auditTime;
    private LocalDateTime refuseTime;
    private LocalDateTime refundTime;
    private final List<DomainEvent> events = new ArrayList<>();

    AfterSale(AfterSaleId id, String no, Long userId, Long orderId, Long orderItemId,
              Long spuId, Long skuId, Integer count, Integer type, String reason,
              String description, String[] proofPictures, Integer status, Integer refundPrice,
              String rejectReason, String payChannelCode, Long payRefundId,
              LocalDateTime auditTime, LocalDateTime refuseTime, LocalDateTime refundTime) {
        this.id = id;
        this.no = no; this.userId = Objects.requireNonNull(userId);
        this.orderId = Objects.requireNonNull(orderId);
        this.orderItemId = Objects.requireNonNull(orderItemId);
        this.spuId = spuId; this.skuId = skuId;
        this.count = Objects.requireNonNull(count);
        this.type = Objects.requireNonNull(type);
        this.reason = Objects.requireNonNull(reason);
        this.description = description; this.proofPictures = proofPictures;
        this.status = status; this.refundPrice = refundPrice;
        this.rejectReason = rejectReason; this.payChannelCode = payChannelCode;
        this.payRefundId = payRefundId; this.auditTime = auditTime;
        this.refuseTime = refuseTime; this.refundTime = refundTime;
    }

    /** 审核通过 */
    public void approve(String payChannelCode, LocalDateTime auditTime) {
        this.status = 10; // APPROVING
        this.payChannelCode = payChannelCode;
        this.auditTime = auditTime;
    }

    /** 审核拒绝 */
    public void reject(String rejectReason, LocalDateTime refuseTime) {
        this.status = 20; // REJECTED
        this.rejectReason = rejectReason;
        this.refuseTime = refuseTime;
    }

    /** 退款完成 */
    public void refundComplete(Long payRefundId, LocalDateTime refundTime) {
        this.status = 30; // COMPLETED
        this.payRefundId = payRefundId;
        this.refundTime = refundTime;
    }

    public AfterSaleId id() { return id; }
    public String no() { return no; }
    public Long userId() { return userId; }
    public Long orderId() { return orderId; }
    public Long orderItemId() { return orderItemId; }
    public Long spuId() { return spuId; }
    public Long skuId() { return skuId; }
    public Integer count() { return count; }
    public Integer type() { return type; }
    public String reason() { return reason; }
    public String description() { return description; }
    public String[] proofPictures() { return proofPictures; }
    public Integer status() { return status; }
    public Integer refundPrice() { return refundPrice; }
    public String rejectReason() { return rejectReason; }
    public String payChannelCode() { return payChannelCode; }
    public Long payRefundId() { return payRefundId; }
    public LocalDateTime auditTime() { return auditTime; }
    public LocalDateTime refuseTime() { return refuseTime; }
    public LocalDateTime refundTime() { return refundTime; }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> result = new ArrayList<>(events);
        events.clear();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AfterSale that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() { return "AfterSale{id=" + id + ", no='" + no + "'}"; }
}
