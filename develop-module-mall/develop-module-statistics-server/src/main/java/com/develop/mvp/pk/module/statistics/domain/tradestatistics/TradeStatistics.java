package com.develop.mvp.pk.module.statistics.domain.tradestatistics;

// Skill: AggregateRoot_TradeStatistics_Validation_Skill — 聚合根 TradeStatistics
// DDD 角色：交易统计聚合根，封装交易维度的统计数据
// 验收标准 AC01/AC02：无 MyBatis/Spring 注解

import com.develop.mvp.pk.module.statistics.domain.tradestatistics.valueobject.TradeStatisticsId;
import com.develop.mvp.pk.module.statistics.domain.event.DomainEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class TradeStatistics {

    private final TradeStatisticsId id;
    private LocalDate date;
    private Integer orderCount;
    private Integer orderPayCount;
    private Integer orderPayPrice;
    private Integer refundCount;
    private Integer refundPrice;
    private Integer brokerageSettlementPrice;
    private final List<DomainEvent> events = new ArrayList<>();

    TradeStatistics(TradeStatisticsId id, LocalDate date, Integer orderCount, Integer orderPayCount,
                    Integer orderPayPrice, Integer refundCount, Integer refundPrice,
                    Integer brokerageSettlementPrice) {
        this.id = id;
        this.date = Objects.requireNonNull(date);
        this.orderCount = orderCount != null ? orderCount : 0;
        this.orderPayCount = orderPayCount != null ? orderPayCount : 0;
        this.orderPayPrice = orderPayPrice != null ? orderPayPrice : 0;
        this.refundCount = refundCount != null ? refundCount : 0;
        this.refundPrice = refundPrice != null ? refundPrice : 0;
        this.brokerageSettlementPrice = brokerageSettlementPrice != null ? brokerageSettlementPrice : 0;
    }

    public void recordOrder(int count, int payCount, int payPrice) {
        this.orderCount += count;
        this.orderPayCount += payCount;
        this.orderPayPrice += payPrice;
    }

    public void recordRefund(int count, int price) {
        this.refundCount += count;
        this.refundPrice += price;
    }

    public void recordBrokerageSettlement(int price) {
        this.brokerageSettlementPrice += price;
    }

    public TradeStatisticsId id() { return id; }
    public LocalDate date() { return date; }
    public Integer orderCount() { return orderCount; }
    public Integer orderPayCount() { return orderPayCount; }
    public Integer orderPayPrice() { return orderPayPrice; }
    public Integer refundCount() { return refundCount; }
    public Integer refundPrice() { return refundPrice; }
    public Integer brokerageSettlementPrice() { return brokerageSettlementPrice; }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> result = new ArrayList<>(events);
        events.clear();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TradeStatistics that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
