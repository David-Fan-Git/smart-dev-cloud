package com.develop.mvp.pk.module.infra.application.logger.port.inbound;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.infra.domain.logger.ApiErrorLog;
import com.develop.mvp.pk.module.infra.domain.logger.repository.ApiErrorLogPageQuery;

public interface ApiErrorLogUseCase {

    void processApiErrorLog(Long id, Integer processStatus, Long processUserId);

    ApiErrorLog getApiErrorLog(Long id);

    PageResult<ApiErrorLog> getApiErrorLogPage(ApiErrorLogPageQuery query);

    Integer cleanErrorLog(Integer exceedDay, Integer deleteLimit);
}
