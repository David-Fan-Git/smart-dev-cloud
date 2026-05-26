package com.develop.mvp.pk.module.system.api.logger;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.biz.system.logger.dto.OperateLogCreateReqDTO;
import com.develop.mvp.pk.module.system.api.logger.dto.OperateLogPageReqDTO;
import com.develop.mvp.pk.module.system.api.logger.dto.OperateLogRespDTO;
import com.develop.mvp.pk.module.system.application.logger.dto.OperateLogDTO;
import com.develop.mvp.pk.module.system.application.logger.port.inbound.LoggerUseCase;
import com.develop.mvp.pk.module.system.convert.logger.LoggerConvert;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

/**
 * Operate Log Api Impl 模块 API 实现。
 */
@RestController // 提供 RESTful API 接口，给 Feign 调用
@Validated
@Primary // 由于 OperateLogCommonApi 的存在，必须声明为 @Primary Bean
public class OperateLogApiImpl implements OperateLogApi {

    @Resource
    private LoggerUseCase loggerUseCase;

    /**
     * 创建 create Operate Log 对应的数据。
     *
     * @param createReqDTO createReqDTO 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<Boolean> createOperateLog(OperateLogCreateReqDTO createReqDTO) {
        loggerUseCase.createOperateLog(createReqDTO);
        return success(true);
    }

    /**
     * 查询 get Operate Log Page 对应的数据。
     *
     * @param pageReqDTO pageReqDTO 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<PageResult<OperateLogRespDTO>> getOperateLogPage(OperateLogPageReqDTO pageReqDTO) {
        PageResult<OperateLogDTO> operateLogPage = loggerUseCase.getOperateLogPage(LoggerConvert.INSTANCE.convert(pageReqDTO));
        return success(LoggerConvert.INSTANCE.convertOperateLogApiPage(operateLogPage));
    }

}
