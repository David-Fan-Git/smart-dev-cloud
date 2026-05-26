package com.develop.mvp.pk.module.bpm.service.task.trigger;

import com.develop.mvp.pk.module.bpm.enums.definition.BpmTriggerTypeEnum;

// TODO @David：可能会想换个包地址
/**
 * BPM 触发器接口
 * <p>
 * 处理不同的动作
 *
 * @author David
 */
public interface BpmTrigger {

    /**
     * 对应触发器类型
     *
     * @return 触发器类型
     */
    BpmTriggerTypeEnum getType();

    /**
     * 触发器执行
     *
     * @param processInstanceId 流程实例编号
     * @param param 触发器参数
     */
    void execute(String processInstanceId, String param);

}
