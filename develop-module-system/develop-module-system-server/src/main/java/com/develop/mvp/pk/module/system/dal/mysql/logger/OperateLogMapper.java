package com.develop.mvp.pk.module.system.dal.mysql.logger;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.system.dal.dataobject.logger.OperateLogDO;
import com.develop.mvp.pk.module.system.domain.logger.repository.OperateLogPageCriteria;
import org.apache.ibatis.annotations.Mapper;

/**
 * Operate Log Mapper 持久化 Mapper。
 */
@Mapper
public interface OperateLogMapper extends BaseMapperX<OperateLogDO> {

    /**
     * 根据仓储查询条件分页查询操作日志。
     *
     * @param criteria 操作日志分页查询条件
     * @return 操作日志分页结果
     */
    default PageResult<OperateLogDO> selectPage(OperateLogPageCriteria criteria) {
        LambdaQueryWrapperX<OperateLogDO> query = new LambdaQueryWrapperX<OperateLogDO>()
                .eqIfPresent(OperateLogDO::getUserId, criteria.getUserId())
                .eqIfPresent(OperateLogDO::getBizId, criteria.getBizId())
                .likeIfPresent(OperateLogDO::getSubType, criteria.getSubType())
                .likeIfPresent(OperateLogDO::getAction, criteria.getAction())
                .betweenIfPresent(OperateLogDO::getCreateTime, criteria.getCreateTime())
                .orderByDesc(OperateLogDO::getId);
        if (criteria.isExactType()) {
            query.eqIfPresent(OperateLogDO::getType, criteria.getType());
        } else {
            query.likeIfPresent(OperateLogDO::getType, criteria.getType());
        }
        return selectPage(criteria, query);
    }

}
