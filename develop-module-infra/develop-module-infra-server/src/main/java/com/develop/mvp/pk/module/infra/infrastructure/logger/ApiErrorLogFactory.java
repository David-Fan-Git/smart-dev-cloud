package com.develop.mvp.pk.module.infra.infrastructure.logger;

// DDD 角色：工厂，负责创建和重建 ApiErrorLog 聚合

import com.develop.mvp.pk.module.infra.domain.logger.ApiErrorLog;

import java.time.LocalDateTime;

public final class ApiErrorLogFactory {

    private ApiErrorLogFactory() {}

    /** 重建 ApiErrorLog */
    public static ApiErrorLog reconstitute(Long id, String traceId, Long userId, Integer userType,
                                            String applicationName, String requestMethod,
                                            Integer requestParams, String requestUrl,
                                            String userIp, String userAgent,
                                            LocalDateTime exceptionTime, String exceptionName,
                                            String exceptionRootCauseMessage,
                                            String exceptionStackTrace, String exceptionClassName,
                                            String exceptionFileName, Integer exceptionLineNumber,
                                            Integer processStatus, Long processUserId,
                                            LocalDateTime processTime) {
        return new ApiErrorLog(id, traceId, userId, userType, applicationName, requestMethod,
                requestParams, requestUrl, userIp, userAgent, exceptionTime, exceptionName,
                exceptionRootCauseMessage, exceptionStackTrace, exceptionClassName,
                exceptionFileName, exceptionLineNumber, processStatus, processUserId, processTime);
    }
}
