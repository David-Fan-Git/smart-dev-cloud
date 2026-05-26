package com.develop.mvp.pk.module.system.api.permission;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.biz.system.permission.dto.DeptDataPermissionRespDTO;
import com.develop.mvp.pk.module.system.application.permission.port.inbound.PermissionUseCase;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.Set;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

/**
 * Permission Api Impl 模块 API 实现。
 */
@RestController // 提供 RESTful API 接口，给 Feign 调用
@Validated
@Primary // 由于 PermissionCommonApi 的存在，必须声明为 @Primary Bean
public class PermissionApiImpl implements PermissionApi {

    @Resource
    private PermissionUseCase permissionService;

    /**
     * 查询 get User Role Id List By Role Ids 对应的数据。
     *
     * @param roleIds roleIds 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<Set<Long>> getUserRoleIdListByRoleIds(Collection<Long> roleIds) {
        return success(permissionService.getUserRoleIdListByRoleId(roleIds));
    }

    /**
     * 判断 has Any Permissions 对应的条件是否成立。
     *
     * @param userId userId 参数
     * @param permissions permissions 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<Boolean> hasAnyPermissions(Long userId, String... permissions) {
        return success(permissionService.hasAnyPermissions(userId, permissions));
    }

    /**
     * 判断 has Any Roles 对应的条件是否成立。
     *
     * @param userId userId 参数
     * @param roles roles 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<Boolean> hasAnyRoles(Long userId, String... roles) {
        return success(permissionService.hasAnyRoles(userId, roles));
    }

    /**
     * 查询 get Dept Data Permission 对应的数据。
     *
     * @param userId userId 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<DeptDataPermissionRespDTO> getDeptDataPermission(Long userId) {
        return success(permissionService.getDeptDataPermission(userId));
    }

}
