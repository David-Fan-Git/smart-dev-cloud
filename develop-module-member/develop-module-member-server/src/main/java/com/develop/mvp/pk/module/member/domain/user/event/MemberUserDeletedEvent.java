package com.develop.mvp.pk.module.member.domain.user.event;

// Skill: AggregateRoot_MemberUser_Skill — 领域事件 MemberUserDeletedEvent
public class MemberUserDeletedEvent {
    private final Long userId;
    public MemberUserDeletedEvent(Long userId) { this.userId = userId; }
    public Long userId() { return userId; }
}
