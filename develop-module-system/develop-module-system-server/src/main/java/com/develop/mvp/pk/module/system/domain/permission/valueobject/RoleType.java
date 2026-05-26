package com.develop.mvp.pk.module.system.domain.permission.valueobject;

import com.develop.mvp.pk.module.system.enums.permission.RoleTypeEnum;
import java.util.Objects;

/**
 * Role Type 值对象。
 */
public final class RoleType {
    public static final RoleType SYSTEM = new RoleType(RoleTypeEnum.SYSTEM.getType());
    public static final RoleType CUSTOM = new RoleType(RoleTypeEnum.CUSTOM.getType());
    private final Integer code;
    /**
     * 创建 RoleType 实例。
     *
     * @param code code 参数
     */
    private RoleType(Integer code) { this.code = Objects.requireNonNull(code); }
    /**
     * 执行 of 对应的业务操作。
     *
     * @param code code 参数
     * @return 处理结果
     */
    public static RoleType of(Integer code) {
        if (RoleTypeEnum.SYSTEM.getType().equals(code)) return SYSTEM;
        if (RoleTypeEnum.CUSTOM.getType().equals(code)) return CUSTOM;
        throw new IllegalArgumentException("无效的角色类型: " + code);
    }
    /**
     * 执行 from Persisted 对应的业务操作。
     *
     * @param code code 参数
     * @return 处理结果
     */
    public static RoleType fromPersisted(Integer code) {
        return RoleTypeEnum.SYSTEM.getType().equals(code) ? SYSTEM : CUSTOM;
    }
    /**
     * 判断 is System 对应的条件是否成立。
     *
     * @return 处理结果
     */
    public boolean isSystem() { return code.equals(RoleTypeEnum.SYSTEM.getType()); }
    /**
     * 执行 code 对应的业务操作。
     *
     * @return 处理结果
     */
    public Integer code() { return code; }
    @Override public boolean equals(Object o) { return o instanceof RoleType r && code.equals(r.code); }
    @Override public int hashCode() { return Objects.hash(code); }
}
