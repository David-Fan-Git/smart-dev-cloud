package com.develop.mvp.pk.module.system.domain.logger.repository;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.domain.logger.OperateLog;

/**
 * Operate Log Repository 领域仓储接口。
 */
public interface OperateLogRepository {

    /**
     * 保存操作日志领域对象。
     *
     * @param log 操作日志领域对象
     */
    void save(OperateLog log);

    /**
     * 根据编号查询操作日志。
     *
     * @param id 操作日志编号
     * @return 操作日志领域对象，不存在时返回 null
     */
    OperateLog findById(Long id);

    /**
     * 分页查询操作日志。
     *
     * @param criteria 操作日志分页查询条件
     * @return 操作日志分页结果
     */
    PageResult<OperateLog> findPage(OperateLogPageCriteria criteria);

}
