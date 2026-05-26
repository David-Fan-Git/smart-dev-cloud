package com.develop.mvp.pk.module.member.domain.user.event;

// Skill: AggregateRoot_MemberUser_Skill — 领域事件 MemberUserPasswordChangedEvent
public class MemberUserPasswordChangedEvent {
    private final Long userId;
    public MemberUserPasswordChangedEvent(Long userId) { this.userId = userId; }
    public Long userId() { return userId; }
}
