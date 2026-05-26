package com.develop.mvp.pk.module.system.job.demo;

import com.develop.mvp.pk.framework.tenant.core.job.TenantJob;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

/**
 * Demo Job 定时任务。
 */
@Component
public class DemoJob {

    /**
     * 执行 execute 对应的业务操作。
     */
    @XxlJob("demoJob")
    @TenantJob
    public void execute() {
        System.out.println("美滋滋");
    }

}
