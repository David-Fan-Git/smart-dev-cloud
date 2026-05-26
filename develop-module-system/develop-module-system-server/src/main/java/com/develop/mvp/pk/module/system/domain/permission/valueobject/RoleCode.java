package com.develop.mvp.pk.module.system.domain.permission.valueobject;

import com.develop.mvp.pk.module.system.enums.permission.RoleCodeEnum;
import java.util.Objects;

/**
 * Role Code 值对象。
 */
public final class RoleCode {
    private final String value;
    /**
     * 创建 RoleCode 实例。
     *
     * @param value value 参数
     */
    private RoleCode(String value) { this.value = Objects.requireNonNull(value, "角色编码不能为空"); }
    /**
     * 执行 of 对应的业务操作。
     *
     * @param value value 参数
     * @return 处理结果
     */
    public static RoleCode of(String value) { return new RoleCode(value); }
    /**
     * 执行 value 对应的业务操作。
     *
     * @return 处理结果
     */
    public String value() { return value; }
    /**
     * 判断 is Super Admin 对应的条件是否成立。
     *
     * @return 处理结果
     */
    public boolean isSuperAdmin() { return RoleCodeEnum.isSuperAdmin(value); }
    @Override public boolean equals(Object o) { return o instanceof RoleCode r && value.equals(r.value); }
    @Override public int hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value; }
}
