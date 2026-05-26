package com.develop.mvp.pk.module.bpm.domain.leave.repository;
// DDD 角色：BPM OA请假单仓储接口 - AggregateRoot_Bpm_Skill
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.bpm.domain.leave.BpmOALeave;
import com.develop.mvp.pk.module.bpm.domain.leave.valueobject.LeaveId;

public interface BpmOALeaveRepository {
    void save(BpmOALeave leave);
    BpmOALeave findById(LeaveId id);
    PageResult<BpmOALeave> findPage(Long userId, Integer status, Integer type, String reason,
                                    Integer pageNo, Integer pageSize);
}
