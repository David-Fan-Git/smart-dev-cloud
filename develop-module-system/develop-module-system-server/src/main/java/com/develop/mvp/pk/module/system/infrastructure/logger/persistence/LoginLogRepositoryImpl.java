package com.develop.mvp.pk.module.system.infrastructure.logger.persistence;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.dal.dataobject.logger.LoginLogDO;
import com.develop.mvp.pk.module.system.dal.mysql.logger.LoginLogMapper;
import com.develop.mvp.pk.module.system.domain.logger.LoginLog;
import com.develop.mvp.pk.module.system.domain.logger.repository.LoginLogPageCriteria;
import com.develop.mvp.pk.module.system.domain.logger.repository.LoginLogRepository;
import org.springframework.stereotype.Repository;

/**
 * Login Log Repository Impl 领域仓储实现。
 */
@Repository
public class LoginLogRepositoryImpl implements LoginLogRepository {

    private final LoginLogMapper loginLogMapper;

    /**
     * 创建 LoginLogRepositoryImpl 实例。
     *
     * @param loginLogMapper loginLogMapper 参数
     */
    public LoginLogRepositoryImpl(LoginLogMapper loginLogMapper) {
        this.loginLogMapper = loginLogMapper;
    }

    /**
     * 创建 save 对应的数据。
     *
     * @param log log 参数
     */
    @Override
    public void save(LoginLog log) {
        loginLogMapper.insert(toDataObject(log));
    }

    /**
     * 根据编号查询登录日志。
     *
     * @param id 登录日志编号
     * @return 登录日志领域对象，不存在时返回 null
     */
    @Override
    public LoginLog findById(Long id) {
        LoginLogDO loginLogDO = loginLogMapper.selectById(id);
        return loginLogDO != null ? toDomain(loginLogDO) : null;
    }

    /**
     * 分页查询登录日志。
     *
     * @param criteria 登录日志分页查询条件
     * @return 登录日志分页结果
     */
    @Override
    public PageResult<LoginLog> findPage(LoginLogPageCriteria criteria) {
        PageResult<LoginLogDO> pageResult = loginLogMapper.selectPage(criteria);
        return new PageResult<>(pageResult.getList().stream().map(this::toDomain).toList(), pageResult.getTotal());
    }

    /**
     * 执行 to Data Object 对应的业务操作。
     *
     * @param log log 参数
     * @return 处理结果
     */
    private LoginLogDO toDataObject(LoginLog log) {
        LoginLogDO loginLogDO = new LoginLogDO();
        loginLogDO.setId(log.id());
        loginLogDO.setLogType(log.logType());
        loginLogDO.setTraceId(log.traceId());
        loginLogDO.setUserId(log.userId());
        loginLogDO.setUserType(log.userType());
        loginLogDO.setUsername(log.username());
        loginLogDO.setResult(log.result());
        loginLogDO.setUserIp(log.userIp());
        loginLogDO.setUserAgent(log.userAgent());
        return loginLogDO;
    }

    /**
     * 将登录日志持久化对象转换为领域对象。
     *
     * @param loginLogDO 登录日志持久化对象
     * @return 登录日志领域对象
     */
    private LoginLog toDomain(LoginLogDO loginLogDO) {
        return LoginLog.builder()
                .id(loginLogDO.getId())
                .logType(loginLogDO.getLogType())
                .traceId(loginLogDO.getTraceId())
                .userId(loginLogDO.getUserId())
                .userType(loginLogDO.getUserType())
                .username(loginLogDO.getUsername())
                .result(loginLogDO.getResult())
                .userIp(loginLogDO.getUserIp())
                .userAgent(loginLogDO.getUserAgent())
                .createTime(loginLogDO.getCreateTime())
                .build();
    }
}
