package com.develop.mvp.pk.module.system.domain.dept.event;
import java.time.LocalDateTime;
/**
 * Dept Deleted Event 领域事件。
 */
public record DeptDeletedEvent(Long deptId, LocalDateTime occurredAt) implements DeptDomainEvent {
    /**
     * 创建 DeptDeletedEvent 实例。
     *
     */
    public DeptDeletedEvent(Long deptId) { this(deptId, LocalDateTime.now()); }
}
