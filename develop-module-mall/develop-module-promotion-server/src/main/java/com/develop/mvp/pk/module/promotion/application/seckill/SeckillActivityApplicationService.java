package com.develop.mvp.pk.module.promotion.application.seckill;

// Skill: AggregateRoot_SeckillActivity_Validation_Skill — 应用服务

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.promotion.domain.seckill.SeckillActivity;
import com.develop.mvp.pk.module.promotion.domain.seckill.SeckillActivityFactory;
import com.develop.mvp.pk.module.promotion.domain.seckill.repository.SeckillActivityRepository;
import com.develop.mvp.pk.module.promotion.domain.seckill.valueobject.SeckillActivityId;
import com.develop.mvp.pk.module.promotion.domain.seckill.valueobject.SeckillProduct;
import com.develop.mvp.pk.module.promotion.domain.event.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.promotion.enums.ErrorCodeConstants.*;

@Service
public class SeckillActivityApplicationService {

    private final SeckillActivityRepository seckillActivityRepository;
    private final DomainEventPublisher eventPublisher;

    public SeckillActivityApplicationService(SeckillActivityRepository seckillActivityRepository,
                                              DomainEventPublisher eventPublisher) {
        this.seckillActivityRepository = seckillActivityRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Long createActivity(Long id, Long spuId, String name, Integer status, String remark,
                                LocalDateTime startTime, LocalDateTime endTime, Integer sort,
                                List<Long> configIds, Integer totalLimitCount, Integer singleLimitCount,
                                List<Map<String, Object>> productParams) {
        List<SeckillProduct> products = buildProducts(productParams);
        SeckillActivity activity = SeckillActivityFactory.create(id, spuId, name, status, remark,
                startTime, endTime, sort, configIds, totalLimitCount, singleLimitCount, products);
        activity = seckillActivityRepository.save(activity);
        publishEvents(activity);
        return activity.id().value();
    }

    @Transactional
    public void updateActivity(Long id, Long spuId, String name, String remark,
                                LocalDateTime startTime, LocalDateTime endTime, Integer sort,
                                List<Long> configIds, Integer totalLimitCount, Integer singleLimitCount,
                                List<Map<String, Object>> productParams) {
        SeckillActivity activity = findExistingActivity(SeckillActivityId.of(id));
        activity.updateProfile(name, remark, startTime, endTime, sort, configIds, totalLimitCount, singleLimitCount);
        if (productParams != null) {
            activity.updateProducts(buildProducts(productParams));
        }
        seckillActivityRepository.save(activity);
        publishEvents(activity);
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        SeckillActivity activity = findExistingActivity(SeckillActivityId.of(id));
        if (com.develop.mvp.pk.framework.common.enums.CommonStatusEnum.ENABLE.getStatus().equals(status)) {
            activity.enable();
        } else {
            activity.disable();
        }
        seckillActivityRepository.save(activity);
        publishEvents(activity);
    }

    @Transactional
    public void deleteActivity(Long id) {
        SeckillActivity activity = findExistingActivity(SeckillActivityId.of(id));
        seckillActivityRepository.delete(activity.id());
        publishEvents(activity);
    }

    // ── 查询 ──

    public SeckillActivity getActivity(Long id) {
        return seckillActivityRepository.findById(SeckillActivityId.of(id));
    }

    public PageResult<SeckillActivity> getActivityPage(String name, Integer status, Long spuId,
                                                        Integer pageNo, Integer pageSize) {
        return seckillActivityRepository.findPage(name, status, spuId, pageNo, pageSize);
    }

    public List<SeckillActivity> getActiveActivities() {
        return seckillActivityRepository.findActiveActivities();
    }

    // ── 私有方法 ──

    private SeckillActivity findExistingActivity(SeckillActivityId id) {
        SeckillActivity activity = seckillActivityRepository.findById(id);
        if (activity == null) throw exception(SECKILL_ACTIVITY_NOT_EXISTS);
        return activity;
    }

    private List<SeckillProduct> buildProducts(List<Map<String, Object>> productParams) {
        if (productParams == null) return List.of();
        return productParams.stream().map(p -> new SeckillProduct(
                p.get("id") != null ? ((Number) p.get("id")).longValue() : null,
                p.get("configIds") != null ? ((List<Long>) p.get("configIds")) : null,
                ((Number) p.get("spuId")).longValue(),
                ((Number) p.get("skuId")).longValue(),
                (Integer) p.get("seckillPrice"),
                (Integer) p.get("stock")
        )).collect(Collectors.toList());
    }

    private void publishEvents(SeckillActivity activity) {
        for (var event : activity.pullEvents()) {
            eventPublisher.publish(event);
        }
    }
}
