package com.develop.mvp.pk.module.system.infrastructure.permission.persistence;

import cn.hutool.core.collection.CollUtil;
import com.develop.mvp.pk.framework.common.util.collection.CollectionUtils;
import com.develop.mvp.pk.module.system.dal.dataobject.permission.UserRoleDO;
import com.develop.mvp.pk.module.system.dal.mysql.permission.UserRoleMapper;
import com.develop.mvp.pk.module.system.domain.permission.repository.UserRoleRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * User Role Repository Impl 领域仓储实现。
 */
@Repository
public class UserRoleRepositoryImpl implements UserRoleRepository {
    private final UserRoleMapper mapper;
    /**
     * 创建 UserRoleRepositoryImpl 实例。
     *
     * @param mapper mapper 参数
     */
    public UserRoleRepositoryImpl(UserRoleMapper mapper) { this.mapper = mapper; }

    /**
     * 执行 assign 对应的业务操作。
     *
     * @param userId userId 参数
     * @param roleIds roleIds 参数
     */
    @Override
    public void assign(Long userId, Set<Long> roleIds) {
        Set<Long> db = convertSet(mapper.selectListByUserId(userId), UserRoleDO::getRoleId);
        Set<Long> ids = CollUtil.emptyIfNull(roleIds);
        Collection<Long> create = CollUtil.subtract(ids, db);
        Collection<Long> delete = CollUtil.subtract(db, ids);
        if (!create.isEmpty()) mapper.insertBatch(CollectionUtils.convertList(create,
                roleId -> new UserRoleDO().setUserId(userId).setRoleId(roleId)));
        if (!delete.isEmpty()) mapper.deleteListByUserIdAndRoleIdIds(userId, delete);
    }

    /**
     * 查询 find By User Id 对应的数据。
     *
     * @param userId userId 参数
     * @return 处理结果
     */
    @Override
    public Set<Long> findByUserId(Long userId) {
        return convertSet(mapper.selectListByUserId(userId), UserRoleDO::getRoleId);
    }

    /**
     * 查询 find By Role Ids 对应的数据。
     *
     * @param roleIds roleIds 参数
     * @return 处理结果
     */
    @Override
    public Set<Long> findByRoleIds(Collection<Long> roleIds) {
        return convertSet(mapper.selectListByRoleIds(roleIds), UserRoleDO::getUserId);
    }

    /**
     * 删除 delete By User Id 对应的数据。
     *
     * @param userId userId 参数
     */
    @Override
    public void deleteByUserId(Long userId) { mapper.deleteListByUserId(userId); }

    /**
     * 删除 delete By Role Id 对应的数据。
     *
     * @param roleId roleId 参数
     */
    @Override
    public void deleteByRoleId(Long roleId) { mapper.deleteListByRoleId(roleId); }
}
