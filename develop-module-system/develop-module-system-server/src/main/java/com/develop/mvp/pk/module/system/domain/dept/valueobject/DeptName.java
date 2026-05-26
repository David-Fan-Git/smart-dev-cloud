package com.develop.mvp.pk.module.system.domain.dept.valueobject;

import com.develop.mvp.pk.framework.common.exception.ServiceException;
import java.util.Objects;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.DEPT_NAME_DUPLICATE;

/**
 * Dept Name 值对象。
 */
public final class DeptName {
    private final String value;
    /**
     * 创建 DeptName 实例。
     *
     * @param value value 参数
     */
    private DeptName(String value) {
        if (value == null || value.isBlank()) throw new ServiceException(DEPT_NAME_DUPLICATE.getCode(), "部门名称不能为空");
        this.value = value.trim();
    }
    /**
     * 执行 of 对应的业务操作。
     *
     * @param value value 参数
     * @return 处理结果
     */
    public static DeptName of(String value) { return new DeptName(value); }
    /**
     * 执行 value 对应的业务操作。
     *
     * @return 处理结果
     */
    public String value() { return value; }
    @Override public boolean equals(Object o) { return o instanceof DeptName d && value.equals(d.value); }
    @Override public int hashCode() { return Objects.hash(value); }
}
