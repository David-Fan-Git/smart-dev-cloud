package com.develop.mvp.pk.module.statistics.infrastructure.tradestatistics;

import com.develop.mvp.pk.module.statistics.dal.dataobject.trade.TradeStatisticsDO;
import com.develop.mvp.pk.module.statistics.dal.mysql.trade.TradeStatisticsMapper;
import com.develop.mvp.pk.module.statistics.domain.tradestatistics.TradeStatistics;
import com.develop.mvp.pk.module.statistics.domain.tradestatistics.TradeStatisticsFactory;
import com.develop.mvp.pk.module.statistics.domain.tradestatistics.repository.TradeStatisticsRepository;
import com.develop.mvp.pk.module.statistics.domain.tradestatistics.valueobject.TradeStatisticsId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class TradeStatisticsRepositoryImpl implements TradeStatisticsRepository {

    private final TradeStatisticsMapper tradeStatisticsMapper;

    public TradeStatisticsRepositoryImpl(TradeStatisticsMapper tradeStatisticsMapper) {
        this.tradeStatisticsMapper = tradeStatisticsMapper;
    }

    @Override
    @Transactional
    public TradeStatistics save(TradeStatistics stats) {
        TradeStatisticsDO statsDO = toDataObject(stats);
        if (stats.id() == null || tradeStatisticsMapper.selectById(stats.id().value()) == null) {
            tradeStatisticsMapper.insert(statsDO);
            return toDomain(statsDO);
        }
        tradeStatisticsMapper.updateById(statsDO);
        return stats;
    }

    @Override
    public TradeStatistics findById(TradeStatisticsId id) {
        TradeStatisticsDO statsDO = tradeStatisticsMapper.selectById(id.value());
        return statsDO != null ? toDomain(statsDO) : null;
    }

    @Override
    public TradeStatistics findByDate(LocalDate date) {
        TradeStatisticsDO statsDO = tradeStatisticsMapper.selectByTimeBetween(
                date.atStartOfDay(), date.plusDays(1).atStartOfDay());
        return statsDO != null ? toDomain(statsDO) : null;
    }

    @Override
    public List<TradeStatistics> findByDateBetween(LocalDate start, LocalDate end) {
        return tradeStatisticsMapper.selectListByTimeBetween(
                        start.atStartOfDay(), end.plusDays(1).atStartOfDay()).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    private TradeStatisticsDO toDataObject(TradeStatistics stats) {
        TradeStatisticsDO statsDO = new TradeStatisticsDO();
        statsDO.setId(stats.id() != null ? stats.id().value() : null);
        statsDO.setTime(stats.date().atStartOfDay());
        statsDO.setOrderCreateCount(stats.orderCount());
        statsDO.setOrderPayCount(stats.orderPayCount());
        statsDO.setOrderPayPrice(stats.orderPayPrice());
        statsDO.setAfterSaleCount(stats.refundCount());
        statsDO.setAfterSaleRefundPrice(stats.refundPrice());
        statsDO.setBrokerageSettlementPrice(stats.brokerageSettlementPrice());
        return statsDO;
    }

    private TradeStatistics toDomain(TradeStatisticsDO statsDO) {
        return TradeStatisticsFactory.reconstitute(
                statsDO.getId(), statsDO.getTime().toLocalDate(),
                statsDO.getOrderCreateCount(), statsDO.getOrderPayCount(),
                statsDO.getOrderPayPrice(), statsDO.getAfterSaleCount(),
                statsDO.getAfterSaleRefundPrice(), statsDO.getBrokerageSettlementPrice());
    }
}
