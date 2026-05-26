package com.develop.mvp.pk.module.infra.domain.logger;

// DDD 角色：API 访问日志聚合根

import com.develop.mvp.pk.module.infra.domain.event.DomainEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ApiAccessLog {

    private final Long id;
    private String traceId;
    private Long userId;
    private Integer userType;
    private String applicationName;
    private String requestMethod;
    private Integer requestParams;
    private String responseBody;
    private String requestUrl;
    private String userIp;
    private String userAgent;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private Integer duration;
    private Integer resultCode;
    private String resultMsg;

    private final List<DomainEvent> events = new ArrayList<>();

    public ApiAccessLog(Long id, String traceId, Long userId, Integer userType,
                 String applicationName, String requestMethod, Integer requestParams,
                 String responseBody, String requestUrl, String userIp, String userAgent,
                 LocalDateTime beginTime, LocalDateTime endTime, Integer duration,
                 Integer resultCode, String resultMsg) {
        this.id = id;
        this.traceId = traceId;
        this.userId = userId;
        this.userType = userType;
        this.applicationName = applicationName;
        this.requestMethod = requestMethod;
        this.requestParams = requestParams;
        this.responseBody = responseBody;
        this.requestUrl = requestUrl;
        this.userIp = userIp;
        this.userAgent = userAgent;
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.duration = duration;
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
    }

    // ── 查询方法 ──

    public Long id() { return id; }
    public String traceId() { return traceId; }
    public Long userId() { return userId; }
    public Integer userType() { return userType; }
    public String applicationName() { return applicationName; }
    public String requestMethod() { return requestMethod; }
    public Integer requestParams() { return requestParams; }
    public String responseBody() { return responseBody; }
    public String requestUrl() { return requestUrl; }
    public String userIp() { return userIp; }
    public String userAgent() { return userAgent; }
    public LocalDateTime beginTime() { return beginTime; }
    public LocalDateTime endTime() { return endTime; }
    public Integer duration() { return duration; }
    public Integer resultCode() { return resultCode; }
    public String resultMsg() { return resultMsg; }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> result = new ArrayList<>(events);
        events.clear();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApiAccessLog that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
