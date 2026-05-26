package com.develop.mvp.pk.module.system.application.permission.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.system.application.permission.port.inbound.MenuUseCase;
import com.develop.mvp.pk.module.system.application.permission.port.inbound.PermissionUseCase;
import com.develop.mvp.pk.module.system.application.tenant.port.inbound.TenantUseCase;
import com.develop.mvp.pk.module.system.controller.admin.permission.vo.menu.MenuListReqVO;
import com.develop.mvp.pk.module.system.controller.admin.permission.vo.menu.MenuSaveVO;
import com.develop.mvp.pk.module.system.dal.dataobject.permission.MenuDO;
import com.develop.mvp.pk.module.system.dal.mysql.permission.MenuMapper;
import com.develop.mvp.pk.module.system.dal.redis.RedisKeyConstants;
import com.develop.mvp.pk.module.system.enums.permission.MenuTypeEnum;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertList;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertMap;
import static com.develop.mvp.pk.module.system.dal.dataobject.permission.MenuDO.ID_ROOT;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.*;

/**
 * Menu Application Service 应用服务。
 */
@Slf4j
public class MenuApplicationService implements MenuUseCase {

    private final MenuMapper menuMapper;
    private final PermissionUseCase permissionService;
    private final TenantUseCase tenantService;

    /**
     * 创建 MenuApplicationService 实例。
     *
     * @param menuMapper menuMapper 参数
     * @param permissionService permissionService 参数
     * @param tenantService tenantService 参数
     */
    public MenuApplicationService(MenuMapper menuMapper,
                                  @Lazy PermissionUseCase permissionService,
                                  @Lazy TenantUseCase tenantService) {
        this.menuMapper = menuMapper;
        this.permissionService = permissionService;
        this.tenantService = tenantService;
    }

    @CacheEvict(value = RedisKeyConstants.PERMISSION_MENU_ID_LIST, key = "#createReqVO.permission",
            condition = "#createReqVO.permission != null")
    /**
     * 创建 create Menu 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @return 处理结果
     */
    public Long createMenu(MenuSaveVO createReqVO) {
        validateParentMenu(createReqVO.getParentId(), null);
        validateMenuName(createReqVO.getParentId(), createReqVO.getName(), null);
        validateMenuComponentName(createReqVO.getComponentName(), null);
        MenuDO menu = BeanUtils.toBean(createReqVO, MenuDO.class);
        initMenuProperty(menu);
        menuMapper.insert(menu);
        return menu.getId();
    }

    /**
     * 更新 update Menu 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     */
    @CacheEvict(value = RedisKeyConstants.PERMISSION_MENU_ID_LIST, allEntries = true)
    public void updateMenu(MenuSaveVO updateReqVO) {
        if (menuMapper.selectById(updateReqVO.getId()) == null) {
            throw exception(MENU_NOT_EXISTS);
        }
        validateParentMenu(updateReqVO.getParentId(), updateReqVO.getId());
        validateMenuName(updateReqVO.getParentId(), updateReqVO.getName(), updateReqVO.getId());
        validateMenuComponentName(updateReqVO.getComponentName(), updateReqVO.getId());
        MenuDO updateObj = BeanUtils.toBean(updateReqVO, MenuDO.class);
        initMenuProperty(updateObj);
        menuMapper.updateById(updateObj);
    }

    /**
     * 删除 delete Menu 对应的数据。
     *
     * @param id id 参数
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = RedisKeyConstants.PERMISSION_MENU_ID_LIST, allEntries = true)
    public void deleteMenu(Long id) {
        if (menuMapper.selectCountByParentId(id) > 0) {
            throw exception(MENU_EXISTS_CHILDREN);
        }
        if (menuMapper.selectById(id) == null) {
            throw exception(MENU_NOT_EXISTS);
        }
        menuMapper.deleteById(id);
        permissionService.processMenuDeleted(id);
    }

    /**
     * 删除 delete Menu List 对应的数据。
     *
     * @param ids ids 参数
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = RedisKeyConstants.PERMISSION_MENU_ID_LIST, allEntries = true)
    public void deleteMenuList(List<Long> ids) {
        ids.forEach(id -> {
            if (menuMapper.selectCountByParentId(id) > 0) {
                throw exception(MENU_EXISTS_CHILDREN);
            }
        });
        menuMapper.deleteByIds(ids);
        ids.forEach(id -> permissionService.processMenuDeleted(id));
    }

    /**
     * 查询 get Menu List 对应的数据。
     *
     * @return 处理结果
     */
    public List<MenuDO> getMenuList() {
        return menuMapper.selectList();
    }

    /**
     * 查询 get Menu List By Tenant 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    public List<MenuDO> getMenuListByTenant(MenuListReqVO reqVO) {
        List<MenuDO> menus = getMenuList(reqVO);
        tenantService.handleTenantMenu(menuIds -> menus.removeIf(menu -> !CollUtil.contains(menuIds, menu.getId())));
        return menus;
    }

    /**
     * 执行 filter Disable Menus 对应的业务操作。
     *
     * @param menuList menuList 参数
     * @return 处理结果
     */
    public List<MenuDO> filterDisableMenus(List<MenuDO> menuList) {
        if (CollUtil.isEmpty(menuList)) {
            return Collections.emptyList();
        }
        Map<Long, MenuDO> menuMap = convertMap(menuList, MenuDO::getId);
        List<MenuDO> enabledMenus = new ArrayList<>();
        Set<Long> disabledMenuCache = new HashSet<>();
        for (MenuDO menu : menuList) {
            if (isMenuDisabled(menu, menuMap, disabledMenuCache)) {
                continue;
            }
            enabledMenus.add(menu);
        }
        return enabledMenus;
    }

    /**
     * 判断 is Menu Disabled 对应的条件是否成立。
     *
     * @param node node 参数
     * @param menuMap menuMap 参数
     * @param disabledMenuCache disabledMenuCache 参数
     * @return 处理结果
     */
    private boolean isMenuDisabled(MenuDO node, Map<Long, MenuDO> menuMap, Set<Long> disabledMenuCache) {
        if (disabledMenuCache.contains(node.getId())) {
            return true;
        }
        if (CommonStatusEnum.isDisable(node.getStatus())) {
            disabledMenuCache.add(node.getId());
            return true;
        }
        Long parentId = node.getParentId();
        if (ObjUtil.equal(parentId, ID_ROOT)) {
            return false;
        }
        MenuDO parent = menuMap.get(parentId);
        if (parent == null || isMenuDisabled(parent, menuMap, disabledMenuCache)) {
            disabledMenuCache.add(node.getId());
            return true;
        }
        return false;
    }

    /**
     * 查询 get Menu List 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    public List<MenuDO> getMenuList(MenuListReqVO reqVO) {
        return menuMapper.selectList(reqVO);
    }

    /**
     * 查询 get Menu Id List By Permission From Cache 对应的数据。
     *
     * @param permission permission 参数
     * @return 处理结果
     */
    @Cacheable(value = RedisKeyConstants.PERMISSION_MENU_ID_LIST, key = "#permission")
    public List<Long> getMenuIdListByPermissionFromCache(String permission) {
        List<MenuDO> menus = menuMapper.selectListByPermission(permission);
        return convertList(menus, MenuDO::getId);
    }

    /**
     * 查询 get Menu 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    public MenuDO getMenu(Long id) {
        return menuMapper.selectById(id);
    }

    /**
     * 查询 get Menu List 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    public List<MenuDO> getMenuList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Lists.newArrayList();
        }
        return menuMapper.selectByIds(ids);
    }

    /**
     * 校验 validate Parent Menu 对应的业务规则。
     *
     * @param parentId parentId 参数
     * @param childId childId 参数
     */
    @VisibleForTesting
    public void validateParentMenu(Long parentId, Long childId) {
        if (parentId == null || ID_ROOT.equals(parentId)) {
            return;
        }
        if (parentId.equals(childId)) {
            throw exception(MENU_PARENT_ERROR);
        }
        MenuDO menu = menuMapper.selectById(parentId);
        if (menu == null) {
            throw exception(MENU_PARENT_NOT_EXISTS);
        }
        if (!MenuTypeEnum.DIR.getType().equals(menu.getType())
                && !MenuTypeEnum.MENU.getType().equals(menu.getType())) {
            throw exception(MENU_PARENT_NOT_DIR_OR_MENU);
        }
    }

    /**
     * 校验 validate Menu Name 对应的业务规则。
     *
     * @param parentId parentId 参数
     * @param name name 参数
     * @param id id 参数
     */
    @VisibleForTesting
    public void validateMenuName(Long parentId, String name, Long id) {
        MenuDO menu = menuMapper.selectByParentIdAndName(parentId, name);
        if (menu == null) {
            return;
        }
        if (id == null || !menu.getId().equals(id)) {
            throw exception(MENU_NAME_DUPLICATE);
        }
    }

    /**
     * 校验 validate Menu Component Name 对应的业务规则。
     *
     * @param componentName componentName 参数
     * @param id id 参数
     */
    @VisibleForTesting
    public void validateMenuComponentName(String componentName, Long id) {
        if (StrUtil.isBlank(componentName)) {
            return;
        }
        MenuDO menu = menuMapper.selectByComponentName(componentName);
        if (menu == null) {
            return;
        }
        if (id == null || !menu.getId().equals(id)) {
            throw exception(MENU_COMPONENT_NAME_DUPLICATE);
        }
    }

    /**
     * 执行 init Menu Property 对应的业务操作。
     *
     * @param menu menu 参数
     */
    private void initMenuProperty(MenuDO menu) {
        if (MenuTypeEnum.BUTTON.getType().equals(menu.getType())) {
            menu.setComponent("");
            menu.setComponentName("");
            menu.setIcon("");
            menu.setPath("");
        }
    }
}
