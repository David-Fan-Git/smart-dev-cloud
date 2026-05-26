package com.develop.mvp.pk.module.promotion.domain.coupon;

// Skill: AggregateRoot_CouponTemplate_Validation_Skill — 聚合根 CouponTemplate
// DDD 角色：优惠券模板聚合根，封装模板生命周期和发放规则
// 验收标准 AC01/AC02：无 MyBatis/Spring 注解

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.module.promotion.domain.coupon.valueobject.CouponTemplateId;
import com.develop.mvp.pk.module.promotion.domain.event.DomainEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CouponTemplate {

    private final CouponTemplateId id;
    private String name;
    private String description;
    private Integer type;
    private Integer status;
    private Integer totalCount;
    private Integer limitCount;
    private Integer distributeCount;
    private Integer useCount;
    private Integer discountType;
    private Integer discountPercent;
    private Integer discountPrice;
    private Integer minimumPrice;
    private Integer maximumPrice;
    private LocalDateTime validStartTime;
    private LocalDateTime validEndTime;
    private final List<DomainEvent> events = new ArrayList<>();

    CouponTemplate(CouponTemplateId id, String name, String description, Integer type, Integer status,
                   Integer totalCount, Integer limitCount, Integer distributeCount, Integer useCount,
                   Integer discountType, Integer discountPercent, Integer discountPrice,
                   Integer minimumPrice, Integer maximumPrice,
                   LocalDateTime validStartTime, LocalDateTime validEndTime) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "模板名称不能为空");
        this.description = description;
        this.type = type;
        this.status = status != null ? status : CommonStatusEnum.ENABLE.getStatus();
        this.totalCount = totalCount;
        this.limitCount = limitCount;
        this.distributeCount = distributeCount != null ? distributeCount : 0;
        this.useCount = useCount != null ? useCount : 0;
        this.discountType = Objects.requireNonNull(discountType, "优惠类型不能为空");
        this.discountPercent = discountPercent;
        this.discountPrice = discountPrice;
        this.minimumPrice = minimumPrice;
        this.maximumPrice = maximumPrice;
        this.validStartTime = validStartTime;
        this.validEndTime = validEndTime;
    }

    public void updateProfile(String name, String description, Integer totalCount, Integer limitCount,
                               Integer discountType, Integer discountPercent, Integer discountPrice,
                               Integer minimumPrice, Integer maximumPrice,
                               LocalDateTime validStartTime, LocalDateTime validEndTime) {
        this.name = Objects.requireNonNull(name);
        this.description = description;
        this.totalCount = totalCount;
        this.limitCount = limitCount;
        this.discountType = Objects.requireNonNull(discountType);
        this.discountPercent = discountPercent;
        this.discountPrice = discountPrice;
        this.minimumPrice = minimumPrice;
        this.maximumPrice = maximumPrice;
        this.validStartTime = validStartTime;
        this.validEndTime = validEndTime;
    }

    public void enable() { this.status = CommonStatusEnum.ENABLE.getStatus(); }
    public void disable() { this.status = CommonStatusEnum.DISABLE.getStatus(); }

    public void incrementDistributeCount(int count) { this.distributeCount += count; }
    public void incrementUseCount(int count) { this.useCount += count; }

    public boolean canDistribute() {
        return isEnabled() && (totalCount == null || totalCount <= 0 || distributeCount < totalCount);
    }

    public CouponTemplateId id() { return id; }
    public String name() { return name; }
    public String description() { return description; }
    public Integer type() { return type; }
    public Integer status() { return status; }
    public Integer totalCount() { return totalCount; }
    public Integer limitCount() { return limitCount; }
    public Integer distributeCount() { return distributeCount; }
    public Integer useCount() { return useCount; }
    public Integer discountType() { return discountType; }
    public Integer discountPercent() { return discountPercent; }
    public Integer discountPrice() { return discountPrice; }
    public Integer minimumPrice() { return minimumPrice; }
    public Integer maximumPrice() { return maximumPrice; }
    public LocalDateTime validStartTime() { return validStartTime; }
    public LocalDateTime validEndTime() { return validEndTime; }

    public boolean isEnabled() { return CommonStatusEnum.ENABLE.getStatus().equals(status); }
    public boolean isDisabled() { return CommonStatusEnum.DISABLE.getStatus().equals(status); }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> result = new ArrayList<>(events);
        events.clear();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CouponTemplate that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
