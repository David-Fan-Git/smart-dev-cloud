package com.develop.mvp.pk.module.system.domain.dict;

import com.develop.mvp.pk.module.system.domain.dict.valueobject.DictDataId;
import com.develop.mvp.pk.module.system.domain.dict.valueobject.DictDataValue;
import com.develop.mvp.pk.module.system.domain.dict.valueobject.DictTypeKey;

import java.util.Objects;

/**
 * Dict Data 领域模型。
 */
public final class DictData {

    private final DictDataId id;
    private final DictTypeKey dictType;
    private final DictDataValue value;
    private final String label;
    private final Integer sort;
    private final Integer status;
    private final String colorType;
    private final String cssClass;
    private final String remark;

    /**
     * 创建 DictData 实例。
     *
     * @param id id 参数
     * @param dictType dictType 参数
     * @param value value 参数
     * @param label label 参数
     * @param sort sort 参数
     * @param status status 参数
     * @param colorType colorType 参数
     * @param cssClass cssClass 参数
     * @param remark remark 参数
     */
    public DictData(DictDataId id, DictTypeKey dictType, DictDataValue value, String label,
                    Integer sort, Integer status, String colorType, String cssClass, String remark) {
        this.id = id;
        this.dictType = Objects.requireNonNull(dictType);
        this.value = Objects.requireNonNull(value);
        this.label = label;
        this.sort = sort != null ? sort : 0;
        this.status = status != null ? status : 0;
        this.colorType = colorType;
        this.cssClass = cssClass;
        this.remark = remark;
    }

    /**
     * 执行 id 对应的业务操作。
     *
     * @return 处理结果
     */
    public DictDataId id() {
        return id;
    }

    /**
     * 执行 dict Type 对应的业务操作。
     *
     * @return 处理结果
     */
    public DictTypeKey dictType() {
        return dictType;
    }

    /**
     * 执行 value 对应的业务操作。
     *
     * @return 处理结果
     */
    public DictDataValue value() {
        return value;
    }

    /**
     * 执行 label 对应的业务操作。
     *
     * @return 处理结果
     */
    public String label() {
        return label;
    }

    /**
     * 执行 sort 对应的业务操作。
     *
     * @return 处理结果
     */
    public Integer sort() {
        return sort;
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
     * 执行 color Type 对应的业务操作。
     *
     * @return 处理结果
     */
    public String colorType() {
        return colorType;
    }

    /**
     * 执行 css Class 对应的业务操作。
     *
     * @return 处理结果
     */
    public String cssClass() {
        return cssClass;
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
        return o instanceof DictData d && Objects.equals(id, d.id);
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
