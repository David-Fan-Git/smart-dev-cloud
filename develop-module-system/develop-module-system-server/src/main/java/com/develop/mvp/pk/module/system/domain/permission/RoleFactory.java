package com.develop.mvp.pk.module.system.domain.permission;

import com.develop.mvp.pk.module.system.domain.permission.valueobject.*;

import java.util.Set;

/**
 * Role Factory 工厂。
 */
public final class RoleFactory {
    /**
     * 创建 RoleFactory 实例。
     */
    private RoleFactory() {}

    /**
     * 创建 create 对应的数据。
     *
     * @param id id 参数
     * @param name name 参数
     * @param code code 参数
     * @param sort sort 参数
     * @param status status 参数
     * @param type type 参数
     * @param remark remark 参数
     * @param tenantId tenantId 参数
     * @param dataScope dataScope 参数
     * @param dataScopeDeptIds dataScopeDeptIds 参数
     * @return 处理结果
     */
    public static Role create(Long id, String name, String code, Integer sort, Integer status,
                              Integer type, String remark, Long tenantId,
                              Integer dataScope, Set<Long> dataScopeDeptIds) {
        return new Role(id != null ? RoleId.of(id) : null, RoleName.of(name), RoleCode.of(code),
                sort, status != null ? RoleStatus.of(status) : RoleStatus.ENABLED,
                type != null ? RoleType.of(type) : RoleType.CUSTOM, remark, tenantId,
                DataScope.of(dataScope, dataScopeDeptIds), null);
    }

    /**
     * 执行 reconstitute 对应的业务操作。
     *
     * @param id id 参数
     * @param name name 参数
     * @param code code 参数
     * @param sort sort 参数
     * @param status status 参数
     * @param type type 参数
     * @param remark remark 参数
     * @param tenantId tenantId 参数
     * @param dataScope dataScope 参数
     * @param dataScopeDeptIds dataScopeDeptIds 参数
     * @param menuIds menuIds 参数
     * @return 处理结果
     */
    public static Role reconstitute(Long id, String name, String code, Integer sort,
                                     Integer status, Integer type, String remark, Long tenantId,
                                     Integer dataScope, Set<Long> dataScopeDeptIds,
                                     Set<Long> menuIds) {
        return new Role(id != null ? RoleId.of(id) : null, RoleName.of(name), RoleCode.of(code),
                sort, RoleStatus.fromPersisted(status), RoleType.fromPersisted(type), remark, tenantId,
                DataScope.of(dataScope, dataScopeDeptIds), menuIds);
    }
}
