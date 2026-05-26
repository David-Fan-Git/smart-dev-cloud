package com.develop.mvp.pk.module.statistics.domain.tradestatistics;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNull;

class TradeStatisticsTest {

    @Test
    void create_allowsTransientId() {
        TradeStatistics statistics = TradeStatisticsFactory.create(null, LocalDate.of(2026, 5, 23));

        assertNull(statistics.id());
    }
}
