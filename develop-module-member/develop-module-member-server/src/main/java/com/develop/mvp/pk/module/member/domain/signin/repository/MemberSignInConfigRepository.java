package com.develop.mvp.pk.module.member.domain.signin.repository;

// Skill: AggregateRoot_MemberSignInConfig_Skill — 仓储接口 MemberSignInConfigRepository
// 验收标准 AC03：在领域层，不 import MyBatis 类

import com.develop.mvp.pk.module.member.domain.signin.MemberSignInConfig;

import java.util.List;

public interface MemberSignInConfigRepository {
    MemberSignInConfig save(MemberSignInConfig config);
    void delete(Long id);
    MemberSignInConfig findById(Long id);
    MemberSignInConfig findByDay(Integer day);
    List<MemberSignInConfig> findAll();
    List<MemberSignInConfig> findByStatus(Integer status);
}
