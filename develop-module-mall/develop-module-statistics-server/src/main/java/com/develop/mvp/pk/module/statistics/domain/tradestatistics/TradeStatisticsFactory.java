package com.develop.mvp.pk.module.statistics.domain.tradestatistics;

import com.develop.mvp.pk.module.statistics.domain.tradestatistics.valueobject.TradeStatisticsId;
import java.time.LocalDate;

public final class TradeStatisticsFactory {
    private TradeStatisticsFactory() {}

    public static TradeStatistics create(Long id, LocalDate date) {
        return new TradeStatistics(id != null ? TradeStatisticsId.of(id) : null, date, 0, 0, 0, 0, 0, 0);
    }

    public static TradeStatistics reconstitute(Long id, LocalDate date,
                                                Integer orderCount, Integer orderPayCount,
                                                Integer orderPayPrice, Integer refundCount,
                                                Integer refundPrice, Integer brokerageSettlementPrice) {
        return new TradeStatistics(TradeStatisticsId.of(id), date, orderCount, orderPayCount,
                orderPayPrice, refundCount, refundPrice, brokerageSettlementPrice);
    }
}
