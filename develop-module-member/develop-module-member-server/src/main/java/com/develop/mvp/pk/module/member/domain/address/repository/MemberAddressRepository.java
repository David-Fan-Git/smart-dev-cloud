package com.develop.mvp.pk.module.member.domain.address.repository;

// Skill: AggregateRoot_MemberAddress_Skill — 仓储接口 MemberAddressRepository
// 验收标准 AC03：在领域层，不 import MyBatis 类

import com.develop.mvp.pk.module.member.domain.address.MemberAddress;

import java.util.List;

public interface MemberAddressRepository {
    MemberAddress save(MemberAddress address);
    void delete(Long id);
    MemberAddress findById(Long id);
    MemberAddress findByIdAndUserId(Long id, Long userId);
    List<MemberAddress> findByUserId(Long userId);
    List<MemberAddress> findByUserIdAndDefaulted(Long userId, Boolean defaulted);
}
