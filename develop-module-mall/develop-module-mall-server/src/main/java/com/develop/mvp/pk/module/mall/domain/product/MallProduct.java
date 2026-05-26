package com.develop.mvp.pk.module.mall.domain.product;
import java.util.Objects;
public final class MallProduct { private final Long id; private final String name; private Long categoryId; private Integer price, stock, status;
    public MallProduct(Long id, String name) { this.id = id; this.name = Objects.requireNonNull(name); }
    public static MallProduct of(Long id, String name) { return new MallProduct(id, name); }
    public Long id() { return id; } public String name() { return name; } public Long categoryId() { return categoryId; }
    public Integer price() { return price; } public Integer stock() { return stock; } public Integer status() { return status; }
    public MallProduct categoryId(Long v) { categoryId = v; return this; } public MallProduct price(Integer v) { price = v; return this; }
    public MallProduct stock(Integer v) { stock = v; return this; } public MallProduct status(Integer v) { status = v; return this; }
    @Override public boolean equals(Object o) { return o instanceof MallProduct p && id.equals(p.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
