package com.develop.mvp.pk.module.infra.infrastructure.logger.persistence;

// DDD 角色：ApiAccessLogRepository 的 MyBatis 实现

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.infra.dal.dataobject.logger.ApiAccessLogDO;
import com.develop.mvp.pk.module.infra.dal.mysql.logger.ApiAccessLogMapper;
import com.develop.mvp.pk.module.infra.domain.logger.ApiAccessLog;
import com.develop.mvp.pk.module.infra.domain.logger.repository.ApiAccessLogPageQuery;
import com.develop.mvp.pk.module.infra.domain.logger.repository.ApiAccessLogRepository;
import com.develop.mvp.pk.module.infra.infrastructure.logger.ApiAccessLogFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ApiAccessLogRepositoryImpl implements ApiAccessLogRepository {

    private final ApiAccessLogMapper apiAccessLogMapper;

    public ApiAccessLogRepositoryImpl(ApiAccessLogMapper apiAccessLogMapper) {
        this.apiAccessLogMapper = apiAccessLogMapper;
    }

    @Override
    public ApiAccessLog save(ApiAccessLog log) {
        ApiAccessLogDO logDO = BeanUtils.toBean(log, ApiAccessLogDO.class);
        apiAccessLogMapper.insert(logDO);
        return log;
    }

    @Override
    public ApiAccessLog findById(Long id) {
        ApiAccessLogDO logDO = apiAccessLogMapper.selectById(id);
        return logDO != null ? ApiAccessLogFactory.reconstitute(
                logDO.getId(), logDO.getTraceId(), logDO.getUserId(), logDO.getUserType(),
                logDO.getApplicationName(), logDO.getRequestMethod(), null,
                logDO.getResponseBody(), logDO.getRequestUrl(), logDO.getUserIp(),
                logDO.getUserAgent(), logDO.getBeginTime(), logDO.getEndTime(),
                logDO.getDuration(), logDO.getResultCode(), logDO.getResultMsg()
        ) : null;
    }

    @Override
    public PageResult<ApiAccessLog> findPage(ApiAccessLogPageQuery query) {
        var reqVO = new com.develop.mvp.pk.module.infra.controller.admin.logger.vo.apiaccesslog.ApiAccessLogPageReqVO();
        reqVO.setUserId(query.userId());
        reqVO.setUserType(query.userType());
        reqVO.setApplicationName(query.applicationName());
        reqVO.setRequestUrl(query.requestUrl());
        reqVO.setBeginTime(query.beginTime());
        reqVO.setDuration(query.duration());
        reqVO.setResultCode(query.resultCode());
        reqVO.setPageNo(query.pageNo());
        reqVO.setPageSize(query.pageSize());

        PageResult<ApiAccessLogDO> doPage = apiAccessLogMapper.selectPage(reqVO);
        List<ApiAccessLog> logs = doPage.getList().stream()
                .map(doObj -> ApiAccessLogFactory.reconstitute(
                        doObj.getId(), doObj.getTraceId(), doObj.getUserId(), doObj.getUserType(),
                        doObj.getApplicationName(), doObj.getRequestMethod(), null,
                        doObj.getResponseBody(), doObj.getRequestUrl(), doObj.getUserIp(),
                        doObj.getUserAgent(), doObj.getBeginTime(), doObj.getEndTime(),
                        doObj.getDuration(), doObj.getResultCode(), doObj.getResultMsg()
                )).collect(Collectors.toList());
        return new PageResult<>(logs, doPage.getTotal());
    }

    @Override
    public Integer deleteByCreateTimeLt(LocalDateTime expireDate, Integer deleteLimit) {
        return apiAccessLogMapper.deleteByCreateTimeLt(expireDate, deleteLimit);
    }
}
