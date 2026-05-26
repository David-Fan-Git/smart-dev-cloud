package com.develop.mvp.pk.module.system.domain.dict.valueobject;
import java.util.Objects;
/**
 * Dict Type Name 值对象。
 */
public final class DictTypeName { private final String v; private DictTypeName(String v) { if (v == null || v.isBlank()) throw new IllegalArgumentException("名称不能为空"); this.v = v; } public static DictTypeName of(String v) { return new DictTypeName(v); } public String value() { return v; } @Override public boolean equals(Object o) { return o instanceof DictTypeName d && v.equals(d.v); } @Override public int hashCode() { return Objects.hash(v); } }
