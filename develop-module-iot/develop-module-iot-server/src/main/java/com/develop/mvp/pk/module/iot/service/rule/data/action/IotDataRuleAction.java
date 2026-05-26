package com.develop.mvp.pk.module.iot.service.rule.data.action;

import com.develop.mvp.pk.module.iot.core.mq.message.IotDeviceMessage;
import com.develop.mvp.pk.module.iot.dal.dataobject.rule.IotDataSinkDO;

/**
 * IoT 数据流转目的的执行器 action 接口
 *
 * @author David
 */
public interface IotDataRuleAction {

    /**
     * 获取数据流转目的类型
     *
     * @return 数据流转目的类型
     */
    Integer getType();

    /**
     * 执行数据流转目的操作
     *
     * @param message    设备消息
     * @param dataSink 数据流转目的
     */
    void execute(IotDeviceMessage message, IotDataSinkDO dataSink);

}
