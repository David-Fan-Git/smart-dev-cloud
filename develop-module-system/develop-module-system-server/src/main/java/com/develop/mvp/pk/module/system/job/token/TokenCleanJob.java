package com.develop.mvp.pk.module.system.job.token;

import com.develop.mvp.pk.framework.tenant.core.aop.TenantIgnore;
import com.develop.mvp.pk.module.system.application.oauth2.port.inbound.OAuth2UseCase;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 物理删除过期 N 天的令牌的 Job
 *
 * @author David
 */
@Component
@Slf4j
public class TokenCleanJob {

    @Resource
    private OAuth2UseCase oauth2TokenService;

    /**
     * 清理过期（14）天的令牌
     */
    private static final Integer JOB_CLEAN_RETAIN_DAY = 14;

    /**
     * 每次删除间隔的条数，如果值太高可能会造成数据库的压力过大
     */
    private static final Integer DELETE_LIMIT = 100;

    /**
     * 执行 execute 对应的业务操作。
     */
    @XxlJob("tokenCleanJob")
    @TenantIgnore
    public void execute() {
        Integer refreshCount = oauth2TokenService.cleanRefreshToken(JOB_CLEAN_RETAIN_DAY, DELETE_LIMIT);
        log.info("[execute][定时执行清理刷新令牌数量 ({}) 个]", refreshCount);
        Integer accessCount = oauth2TokenService.cleanAccessToken(JOB_CLEAN_RETAIN_DAY, DELETE_LIMIT);
        log.info("[execute][定时执行清理访问令牌数量 ({}) 个]", accessCount);
        XxlJobHelper.handleSuccess(
                String.format("定时执行清理刷新令牌数量 %s 个，访问令牌数量 %s 个", refreshCount, accessCount));
    }

}
