package com.develop.mvp.pk.module.statistics.domain.tradestatistics.repository;

import com.develop.mvp.pk.module.statistics.domain.tradestatistics.TradeStatistics;
import com.develop.mvp.pk.module.statistics.domain.tradestatistics.valueobject.TradeStatisticsId;
import java.time.LocalDate;
import java.util.List;

public interface TradeStatisticsRepository {
    TradeStatistics save(TradeStatistics stats);
    TradeStatistics findById(TradeStatisticsId id);
    TradeStatistics findByDate(LocalDate date);
    List<TradeStatistics> findByDateBetween(LocalDate start, LocalDate end);
}
