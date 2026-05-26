package com.develop.mvp.pk.module.bpm.service.oa.listener;

import com.develop.mvp.pk.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import com.develop.mvp.pk.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import com.develop.mvp.pk.module.bpm.service.oa.BpmOALeaveService;
import com.develop.mvp.pk.module.bpm.service.oa.BpmOALeaveServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * OA 请假单的结果的监听器实现类
 *
 * @author David
 */
@Component
public class BpmOALeaveStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private BpmOALeaveService leaveService;

    @Override
    protected String getProcessDefinitionKey() {
        return BpmOALeaveServiceImpl.PROCESS_KEY;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        leaveService.updateLeaveStatus(Long.parseLong(event.getBusinessKey()), event.getStatus());
    }

}
