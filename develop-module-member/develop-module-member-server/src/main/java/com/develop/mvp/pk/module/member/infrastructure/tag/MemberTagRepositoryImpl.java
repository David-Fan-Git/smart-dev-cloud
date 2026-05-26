package com.develop.mvp.pk.module.member.infrastructure.tag;

// Skill: AggregateRoot_MemberTag_Skill — 仓储实现 MemberTagRepositoryImpl

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.member.controller.admin.tag.vo.MemberTagPageReqVO;
import com.develop.mvp.pk.module.member.dal.dataobject.tag.MemberTagDO;
import com.develop.mvp.pk.module.member.dal.mysql.tag.MemberTagMapper;
import com.develop.mvp.pk.module.member.domain.tag.MemberTag;
import com.develop.mvp.pk.module.member.domain.tag.repository.MemberTagRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MemberTagRepositoryImpl implements MemberTagRepository {

    private final MemberTagMapper mapper;
    public MemberTagRepositoryImpl(MemberTagMapper mapper) { this.mapper = mapper; }

    @Override @Transactional
    public MemberTag save(MemberTag t) {
        MemberTagDO d = toDO(t);
        if (t.id() == null) { mapper.insert(d); } else { mapper.updateById(d); }
        return fromDO(d);
    }

    @Override @Transactional
    public void delete(Long id) { mapper.deleteById(id); }

    @Override
    public MemberTag findById(Long id) { return fromDO(mapper.selectById(id)); }

    @Override
    public List<MemberTag> findByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) return ListUtil.empty();
        return mapper.selectByIds(ids).stream().map(this::fromDO).collect(Collectors.toList());
    }

    @Override
    public MemberTag findByName(String name) { return fromDO(mapper.selelctByName(name)); }

    @Override
    public PageResult<MemberTag> findPage(String name, String createTimeStart, String createTimeEnd,
                                          Integer pageNo, Integer pageSize) {
        var reqVO = new MemberTagPageReqVO().setName(name);
        var wrapper = new LambdaQueryWrapperX<MemberTagDO>()
                .likeIfPresent(MemberTagDO::getName, reqVO.getName())
                .betweenIfPresent(MemberTagDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MemberTagDO::getId);
        PageResult<MemberTagDO> result = mapper.selectPage(
                new com.develop.mvp.pk.framework.common.pojo.PageParam().setPageNo(pageNo).setPageSize(pageSize), wrapper);
        return new PageResult<>(result.getList().stream().map(this::fromDO).collect(Collectors.toList()), result.getTotal());
    }

    @Override
    public List<MemberTag> findAll() {
        return mapper.selectList().stream().map(this::fromDO).collect(Collectors.toList());
    }

    // ── DO ↔ Domain 映射 ──
    private MemberTagDO toDO(MemberTag t) {
        return MemberTagDO.builder().id(t.id()).name(t.name()).build();
    }

    private MemberTag fromDO(MemberTagDO d) {
        if (d == null) return null;
        return MemberTag.reconstitute(d.getId(), d.getName());
    }
}
