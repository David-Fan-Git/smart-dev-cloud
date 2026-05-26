package com.develop.mvp.pk.module.system.domain.dict.valueobject;
import java.util.Objects;
/**
 * Dict Data Value 值对象。
 */
public final class DictDataValue { private final String v; private DictDataValue(String v) { if (v == null) throw new IllegalArgumentException("值不能为空"); this.v = v; } public static DictDataValue of(String v) { return new DictDataValue(v); } public String value() { return v; } @Override public boolean equals(Object o) { return o instanceof DictDataValue d && v.equals(d.v); } @Override public int hashCode() { return Objects.hash(v); } }
