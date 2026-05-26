package com.develop.mvp.pk.module.bpm.application.listener;
// DDD 角色：BPM流程监听器应用服务 - AggregateRoot_Bpm_Skill

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.bpm.domain.listener.BpmProcessListener;
import com.develop.mvp.pk.module.bpm.domain.listener.BpmProcessListenerFactory;
import com.develop.mvp.pk.module.bpm.domain.listener.event.ListenerDomainEvent;
import com.develop.mvp.pk.module.bpm.domain.listener.repository.BpmProcessListenerRepository;
import com.develop.mvp.pk.module.bpm.domain.listener.valueobject.ListenerId;
import com.develop.mvp.pk.module.bpm.enums.definition.BpmProcessListenerTypeEnum;
import com.develop.mvp.pk.module.bpm.enums.definition.BpmProcessListenerValueTypeEnum;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.delegate.TaskListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.bpm.enums.ErrorCodeConstants.*;

@Service
@RequiredArgsConstructor
public class BpmProcessListenerApplicationService {
    private final BpmProcessListenerRepository repo;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long create(String name, Integer status, String type, String event, String valueType, String value) {
        validateListenerValue(type, valueType, value);
        BpmProcessListener l = BpmProcessListenerFactory.create(null, name, type, event, valueType, value);
        repo.save(l);
        publishEvents(l);
        return l.id().value();
    }

    @Transactional
    public void update(Long id, String name, Integer status, String type, String event, String valueType, String value) {
        findExisting(id);
        validateListenerValue(type, valueType, value);
        BpmProcessListener l = BpmProcessListenerFactory.reconstitute(id, name, status, type, event, valueType, value);
        repo.save(l);
        publishEvents(l);
    }

    @Transactional
    public void delete(Long id) {
        findExisting(id);
        BpmProcessListener l = BpmProcessListenerFactory.reconstitute(id, "", 0, null, null, null, null);
        l.markDeleted();
        repo.delete(l.id());
        publishEvents(l);
    }

    public BpmProcessListener get(Long id) { return repo.findById(ListenerId.of(id)); }
    public PageResult<BpmProcessListener> getPage(String name, String type, String event, Integer status,
                                                   Integer pageNo, Integer pageSize) {
        return repo.findPage(name, type, event, status, pageNo, pageSize);
    }

    private void validateListenerValue(String type, String valueType, String value) {
        if (BpmProcessListenerValueTypeEnum.CLASS.getType().equals(valueType)) {
            try {
                Class<?> clazz = Class.forName(value);
                if (BpmProcessListenerTypeEnum.EXECUTION.getType().equals(type)
                        && !JavaDelegate.class.isAssignableFrom(clazz)) {
                    throw exception(PROCESS_LISTENER_CLASS_IMPLEMENTS_ERROR, value, JavaDelegate.class.getName());
                } else if (BpmProcessListenerTypeEnum.TASK.getType().equals(type)
                        && !TaskListener.class.isAssignableFrom(clazz)) {
                    throw exception(PROCESS_LISTENER_CLASS_IMPLEMENTS_ERROR, value, TaskListener.class.getName());
                }
            } catch (ClassNotFoundException e) {
                throw exception(PROCESS_LISTENER_CLASS_NOT_FOUND, value);
            }
        }
    }

    private BpmProcessListener findExisting(Long id) {
        BpmProcessListener l = repo.findById(ListenerId.of(id));
        if (l == null) throw exception(PROCESS_LISTENER_NOT_EXISTS);
        return l;
    }

    private void publishEvents(BpmProcessListener listener) {
        for (ListenerDomainEvent event : listener.pullEvents()) eventPublisher.publishEvent(event);
    }
}
