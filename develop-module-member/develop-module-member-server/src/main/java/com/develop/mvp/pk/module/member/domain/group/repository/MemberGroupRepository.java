package com.develop.mvp.pk.module.member.domain.group.repository;

// Skill: AggregateRoot_MemberGroup_Skill — 仓储接口 MemberGroupRepository
// 验收标准 AC03：在领域层，不 import MyBatis 类

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.member.domain.group.MemberGroup;

import java.util.Collection;
import java.util.List;

public interface MemberGroupRepository {
    MemberGroup save(MemberGroup group);
    void delete(Long id);
    MemberGroup findById(Long id);
    List<MemberGroup> findByIds(Collection<Long> ids);
    PageResult<MemberGroup> findPage(String name, Integer status, String createTimeStart, String createTimeEnd,
                                     Integer pageNo, Integer pageSize);
    List<MemberGroup> findByStatus(Integer status);
    long countByGroupId(Long groupId);
}
