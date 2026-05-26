package com.develop.mvp.pk.module.trade.domain.cart;

// Skill: AggregateRoot_Cart_Validation_Skill — 聚合根 Cart
// DDD 角色：购物车聚合根，封装购物车项管理业务规则
// 验收标准 AC01/AC02：无 MyBatis/Spring 注解

import com.develop.mvp.pk.module.trade.domain.cart.valueobject.CartId;
import com.develop.mvp.pk.module.trade.domain.event.DomainEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Cart {

    private final CartId id;
    private Long userId;
    private Long spuId;
    private Long skuId;
    private Integer count;
    private Boolean selected;
    private final List<DomainEvent> events = new ArrayList<>();

    Cart(CartId id, Long userId, Long spuId, Long skuId, Integer count, Boolean selected) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId, "用户编号不能为空");
        this.spuId = Objects.requireNonNull(spuId, "SPU编号不能为空");
        this.skuId = Objects.requireNonNull(skuId, "SKU编号不能为空");
        this.count = Objects.requireNonNull(count, "数量不能为空");
        this.selected = selected != null ? selected : true;
    }

    public void updateCount(Integer count) {
        if (count <= 0) throw new IllegalArgumentException("数量必须大于0");
        this.count = count;
    }

    public void select() { this.selected = true; }
    public void unselect() { this.selected = false; }

    public CartId id() { return id; }
    public Long userId() { return userId; }
    public Long spuId() { return spuId; }
    public Long skuId() { return skuId; }
    public Integer count() { return count; }
    public Boolean selected() { return selected; }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> result = new ArrayList<>(events);
        events.clear();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cart that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
