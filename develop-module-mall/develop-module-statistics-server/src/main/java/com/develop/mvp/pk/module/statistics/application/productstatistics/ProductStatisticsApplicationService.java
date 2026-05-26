package com.develop.mvp.pk.module.statistics.application.productstatistics;

import com.develop.mvp.pk.module.statistics.domain.productstatistics.ProductStatistics;
import com.develop.mvp.pk.module.statistics.domain.productstatistics.ProductStatisticsFactory;
import com.develop.mvp.pk.module.statistics.domain.productstatistics.repository.ProductStatisticsRepository;
import com.develop.mvp.pk.module.statistics.domain.productstatistics.valueobject.ProductStatisticsId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProductStatisticsApplicationService {

    private final ProductStatisticsRepository productStatisticsRepository;

    public ProductStatisticsApplicationService(ProductStatisticsRepository productStatisticsRepository) {
        this.productStatisticsRepository = productStatisticsRepository;
    }

    @Transactional
    public void recordBrowse(Long spuId, LocalDate date, int count) {
        recordMetric(spuId, date).incrementBrowse(count);
    }

    @Transactional
    public void recordFavorite(Long spuId, LocalDate date, int count) {
        recordMetric(spuId, date).incrementFavorite(count);
    }

    @Transactional
    public void recordCart(Long spuId, LocalDate date, int count) {
        recordMetric(spuId, date).incrementCart(count);
    }

    @Transactional
    public void recordOrder(Long spuId, LocalDate date, int count) {
        recordMetric(spuId, date).incrementOrder(count);
    }

    @Transactional
    public void recordOrderPay(Long spuId, LocalDate date, int count, int price) {
        recordMetric(spuId, date).incrementOrderPay(count, price);
    }

    public ProductStatistics getBySpuAndDate(Long spuId, LocalDate date) {
        return productStatisticsRepository.findBySpuIdAndDate(spuId, date);
    }

    public List<ProductStatistics> getByDateRange(LocalDate start, LocalDate end) {
        return productStatisticsRepository.findByDateBetween(start, end);
    }

    public List<ProductStatistics> getBySpuId(Long spuId) {
        return productStatisticsRepository.findBySpuId(spuId);
    }

    private ProductStatistics recordMetric(Long spuId, LocalDate date) {
        ProductStatistics stats = productStatisticsRepository.findBySpuIdAndDate(spuId, date);
        if (stats == null) {
            stats = ProductStatisticsFactory.create(null, spuId, date);
        }
        productStatisticsRepository.save(stats);
        return stats;
    }
}
