package com.develop.mvp.pk.module.bpm.domain.leave.event;
// DDD 角色：BPM请假单状态更新事件 - AggregateRoot_Bpm_Skill
import java.time.LocalDateTime;
public record LeaveStatusUpdatedEvent(Long leaveId, Integer newStatus, LocalDateTime occurredAt) implements LeaveDomainEvent {
    public LeaveStatusUpdatedEvent(Long leaveId, Integer newStatus) { this(leaveId, newStatus, LocalDateTime.now()); }
}
