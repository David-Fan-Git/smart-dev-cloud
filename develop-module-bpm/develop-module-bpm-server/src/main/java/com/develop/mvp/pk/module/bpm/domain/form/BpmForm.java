package com.develop.mvp.pk.module.bpm.domain.form;
// DDD 角色：BPM表单聚合根 - AggregateRoot_Bpm_Skill

import com.develop.mvp.pk.module.bpm.domain.form.event.*;
import com.develop.mvp.pk.module.bpm.domain.form.valueobject.*;
import java.util.*;

public final class BpmForm {
    private final FormId id;
    private final FormName name;
    private FormStatus status;
    private final String conf;
    private final List<String> fields;
    private final String remark;
    private final List<FormDomainEvent> events = new ArrayList<>();

    BpmForm(FormId id, FormName name, FormStatus status, String conf, List<String> fields, String remark) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.status = status != null ? status : FormStatus.ENABLED;
        this.conf = conf;
        this.fields = fields;
        this.remark = remark;
    }

    public void markDeleted() { events.add(new FormDeletedEvent(this.id().value())); }

    // ── accessors ──
    public FormId id() { return id; }
    public FormName name() { return name; }
    public FormStatus status() { return status; }
    public String conf() { return conf; }
    public List<String> fields() { return fields; }
    public String remark() { return remark; }
    public boolean isEnabled() { return status.isEnabled(); }
    public List<FormDomainEvent> pullEvents() { List<FormDomainEvent> r = new ArrayList<>(events); events.clear(); return r; }

    @Override public boolean equals(Object o) { return o instanceof BpmForm f && id.equals(f.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
