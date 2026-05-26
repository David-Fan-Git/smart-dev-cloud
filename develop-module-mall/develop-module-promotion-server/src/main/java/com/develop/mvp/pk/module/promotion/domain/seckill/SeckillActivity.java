package com.develop.mvp.pk.module.promotion.domain.seckill;

// Skill: AggregateRoot_SeckillActivity_Validation_Skill — 聚合根 SeckillActivity
// DDD 角色：秒杀活动聚合根，封装活动生命周期和业务规则
// 验收标准 AC01/AC02：无 MyBatis/Spring 注解，不注入 Mapper

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.module.promotion.domain.seckill.event.SeckillActivityStatusChangedEvent;
import com.develop.mvp.pk.module.promotion.domain.seckill.valueobject.SeckillActivityId;
import com.develop.mvp.pk.module.promotion.domain.seckill.valueobject.SeckillProduct;
import com.develop.mvp.pk.module.promotion.domain.event.DomainEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class SeckillActivity {

    private final SeckillActivityId id;
    private Long spuId;
    private String name;
    private Integer status;
    private String remark;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer sort;
    private List<Long> configIds;
    private Integer totalLimitCount;
    private Integer singleLimitCount;
    private Integer stock;
    private Integer totalStock;
    private List<SeckillProduct> products;
    private final List<DomainEvent> events = new ArrayList<>();

    SeckillActivity(SeckillActivityId id, Long spuId, String name, Integer status, String remark,
                    LocalDateTime startTime, LocalDateTime endTime, Integer sort, List<Long> configIds,
                    Integer totalLimitCount, Integer singleLimitCount, Integer stock, Integer totalStock,
                    List<SeckillProduct> products) {
        this.id = id;
        this.spuId = Objects.requireNonNull(spuId, "商品SPU编号不能为空");
        this.name = Objects.requireNonNull(name, "活动名称不能为空");
        this.status = status != null ? status : CommonStatusEnum.ENABLE.getStatus();
        this.remark = remark;
        this.startTime = Objects.requireNonNull(startTime, "开始时间不能为空");
        this.endTime = Objects.requireNonNull(endTime, "结束时间不能为空");
        this.sort = sort != null ? sort : 0;
        this.configIds = configIds != null ? new ArrayList<>(configIds) : new ArrayList<>();
        this.totalLimitCount = totalLimitCount;
        this.singleLimitCount = singleLimitCount;
        this.stock = stock != null ? stock : 0;
        this.totalStock = totalStock != null ? totalStock : 0;
        this.products = products != null ? new ArrayList<>(products) : new ArrayList<>();
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("结束时间不能早于开始时间");
        }
    }

    public void updateProfile(String name, String remark, LocalDateTime startTime, LocalDateTime endTime,
                               Integer sort, List<Long> configIds, Integer totalLimitCount, Integer singleLimitCount) {
        this.name = Objects.requireNonNull(name, "活动名称不能为空");
        this.remark = remark;
        this.startTime = Objects.requireNonNull(startTime);
        this.endTime = Objects.requireNonNull(endTime);
        this.sort = sort != null ? sort : this.sort;
        this.configIds = configIds != null ? new ArrayList<>(configIds) : this.configIds;
        this.totalLimitCount = totalLimitCount;
        this.singleLimitCount = singleLimitCount;
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("结束时间不能早于开始时间");
        }
    }

    public void updateProducts(List<SeckillProduct> newProducts) {
        this.products = new ArrayList<>(newProducts);
        this.totalStock = newProducts.stream().mapToInt(SeckillProduct::stock).sum();
        this.stock = this.totalStock;
    }

    public void enable() {
        if (isEnabled()) return;
        Integer old = this.status;
        this.status = CommonStatusEnum.ENABLE.getStatus();
        events.add(new SeckillActivityStatusChangedEvent(this.id.value(), old, this.status));
    }

    public void disable() {
        if (isDisabled()) return;
        Integer old = this.status;
        this.status = CommonStatusEnum.DISABLE.getStatus();
        events.add(new SeckillActivityStatusChangedEvent(this.id.value(), old, this.status));
    }

    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return isEnabled() && !now.isBefore(startTime) && !now.isAfter(endTime);
    }

    // ── 查询方法 ──

    public SeckillActivityId id() { return id; }
    public Long spuId() { return spuId; }
    public String name() { return name; }
    public Integer status() { return status; }
    public String remark() { return remark; }
    public LocalDateTime startTime() { return startTime; }
    public LocalDateTime endTime() { return endTime; }
    public Integer sort() { return sort; }
    public List<Long> configIds() { return Collections.unmodifiableList(configIds); }
    public Integer totalLimitCount() { return totalLimitCount; }
    public Integer singleLimitCount() { return singleLimitCount; }
    public Integer stock() { return stock; }
    public Integer totalStock() { return totalStock; }
    public List<SeckillProduct> products() { return Collections.unmodifiableList(products); }

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
        if (!(o instanceof SeckillActivity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() { return "SeckillActivity{id=" + id + ", name='" + name + "'}"; }
}
