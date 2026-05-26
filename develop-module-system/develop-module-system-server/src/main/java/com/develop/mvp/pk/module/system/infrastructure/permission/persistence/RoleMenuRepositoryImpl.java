package com.develop.mvp.pk.module.system.infrastructure.permission.persistence;

import cn.hutool.core.collection.CollUtil;
import com.develop.mvp.pk.framework.common.util.collection.CollectionUtils;
import com.develop.mvp.pk.module.system.dal.dataobject.permission.RoleMenuDO;
import com.develop.mvp.pk.module.system.dal.mysql.permission.RoleMenuMapper;
import com.develop.mvp.pk.module.system.domain.permission.repository.RoleMenuRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * Role Menu Repository Impl 领域仓储实现。
 */
@Repository
public class RoleMenuRepositoryImpl implements RoleMenuRepository {
    private final RoleMenuMapper mapper;
    /**
     * 创建 RoleMenuRepositoryImpl 实例。
     *
     * @param mapper mapper 参数
     */
    public RoleMenuRepositoryImpl(RoleMenuMapper mapper) { this.mapper = mapper; }

    /**
     * 执行 assign 对应的业务操作。
     *
     * @param roleId roleId 参数
     * @param menuIds menuIds 参数
     */
    @Override
    public void assign(Long roleId, Set<Long> menuIds) {
        Set<Long> db = convertSet(mapper.selectListByRoleId(roleId), RoleMenuDO::getMenuId);
        Set<Long> ids = CollUtil.emptyIfNull(menuIds);
        Collection<Long> create = CollUtil.subtract(ids, db);
        Collection<Long> delete = CollUtil.subtract(db, ids);
        if (!create.isEmpty()) mapper.insertBatch(CollectionUtils.convertList(create,
                menuId -> new RoleMenuDO().setRoleId(roleId).setMenuId(menuId)));
        if (!delete.isEmpty()) mapper.deleteListByRoleIdAndMenuIds(roleId, delete);
    }

    /**
     * 查询 find By Role Id 对应的数据。
     *
     * @param roleId roleId 参数
     * @return 处理结果
     */
    @Override
    public Set<Long> findByRoleId(Long roleId) {
        return convertSet(mapper.selectListByRoleId(roleId), RoleMenuDO::getMenuId);
    }

    /**
     * 查询 find By Role Ids 对应的数据。
     *
     * @param roleIds roleIds 参数
     * @return 处理结果
     */
    @Override
    public Set<Long> findByRoleIds(Collection<Long> roleIds) {
        return convertSet(mapper.selectListByRoleId(roleIds), RoleMenuDO::getMenuId);
    }

    /**
     * 查询 find By Menu Id 对应的数据。
     *
     * @param menuId menuId 参数
     * @return 处理结果
     */
    @Override
    public Set<Long> findByMenuId(Long menuId) {
        return convertSet(mapper.selectListByMenuId(menuId), RoleMenuDO::getRoleId);
    }

    /**
     * 删除 delete By Role Id 对应的数据。
     *
     * @param roleId roleId 参数
     */
    @Override
    public void deleteByRoleId(Long roleId) { mapper.deleteListByRoleId(roleId); }

    /**
     * 删除 delete By Menu Id 对应的数据。
     *
     * @param menuId menuId 参数
     */
    @Override
    public void deleteByMenuId(Long menuId) { mapper.deleteListByMenuId(menuId); }
}
