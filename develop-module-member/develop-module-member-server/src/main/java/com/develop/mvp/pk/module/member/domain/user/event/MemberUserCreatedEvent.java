package com.develop.mvp.pk.module.member.domain.user.event;

// Skill: AggregateRoot_MemberUser_Skill — 领域事件 MemberUserCreatedEvent
public class MemberUserCreatedEvent {
    private final Long userId;
    public MemberUserCreatedEvent(Long userId) { this.userId = userId; }
    public Long userId() { return userId; }
}
