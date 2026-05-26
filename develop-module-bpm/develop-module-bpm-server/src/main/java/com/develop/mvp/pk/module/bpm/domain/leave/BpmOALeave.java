package com.develop.mvp.pk.module.bpm.domain.leave;
// DDD 角色：BPM OA请假单聚合根 - AggregateRoot_Bpm_Skill

import com.develop.mvp.pk.module.bpm.domain.leave.event.*;
import com.develop.mvp.pk.module.bpm.domain.leave.valueobject.*;
import java.time.LocalDateTime;
import java.util.*;

public final class BpmOALeave {
    private final LeaveId id;
    private final Long userId;
    private final Integer type;
    private final String reason;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final Long day;
    private LeaveStatus status;
    private final String processInstanceId;
    private final List<LeaveDomainEvent> events = new ArrayList<>();

    BpmOALeave(LeaveId id, Long userId, Integer type, String reason, LocalDateTime startTime,
               LocalDateTime endTime, Long day, LeaveStatus status, String processInstanceId) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.type = type;
        this.reason = reason;
        this.startTime = startTime;
        this.endTime = endTime;
        this.day = day;
        this.status = status != null ? status : LeaveStatus.RUNNING;
        this.processInstanceId = processInstanceId;
    }

    public void updateStatus(Integer newCode) {
        LeaveStatus newStatus = LeaveStatus.of(newCode);
        this.status = newStatus;
        events.add(new LeaveStatusUpdatedEvent(this.id().value(), newCode));
    }

    // ── accessors ──
    public LeaveId id() { return id; }
    public Long userId() { return userId; }
    public Integer type() { return type; }
    public String reason() { return reason; }
    public LocalDateTime startTime() { return startTime; }
    public LocalDateTime endTime() { return endTime; }
    public Long day() { return day; }
    public LeaveStatus status() { return status; }
    public String processInstanceId() { return processInstanceId; }
    public boolean isRunning() { return status.isRunning(); }
    public List<LeaveDomainEvent> pullEvents() { List<LeaveDomainEvent> r = new ArrayList<>(events); events.clear(); return r; }

    @Override public boolean equals(Object o) { return o instanceof BpmOALeave l && id.equals(l.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
