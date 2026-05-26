package com.develop.mvp.pk.module.member.infrastructure.group;

// Skill: AggregateRoot_MemberGroup_Skill — 仓储实现 MemberGroupRepositoryImpl

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.member.controller.admin.group.vo.MemberGroupPageReqVO;
import com.develop.mvp.pk.module.member.dal.dataobject.group.MemberGroupDO;
import com.develop.mvp.pk.module.member.dal.mysql.group.MemberGroupMapper;
import com.develop.mvp.pk.module.member.domain.group.MemberGroup;
import com.develop.mvp.pk.module.member.domain.group.repository.MemberGroupRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MemberGroupRepositoryImpl implements MemberGroupRepository {

    private final MemberGroupMapper mapper;
    public MemberGroupRepositoryImpl(MemberGroupMapper mapper) { this.mapper = mapper; }

    @Override @Transactional
    public MemberGroup save(MemberGroup g) {
        MemberGroupDO d = toDO(g);
        if (g.id() == null) { mapper.insert(d); } else { mapper.updateById(d); }
        return fromDO(d);
    }

    @Override @Transactional
    public void delete(Long id) { mapper.deleteById(id); }

    @Override
    public MemberGroup findById(Long id) { return fromDO(mapper.selectById(id)); }

    @Override
    public List<MemberGroup> findByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) return ListUtil.empty();
        return mapper.selectByIds(ids).stream().map(this::fromDO).collect(Collectors.toList());
    }

    @Override
    public PageResult<MemberGroup> findPage(String name, Integer status, String createTimeStart, String createTimeEnd,
                                            Integer pageNo, Integer pageSize) {
        var reqVO = new MemberGroupPageReqVO().setName(name).setStatus(status);
        var wrapper = new LambdaQueryWrapperX<MemberGroupDO>()
                .likeIfPresent(MemberGroupDO::getName, reqVO.getName())
                .eqIfPresent(MemberGroupDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(MemberGroupDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MemberGroupDO::getId);
        PageResult<MemberGroupDO> result = mapper.selectPage(
                new com.develop.mvp.pk.framework.common.pojo.PageParam().setPageNo(pageNo).setPageSize(pageSize), wrapper);
        return new PageResult<>(result.getList().stream().map(this::fromDO).collect(Collectors.toList()), result.getTotal());
    }

    @Override
    public List<MemberGroup> findByStatus(Integer status) {
        return mapper.selectListByStatus(status).stream().map(this::fromDO).collect(Collectors.toList());
    }

    @Override
    public long countByGroupId(Long groupId) { return 0; }

    // ── DO ↔ Domain 映射 ──
    private MemberGroupDO toDO(MemberGroup g) {
        return MemberGroupDO.builder()
                .id(g.id()).name(g.name()).remark(g.remark()).status(g.status())
                .build();
    }

    private MemberGroup fromDO(MemberGroupDO d) {
        if (d == null) return null;
        return MemberGroup.reconstitute(d.getId(), d.getName(), d.getRemark(), d.getStatus());
    }
}
