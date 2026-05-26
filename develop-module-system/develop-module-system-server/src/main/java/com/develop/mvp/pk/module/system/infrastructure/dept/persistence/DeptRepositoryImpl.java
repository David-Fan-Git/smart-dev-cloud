package com.develop.mvp.pk.module.system.infrastructure.dept.persistence;

import cn.hutool.core.collection.CollUtil;
import com.develop.mvp.pk.module.system.controller.admin.dept.vo.dept.DeptListReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.dept.DeptDO;
import com.develop.mvp.pk.module.system.dal.mysql.dept.DeptMapper;
import com.develop.mvp.pk.module.system.domain.dept.Dept;
import com.develop.mvp.pk.module.system.domain.dept.DeptFactory;
import com.develop.mvp.pk.module.system.domain.dept.repository.DeptRepository;
import com.develop.mvp.pk.module.system.domain.dept.valueobject.DeptId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Dept Repository Impl 领域仓储实现。
 */
@Repository
public class DeptRepositoryImpl implements DeptRepository {
    private final DeptMapper deptMapper;

    /**
     * 创建 DeptRepositoryImpl 实例。
     *
     * @param deptMapper deptMapper 参数
     */
    public DeptRepositoryImpl(DeptMapper deptMapper) {
        this.deptMapper = deptMapper;
    }

    /**
     * 创建 save 对应的数据。
     *
     * @param dept dept 参数
     */
    @Override @Transactional
    public void save(Dept dept) {
        DeptDO d = toDO(dept);
        if (deptMapper.selectById(dept.id().value()) == null) deptMapper.insert(d);
        else deptMapper.updateById(d);
    }

    /**
     * 删除 delete 对应的数据。
     *
     */
    @Override @Transactional
    public void delete(DeptId id) { deptMapper.deleteById(id.value()); }

    /**
     * 查询 find By Id 对应的数据。
     *
     * @return 处理结果
     */
    @Override
    public Dept findById(DeptId id) { return fromDO(deptMapper.selectById(id.value())); }

    /**
     * 查询 find By Ids 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    @Override
    public List<Dept> findByIds(Collection<DeptId> ids) {
        if (CollUtil.isEmpty(ids)) return Collections.emptyList();
        return deptMapper.selectBatchIds(ids.stream().map(DeptId::value).toList())
                .stream().map(this::fromDO).collect(Collectors.toList());
    }

    /**
     * 查询 find By Parent Id 对应的数据。
     *
     * @param parentId parentId 参数
     * @return 处理结果
     */
    @Override
    public List<Dept> findByParentId(Long parentId) {
        return deptMapper.selectListByParentId(Collections.singleton(parentId))
                .stream().map(this::fromDO).collect(Collectors.toList());
    }

    /**
     * 查询 find By Leader User Id 对应的数据。
     *
     * @param userId userId 参数
     * @return 处理结果
     */
    @Override
    public List<Dept> findByLeaderUserId(Long userId) {
        return deptMapper.selectListByLeaderUserId(userId).stream().map(this::fromDO).collect(Collectors.toList());
    }

    /**
     * 查询 find All 对应的数据。
     *
     * @return 处理结果
     */
    @Override
    public List<Dept> findAll() {
        return deptMapper.selectList(new DeptListReqVO()).stream().map(this::fromDO).collect(Collectors.toList());
    }

    /**
     * 查询 find Child Ids From Cache 对应的数据。
     *
     * @param parentId parentId 参数
     * @return 处理结果
     */
    @Override
    public Set<Long> findChildIdsFromCache(Long parentId) {
        return deptMapper.selectListByParentId(Collections.singleton(parentId)).stream()
                .map(DeptDO::getId).collect(Collectors.toSet());
    }

    /**
     * 执行 exists By Name 对应的业务操作。
     *
     * @param name name 参数
     * @param excludeId excludeId 参数
     * @return 处理结果
     */
    @Override
    public boolean existsByName(String name, Long excludeId) {
        DeptDO existing = deptMapper.selectByParentIdAndName(null, name);
        if (existing == null) return false;
        return excludeId == null || !existing.getId().equals(excludeId);
    }

    /**
     * 执行 to DO 对应的业务操作。
     *
     * @param dept dept 参数
     * @return 处理结果
     */
    private DeptDO toDO(Dept dept) {
        DeptDO d = new DeptDO();
        d.setId(dept.id().value()); d.setName(dept.name().value());
        d.setParentId(dept.parentId()); d.setSort(dept.sort());
        d.setLeaderUserId(dept.leaderUserId()); d.setPhone(dept.phone());
        d.setEmail(dept.email()); d.setStatus(dept.status().code());
        return d;
    }

    /**
     * 执行 from DO 对应的业务操作。
     *
     * @param d d 参数
     * @return 处理结果
     */
    private Dept fromDO(DeptDO d) {
        if (d == null) return null;
        return DeptFactory.reconstitute(d.getId(), d.getName(), d.getParentId(),
                d.getSort(), d.getLeaderUserId(), d.getPhone(), d.getEmail(), d.getStatus());
    }
}
