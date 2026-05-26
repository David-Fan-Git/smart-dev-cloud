package com.develop.mvp.pk.module.system.domain.dict.valueobject;

import java.util.Objects;

/**
 * Dict Data Id 值对象。
 */
public final class DictDataId {

    private final Long v;

    /**
     * 创建 DictDataId 实例。
     *
     * @param v v 参数
     */
    private DictDataId(Long v) {
        this.v = v;
    }

    /**
     * 执行 of 对应的业务操作。
     *
     * @param v v 参数
     * @return 处理结果
     */
    public static DictDataId of(Long v) {
        return new DictDataId(v);
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
        return o instanceof DictDataId d && Objects.equals(v, d.v);
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
