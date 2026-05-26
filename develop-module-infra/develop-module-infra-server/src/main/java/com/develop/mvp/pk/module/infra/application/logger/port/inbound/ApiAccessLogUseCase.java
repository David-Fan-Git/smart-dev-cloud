package com.develop.mvp.pk.module.infra.application.logger.port.inbound;

import com.develop.mvp.pk.framework.common.biz.infra.logger.dto.ApiAccessLogCreateReqDTO;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.infra.domain.logger.ApiAccessLog;
import com.develop.mvp.pk.module.infra.domain.logger.repository.ApiAccessLogPageQuery;

public interface ApiAccessLogUseCase {

    void createApiAccessLog(ApiAccessLogCreateReqDTO createDTO);

    ApiAccessLog getApiAccessLog(Long id);

    PageResult<ApiAccessLog> getApiAccessLogPage(ApiAccessLogPageQuery query);

    Integer cleanAccessLog(Integer exceedDay, Integer deleteLimit);
}
