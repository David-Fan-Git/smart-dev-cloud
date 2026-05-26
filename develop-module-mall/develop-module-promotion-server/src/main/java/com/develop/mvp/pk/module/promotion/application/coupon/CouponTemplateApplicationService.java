package com.develop.mvp.pk.module.promotion.application.coupon;

// Skill: AggregateRoot_CouponTemplate_Validation_Skill — 应用服务

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.promotion.domain.coupon.CouponTemplate;
import com.develop.mvp.pk.module.promotion.domain.coupon.CouponTemplateFactory;
import com.develop.mvp.pk.module.promotion.domain.coupon.repository.CouponTemplateRepository;
import com.develop.mvp.pk.module.promotion.domain.coupon.valueobject.CouponTemplateId;
import com.develop.mvp.pk.module.promotion.domain.event.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.promotion.enums.ErrorCodeConstants.*;

@Service
public class CouponTemplateApplicationService {

    private final CouponTemplateRepository couponTemplateRepository;
    private final DomainEventPublisher eventPublisher;

    public CouponTemplateApplicationService(CouponTemplateRepository couponTemplateRepository,
                                              DomainEventPublisher eventPublisher) {
        this.couponTemplateRepository = couponTemplateRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Long createTemplate(Long id, String name, String description, Integer type, Integer status,
                                Integer totalCount, Integer limitCount,
                                Integer discountType, Integer discountPercent, Integer discountPrice,
                                Integer minimumPrice, Integer maximumPrice,
                                LocalDateTime validStartTime, LocalDateTime validEndTime) {
        CouponTemplate template = CouponTemplateFactory.create(id, name, description, type, status,
                totalCount, limitCount, discountType, discountPercent, discountPrice,
                minimumPrice, maximumPrice, validStartTime, validEndTime);
        template = couponTemplateRepository.save(template);
        publishEvents(template);
        return template.id().value();
    }

    @Transactional
    public void updateTemplate(Long id, String name, String description, Integer totalCount, Integer limitCount,
                                Integer discountType, Integer discountPercent, Integer discountPrice,
                                Integer minimumPrice, Integer maximumPrice,
                                LocalDateTime validStartTime, LocalDateTime validEndTime) {
        CouponTemplate template = findExistingTemplate(CouponTemplateId.of(id));
        template.updateProfile(name, description, totalCount, limitCount, discountType,
                discountPercent, discountPrice, minimumPrice, maximumPrice,
                validStartTime, validEndTime);
        couponTemplateRepository.save(template);
        publishEvents(template);
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        CouponTemplate template = findExistingTemplate(CouponTemplateId.of(id));
        if (com.develop.mvp.pk.framework.common.enums.CommonStatusEnum.ENABLE.getStatus().equals(status)) {
            template.enable();
        } else {
            template.disable();
        }
        couponTemplateRepository.save(template);
        publishEvents(template);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        CouponTemplate template = findExistingTemplate(CouponTemplateId.of(id));
        couponTemplateRepository.delete(template.id());
        publishEvents(template);
    }

    public CouponTemplate getTemplate(Long id) {
        return couponTemplateRepository.findById(CouponTemplateId.of(id));
    }

    public List<CouponTemplate> getTemplateListByStatus(Integer status) {
        return couponTemplateRepository.findByStatus(status);
    }

    public PageResult<CouponTemplate> getTemplatePage(String name, Integer status, Integer discountType,
                                                       Integer pageNo, Integer pageSize) {
        return couponTemplateRepository.findPage(name, status, discountType, pageNo, pageSize);
    }

    private CouponTemplate findExistingTemplate(CouponTemplateId id) {
        CouponTemplate template = couponTemplateRepository.findById(id);
        if (template == null) throw exception(COUPON_TEMPLATE_NOT_EXISTS);
        return template;
    }

    private void publishEvents(CouponTemplate template) {
        for (var event : template.pullEvents()) {
            eventPublisher.publish(event);
        }
    }
}
