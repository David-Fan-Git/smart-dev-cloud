package com.develop.mvp.pk.module.infra.infrastructure.logger;

// DDD 角色：工厂，负责创建和重建 ApiAccessLog 聚合

import com.develop.mvp.pk.module.infra.domain.logger.ApiAccessLog;

import java.time.LocalDateTime;

public final class ApiAccessLogFactory {

    private ApiAccessLogFactory() {}

    /** 重建 ApiAccessLog */
    public static ApiAccessLog reconstitute(Long id, String traceId, Long userId, Integer userType,
                                             String applicationName, String requestMethod,
                                             Integer requestParams, String responseBody,
                                             String requestUrl, String userIp, String userAgent,
                                             LocalDateTime beginTime, LocalDateTime endTime,
                                             Integer duration, Integer resultCode, String resultMsg) {
        return new ApiAccessLog(id, traceId, userId, userType, applicationName, requestMethod,
                requestParams, responseBody, requestUrl, userIp, userAgent, beginTime, endTime,
                duration, resultCode, resultMsg);
    }
}
