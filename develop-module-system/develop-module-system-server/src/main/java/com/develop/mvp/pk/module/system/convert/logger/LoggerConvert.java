package com.develop.mvp.pk.module.system.convert.logger;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.system.api.logger.dto.OperateLogPageReqDTO;
import com.develop.mvp.pk.module.system.api.logger.dto.OperateLogRespDTO;
import com.develop.mvp.pk.module.system.application.logger.dto.LoginLogDTO;
import com.develop.mvp.pk.module.system.application.logger.dto.OperateLogDTO;
import com.develop.mvp.pk.module.system.application.logger.query.LoginLogPageQuery;
import com.develop.mvp.pk.module.system.application.logger.query.OperateLogPageQuery;
import com.develop.mvp.pk.module.system.controller.admin.logger.vo.loginlog.LoginLogPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.logger.vo.loginlog.LoginLogRespVO;
import com.develop.mvp.pk.module.system.controller.admin.logger.vo.operatelog.OperateLogPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.logger.vo.operatelog.OperateLogRespVO;
import com.develop.mvp.pk.module.system.domain.logger.LoginLog;
import com.develop.mvp.pk.module.system.domain.logger.OperateLog;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Logger Convert 对象转换器。
 */
@Mapper
public interface LoggerConvert {

    LoggerConvert INSTANCE = Mappers.getMapper(LoggerConvert.class);

    /**
     * 将登录日志分页请求 VO 转换为应用查询对象。
     *
     * @param reqVO 登录日志分页请求 VO
     * @return 登录日志分页查询对象
     */
    LoginLogPageQuery convert(LoginLogPageReqVO reqVO);

    /**
     * 将操作日志分页请求 VO 转换为应用查询对象。
     *
     * @param reqVO 操作日志分页请求 VO
     * @return 操作日志分页查询对象
     */
    OperateLogPageQuery convert(OperateLogPageReqVO reqVO);

    /**
     * 将操作日志分页 API DTO 转换为应用查询对象。
     *
     * @param reqDTO 操作日志分页 API DTO
     * @return 操作日志分页查询对象
     */
    default OperateLogPageQuery convert(OperateLogPageReqDTO reqDTO) {
        OperateLogPageQuery query = BeanUtils.toBean(reqDTO, OperateLogPageQuery.class);
        query.setExactType(true);
        return query;
    }

    /**
     * 将登录日志领域对象转换为应用 DTO。
     *
     * @param log 登录日志领域对象
     * @return 登录日志应用 DTO
     */
    default LoginLogDTO convert(LoginLog log) {
        if (log == null) {
            return null;
        }
        LoginLogDTO dto = new LoginLogDTO();
        dto.setId(log.id());
        dto.setLogType(log.logType());
        dto.setTraceId(log.traceId());
        dto.setUserId(log.userId());
        dto.setUserType(log.userType());
        dto.setUsername(log.username());
        dto.setResult(log.result());
        dto.setUserIp(log.userIp());
        dto.setUserAgent(log.userAgent());
        dto.setCreateTime(log.createTime());
        return dto;
    }

    /**
     * 将操作日志领域对象转换为应用 DTO。
     *
     * @param log 操作日志领域对象
     * @return 操作日志应用 DTO
     */
    default OperateLogDTO convert(OperateLog log) {
        if (log == null) {
            return null;
        }
        OperateLogDTO dto = new OperateLogDTO();
        dto.setId(log.id());
        dto.setTraceId(log.traceId());
        dto.setUserId(log.userId());
        dto.setUserType(log.userType());
        dto.setType(log.type());
        dto.setSubType(log.subType());
        dto.setBizId(log.bizId());
        dto.setAction(log.action());
        dto.setExtra(log.extra());
        dto.setRequestMethod(log.requestMethod());
        dto.setRequestUrl(log.requestUrl());
        dto.setUserIp(log.userIp());
        dto.setUserAgent(log.userAgent());
        dto.setCreateTime(log.createTime());
        return dto;
    }

    /**
     * 将登录日志应用 DTO 转换为响应 VO。
     *
     * @param dto 登录日志应用 DTO
     * @return 登录日志响应 VO
     */
    LoginLogRespVO convert(LoginLogDTO dto);

    /**
     * 将操作日志应用 DTO 转换为响应 VO。
     *
     * @param dto 操作日志应用 DTO
     * @return 操作日志响应 VO
     */
    OperateLogRespVO convert(OperateLogDTO dto);

    /**
     * 将操作日志应用 DTO 转换为 API 响应 DTO。
     *
     * @param dto 操作日志应用 DTO
     * @return 操作日志 API 响应 DTO
     */
    OperateLogRespDTO convertToApi(OperateLogDTO dto);

    /**
     * 将登录日志应用 DTO 列表转换为响应 VO 列表。
     *
     * @param list 登录日志应用 DTO 列表
     * @return 登录日志响应 VO 列表
     */
    List<LoginLogRespVO> convertLoginLogRespList(List<LoginLogDTO> list);

    /**
     * 将操作日志应用 DTO 列表转换为响应 VO 列表。
     *
     * @param list 操作日志应用 DTO 列表
     * @return 操作日志响应 VO 列表
     */
    List<OperateLogRespVO> convertOperateLogRespList(List<OperateLogDTO> list);

    /**
     * 将操作日志应用 DTO 列表转换为 API 响应 DTO 列表。
     *
     * @param list 操作日志应用 DTO 列表
     * @return 操作日志 API 响应 DTO 列表
     */
    List<OperateLogRespDTO> convertOperateLogApiList(List<OperateLogDTO> list);

    /**
     * 将登录日志分页应用 DTO 转换为分页响应 VO。
     *
     * @param pageResult 登录日志分页应用 DTO
     * @return 登录日志分页响应 VO
     */
    default PageResult<LoginLogRespVO> convertLoginLogRespPage(PageResult<LoginLogDTO> pageResult) {
        return new PageResult<>(convertLoginLogRespList(pageResult.getList()), pageResult.getTotal());
    }

    /**
     * 将操作日志分页应用 DTO 转换为分页响应 VO。
     *
     * @param pageResult 操作日志分页应用 DTO
     * @return 操作日志分页响应 VO
     */
    default PageResult<OperateLogRespVO> convertOperateLogRespPage(PageResult<OperateLogDTO> pageResult) {
        return new PageResult<>(convertOperateLogRespList(pageResult.getList()), pageResult.getTotal());
    }

    /**
     * 将操作日志分页应用 DTO 转换为分页 API 响应 DTO。
     *
     * @param pageResult 操作日志分页应用 DTO
     * @return 操作日志分页 API 响应 DTO
     */
    default PageResult<OperateLogRespDTO> convertOperateLogApiPage(PageResult<OperateLogDTO> pageResult) {
        return new PageResult<>(convertOperateLogApiList(pageResult.getList()), pageResult.getTotal());
    }

}
