package com.develop.mvp.pk.module.statistics.application.tradestatistics;

import com.develop.mvp.pk.module.statistics.domain.tradestatistics.TradeStatistics;
import com.develop.mvp.pk.module.statistics.domain.tradestatistics.TradeStatisticsFactory;
import com.develop.mvp.pk.module.statistics.domain.tradestatistics.repository.TradeStatisticsRepository;
import com.develop.mvp.pk.module.statistics.domain.tradestatistics.valueobject.TradeStatisticsId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TradeStatisticsApplicationService {

    private final TradeStatisticsRepository tradeStatisticsRepository;

    public TradeStatisticsApplicationService(TradeStatisticsRepository tradeStatisticsRepository) {
        this.tradeStatisticsRepository = tradeStatisticsRepository;
    }

    @Transactional
    public void recordOrder(LocalDate date, int count, int payCount, int payPrice) {
        TradeStatistics stats = getOrCreate(date);
        stats.recordOrder(count, payCount, payPrice);
        tradeStatisticsRepository.save(stats);
    }

    @Transactional
    public void recordRefund(LocalDate date, int count, int price) {
        TradeStatistics stats = getOrCreate(date);
        stats.recordRefund(count, price);
        tradeStatisticsRepository.save(stats);
    }

    @Transactional
    public void recordBrokerageSettlement(LocalDate date, int price) {
        TradeStatistics stats = getOrCreate(date);
        stats.recordBrokerageSettlement(price);
        tradeStatisticsRepository.save(stats);
    }

    public TradeStatistics getByDate(LocalDate date) {
        return tradeStatisticsRepository.findByDate(date);
    }

    public List<TradeStatistics> getByDateRange(LocalDate start, LocalDate end) {
        return tradeStatisticsRepository.findByDateBetween(start, end);
    }

    private TradeStatistics getOrCreate(LocalDate date) {
        TradeStatistics stats = tradeStatisticsRepository.findByDate(date);
        if (stats == null) {
            stats = TradeStatisticsFactory.create(null, date);
        }
        return stats;
    }
}
