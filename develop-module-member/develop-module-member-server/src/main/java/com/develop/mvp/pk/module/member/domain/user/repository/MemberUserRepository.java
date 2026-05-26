package com.develop.mvp.pk.module.member.domain.user.repository;

// Skill: AggregateRoot_MemberUser_Skill — 仓储接口 MemberUserRepository
// 验收标准 AC03：在领域层，不 import MyBatis 类
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.member.domain.user.MemberUser;
import com.develop.mvp.pk.module.member.domain.user.valueobject.Mobile;

import java.util.*;

public interface MemberUserRepository {
    MemberUser save(MemberUser u);
    void delete(Long id);
    MemberUser findById(Long id);
    Optional<MemberUser> findByMobile(Mobile mobile);
    List<MemberUser> findByIds(Collection<Long> ids);
    List<MemberUser> findByNicknameLike(String keyword);
    List<MemberUser> findByStatus(Integer status);
    PageResult<MemberUser> findPage(String nickname, Mobile mobile, Integer status, Long levelId, Long groupId,
                                    List<Long> tagIds, String loginDateStart, String loginDateEnd,
                                    String createTimeStart, String createTimeEnd, Integer pageNo, Integer pageSize);
    boolean existsByMobile(Mobile mobile);
    long count();
    long countByGroupId(Long groupId);
    long countByLevelId(Long levelId);
    long countByTagId(Long tagId);
    int updatePointIncr(Long id, Integer incrCount);
    int updatePointDecr(Long id, Integer decrCount);
}
