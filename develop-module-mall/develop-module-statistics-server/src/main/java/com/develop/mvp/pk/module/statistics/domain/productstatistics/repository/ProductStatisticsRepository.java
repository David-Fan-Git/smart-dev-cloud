package com.develop.mvp.pk.module.statistics.domain.productstatistics.repository;

import com.develop.mvp.pk.module.statistics.domain.productstatistics.ProductStatistics;
import com.develop.mvp.pk.module.statistics.domain.productstatistics.valueobject.ProductStatisticsId;
import java.time.LocalDate;
import java.util.List;

public interface ProductStatisticsRepository {
    ProductStatistics save(ProductStatistics stats);
    ProductStatistics findById(ProductStatisticsId id);
    ProductStatistics findBySpuIdAndDate(Long spuId, LocalDate date);
    List<ProductStatistics> findByDateBetween(LocalDate start, LocalDate end);
    List<ProductStatistics> findBySpuId(Long spuId);
}
