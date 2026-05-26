package com.develop.mvp.pk.module.bpm.domain.usergroup.repository;
// DDD 角色：BPM用户组仓储接口 - AggregateRoot_Bpm_Skill
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.bpm.domain.usergroup.BpmUserGroup;
import com.develop.mvp.pk.module.bpm.domain.usergroup.valueobject.*;
import java.util.*;

public interface BpmUserGroupRepository {
    void save(BpmUserGroup group);
    void delete(UserGroupId id);
    BpmUserGroup findById(UserGroupId id);
    List<BpmUserGroup> findByIds(Collection<UserGroupId> ids);
    List<BpmUserGroup> findByStatus(UserGroupStatus status);
    List<BpmUserGroup> findAll();
    PageResult<BpmUserGroup> findPage(String name, Integer status, Integer pageNo, Integer pageSize);
}
