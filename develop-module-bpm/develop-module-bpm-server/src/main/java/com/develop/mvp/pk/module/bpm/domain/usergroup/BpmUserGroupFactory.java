package com.develop.mvp.pk.module.bpm.domain.usergroup;
// DDD 角色：BPM用户组工厂 - AggregateRoot_Bpm_Skill
import com.develop.mvp.pk.module.bpm.domain.usergroup.valueobject.*;
import java.util.Set;

public final class BpmUserGroupFactory {
    private BpmUserGroupFactory() {}
    public static BpmUserGroup create(Long id, String name, String description, Set<Long> userIds) {
        return new BpmUserGroup(UserGroupId.of(id), UserGroupName.of(name), description, UserGroupStatus.ENABLED, userIds);
    }
    public static BpmUserGroup reconstitute(Long id, String name, String description, Integer status, Set<Long> userIds) {
        return new BpmUserGroup(UserGroupId.of(id), UserGroupName.of(name), description, UserGroupStatus.of(status), userIds);
    }
}
