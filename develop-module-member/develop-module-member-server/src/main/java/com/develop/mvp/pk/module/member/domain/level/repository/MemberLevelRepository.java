package com.develop.mvp.pk.module.member.domain.level.repository;

// Skill: AggregateRoot_MemberLevel_Skill — 仓储接口 MemberLevelRepository
// 验收标准 AC03：在领域层，不 import MyBatis 类

import com.develop.mvp.pk.module.member.domain.level.MemberLevel;

import java.util.Collection;
import java.util.List;

public interface MemberLevelRepository {
    MemberLevel save(MemberLevel level);
    void delete(Long id);
    MemberLevel findById(Long id);
    List<MemberLevel> findByIds(Collection<Long> ids);
    List<MemberLevel> findByNameLike(String name);
    List<MemberLevel> findByStatus(Integer status);
    List<MemberLevel> findAll();
}
