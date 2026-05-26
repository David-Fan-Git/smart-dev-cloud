package com.develop.mvp.pk.module.bpm.domain.copy;
// DDD 角色：BPM流程抄送聚合根 - AggregateRoot_Bpm_Skill

import com.develop.mvp.pk.module.bpm.domain.copy.event.*;
import com.develop.mvp.pk.module.bpm.domain.copy.valueobject.*;
import java.util.*;

public final class BpmProcessInstanceCopy {
    private final CopyId id;
    private final Long startUserId;
    private final String processInstanceName;
    private final String processInstanceId;
    private final String processDefinitionId;
    private final String category;
    private final String activityId;
    private final String activityName;
    private final String taskId;
    private final Long userId;
    private final String reason;
    private final List<CopyDomainEvent> events = new ArrayList<>();

    BpmProcessInstanceCopy(CopyId id, Long startUserId, String processInstanceName,
                           String processInstanceId, String processDefinitionId, String category,
                           String activityId, String activityName, String taskId,
                           Long userId, String reason) {
        this.id = Objects.requireNonNull(id);
        this.startUserId = startUserId;
        this.processInstanceName = processInstanceName;
        this.processInstanceId = processInstanceId;
        this.processDefinitionId = processDefinitionId;
        this.category = category;
        this.activityId = activityId;
        this.activityName = activityName;
        this.taskId = taskId;
        this.userId = Objects.requireNonNull(userId);
        this.reason = reason;
        events.add(new CopyCreatedEvent(this.id.value(), userId, processInstanceId));
    }

    // ── accessors ──
    public CopyId id() { return id; }
    public Long startUserId() { return startUserId; }
    public String processInstanceName() { return processInstanceName; }
    public String processInstanceId() { return processInstanceId; }
    public String processDefinitionId() { return processDefinitionId; }
    public String category() { return category; }
    public String activityId() { return activityId; }
    public String activityName() { return activityName; }
    public String taskId() { return taskId; }
    public Long userId() { return userId; }
    public String reason() { return reason; }
    public List<CopyDomainEvent> pullEvents() { List<CopyDomainEvent> r = new ArrayList<>(events); events.clear(); return r; }

    @Override public boolean equals(Object o) { return o instanceof BpmProcessInstanceCopy c && id.equals(c.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
