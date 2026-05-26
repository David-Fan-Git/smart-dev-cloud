package com.develop.mvp.pk.module.bpm.domain.copy;
// DDD 角色：BPM流程抄送工厂 - AggregateRoot_Bpm_Skill
import com.develop.mvp.pk.module.bpm.domain.copy.valueobject.CopyId;

public final class BpmProcessInstanceCopyFactory {
    private BpmProcessInstanceCopyFactory() {}
    public static BpmProcessInstanceCopy create(Long id, Long startUserId, String processInstanceName,
                                                 String processInstanceId, String processDefinitionId, String category,
                                                 String activityId, String activityName, String taskId,
                                                 Long userId, String reason) {
        return new BpmProcessInstanceCopy(CopyId.of(id), startUserId, processInstanceName,
                processInstanceId, processDefinitionId, category, activityId, activityName, taskId, userId, reason);
    }
}
