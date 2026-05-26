package com.develop.mvp.pk.module.bpm.domain.leave;
// DDD 角色：BPM OA请假单工厂 - AggregateRoot_Bpm_Skill
import com.develop.mvp.pk.module.bpm.domain.leave.valueobject.*;
import java.time.LocalDateTime;

public final class BpmOALeaveFactory {
    private BpmOALeaveFactory() {}
    public static BpmOALeave create(Long id, Long userId, Integer type, String reason,
                                     LocalDateTime startTime, LocalDateTime endTime, Long day) {
        return new BpmOALeave(LeaveId.of(id), userId, type, reason, startTime, endTime, day, LeaveStatus.RUNNING, null);
    }
    public static BpmOALeave reconstitute(Long id, Long userId, Integer type, String reason,
                                           LocalDateTime startTime, LocalDateTime endTime,
                                           Long day, Integer status, String processInstanceId) {
        return new BpmOALeave(LeaveId.of(id), userId, type, reason, startTime, endTime, day,
                LeaveStatus.of(status), processInstanceId);
    }
}
