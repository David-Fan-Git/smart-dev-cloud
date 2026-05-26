package com.develop.mvp.pk.module.system.infrastructure.logger.persistence;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.dal.dataobject.logger.OperateLogDO;
import com.develop.mvp.pk.module.system.dal.mysql.logger.OperateLogMapper;
import com.develop.mvp.pk.module.system.domain.logger.OperateLog;
import com.develop.mvp.pk.module.system.domain.logger.repository.OperateLogPageCriteria;
import com.develop.mvp.pk.module.system.domain.logger.repository.OperateLogRepository;
import org.springframework.stereotype.Repository;

/**
 * Operate Log Repository Impl 领域仓储实现。
 */
@Repository
public class OperateLogRepositoryImpl implements OperateLogRepository {

    private final OperateLogMapper operateLogMapper;

    /**
     * 创建 OperateLogRepositoryImpl 实例。
     *
     * @param operateLogMapper operateLogMapper 参数
     */
    public OperateLogRepositoryImpl(OperateLogMapper operateLogMapper) {
        this.operateLogMapper = operateLogMapper;
    }

    /**
     * 创建 save 对应的数据。
     *
     * @param log log 参数
     */
    @Override
    public void save(OperateLog log) {
        operateLogMapper.insert(toDataObject(log));
    }

    /**
     * 根据编号查询操作日志。
     *
     * @param id 操作日志编号
     * @return 操作日志领域对象，不存在时返回 null
     */
    @Override
    public OperateLog findById(Long id) {
        OperateLogDO operateLogDO = operateLogMapper.selectById(id);
        return operateLogDO != null ? toDomain(operateLogDO) : null;
    }

    /**
     * 分页查询操作日志。
     *
     * @param criteria 操作日志分页查询条件
     * @return 操作日志分页结果
     */
    @Override
    public PageResult<OperateLog> findPage(OperateLogPageCriteria criteria) {
        PageResult<OperateLogDO> pageResult = operateLogMapper.selectPage(criteria);
        return new PageResult<>(pageResult.getList().stream().map(this::toDomain).toList(), pageResult.getTotal());
    }

    /**
     * 执行 to Data Object 对应的业务操作。
     *
     * @param log log 参数
     * @return 处理结果
     */
    private OperateLogDO toDataObject(OperateLog log) {
        OperateLogDO operateLogDO = new OperateLogDO();
        operateLogDO.setId(log.id());
        operateLogDO.setTraceId(log.traceId());
        operateLogDO.setUserId(log.userId());
        operateLogDO.setUserType(log.userType());
        operateLogDO.setType(log.type());
        operateLogDO.setSubType(log.subType());
        operateLogDO.setBizId(log.bizId());
        operateLogDO.setAction(log.action());
        operateLogDO.setExtra(log.extra());
        operateLogDO.setRequestMethod(log.requestMethod());
        operateLogDO.setRequestUrl(log.requestUrl());
        operateLogDO.setUserIp(log.userIp());
        operateLogDO.setUserAgent(log.userAgent());
        return operateLogDO;
    }

    /**
     * 将操作日志持久化对象转换为领域对象。
     *
     * @param operateLogDO 操作日志持久化对象
     * @return 操作日志领域对象
     */
    private OperateLog toDomain(OperateLogDO operateLogDO) {
        return OperateLog.builder()
                .id(operateLogDO.getId())
                .traceId(operateLogDO.getTraceId())
                .userId(operateLogDO.getUserId())
                .userType(operateLogDO.getUserType())
                .type(operateLogDO.getType())
                .subType(operateLogDO.getSubType())
                .bizId(operateLogDO.getBizId())
                .action(operateLogDO.getAction())
                .extra(operateLogDO.getExtra())
                .requestMethod(operateLogDO.getRequestMethod())
                .requestUrl(operateLogDO.getRequestUrl())
                .userIp(operateLogDO.getUserIp())
                .userAgent(operateLogDO.getUserAgent())
                .createTime(operateLogDO.getCreateTime())
                .build();
    }
}
