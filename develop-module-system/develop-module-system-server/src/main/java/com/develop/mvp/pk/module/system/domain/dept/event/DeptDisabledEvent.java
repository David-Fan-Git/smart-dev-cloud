package com.develop.mvp.pk.module.system.domain.dept.event;
import java.time.LocalDateTime;
/**
 * Dept Disabled Event 领域事件。
 */
public record DeptDisabledEvent(Long deptId, LocalDateTime occurredAt) implements DeptDomainEvent {
    /**
     * 创建 DeptDisabledEvent 实例。
     *
     */
    public DeptDisabledEvent(Long deptId) { this(deptId, LocalDateTime.now()); }
}
