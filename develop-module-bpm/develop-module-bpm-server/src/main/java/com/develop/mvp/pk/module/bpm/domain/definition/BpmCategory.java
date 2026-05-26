package com.develop.mvp.pk.module.bpm.domain.definition;
// DDD 角色：BPM流程分类聚合根 - AggregateRoot_Bpm_Skill

import com.develop.mvp.pk.module.bpm.domain.definition.event.*;
import com.develop.mvp.pk.module.bpm.domain.definition.valueobject.*;
import java.util.*;

public final class BpmCategory {
    private final CategoryId id;
    private final CategoryName name;
    private final CategoryCode code;
    private final Integer sort;
    private CategoryStatus status;
    private final List<CategoryDomainEvent> events = new ArrayList<>();

    BpmCategory(CategoryId id, CategoryName name, CategoryCode code, Integer sort, CategoryStatus status) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.code = Objects.requireNonNull(code);
        this.sort = sort != null ? sort : 0;
        this.status = status != null ? status : CategoryStatus.ENABLED;
    }

    public void disable() { if (!this.status.isEnabled()) return; this.status = this.status.disable(); events.add(new CategoryDeletedEvent(this.id().value())); }
    public void enable() { this.status = CategoryStatus.ENABLED; }
    public void markDeleted() { events.add(new CategoryDeletedEvent(this.id().value())); }

    // ── accessors ──
    public CategoryId id() { return id; }
    public CategoryName name() { return name; }
    public CategoryCode code() { return code; }
    public Integer sort() { return sort; }
    public CategoryStatus status() { return status; }
    public boolean isEnabled() { return status.isEnabled(); }
    public List<CategoryDomainEvent> pullEvents() { List<CategoryDomainEvent> r = new ArrayList<>(events); events.clear(); return r; }

    @Override public boolean equals(Object o) { return o instanceof BpmCategory c && id.equals(c.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
