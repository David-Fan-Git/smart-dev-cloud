package com.develop.mvp.pk.module.system.application.permission.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.develop.mvp.pk.framework.common.biz.system.permission.dto.DeptDataPermissionRespDTO;
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.util.collection.CollectionUtils;
import com.develop.mvp.pk.framework.datapermission.core.annotation.DataPermission;
import com.develop.mvp.pk.module.system.application.dept.port.inbound.DeptUseCase;
import com.develop.mvp.pk.module.system.application.permission.port.inbound.MenuUseCase;
import com.develop.mvp.pk.module.system.application.permission.port.inbound.PermissionUseCase;
import com.develop.mvp.pk.module.system.application.permission.port.inbound.RoleUseCase;
import com.develop.mvp.pk.module.system.application.user.port.inbound.AdminUserUseCase;
import com.develop.mvp.pk.module.system.dal.dataobject.permission.MenuDO;
import com.develop.mvp.pk.module.system.dal.dataobject.permission.RoleDO;
import com.develop.mvp.pk.module.system.dal.redis.RedisKeyConstants;
import com.develop.mvp.pk.module.system.domain.permission.repository.RoleMenuRepository;
import com.develop.mvp.pk.module.system.domain.permission.repository.UserRoleRepository;
import com.develop.mvp.pk.module.system.enums.permission.DataScopeEnum;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Supplier;

import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertSet;
import static com.develop.mvp.pk.framework.common.util.json.JsonUtils.toJsonString;

/**
 * Permission Application Service 应用服务。
 */
@Slf4j
public class PermissionApplicationService implements PermissionUseCase {

    private final UserRoleRepository userRoleRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final RoleUseCase roleService;
    private final MenuUseCase menuService;
    private final DeptUseCase deptUseCase;
    private final AdminUserUseCase userService;

    /**
     * 创建 PermissionApplicationService 实例。
     *
     * @param userRoleRepository userRoleRepository 参数
     * @param roleMenuRepository roleMenuRepository 参数
     * @param roleService roleService 参数
     * @param menuService menuService 参数
     * @param deptUseCase deptUseCase 参数
     * @param userService userService 参数
     */
    public PermissionApplicationService(UserRoleRepository userRoleRepository,
                                        RoleMenuRepository roleMenuRepository,
                                        @Lazy RoleUseCase roleService,
                                        @Lazy MenuUseCase menuService,
                                        DeptUseCase deptUseCase,
                                        AdminUserUseCase userService) {
        this.userRoleRepository = userRoleRepository;
        this.roleMenuRepository = roleMenuRepository;
        this.roleService = roleService;
        this.menuService = menuService;
        this.deptUseCase = deptUseCase;
        this.userService = userService;
    }

    /**
     * 判断 has Any Permissions 对应的条件是否成立。
     *
     * @param userId userId 参数
     * @param permissions permissions 参数
     * @return 处理结果
     */
    @Override
    public boolean hasAnyPermissions(Long userId, String... permissions) {
        if (ArrayUtil.isEmpty(permissions)) {
            return true;
        }
        List<RoleDO> roles = getEnableUserRoleListByUserIdFromCache(userId);
        if (CollUtil.isEmpty(roles)) {
            return false;
        }
        for (String permission : permissions) {
            if (hasAnyPermission(roles, permission)) {
                return true;
            }
        }
        return roleService.hasAnySuperAdmin(convertSet(roles, RoleDO::getId));
    }

    /**
     * 判断 has Any Permission 对应的条件是否成立。
     *
     * @param roles roles 参数
     * @param permission permission 参数
     * @return 处理结果
     */
    private boolean hasAnyPermission(List<RoleDO> roles, String permission) {
        List<Long> menuIds = menuService.getMenuIdListByPermissionFromCache(permission);
        if (CollUtil.isEmpty(menuIds)) {
            return false;
        }
        Set<Long> roleIds = convertSet(roles, RoleDO::getId);
        for (Long menuId : menuIds) {
            Set<Long> menuRoleIds = getSelf().getMenuRoleIdListByMenuIdFromCache(menuId);
            if (CollUtil.containsAny(menuRoleIds, roleIds)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断 has Any Roles 对应的条件是否成立。
     *
     * @param userId userId 参数
     * @param roles roles 参数
     * @return 处理结果
     */
    @Override
    public boolean hasAnyRoles(Long userId, String... roles) {
        if (ArrayUtil.isEmpty(roles)) {
            return true;
        }
        List<RoleDO> roleList = getEnableUserRoleListByUserIdFromCache(userId);
        if (CollUtil.isEmpty(roleList)) {
            return false;
        }
        Set<String> userRoles = convertSet(roleList, RoleDO::getCode);
        return CollUtil.containsAny(userRoles, Sets.newHashSet(roles));
    }

    /**
     * 执行 assign Role Menu 对应的业务操作。
     *
     * @param roleId roleId 参数
     * @param menuIds menuIds 参数
     */
    @Override
    @DSTransactional
    @Caching(evict = {
            @CacheEvict(value = RedisKeyConstants.MENU_ROLE_ID_LIST, allEntries = true),
            @CacheEvict(value = RedisKeyConstants.PERMISSION_MENU_ID_LIST, allEntries = true)
    })
    public void assignRoleMenu(Long roleId, Set<Long> menuIds) {
        roleMenuRepository.assign(roleId, menuIds);
    }

    /**
     * 处理 process Role Deleted 对应的业务逻辑。
     *
     * @param roleId roleId 参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = RedisKeyConstants.MENU_ROLE_ID_LIST, allEntries = true),
            @CacheEvict(value = RedisKeyConstants.USER_ROLE_ID_LIST, allEntries = true)
    })
    public void processRoleDeleted(Long roleId) {
        userRoleRepository.deleteByRoleId(roleId);
        roleMenuRepository.deleteByRoleId(roleId);
    }

    /**
     * 处理 process Menu Deleted 对应的业务逻辑。
     *
     * @param menuId menuId 参数
     */
    @Override
    @CacheEvict(value = RedisKeyConstants.MENU_ROLE_ID_LIST, key = "#menuId")
    public void processMenuDeleted(Long menuId) {
        roleMenuRepository.deleteByMenuId(menuId);
    }

    /**
     * 查询 get Role Menu List By Role Id 对应的数据。
     *
     * @param roleId roleId 参数
     * @return 处理结果
     */
    @Override
    public Set<Long> getRoleMenuListByRoleId(Long roleId) {
        return getRoleMenuListByRoleId(Collections.singleton(roleId));
    }

    /**
     * 查询 get Role Menu List By Role Id 对应的数据。
     *
     * @param roleIds roleIds 参数
     * @return 处理结果
     */
    @Override
    public Set<Long> getRoleMenuListByRoleId(Collection<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return Collections.emptySet();
        }
        if (roleService.hasAnySuperAdmin(roleIds)) {
            return convertSet(menuService.getMenuList(), MenuDO::getId);
        }
        return getRoleMenuIds(roleIds);
    }

    /**
     * 查询 get Role Menu Ids 对应的数据。
     *
     * @param roleId roleId 参数
     * @return 处理结果
     */
    @Override
    public Set<Long> getRoleMenuIds(Long roleId) {
        return roleMenuRepository.findByRoleId(roleId);
    }

    /**
     * 查询 get Role Menu Ids 对应的数据。
     *
     * @param roleIds roleIds 参数
     * @return 处理结果
     */
    @Override
    public Set<Long> getRoleMenuIds(Collection<Long> roleIds) {
        return roleMenuRepository.findByRoleIds(roleIds);
    }

    /**
     * 查询 get Menu Role Id List By Menu Id From Cache 对应的数据。
     *
     * @param menuId menuId 参数
     * @return 处理结果
     */
    @Override
    @Cacheable(value = RedisKeyConstants.MENU_ROLE_ID_LIST, key = "#menuId")
    public Set<Long> getMenuRoleIdListByMenuIdFromCache(Long menuId) {
        return getMenuRoleIds(menuId);
    }

    /**
     * 查询 get Menu Role Ids 对应的数据。
     *
     * @param menuId menuId 参数
     * @return 处理结果
     */
    @Override
    public Set<Long> getMenuRoleIds(Long menuId) {
        return roleMenuRepository.findByMenuId(menuId);
    }

    /**
     * 执行 assign User Role 对应的业务操作。
     *
     * @param userId userId 参数
     * @param roleIds roleIds 参数
     */
    @Override
    @DSTransactional
    @CacheEvict(value = RedisKeyConstants.USER_ROLE_ID_LIST, key = "#userId")
    public void assignUserRole(Long userId, Set<Long> roleIds) {
        userRoleRepository.assign(userId, roleIds);
    }

    /**
     * 处理 process User Deleted 对应的业务逻辑。
     *
     * @param userId userId 参数
     */
    @Override
    @CacheEvict(value = RedisKeyConstants.USER_ROLE_ID_LIST, key = "#userId")
    public void processUserDeleted(Long userId) {
        userRoleRepository.deleteByUserId(userId);
    }

    /**
     * 查询 get User Role Id List By User Id 对应的数据。
     *
     * @param userId userId 参数
     * @return 处理结果
     */
    @Override
    public Set<Long> getUserRoleIdListByUserId(Long userId) {
        return getUserRoleIds(userId);
    }

    /**
     * 查询 get User Role Id List By User Id From Cache 对应的数据。
     *
     * @param userId userId 参数
     * @return 处理结果
     */
    @Override
    @Cacheable(value = RedisKeyConstants.USER_ROLE_ID_LIST, key = "#userId")
    public Set<Long> getUserRoleIdListByUserIdFromCache(Long userId) {
        return getUserRoleIdListByUserId(userId);
    }

    /**
     * 查询 get User Role Ids 对应的数据。
     *
     * @param userId userId 参数
     * @return 处理结果
     */
    @Override
    public Set<Long> getUserRoleIds(Long userId) {
        return userRoleRepository.findByUserId(userId);
    }

    /**
     * 查询 get User Role Id List By Role Id 对应的数据。
     *
     * @param roleIds roleIds 参数
     * @return 处理结果
     */
    @Override
    public Set<Long> getUserRoleIdListByRoleId(Collection<Long> roleIds) {
        return getUserIdsByRoleIds(roleIds);
    }

    /**
     * 查询 get User Ids By Role Ids 对应的数据。
     *
     * @param roleIds roleIds 参数
     * @return 处理结果
     */
    @Override
    public Set<Long> getUserIdsByRoleIds(Collection<Long> roleIds) {
        return userRoleRepository.findByRoleIds(roleIds);
    }

    /**
     * 查询 get Enable User Role List By User Id From Cache 对应的数据。
     *
     * @param userId userId 参数
     * @return 处理结果
     */
    @Override
    @VisibleForTesting
    public List<RoleDO> getEnableUserRoleListByUserIdFromCache(Long userId) {
        Set<Long> roleIds = getSelf().getUserRoleIdListByUserIdFromCache(userId);
        List<RoleDO> roles = roleService.getRoleListFromCache(roleIds);
        roles.removeIf(role -> !CommonStatusEnum.ENABLE.getStatus().equals(role.getStatus()));
        return roles;
    }

    /**
     * 执行 assign Role Data Scope 对应的业务操作。
     *
     * @param roleId roleId 参数
     * @param dataScope dataScope 参数
     * @param dataScopeDeptIds dataScopeDeptIds 参数
     */
    @Override
    public void assignRoleDataScope(Long roleId, Integer dataScope, Set<Long> dataScopeDeptIds) {
        roleService.updateRoleDataScope(roleId, dataScope, dataScopeDeptIds);
    }

    /**
     * 查询 get Dept Data Permission 对应的数据。
     *
     * @param userId userId 参数
     * @return 处理结果
     */
    @Override
    @DataPermission(enable = false)
    public DeptDataPermissionRespDTO getDeptDataPermission(Long userId) {
        List<RoleDO> roles = getEnableUserRoleListByUserIdFromCache(userId);
        DeptDataPermissionRespDTO result = new DeptDataPermissionRespDTO();
        if (CollUtil.isEmpty(roles)) {
            result.setSelf(true);
            return result;
        }
        Supplier<Long> userDeptId = Suppliers.memoize(() -> userService.getUser(userId).getDeptId());
        for (RoleDO role : roles) {
            if (role.getDataScope() == null) {
                continue;
            }
            if (Objects.equals(role.getDataScope(), DataScopeEnum.ALL.getScope())) {
                result.setAll(true);
                continue;
            }
            if (Objects.equals(role.getDataScope(), DataScopeEnum.DEPT_CUSTOM.getScope())) {
                CollUtil.addAll(result.getDeptIds(), role.getDataScopeDeptIds());
                CollectionUtils.addIfNotNull(result.getDeptIds(), userDeptId.get());
                continue;
            }
            if (Objects.equals(role.getDataScope(), DataScopeEnum.DEPT_ONLY.getScope())) {
                CollectionUtils.addIfNotNull(result.getDeptIds(), userDeptId.get());
                continue;
            }
            if (Objects.equals(role.getDataScope(), DataScopeEnum.DEPT_AND_CHILD.getScope())) {
                Long deptId = userDeptId.get();
                if (deptId == null) {
                    continue;
                }
                CollUtil.addAll(result.getDeptIds(), deptUseCase.getChildDeptIdListFromCache(deptId));
                result.getDeptIds().add(deptId);
                continue;
            }
            if (Objects.equals(role.getDataScope(), DataScopeEnum.SELF.getScope())) {
                result.setSelf(true);
                continue;
            }
            log.error("[getDeptDataPermission][LoginUser({}) role({}) 无法处理]", userId, toJsonString(result));
        }
        return result;
    }

    /**
     * 查询 get Self 对应的数据。
     *
     * @return 处理结果
     */
    private PermissionApplicationService getSelf() {
        return SpringUtil.getBean(getClass());
    }
}
