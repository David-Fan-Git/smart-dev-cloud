package com.develop.mvp.pk.module.bpm.domain.usergroup.event;
// DDD 角色：BPM用户组删除事件 - AggregateRoot_Bpm_Skill
import java.time.LocalDateTime;
public record UserGroupDeletedEvent(Long userGroupId, LocalDateTime occurredAt) implements UserGroupDomainEvent {
    public UserGroupDeletedEvent(Long userGroupId) { this(userGroupId, LocalDateTime.now()); }
}
