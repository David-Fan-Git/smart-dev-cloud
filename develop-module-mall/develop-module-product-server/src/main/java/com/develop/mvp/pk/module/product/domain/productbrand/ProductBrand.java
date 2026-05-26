package com.develop.mvp.pk.module.product.domain.productbrand;

// Skill: AggregateRoot_ProductBrand_Validation_Skill — 聚合根 ProductBrand
// DDD 角色：商品品牌聚合根，封装品牌生命周期和状态管理
// 验收标准 AC01/AC02：无 MyBatis/Spring 注解，不注入 Mapper

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.module.product.domain.productbrand.event.ProductBrandStatusChangedEvent;
import com.develop.mvp.pk.module.product.domain.productbrand.valueobject.ProductBrandId;
import com.develop.mvp.pk.module.product.domain.productbrand.valueobject.ProductBrandName;
import com.develop.mvp.pk.module.product.domain.event.DomainEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ProductBrand {

    // ── 聚合根标识 ──
    private final ProductBrandId id;

    // ── 核心属性 ──
    private ProductBrandName name;
    private String picUrl;
    private Integer sort;
    private String description;
    private Integer status;

    // ── 领域事件收集 ──
    private final List<DomainEvent> events = new ArrayList<>();

    ProductBrand(ProductBrandId id, ProductBrandName name, String picUrl,
                 Integer sort, String description, Integer status) {
        this.id = Objects.requireNonNull(id, "品牌编号不能为空");
        this.name = Objects.requireNonNull(name, "品牌名称不能为空");
        this.picUrl = picUrl;
        this.sort = sort != null ? sort : 0;
        this.description = description;
        this.status = status != null ? status : CommonStatusEnum.ENABLE.getStatus();
    }

    // ── 业务方法 ──

    /** 更新品牌信息 */
    public void updateProfile(ProductBrandName newName, String newPicUrl, Integer newSort, String newDescription) {
        this.name = Objects.requireNonNull(newName, "品牌名称不能为空");
        this.picUrl = newPicUrl;
        this.sort = newSort != null ? newSort : this.sort;
        this.description = newDescription;
    }

    /** 启用品牌 */
    public void enable() {
        if (isEnabled()) return;
        Integer oldStatus = this.status;
        this.status = CommonStatusEnum.ENABLE.getStatus();
        events.add(new ProductBrandStatusChangedEvent(this.id.value(), oldStatus, this.status));
    }

    /** 禁用品牌 */
    public void disable() {
        if (isDisabled()) return;
        Integer oldStatus = this.status;
        this.status = CommonStatusEnum.DISABLE.getStatus();
        events.add(new ProductBrandStatusChangedEvent(this.id.value(), oldStatus, this.status));
    }

    // ── 查询方法 ──

    public ProductBrandId id() { return id; }
    public ProductBrandName name() { return name; }
    public String picUrl() { return picUrl; }
    public Integer sort() { return sort; }
    public String description() { return description; }
    public Integer status() { return status; }

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
        if (!(o instanceof ProductBrand that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "ProductBrand{id=" + id + ", name=" + name + '}';
    }
}
