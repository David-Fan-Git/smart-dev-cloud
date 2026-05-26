package com.develop.mvp.pk.module.system.domain.dict.valueobject;

import java.util.Objects;

/**
 * Dict Type Id 值对象。
 */
public final class DictTypeId {

    private final Long v;

    /**
     * 创建 DictTypeId 实例。
     *
     * @param v v 参数
     */
    private DictTypeId(Long v) {
        this.v = v;
    }

    /**
     * 执行 of 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public static DictTypeId of(Long v) {
        return new DictTypeId(v);
    }

    /**
     * 执行 value 对应的业务操作。
     *
     * @return 处理结果
     */
    public Long value() {
        return v;
    }

    /**
     * 执行 equals 对应的业务操作。
     *
     * @param o o 参数
     * @return 处理结果
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof DictTypeId d && Objects.equals(v, d.v);
    }

    /**
     * 判断 hash Code 对应的条件是否成立。
     *
     * @return 处理结果
     */
    @Override
    public int hashCode() {
        return Objects.hash(v);
    }

}
