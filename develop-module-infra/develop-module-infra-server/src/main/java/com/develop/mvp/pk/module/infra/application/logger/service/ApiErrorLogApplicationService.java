package com.develop.mvp.pk.module.infra.application.logger.service;

// DDD 角色：应用编排服务 - API 错误日志

import com.develop.mvp.pk.framework.common.biz.infra.logger.dto.ApiErrorLogCreateReqDTO;
import com.develop.mvp.pk.module.infra.application.logger.port.inbound.ApiErrorLogUseCase;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.string.StrUtils;
import com.develop.mvp.pk.framework.tenant.core.context.TenantContextHolder;
import com.develop.mvp.pk.framework.tenant.core.util.TenantUtils;
import com.develop.mvp.pk.module.infra.domain.event.DomainEventPublisher;
import com.develop.mvp.pk.module.infra.domain.logger.ApiErrorLog;
import com.develop.mvp.pk.module.infra.domain.logger.repository.ApiErrorLogPageQuery;
import com.develop.mvp.pk.module.infra.domain.logger.repository.ApiErrorLogRepository;
import com.develop.mvp.pk.module.infra.enums.logger.ApiErrorLogProcessStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.infra.dal.dataobject.logger.ApiErrorLogDO.REQUEST_PARAMS_MAX_LENGTH;
import static com.develop.mvp.pk.module.infra.enums.ErrorCodeConstants.API_ERROR_LOG_NOT_FOUND;
import static com.develop.mvp.pk.module.infra.enums.ErrorCodeConstants.API_ERROR_LOG_PROCESSED;

@Service
public class ApiErrorLogApplicationService implements ApiErrorLogUseCase {

    private final ApiErrorLogRepository apiErrorLogRepository;
    private final DomainEventPublisher eventPublisher;

    public ApiErrorLogApplicationService(ApiErrorLogRepository apiErrorLogRepository,
                                          DomainEventPublisher eventPublisher) {
        this.apiErrorLogRepository = apiErrorLogRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void processApiErrorLog(Long id, Integer processStatus, Long processUserId) {
        ApiErrorLog errorLog = apiErrorLogRepository.findById(id);
        if (errorLog == null) throw exception(API_ERROR_LOG_NOT_FOUND);
        if (errorLog.isProcessed()) throw exception(API_ERROR_LOG_PROCESSED);
        errorLog.markProcessed(processStatus, processUserId);
        apiErrorLogRepository.update(errorLog);
    }

    public ApiErrorLog getApiErrorLog(Long id) {
        return apiErrorLogRepository.findById(id);
    }

    public PageResult<ApiErrorLog> getApiErrorLogPage(ApiErrorLogPageQuery query) {
        return apiErrorLogRepository.findPage(query);
    }

    @Transactional
    public Integer cleanErrorLog(Integer exceedDay, Integer deleteLimit) {
        LocalDateTime expireDate = LocalDateTime.now().minusDays(exceedDay);
        int count = 0;
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            int deleteCount = apiErrorLogRepository.deleteByCreateTimeLt(expireDate, deleteLimit);
            count += deleteCount;
            if (deleteCount < deleteLimit) break;
        }
        return count;
    }
}
