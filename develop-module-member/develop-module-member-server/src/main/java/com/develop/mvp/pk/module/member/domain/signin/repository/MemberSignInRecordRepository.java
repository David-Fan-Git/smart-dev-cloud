package com.develop.mvp.pk.module.member.domain.signin.repository;

// Skill: AggregateRoot_MemberSignInRecord_Skill — 仓储接口 MemberSignInRecordRepository
// 验收标准 AC03：在领域层，不 import MyBatis 类

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.member.domain.signin.MemberSignInRecord;

import java.util.List;
import java.util.Set;

public interface MemberSignInRecordRepository {
    MemberSignInRecord save(MemberSignInRecord record);
    PageResult<MemberSignInRecord> findPage(String nickname, Long userId, Integer day,
                                            String createTimeStart, String createTimeEnd,
                                            Integer pageNo, Integer pageSize);
    PageResult<MemberSignInRecord> findPageByUser(Long userId, Integer pageNo, Integer pageSize);
    MemberSignInRecord findLastByUserId(Long userId);
    Long countByUserId(Long userId);
    List<MemberSignInRecord> findByUserId(Long userId);
}
