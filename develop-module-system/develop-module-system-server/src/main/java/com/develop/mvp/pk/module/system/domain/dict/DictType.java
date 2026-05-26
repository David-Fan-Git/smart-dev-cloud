package com.develop.mvp.pk.module.system.domain.dict;

import com.develop.mvp.pk.module.system.domain.dict.valueobject.DictTypeId;
import com.develop.mvp.pk.module.system.domain.dict.valueobject.DictTypeKey;
import com.develop.mvp.pk.module.system.domain.dict.valueobject.DictTypeName;

import java.util.Objects;

/**
 * Dict Type 领域模型。
 */
public final class DictType {

    private final DictTypeId id;
    private final DictTypeName name;
    private final DictTypeKey type;
    private final Integer status;
    private final String remark;

    /**
     * 创建 DictType 实例。
     *
     * @param id id 参数
     * @param name name 参数
     * @param type type 参数
     * @param status status 参数
     * @param remark remark 参数
     */
    public DictType(DictTypeId id, DictTypeName name, DictTypeKey type, Integer status, String remark) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
        this.status = status != null ? status : 0;
        this.remark = remark;
    }

    /**
     * 执行 id 对应的业务操作。
     *
     * @return 处理结果
     */
    public DictTypeId id() {
        return id;
    }

    /**
     * 执行 name 对应的业务操作。
     *
     * @return 处理结果
     */
    public DictTypeName name() {
        return name;
    }

    /**
     * 执行 type 对应的业务操作。
     *
     * @return 处理结果
     */
    public DictTypeKey type() {
        return type;
    }

    /**
     * 执行 status 对应的业务操作。
     *
     * @return 处理结果
     */
    public Integer status() {
        return status;
    }

    /**
     * 执行 remark 对应的业务操作。
     *
     * @return 处理结果
     */
    public String remark() {
        return remark;
    }

    /**
     * 判断 is Enabled 对应的条件是否成立。
     *
     * @return 处理结果
     */
    public boolean isEnabled() {
        return Integer.valueOf(0).equals(status);
    }

    /**
     * 执行 equals 对应的业务操作。
     *
     * @param o o 参数
     * @return 处理结果
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof DictType d && Objects.equals(id, d.id);
    }

    /**
     * 判断 hash Code 对应的条件是否成立。
     *
     * @return 处理结果
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
