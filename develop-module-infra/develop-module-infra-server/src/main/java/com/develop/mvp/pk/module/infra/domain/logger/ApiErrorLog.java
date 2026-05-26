package com.develop.mvp.pk.module.infra.domain.logger;

// DDD 角色：API 错误日志聚合根

import com.develop.mvp.pk.module.infra.domain.event.DomainEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ApiErrorLog {

    private final Long id;
    private String traceId;
    private Long userId;
    private Integer userType;
    private String applicationName;
    private String requestMethod;
    private Integer requestParams;
    private String requestUrl;
    private String userIp;
    private String userAgent;
    private LocalDateTime exceptionTime;
    private String exceptionName;
    private String exceptionRootCauseMessage;
    private String exceptionStackTrace;
    private String exceptionClassName;
    private String exceptionFileName;
    private Integer exceptionLineNumber;
    private Integer processStatus;
    private Long processUserId;
    private LocalDateTime processTime;

    private final List<DomainEvent> events = new ArrayList<>();

    public ApiErrorLog(Long id, String traceId, Long userId, Integer userType,
                String applicationName, String requestMethod, Integer requestParams,
                String requestUrl, String userIp, String userAgent,
                LocalDateTime exceptionTime, String exceptionName,
                String exceptionRootCauseMessage, String exceptionStackTrace,
                String exceptionClassName, String exceptionFileName,
                Integer exceptionLineNumber, Integer processStatus,
                Long processUserId, LocalDateTime processTime) {
        this.id = id;
        this.traceId = traceId;
        this.userId = userId;
        this.userType = userType;
        this.applicationName = applicationName;
        this.requestMethod = requestMethod;
        this.requestParams = requestParams;
        this.requestUrl = requestUrl;
        this.userIp = userIp;
        this.userAgent = userAgent;
        this.exceptionTime = exceptionTime;
        this.exceptionName = exceptionName;
        this.exceptionRootCauseMessage = exceptionRootCauseMessage;
        this.exceptionStackTrace = exceptionStackTrace;
        this.exceptionClassName = exceptionClassName;
        this.exceptionFileName = exceptionFileName;
        this.exceptionLineNumber = exceptionLineNumber;
        this.processStatus = processStatus;
        this.processUserId = processUserId;
        this.processTime = processTime;
    }

    // ── 业务方法 ──

    /** 标记处理状态 */
    public void markProcessed(Integer processStatus, Long processUserId) {
        this.processStatus = processStatus;
        this.processUserId = processUserId;
        this.processTime = LocalDateTime.now();
    }

    public boolean isProcessed() {
        return processStatus != null
                && !com.develop.mvp.pk.module.infra.enums.logger.ApiErrorLogProcessStatusEnum.INIT.getStatus()
                .equals(processStatus);
    }

    // ── 查询方法 ──

    public Long id() { return id; }
    public String traceId() { return traceId; }
    public Long userId() { return userId; }
    public Integer userType() { return userType; }
    public String applicationName() { return applicationName; }
    public String requestMethod() { return requestMethod; }
    public Integer requestParams() { return requestParams; }
    public String requestUrl() { return requestUrl; }
    public String userIp() { return userIp; }
    public String userAgent() { return userAgent; }
    public LocalDateTime exceptionTime() { return exceptionTime; }
    public String exceptionName() { return exceptionName; }
    public String exceptionRootCauseMessage() { return exceptionRootCauseMessage; }
    public String exceptionStackTrace() { return exceptionStackTrace; }
    public String exceptionClassName() { return exceptionClassName; }
    public String exceptionFileName() { return exceptionFileName; }
    public Integer exceptionLineNumber() { return exceptionLineNumber; }
    public Integer processStatus() { return processStatus; }
    public Long processUserId() { return processUserId; }
    public LocalDateTime processTime() { return processTime; }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> result = new ArrayList<>(events);
        events.clear();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApiErrorLog that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
