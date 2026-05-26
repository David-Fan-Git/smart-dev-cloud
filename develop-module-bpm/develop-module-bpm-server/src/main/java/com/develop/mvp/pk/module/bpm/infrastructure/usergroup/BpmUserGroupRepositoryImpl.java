package com.develop.mvp.pk.module.bpm.infrastructure.usergroup;
// DDD 角色：BPM用户组仓储实现 - AggregateRoot_Bpm_Skill

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.bpm.controller.admin.definition.vo.group.BpmUserGroupPageReqVO;
import com.develop.mvp.pk.module.bpm.dal.dataobject.definition.BpmUserGroupDO;
import com.develop.mvp.pk.module.bpm.dal.mysql.definition.BpmUserGroupMapper;
import com.develop.mvp.pk.module.bpm.domain.usergroup.BpmUserGroup;
import com.develop.mvp.pk.module.bpm.domain.usergroup.BpmUserGroupFactory;
import com.develop.mvp.pk.module.bpm.domain.usergroup.repository.BpmUserGroupRepository;
import com.develop.mvp.pk.module.bpm.domain.usergroup.valueobject.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class BpmUserGroupRepositoryImpl implements BpmUserGroupRepository {
    private final BpmUserGroupMapper mapper;
    public BpmUserGroupRepositoryImpl(BpmUserGroupMapper mapper) { this.mapper = mapper; }

    @Override @Transactional
    public void save(BpmUserGroup group) {
        BpmUserGroupDO d = toDO(group);
        if (mapper.selectById(group.id().value()) == null) mapper.insert(d);
        else mapper.updateById(d);
    }

    @Override @Transactional
    public void delete(UserGroupId id) { mapper.deleteById(id.value()); }

    @Override
    public BpmUserGroup findById(UserGroupId id) { return fromDO(mapper.selectById(id.value())); }

    @Override
    public List<BpmUserGroup> findByIds(Collection<UserGroupId> ids) {
        if (ids.isEmpty()) return Collections.emptyList();
        return mapper.selectBatchIds(ids.stream().map(UserGroupId::value).toList())
                .stream().map(this::fromDO).collect(Collectors.toList());
    }

    @Override
    public List<BpmUserGroup> findByStatus(UserGroupStatus status) {
        return mapper.selectListByStatus(status.code()).stream().map(this::fromDO).collect(Collectors.toList());
    }

    @Override
    public List<BpmUserGroup> findAll() {
        return mapper.selectList().stream().map(this::fromDO).collect(Collectors.toList());
    }

    @Override
    public PageResult<BpmUserGroup> findPage(String name, Integer status, Integer pageNo, Integer pageSize) {
        BpmUserGroupPageReqVO req = new BpmUserGroupPageReqVO().setName(name).setStatus(status);
        req.setPageNo(pageNo); req.setPageSize(pageSize);
        PageResult<BpmUserGroupDO> page = mapper.selectPage(req);
        return new PageResult<>(page.getList().stream().map(this::fromDO).collect(Collectors.toList()), page.getTotal());
    }

    private BpmUserGroupDO toDO(BpmUserGroup g) {
        BpmUserGroupDO d = new BpmUserGroupDO();
        d.setId(g.id().value()); d.setName(g.name().value());
        d.setDescription(g.description()); d.setStatus(g.status().code());
        d.setUserIds(g.userIds());
        return d;
    }

    private BpmUserGroup fromDO(BpmUserGroupDO d) {
        if (d == null) return null;
        return BpmUserGroupFactory.reconstitute(d.getId(), d.getName(), d.getDescription(), d.getStatus(), d.getUserIds());
    }
}
