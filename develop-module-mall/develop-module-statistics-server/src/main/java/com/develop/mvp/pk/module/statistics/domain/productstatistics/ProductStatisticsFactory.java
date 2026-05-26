package com.develop.mvp.pk.module.statistics.domain.productstatistics;

import com.develop.mvp.pk.module.statistics.domain.productstatistics.valueobject.ProductStatisticsId;
import java.time.LocalDate;

public final class ProductStatisticsFactory {
    private ProductStatisticsFactory() {}

    public static ProductStatistics create(Long id, Long spuId, LocalDate date) {
        return new ProductStatistics(id != null ? ProductStatisticsId.of(id) : null, spuId, date, 0, 0, 0, 0, 0, 0);
    }

    public static ProductStatistics reconstitute(Long id, Long spuId, LocalDate date,
                                                  Integer browseCount, Integer favoriteCount,
                                                  Integer cartCount, Integer orderCount,
                                                  Integer orderPayCount, Integer orderPayPrice) {
        return new ProductStatistics(ProductStatisticsId.of(id), spuId, date,
                browseCount, favoriteCount, cartCount, orderCount, orderPayCount, orderPayPrice);
    }
}
