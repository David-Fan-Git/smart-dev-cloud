package com.develop.mvp.pk.module.statistics.infrastructure.productstatistics;

import com.develop.mvp.pk.module.statistics.dal.dataobject.product.ProductStatisticsDO;
import com.develop.mvp.pk.module.statistics.dal.mysql.product.ProductStatisticsMapper;
import com.develop.mvp.pk.module.statistics.domain.productstatistics.ProductStatistics;
import com.develop.mvp.pk.module.statistics.domain.productstatistics.ProductStatisticsFactory;
import com.develop.mvp.pk.module.statistics.domain.productstatistics.repository.ProductStatisticsRepository;
import com.develop.mvp.pk.module.statistics.domain.productstatistics.valueobject.ProductStatisticsId;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ProductStatisticsRepositoryImpl implements ProductStatisticsRepository {

    private final ProductStatisticsMapper productStatisticsMapper;

    public ProductStatisticsRepositoryImpl(ProductStatisticsMapper productStatisticsMapper) {
        this.productStatisticsMapper = productStatisticsMapper;
    }

    @Override
    @Transactional
    public ProductStatistics save(ProductStatistics stats) {
        ProductStatisticsDO statsDO = toDataObject(stats);
        if (stats.id() == null || productStatisticsMapper.selectById(stats.id().value()) == null) {
            productStatisticsMapper.insert(statsDO);
            return toDomain(statsDO);
        }
        productStatisticsMapper.updateById(statsDO);
        return stats;
    }

    @Override
    public ProductStatistics findById(ProductStatisticsId id) {
        ProductStatisticsDO statsDO = productStatisticsMapper.selectById(id.value());
        return statsDO != null ? toDomain(statsDO) : null;
    }

    @Override
    public ProductStatistics findBySpuIdAndDate(Long spuId, LocalDate date) {
        ProductStatisticsDO statsDO = productStatisticsMapper.selectOne(ProductStatisticsDO::getSpuId, spuId, ProductStatisticsDO::getTime, date);
        return statsDO != null ? toDomain(statsDO) : null;
    }

    @Override
    public List<ProductStatistics> findByDateBetween(LocalDate start, LocalDate end) {
        return productStatisticsMapper.selectList(new LambdaQueryWrapper<ProductStatisticsDO>()
                        .between(ProductStatisticsDO::getTime, start, end)).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<ProductStatistics> findBySpuId(Long spuId) {
        return productStatisticsMapper.selectList(ProductStatisticsDO::getSpuId, spuId).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    private ProductStatisticsDO toDataObject(ProductStatistics stats) {
        ProductStatisticsDO statsDO = new ProductStatisticsDO();
        statsDO.setId(stats.id() != null ? stats.id().value() : null);
        statsDO.setSpuId(stats.spuId());
        statsDO.setTime(stats.date());
        statsDO.setBrowseCount(stats.browseCount());
        statsDO.setFavoriteCount(stats.favoriteCount());
        statsDO.setCartCount(stats.cartCount());
        statsDO.setOrderCount(stats.orderCount());
        statsDO.setOrderPayCount(stats.orderPayCount());
        statsDO.setOrderPayPrice(stats.orderPayPrice());
        return statsDO;
    }

    private ProductStatistics toDomain(ProductStatisticsDO statsDO) {
        return ProductStatisticsFactory.reconstitute(
                statsDO.getId(), statsDO.getSpuId(), statsDO.getTime(),
                statsDO.getBrowseCount(), statsDO.getFavoriteCount(),
                statsDO.getCartCount(), statsDO.getOrderCount(),
                statsDO.getOrderPayCount(), statsDO.getOrderPayPrice());
    }
}
