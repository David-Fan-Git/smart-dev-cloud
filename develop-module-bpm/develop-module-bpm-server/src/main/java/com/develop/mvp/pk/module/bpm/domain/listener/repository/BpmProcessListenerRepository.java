package com.develop.mvp.pk.module.bpm.domain.listener.repository;
// DDD 角色：BPM流程监听器仓储接口 - AggregateRoot_Bpm_Skill
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.bpm.domain.listener.BpmProcessListener;
import com.develop.mvp.pk.module.bpm.domain.listener.valueobject.ListenerId;

public interface BpmProcessListenerRepository {
    void save(BpmProcessListener listener);
    void delete(ListenerId id);
    BpmProcessListener findById(ListenerId id);
    PageResult<BpmProcessListener> findPage(String name, String type, String event, Integer status,
                                            Integer pageNo, Integer pageSize);
}
