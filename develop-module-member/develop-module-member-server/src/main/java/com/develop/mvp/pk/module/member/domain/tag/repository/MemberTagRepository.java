package com.develop.mvp.pk.module.member.domain.tag.repository;

// Skill: AggregateRoot_MemberTag_Skill — 仓储接口 MemberTagRepository
// 验收标准 AC03：在领域层，不 import MyBatis 类

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.member.domain.tag.MemberTag;

import java.util.Collection;
import java.util.List;

public interface MemberTagRepository {
    MemberTag save(MemberTag tag);
    void delete(Long id);
    MemberTag findById(Long id);
    List<MemberTag> findByIds(Collection<Long> ids);
    MemberTag findByName(String name);
    PageResult<MemberTag> findPage(String name, String createTimeStart, String createTimeEnd,
                                   Integer pageNo, Integer pageSize);
    List<MemberTag> findAll();
}
