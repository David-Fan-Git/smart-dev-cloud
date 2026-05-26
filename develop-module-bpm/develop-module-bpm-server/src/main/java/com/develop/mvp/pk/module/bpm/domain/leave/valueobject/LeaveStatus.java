package com.develop.mvp.pk.module.bpm.domain.leave.valueobject;
// DDD 角色：BPM请假单状态值对象 - AggregateRoot_Bpm_Skill
import com.develop.mvp.pk.module.bpm.enums.task.BpmTaskStatusEnum;
import java.util.Objects;

public final class LeaveStatus {
    public static final LeaveStatus RUNNING = new LeaveStatus(BpmTaskStatusEnum.RUNNING.getStatus());
    public static final LeaveStatus APPROVE = new LeaveStatus(BpmTaskStatusEnum.APPROVE.getStatus());
    public static final LeaveStatus REJECT = new LeaveStatus(BpmTaskStatusEnum.REJECT.getStatus());
    public static final LeaveStatus CANCEL = new LeaveStatus(BpmTaskStatusEnum.CANCEL.getStatus());
    private final Integer code;
    private LeaveStatus(Integer code) { this.code = Objects.requireNonNull(code); }
    public static LeaveStatus of(Integer code) { return new LeaveStatus(code); }
    public Integer code() { return code; }
    public boolean isRunning() { return RUNNING.code.equals(code); }
    public boolean isFinished() { return APPROVE.code.equals(code) || REJECT.code.equals(code) || CANCEL.code.equals(code); }
    @Override public boolean equals(Object o) { return o instanceof LeaveStatus s && code.equals(s.code); }
    @Override public int hashCode() { return Objects.hash(code); }
}
