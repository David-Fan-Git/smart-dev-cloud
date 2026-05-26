package com.develop.mvp.pk.module.member.domain.config.repository;

// Skill: AggregateRoot_MemberConfig_Skill — 仓储接口 MemberConfigRepository
// 验收标准 AC03：在领域层，不 import MyBatis 类

import com.develop.mvp.pk.module.member.domain.config.MemberConfig;

import java.util.Optional;

public interface MemberConfigRepository {
    MemberConfig save(MemberConfig config);
    Optional<MemberConfig> findSingle();
}
