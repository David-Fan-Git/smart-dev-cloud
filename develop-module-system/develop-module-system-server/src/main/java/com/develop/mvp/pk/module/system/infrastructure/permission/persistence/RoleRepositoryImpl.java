package com.develop.mvp.pk.module.system.infrastructure.permission.persistence;

import cn.hutool.core.collection.CollUtil;
import com.develop.mvp.pk.framework.common.pojo.PageParam;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.system.dal.dataobject.permission.RoleDO;
import com.develop.mvp.pk.module.system.dal.mysql.permission.RoleMapper;
import com.develop.mvp.pk.module.system.dal.mysql.permission.RoleMenuMapper;
import com.develop.mvp.pk.module.system.dal.dataobject.permission.RoleMenuDO;
import com.develop.mvp.pk.module.system.domain.permission.Role;
import com.develop.mvp.pk.module.system.domain.permission.RoleFactory;
import com.develop.mvp.pk.module.system.domain.permission.repository.RoleRepository;
import com.develop.mvp.pk.module.system.domain.permission.valueobject.RoleId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * Role Repository Impl 领域仓储实现。
 */
@Repository
public class RoleRepositoryImpl implements RoleRepository {

    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;

    /**
     * 创建 RoleRepositoryImpl 实例。
     *
     * @param roleMapper roleMapper 参数
     * @param roleMenuMapper roleMenuMapper 参数
     */
    public RoleRepositoryImpl(RoleMapper roleMapper, RoleMenuMapper roleMenuMapper) {
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    /**
     * 创建 save 对应的数据。
     *
     * @param role role 参数
     * @return 处理结果
     */
    @Override
    @Transactional
    public Role save(Role role) {
        RoleDO roleDO = toDataObject(role);
        if (role.id() == null) {
            roleMapper.insert(roleDO);
            return toDomain(roleDO);
        }
        roleMapper.updateById(roleDO);
        return toDomain(roleDO);
    }

    /**
     * 删除 delete 对应的数据。
     *
     * @param id id 参数
     */
    @Override
    @Transactional
    public void delete(RoleId id) {
        roleMapper.deleteById(id.value());
    }

    /**
     * 查询 find By Id 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @Override
    public Role findById(RoleId id) {
        RoleDO d = roleMapper.selectById(id.value());
        return d != null ? toDomain(d) : null;
    }

    /**
     * 查询 find By Ids 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    @Override
    public List<Role> findByIds(Collection<RoleId> ids) {
        if (CollUtil.isEmpty(ids)) return Collections.emptyList();
        List<Long> rawIds = ids.stream().map(RoleId::value).collect(Collectors.toList());
        return roleMapper.selectByIds(rawIds).stream().map(this::toDomain).collect(Collectors.toList());
    }

    /**
     * 查询 find By Status 对应的数据。
     *
     * @param statuses statuses 参数
     * @return 处理结果
     */
    @Override
    public List<Role> findByStatus(Collection<Integer> statuses) {
        return roleMapper.selectListByStatus(statuses).stream().map(this::toDomain).collect(Collectors.toList());
    }

    /**
     * 查询 find All 对应的数据。
     *
     * @return 处理结果
     */
    @Override
    public List<Role> findAll() {
        return roleMapper.selectList().stream().map(this::toDomain).collect(Collectors.toList());
    }

    /**
     * 查询 find Page 对应的数据。
     *
     * @param name name 参数
     * @param code code 参数
     * @param status status 参数
     * @param createTime createTime 参数
     * @param pageNo pageNo 参数
     * @param pageSize pageSize 参数
     * @return 处理结果
     */
    @Override
    public PageResult<Role> findPage(String name, String code, Integer status,
                                      LocalDateTime[] createTime, Integer pageNo, Integer pageSize) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(pageNo);
        pageParam.setPageSize(pageSize);
        PageResult<RoleDO> doPage = roleMapper.selectPage(pageParam, new LambdaQueryWrapperX<RoleDO>()
                .likeIfPresent(RoleDO::getName, name)
                .likeIfPresent(RoleDO::getCode, code)
                .eqIfPresent(RoleDO::getStatus, status)
                .betweenIfPresent(RoleDO::getCreateTime, createTime)
                .orderByAsc(RoleDO::getSort));
        return new PageResult<>(
                doPage.getList().stream().map(this::toDomain).collect(Collectors.toList()),
                doPage.getTotal());
    }

    /**
     * 查询 find By Name 对应的数据。
     *
     * @param name name 参数
     * @return 处理结果
     */
    @Override
    public Optional<Role> findByName(String name) {
        return Optional.ofNullable(roleMapper.selectByName(name)).map(this::toDomain);
    }

    /**
     * 查询 find By Code 对应的数据。
     *
     * @param code code 参数
     * @return 处理结果
     */
    @Override
    public Optional<Role> findByCode(String code) {
        return Optional.ofNullable(roleMapper.selectByCode(code)).map(this::toDomain);
    }

    /**
     * 执行 to Data Object 对应的业务操作。
     *
     * @param role role 参数
     * @return 处理结果
     */
    private RoleDO toDataObject(Role role) {
        RoleDO d = new RoleDO();
        d.setId(role.id() != null ? role.id().value() : null); d.setName(role.name().value()); d.setCode(role.code().value());
        d.setSort(role.sort()); d.setStatus(role.status().code()); d.setType(role.type().code());
        d.setRemark(role.remark()); d.setTenantId(role.tenantId());
        d.setDataScope(role.dataScope().scope()); d.setDataScopeDeptIds(role.dataScope().deptIds());
        return d;
    }

    /**
     * 执行 to Domain 对应的业务操作。
     *
     * @param d d 参数
     * @return 处理结果
     */
    private Role toDomain(RoleDO d) {
        Set<Long> menuIds = convertSet(roleMenuMapper.selectListByRoleId(d.getId()), RoleMenuDO::getMenuId);
        return RoleFactory.reconstitute(d.getId(), d.getName(), d.getCode(), d.getSort(),
                d.getStatus(), d.getType(), d.getRemark(), d.getTenantId(),
                d.getDataScope(), d.getDataScopeDeptIds(), menuIds);
    }
}
