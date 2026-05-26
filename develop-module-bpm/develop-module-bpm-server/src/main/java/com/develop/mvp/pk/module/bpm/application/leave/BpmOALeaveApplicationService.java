package com.develop.mvp.pk.module.bpm.application.leave;
// DDD 角色：BPM OA请假单应用服务 - AggregateRoot_Bpm_Skill

import cn.hutool.core.date.LocalDateTimeUtil;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.bpm.api.task.BpmProcessInstanceApi;
import com.develop.mvp.pk.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import com.develop.mvp.pk.module.bpm.domain.leave.BpmOALeave;
import com.develop.mvp.pk.module.bpm.domain.leave.BpmOALeaveFactory;
import com.develop.mvp.pk.module.bpm.domain.leave.event.LeaveDomainEvent;
import com.develop.mvp.pk.module.bpm.domain.leave.repository.BpmOALeaveRepository;
import com.develop.mvp.pk.module.bpm.domain.leave.valueobject.LeaveId;
import com.develop.mvp.pk.module.bpm.enums.task.BpmTaskStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.bpm.enums.ErrorCodeConstants.OA_LEAVE_NOT_EXISTS;

@Service
@RequiredArgsConstructor
public class BpmOALeaveApplicationService {
    private static final String PROCESS_KEY = "oa_leave";

    private final BpmOALeaveRepository repo;
    private final BpmProcessInstanceApi processInstanceApi;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long create(Long userId, Integer type, String reason,
                       LocalDateTime startTime, LocalDateTime endTime,
                       Map<String, List<Long>> startUserSelectAssignees) {
        long day = LocalDateTimeUtil.between(startTime, endTime).toDays();
        BpmOALeave leave = BpmOALeaveFactory.create(null, userId, type, reason, startTime, endTime, day);
        repo.save(leave);

        // 发起BPM流程
        Map<String, Object> variables = new HashMap<>();
        variables.put("day", day);
        String processInstanceId = processInstanceApi.createProcessInstance(userId,
                new BpmProcessInstanceCreateReqDTO().setProcessDefinitionKey(PROCESS_KEY)
                        .setVariables(variables).setBusinessKey(String.valueOf(leave.id().value()))
                        .setStartUserSelectAssignees(startUserSelectAssignees)).getCheckedData();

        BpmOALeave updated = BpmOALeaveFactory.reconstitute(leave.id().value(), userId, type, reason,
                startTime, endTime, day, BpmTaskStatusEnum.RUNNING.getStatus(), processInstanceId);
        repo.save(updated);
        publishEvents(updated);
        return leave.id().value();
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        findExisting(id);
        BpmOALeave leave = BpmOALeaveFactory.reconstitute(id, 0L, null, null, null, null, 0L, status, null);
        leave.updateStatus(status);
        repo.save(leave);
        publishEvents(leave);
    }

    public BpmOALeave get(Long id) { return repo.findById(LeaveId.of(id)); }
    public PageResult<BpmOALeave> getPage(Long userId, Integer status, Integer type, String reason,
                                           Integer pageNo, Integer pageSize) {
        return repo.findPage(userId, status, type, reason, pageNo, pageSize);
    }

    private BpmOALeave findExisting(Long id) {
        BpmOALeave leave = repo.findById(LeaveId.of(id));
        if (leave == null) throw exception(OA_LEAVE_NOT_EXISTS);
        return leave;
    }

    private void publishEvents(BpmOALeave leave) {
        for (LeaveDomainEvent event : leave.pullEvents()) eventPublisher.publishEvent(event);
    }
}
