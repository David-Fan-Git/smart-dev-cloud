package com.develop.mvp.pk.module.system.domain.dict.valueobject;
import java.util.Objects;
/**
 * Dict Type Key 值对象。
 */
public final class DictTypeKey { private final String v; private DictTypeKey(String v) { if (v == null || v.isBlank()) throw new IllegalArgumentException("类型不能为空"); this.v = v; } public static DictTypeKey of(String v) { return new DictTypeKey(v); } public String value() { return v; } @Override public boolean equals(Object o) { return o instanceof DictTypeKey d && v.equals(d.v); } @Override public int hashCode() { return Objects.hash(v); } }
