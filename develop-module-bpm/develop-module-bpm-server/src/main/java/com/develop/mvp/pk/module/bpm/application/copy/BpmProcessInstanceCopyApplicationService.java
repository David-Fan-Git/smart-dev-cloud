package com.develop.mvp.pk.module.bpm.application.copy;
// DDD 角色：BPM流程抄送应用服务 - AggregateRoot_Bpm_Skill

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.bpm.domain.copy.BpmProcessInstanceCopy;
import com.develop.mvp.pk.module.bpm.domain.copy.BpmProcessInstanceCopyFactory;
import com.develop.mvp.pk.module.bpm.domain.copy.event.CopyDomainEvent;
import com.develop.mvp.pk.module.bpm.domain.copy.repository.BpmProcessInstanceCopyRepository;
import com.develop.mvp.pk.module.bpm.service.definition.BpmProcessDefinitionService;
import com.develop.mvp.pk.module.bpm.service.task.BpmProcessInstanceService;
import com.develop.mvp.pk.module.bpm.service.task.BpmTaskService;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.bpm.enums.ErrorCodeConstants.*;

@Service
@RequiredArgsConstructor
public class BpmProcessInstanceCopyApplicationService {
    private final BpmProcessInstanceCopyRepository repo;
    private final BpmTaskService taskService;
    private final BpmProcessInstanceService processInstanceService;
    private final BpmProcessDefinitionService processDefinitionService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createCopyFromTask(Collection<Long> userIds, String reason, String taskId) {
        Task task = taskService.getTask(taskId);
        if (task == null) throw exception(TASK_NOT_EXISTS);
        createCopy(userIds, reason, task.getProcessInstanceId(),
                task.getTaskDefinitionKey(), task.getName(), task.getId());
    }

    @Transactional
    public void createCopy(Collection<Long> userIds, String reason, String processInstanceId,
                           String activityId, String activityName, String taskId) {
        ProcessInstance processInstance = processInstanceService.getProcessInstance(processInstanceId);
        if (processInstance == null) throw exception(PROCESS_INSTANCE_NOT_EXISTS);
        ProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(
                processInstance.getProcessDefinitionId());
        if (processDefinition == null) throw exception(PROCESS_DEFINITION_NOT_EXISTS);

        List<BpmProcessInstanceCopy> copies = userIds.stream()
                .map(userId -> BpmProcessInstanceCopyFactory.create(null,
                        Long.valueOf(processInstance.getStartUserId()),
                        processInstance.getName(), processInstanceId,
                        processInstance.getProcessDefinitionId(), processDefinition.getCategory(),
                        activityId, activityName, taskId, userId, reason))
                .collect(Collectors.toList());
        repo.saveBatch(copies);
        copies.forEach(c -> { for (CopyDomainEvent e : c.pullEvents()) eventPublisher.publishEvent(e); });
    }

    @Transactional
    public void deleteByProcessInstanceId(String processInstanceId) {
        repo.deleteByProcessInstanceId(processInstanceId);
    }

    public PageResult<BpmProcessInstanceCopy> getPage(Long userId, String processInstanceName,
                                                       Integer pageNo, Integer pageSize) {
        return repo.findPage(userId, processInstanceName, pageNo, pageSize);
    }
}
