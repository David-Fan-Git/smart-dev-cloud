package com.develop.mvp.pk.module.bpm.domain.copy.repository;
// DDD 角色：BPM流程抄送仓储接口 - AggregateRoot_Bpm_Skill
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.bpm.domain.copy.BpmProcessInstanceCopy;
import java.util.Collection;
import java.util.List;

public interface BpmProcessInstanceCopyRepository {
    void save(BpmProcessInstanceCopy copy);
    void saveBatch(List<BpmProcessInstanceCopy> copies);
    void deleteByProcessInstanceId(String processInstanceId);
    PageResult<BpmProcessInstanceCopy> findPage(Long userId, String processInstanceName,
                                                 Integer pageNo, Integer pageSize);
}
