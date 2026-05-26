package com.develop.mvp.pk.module.bpm.domain.listener;
// DDD 角色：BPM流程监听器聚合根 - AggregateRoot_Bpm_Skill

import com.develop.mvp.pk.module.bpm.domain.listener.event.*;
import com.develop.mvp.pk.module.bpm.domain.listener.valueobject.*;
import java.util.*;

public final class BpmProcessListener {
    private final ListenerId id;
    private final ListenerName name;
    private ListenerStatus status;
    private final String type;
    private final String event;
    private final String valueType;
    private final String value;
    private final List<ListenerDomainEvent> events = new ArrayList<>();

    BpmProcessListener(ListenerId id, ListenerName name, ListenerStatus status,
                       String type, String event, String valueType, String value) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.status = status != null ? status : ListenerStatus.ENABLED;
        this.type = type;
        this.event = event;
        this.valueType = valueType;
        this.value = value;
    }

    public void markDeleted() { events.add(new ListenerDeletedEvent(this.id().value())); }

    // ── accessors ──
    public ListenerId id() { return id; }
    public ListenerName name() { return name; }
    public ListenerStatus status() { return status; }
    public String type() { return type; }
    public String event() { return event; }
    public String valueType() { return valueType; }
    public String value() { return value; }
    public boolean isEnabled() { return status.isEnabled(); }
    public List<ListenerDomainEvent> pullEvents() { List<ListenerDomainEvent> r = new ArrayList<>(events); events.clear(); return r; }

    @Override public boolean equals(Object o) { return o instanceof BpmProcessListener l && id.equals(l.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
