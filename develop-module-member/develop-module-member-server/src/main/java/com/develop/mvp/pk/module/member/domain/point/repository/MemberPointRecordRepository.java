package com.develop.mvp.pk.module.member.domain.point.repository;

// Skill: AggregateRoot_MemberPointRecord_Skill — 仓储接口 MemberPointRecordRepository
// 验收标准 AC03：在领域层，不 import MyBatis 类

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.member.domain.point.MemberPointRecord;

import java.time.LocalDateTime;
import java.util.Set;

public interface MemberPointRecordRepository {
    MemberPointRecord save(MemberPointRecord record);
    PageResult<MemberPointRecord> findPage(String nickname, Long userId, Integer bizType, String title,
                                           Integer pageNo, Integer pageSize);
    PageResult<MemberPointRecord> findPageByUser(Long userId, LocalDateTime createTimeStart, LocalDateTime createTimeEnd,
                                                  Boolean addStatus, Integer pageNo, Integer pageSize);
}
