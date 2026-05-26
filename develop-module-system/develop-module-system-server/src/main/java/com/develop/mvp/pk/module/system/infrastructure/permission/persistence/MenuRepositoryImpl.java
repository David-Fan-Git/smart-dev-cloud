package com.develop.mvp.pk.module.system.infrastructure.permission.persistence;

import cn.hutool.core.collection.CollUtil;
import com.develop.mvp.pk.module.system.dal.dataobject.permission.MenuDO;
import com.develop.mvp.pk.module.system.dal.mysql.permission.MenuMapper;
import com.develop.mvp.pk.module.system.domain.permission.Menu;
import com.develop.mvp.pk.module.system.domain.permission.MenuFactory;
import com.develop.mvp.pk.module.system.domain.permission.repository.MenuRepository;
import com.develop.mvp.pk.module.system.domain.permission.valueobject.MenuId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Menu Repository Impl 领域仓储实现。
 */
@Repository
public class MenuRepositoryImpl implements MenuRepository {

    private final MenuMapper menuMapper;

    /**
     * 创建 MenuRepositoryImpl 实例。
     *
     * @param menuMapper menuMapper 参数
     */
    public MenuRepositoryImpl(MenuMapper menuMapper) { this.menuMapper = menuMapper; }

    /**
     * 创建 save 对应的数据。
     *
     * @param menu menu 参数
     * @return 处理结果
     */
    @Override
    @Transactional
    public Menu save(Menu menu) {
        MenuDO d = toDataObject(menu);
        if (menuMapper.selectById(menu.id().value()) == null) menuMapper.insert(d);
        else menuMapper.updateById(d);
        return menu;
    }

    /**
     * 删除 delete 对应的数据。
     *
     */
    @Override
    @Transactional
    public void delete(MenuId id) { menuMapper.deleteById(id.value()); }

    /**
     * 查询 find By Id 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @Override
    public Menu findById(MenuId id) {
        MenuDO d = menuMapper.selectById(id.value());
        return d != null ? toDomain(d) : null;
    }

    /**
     * 查询 find By Ids 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    @Override
    public List<Menu> findByIds(Collection<MenuId> ids) {
        if (CollUtil.isEmpty(ids)) return Collections.emptyList();
        return menuMapper.selectByIds(ids.stream().map(MenuId::value).collect(Collectors.toList()))
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    /**
     * 查询 find All 对应的数据。
     *
     * @return 处理结果
     */
    @Override
    public List<Menu> findAll() {
        return menuMapper.selectList().stream().map(this::toDomain).collect(Collectors.toList());
    }

    /**
     * 查询 find By Permission 对应的数据。
     *
     * @param permission permission 参数
     * @return 处理结果
     */
    @Override
    public List<Menu> findByPermission(String permission) {
        return menuMapper.selectListByPermission(permission).stream().map(this::toDomain).collect(Collectors.toList());
    }

    /**
     * 查询 find By Parent Id And Name 对应的数据。
     *
     * @param parentId parentId 参数
     * @param name name 参数
     * @return 处理结果
     */
    @Override
    public Optional<Menu> findByParentIdAndName(Long parentId, String name) {
        return Optional.ofNullable(menuMapper.selectByParentIdAndName(parentId, name)).map(this::toDomain);
    }

    /**
     * 查询 find By Component Name 对应的数据。
     *
     * @param componentName componentName 参数
     * @return 处理结果
     */
    @Override
    public Optional<Menu> findByComponentName(String componentName) {
        return Optional.ofNullable(menuMapper.selectByComponentName(componentName)).map(this::toDomain);
    }

    /**
     * 查询 count By Parent Id 对应的数据。
     *
     * @param parentId parentId 参数
     * @return 处理结果
     */
    @Override
    public long countByParentId(MenuId parentId) {
        return menuMapper.selectCountByParentId(parentId.value());
    }

    /**
     * 执行 to Data Object 对应的业务操作。
     *
     * @param m m 参数
     * @return 处理结果
     */
    private MenuDO toDataObject(Menu m) {
        MenuDO d = new MenuDO();
        d.setId(m.id().value()); d.setName(m.name().value());
        d.setPermission(m.permission().value()); d.setType(m.type().code());
        d.setSort(m.sort()); d.setParentId(m.parentId().value());
        d.setPath(m.path()); d.setIcon(m.icon());
        d.setComponent(m.component()); d.setComponentName(m.componentName());
        d.setStatus(m.status()); d.setVisible(m.visible());
        d.setKeepAlive(m.keepAlive()); d.setAlwaysShow(m.alwaysShow());
        return d;
    }

    /**
     * 执行 to Domain 对应的业务操作。
     *
     * @param d d 参数
     * @return 处理结果
     */
    private Menu toDomain(MenuDO d) {
        return MenuFactory.reconstitute(d.getId(), d.getName(), d.getPermission(), d.getType(),
                d.getSort(), d.getParentId(), d.getPath(), d.getIcon(), d.getComponent(),
                d.getComponentName(), d.getStatus(), d.getVisible(), d.getKeepAlive(), d.getAlwaysShow());
    }
}
