package com.develop.mvp.pk.module.system.domain.logger.repository;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.domain.logger.LoginLog;

/**
 * Login Log Repository 领域仓储接口。
 */
public interface LoginLogRepository {

    /**
     * 保存登录日志领域对象。
     *
     * @param log 登录日志领域对象
     */
    void save(LoginLog log);

    /**
     * 根据编号查询登录日志。
     *
     * @param id 登录日志编号
     * @return 登录日志领域对象，不存在时返回 null
     */
    LoginLog findById(Long id);

    /**
     * 分页查询登录日志。
     *
     * @param criteria 登录日志分页查询条件
     * @return 登录日志分页结果
     */
    PageResult<LoginLog> findPage(LoginLogPageCriteria criteria);

}
