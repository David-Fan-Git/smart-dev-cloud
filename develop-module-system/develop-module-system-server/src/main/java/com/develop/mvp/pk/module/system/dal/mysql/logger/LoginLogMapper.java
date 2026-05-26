package com.develop.mvp.pk.module.system.dal.mysql.logger;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.system.dal.dataobject.logger.LoginLogDO;
import com.develop.mvp.pk.module.system.domain.logger.repository.LoginLogPageCriteria;
import com.develop.mvp.pk.module.system.enums.logger.LoginResultEnum;
import org.apache.ibatis.annotations.Mapper;

/**
 * Login Log Mapper 持久化 Mapper。
 */
@Mapper
public interface LoginLogMapper extends BaseMapperX<LoginLogDO> {

    /**
     * 根据仓储查询条件分页查询登录日志。
     *
     * @param criteria 登录日志分页查询条件
     * @return 登录日志分页结果
     */
    default PageResult<LoginLogDO> selectPage(LoginLogPageCriteria criteria) {
        LambdaQueryWrapperX<LoginLogDO> query = new LambdaQueryWrapperX<LoginLogDO>()
                .likeIfPresent(LoginLogDO::getUserIp, criteria.getUserIp())
                .likeIfPresent(LoginLogDO::getUsername, criteria.getUsername())
                .betweenIfPresent(LoginLogDO::getCreateTime, criteria.getCreateTime());
        if (Boolean.TRUE.equals(criteria.getStatus())) {
            query.eq(LoginLogDO::getResult, LoginResultEnum.SUCCESS.getResult());
        } else if (Boolean.FALSE.equals(criteria.getStatus())) {
            query.gt(LoginLogDO::getResult, LoginResultEnum.SUCCESS.getResult());
        }
        query.orderByDesc(LoginLogDO::getId);
        return selectPage(criteria, query);
    }

}
