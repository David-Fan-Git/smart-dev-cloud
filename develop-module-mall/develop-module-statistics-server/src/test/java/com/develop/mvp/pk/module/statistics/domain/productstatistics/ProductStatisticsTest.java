package com.develop.mvp.pk.module.statistics.domain.productstatistics;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNull;

class ProductStatisticsTest {

    @Test
    void create_allowsTransientId() {
        ProductStatistics statistics = ProductStatisticsFactory.create(null, 1L, LocalDate.of(2026, 5, 23));

        assertNull(statistics.id());
    }
}
