package com.develop.mvp.pk.module.product.domain.productcategory;

// Skill: AggregateRoot_ProductCategory_Validation_Skill — 聚合根 ProductCategory
// DDD 角色：商品分类聚合根，封装分类层级管理和状态变化业务规则
// 职责边界：分类名称、层级、排序、状态管理
// 验收标准 AC01/AC02：无 MyBatis/Spring 注解，不注入 Mapper

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.module.product.domain.productcategory.event.ProductCategoryStatusChangedEvent;
import com.develop.mvp.pk.module.product.domain.productcategory.valueobject.ProductCategoryId;
import com.develop.mvp.pk.module.product.domain.productcategory.valueobject.ProductCategoryName;
import com.develop.mvp.pk.module.product.domain.event.DomainEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ProductCategory {

    public static final Long PARENT_ID_NULL = 0L;
    public static final int CATEGORY_LEVEL = 2;

    // ── 聚合根标识 ──
    private final ProductCategoryId id;

    // ── 核心属性 ──
    private ProductCategoryName name;
    private Long parentId;
    private String picUrl;
    private Integer sort;
    private Integer status;

    // ── 领域事件收集 ──
    private final List<DomainEvent> events = new ArrayList<>();

    ProductCategory(ProductCategoryId id, ProductCategoryName name, Long parentId,
                    String picUrl, Integer sort, Integer status) {
        this.id = Objects.requireNonNull(id, "分类编号不能为空");
        this.name = Objects.requireNonNull(name, "分类名称不能为空");
        this.parentId = parentId != null ? parentId : PARENT_ID_NULL;
        this.picUrl = picUrl;
        this.sort = sort != null ? sort : 0;
        this.status = status != null ? status : CommonStatusEnum.ENABLE.getStatus();
        validateHierarchy();
    }

    // ── 业务方法 ──

    /** 更新分类基本信息 */
    public void updateProfile(ProductCategoryName newName, Long newParentId, String newPicUrl, Integer newSort) {
        this.name = Objects.requireNonNull(newName, "分类名称不能为空");
        Long oldParentId = this.parentId;
        this.parentId = newParentId != null ? newParentId : PARENT_ID_NULL;
        this.picUrl = newPicUrl;
        this.sort = newSort != null ? newSort : this.sort;
        validateHierarchy();
    }

    /** 启用分类 */
    public void enable() {
        if (isEnabled()) return;
        Integer oldStatus = this.status;
        this.status = CommonStatusEnum.ENABLE.getStatus();
        events.add(new ProductCategoryStatusChangedEvent(this.id.value(), oldStatus, this.status));
    }

    /** 禁用分类 */
    public void disable() {
        if (isDisabled()) return;
        Integer oldStatus = this.status;
        this.status = CommonStatusEnum.DISABLE.getStatus();
        events.add(new ProductCategoryStatusChangedEvent(this.id.value(), oldStatus, this.status));
    }

    /** 校验分类层级约束 */
    private void validateHierarchy() {
        if (this.parentId != null && this.parentId != PARENT_ID_NULL) {
            if (this.parentId.equals(this.id.value())) {
                throw new IllegalArgumentException("分类不能将自己设为父分类");
            }
        }
    }

    // ── 查询方法 ──

    public ProductCategoryId id() { return id; }
    public ProductCategoryName name() { return name; }
    public Long parentId() { return parentId; }
    public String picUrl() { return picUrl; }
    public Integer sort() { return sort; }
    public Integer status() { return status; }

    public boolean isEnabled() { return CommonStatusEnum.ENABLE.getStatus().equals(status); }
    public boolean isDisabled() { return CommonStatusEnum.DISABLE.getStatus().equals(status); }
    public boolean isRoot() { return PARENT_ID_NULL.equals(parentId); }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> result = new ArrayList<>(events);
        events.clear();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductCategory that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "ProductCategory{id=" + id + ", name=" + name + '}';
    }
}
