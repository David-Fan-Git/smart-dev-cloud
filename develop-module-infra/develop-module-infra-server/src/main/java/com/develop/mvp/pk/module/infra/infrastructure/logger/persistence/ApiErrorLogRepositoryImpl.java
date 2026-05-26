package com.develop.mvp.pk.module.infra.infrastructure.logger.persistence;

// DDD 角色：ApiErrorLogRepository 的 MyBatis 实现

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.infra.dal.dataobject.logger.ApiErrorLogDO;
import com.develop.mvp.pk.module.infra.dal.mysql.logger.ApiErrorLogMapper;
import com.develop.mvp.pk.module.infra.domain.logger.ApiErrorLog;
import com.develop.mvp.pk.module.infra.domain.logger.repository.ApiErrorLogPageQuery;
import com.develop.mvp.pk.module.infra.domain.logger.repository.ApiErrorLogRepository;
import com.develop.mvp.pk.module.infra.infrastructure.logger.ApiErrorLogFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ApiErrorLogRepositoryImpl implements ApiErrorLogRepository {

    private final ApiErrorLogMapper apiErrorLogMapper;

    public ApiErrorLogRepositoryImpl(ApiErrorLogMapper apiErrorLogMapper) {
        this.apiErrorLogMapper = apiErrorLogMapper;
    }

    @Override
    public ApiErrorLog save(ApiErrorLog log) {
        ApiErrorLogDO logDO = ApiErrorLogDO.builder()
                .traceId(log.traceId())
                .userId(log.userId())
                .userType(log.userType())
                .applicationName(log.applicationName())
                .requestMethod(log.requestMethod())
                .requestUrl(log.requestUrl())
                .userIp(log.userIp())
                .userAgent(log.userAgent())
                .exceptionTime(log.exceptionTime())
                .exceptionName(log.exceptionName())
                .exceptionRootCauseMessage(log.exceptionRootCauseMessage())
                .exceptionStackTrace(log.exceptionStackTrace())
                .exceptionClassName(log.exceptionClassName())
                .exceptionFileName(log.exceptionFileName())
                .exceptionLineNumber(log.exceptionLineNumber())
                .processStatus(log.processStatus())
                .build();
        apiErrorLogMapper.insert(logDO);
        return log;
    }

    @Override
    public ApiErrorLog findById(Long id) {
        ApiErrorLogDO logDO = apiErrorLogMapper.selectById(id);
        return logDO != null ? ApiErrorLogFactory.reconstitute(
                logDO.getId(), logDO.getTraceId(), logDO.getUserId(), logDO.getUserType(),
                logDO.getApplicationName(), logDO.getRequestMethod(), null,
                logDO.getRequestUrl(), logDO.getUserIp(), logDO.getUserAgent(),
                logDO.getExceptionTime(), logDO.getExceptionName(),
                logDO.getExceptionRootCauseMessage(), logDO.getExceptionStackTrace(),
                logDO.getExceptionClassName(), logDO.getExceptionFileName(),
                logDO.getExceptionLineNumber(), logDO.getProcessStatus(),
                logDO.getProcessUserId(), logDO.getProcessTime()
        ) : null;
    }

    @Override
    public ApiErrorLog update(ApiErrorLog log) {
        ApiErrorLogDO logDO = new ApiErrorLogDO();
        logDO.setId(log.id());
        logDO.setProcessStatus(log.processStatus());
        logDO.setProcessUserId(log.processUserId());
        logDO.setProcessTime(log.processTime());
        apiErrorLogMapper.updateById(logDO);
        return log;
    }

    @Override
    public PageResult<ApiErrorLog> findPage(ApiErrorLogPageQuery query) {
        var reqVO = new com.develop.mvp.pk.module.infra.controller.admin.logger.vo.apierrorlog.ApiErrorLogPageReqVO();
        reqVO.setUserId(query.userId());
        reqVO.setUserType(query.userType());
        reqVO.setApplicationName(query.applicationName());
        reqVO.setRequestUrl(query.requestUrl());
        reqVO.setExceptionTime(query.beginTime());
        reqVO.setProcessStatus(query.processStatus());
        reqVO.setPageNo(query.pageNo());
        reqVO.setPageSize(query.pageSize());

        PageResult<ApiErrorLogDO> doPage = apiErrorLogMapper.selectPage(reqVO);
        List<ApiErrorLog> logs = doPage.getList().stream()
                .map(doObj -> ApiErrorLogFactory.reconstitute(
                        doObj.getId(), doObj.getTraceId(), doObj.getUserId(), doObj.getUserType(),
                        doObj.getApplicationName(), doObj.getRequestMethod(), null,
                        doObj.getRequestUrl(), doObj.getUserIp(), doObj.getUserAgent(),
                        doObj.getExceptionTime(), doObj.getExceptionName(),
                        doObj.getExceptionRootCauseMessage(), doObj.getExceptionStackTrace(),
                        doObj.getExceptionClassName(), doObj.getExceptionFileName(),
                        doObj.getExceptionLineNumber(), doObj.getProcessStatus(),
                        doObj.getProcessUserId(), doObj.getProcessTime()
                )).collect(Collectors.toList());
        return new PageResult<>(logs, doPage.getTotal());
    }

    @Override
    public Integer deleteByCreateTimeLt(LocalDateTime expireDate, Integer deleteLimit) {
        return apiErrorLogMapper.deleteByCreateTimeLt(expireDate, deleteLimit);
    }
}
