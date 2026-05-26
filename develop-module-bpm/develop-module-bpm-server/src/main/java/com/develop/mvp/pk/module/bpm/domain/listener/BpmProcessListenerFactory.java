package com.develop.mvp.pk.module.bpm.domain.listener;
// DDD 角色：BPM流程监听器工厂 - AggregateRoot_Bpm_Skill
import com.develop.mvp.pk.module.bpm.domain.listener.valueobject.*;

public final class BpmProcessListenerFactory {
    private BpmProcessListenerFactory() {}
    public static BpmProcessListener create(Long id, String name, String type, String event, String valueType, String value) {
        return new BpmProcessListener(ListenerId.of(id), ListenerName.of(name), ListenerStatus.ENABLED,
                type, event, valueType, value);
    }
    public static BpmProcessListener reconstitute(Long id, String name, Integer status,
                                                   String type, String event, String valueType, String value) {
        return new BpmProcessListener(ListenerId.of(id), ListenerName.of(name), ListenerStatus.of(status),
                type, event, valueType, value);
    }
}
