package com.develop.mvp.pk.module.bpm.domain.expression;
// DDD 角色：BPM流程表达式聚合根 - AggregateRoot_Bpm_Skill

import com.develop.mvp.pk.module.bpm.domain.expression.event.*;
import com.develop.mvp.pk.module.bpm.domain.expression.valueobject.*;
import java.util.*;

public final class BpmProcessExpression {
    private final ExpressionId id;
    private final ExpressionName name;
    private ExpressionStatus status;
    private final String expression;
    private final List<ExpressionDomainEvent> events = new ArrayList<>();

    BpmProcessExpression(ExpressionId id, ExpressionName name, ExpressionStatus status, String expression) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.status = status != null ? status : ExpressionStatus.ENABLED;
        this.expression = expression;
    }

    public void markDeleted() { events.add(new ExpressionDeletedEvent(this.id().value())); }

    // ── accessors ──
    public ExpressionId id() { return id; }
    public ExpressionName name() { return name; }
    public ExpressionStatus status() { return status; }
    public String expression() { return expression; }
    public boolean isEnabled() { return status.isEnabled(); }
    public List<ExpressionDomainEvent> pullEvents() { List<ExpressionDomainEvent> r = new ArrayList<>(events); events.clear(); return r; }

    @Override public boolean equals(Object o) { return o instanceof BpmProcessExpression e && id.equals(e.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
