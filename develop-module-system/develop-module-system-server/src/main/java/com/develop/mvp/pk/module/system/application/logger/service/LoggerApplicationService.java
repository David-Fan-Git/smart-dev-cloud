package com.develop.mvp.pk.module.system.application.logger.service;

import com.develop.mvp.pk.framework.common.biz.system.logger.dto.OperateLogCreateReqDTO;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.api.logger.dto.LoginLogCreateReqDTO;
import com.develop.mvp.pk.module.system.application.logger.dto.LoginLogDTO;
import com.develop.mvp.pk.module.system.application.logger.dto.OperateLogDTO;
import com.develop.mvp.pk.module.system.application.logger.port.inbound.LoggerUseCase;
import com.develop.mvp.pk.module.system.application.logger.query.LoginLogPageQuery;
import com.develop.mvp.pk.module.system.application.logger.query.OperateLogPageQuery;
import com.develop.mvp.pk.module.system.convert.logger.LoggerConvert;
import com.develop.mvp.pk.module.system.domain.logger.LoginLog;
import com.develop.mvp.pk.module.system.domain.logger.OperateLog;
import com.develop.mvp.pk.module.system.domain.logger.repository.LoginLogPageCriteria;
import com.develop.mvp.pk.module.system.domain.logger.repository.LoginLogRepository;
import com.develop.mvp.pk.module.system.domain.logger.repository.OperateLogPageCriteria;
import com.develop.mvp.pk.module.system.domain.logger.repository.OperateLogRepository;

/**
 * Logger Application Service 应用服务。
 */
public class LoggerApplicationService implements LoggerUseCase {

    private final LoginLogRepository loginLogRepository;

    private final OperateLogRepository operateLogRepository;

    /**
     * 创建 LoggerApplicationService 实例。
     *
     * @param loginLogRepository loginLogRepository 参数
     * @param operateLogRepository operateLogRepository 参数
     */
    public LoggerApplicationService(LoginLogRepository loginLogRepository, OperateLogRepository operateLogRepository) {
        this.loginLogRepository = loginLogRepository;
        this.operateLogRepository = operateLogRepository;
    }

    /**
     * 根据编号查询登录日志。
     *
     * @param id 登录日志编号
     * @return 登录日志应用 DTO，不存在时返回 null
     */
    public LoginLogDTO getLoginLog(Long id) {
        return LoggerConvert.INSTANCE.convert(loginLogRepository.findById(id));
    }

    /**
     * 分页查询登录日志。
     *
     * @param query 登录日志分页查询条件
     * @return 登录日志分页结果
     */
    public PageResult<LoginLogDTO> getLoginLogPage(LoginLogPageQuery query) {
        PageResult<LoginLog> pageResult = loginLogRepository.findPage(toCriteria(query));
        return new PageResult<>(pageResult.getList().stream().map(LoggerConvert.INSTANCE::convert).toList(), pageResult.getTotal());
    }

    /**
     * 创建 create Login Log 对应的数据。
     *
     * @param reqDTO reqDTO 参数
     */
    public void createLoginLog(LoginLogCreateReqDTO reqDTO) {
        loginLogRepository.save(LoginLog.builder().logType(reqDTO.getLogType()).traceId(reqDTO.getTraceId()).userId(reqDTO.getUserId()).userType(reqDTO.getUserType()).username(reqDTO.getUsername()).result(reqDTO.getResult()).userIp(reqDTO.getUserIp()).userAgent(reqDTO.getUserAgent()).build());
    }

    /**
     * 创建 create Operate Log 对应的数据。
     *
     * @param createReqDTO createReqDTO 参数
     */
    public void createOperateLog(OperateLogCreateReqDTO createReqDTO) {
        operateLogRepository.save(OperateLog.builder().traceId(createReqDTO.getTraceId()).userId(createReqDTO.getUserId()).userType(createReqDTO.getUserType()).type(createReqDTO.getType()).subType(createReqDTO.getSubType()).bizId(createReqDTO.getBizId()).action(createReqDTO.getAction()).extra(createReqDTO.getExtra()).requestMethod(createReqDTO.getRequestMethod()).requestUrl(createReqDTO.getRequestUrl()).userIp(createReqDTO.getUserIp()).userAgent(createReqDTO.getUserAgent()).build());
    }

    /**
     * 根据编号查询操作日志。
     *
     * @param id 操作日志编号
     * @return 操作日志应用 DTO，不存在时返回 null
     */
    public OperateLogDTO getOperateLog(Long id) {
        return LoggerConvert.INSTANCE.convert(operateLogRepository.findById(id));
    }

    /**
     * 分页查询操作日志。
     *
     * @param query 操作日志分页查询条件
     * @return 操作日志分页结果
     */
    public PageResult<OperateLogDTO> getOperateLogPage(OperateLogPageQuery query) {
        PageResult<OperateLog> pageResult = operateLogRepository.findPage(toCriteria(query));
        return new PageResult<>(pageResult.getList().stream().map(LoggerConvert.INSTANCE::convert).toList(), pageResult.getTotal());
    }

    /**
     * 将登录日志应用查询对象转换为仓储查询条件。
     *
     * @param query 登录日志应用查询对象
     * @return 登录日志仓储查询条件
     */
    private LoginLogPageCriteria toCriteria(LoginLogPageQuery query) {
        LoginLogPageCriteria criteria = new LoginLogPageCriteria();
        criteria.setPageNo(query.getPageNo());
        criteria.setPageSize(query.getPageSize());
        criteria.setUserIp(query.getUserIp());
        criteria.setUsername(query.getUsername());
        criteria.setStatus(query.getStatus());
        criteria.setCreateTime(query.getCreateTime());
        return criteria;
    }

    /**
     * 将操作日志应用查询对象转换为仓储查询条件。
     *
     * @param query 操作日志应用查询对象
     * @return 操作日志仓储查询条件
     */
    private OperateLogPageCriteria toCriteria(OperateLogPageQuery query) {
        OperateLogPageCriteria criteria = new OperateLogPageCriteria();
        criteria.setPageNo(query.getPageNo());
        criteria.setPageSize(query.getPageSize());
        criteria.setUserId(query.getUserId());
        criteria.setBizId(query.getBizId());
        criteria.setType(query.getType());
        criteria.setSubType(query.getSubType());
        criteria.setAction(query.getAction());
        criteria.setCreateTime(query.getCreateTime());
        criteria.setExactType(query.isExactType());
        return criteria;
    }

}
