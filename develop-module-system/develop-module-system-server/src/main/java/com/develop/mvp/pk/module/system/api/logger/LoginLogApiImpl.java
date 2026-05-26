package com.develop.mvp.pk.module.system.api.logger;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.module.system.api.logger.dto.LoginLogCreateReqDTO;
import com.develop.mvp.pk.module.system.application.logger.port.inbound.LoggerUseCase;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

/**
 * Login Log Api Impl 模块 API 实现。
 */
@RestController // 提供 RESTful API 接口，给 Feign 调用
@Validated
public class LoginLogApiImpl implements LoginLogApi {

    @Resource
    private LoggerUseCase loggerUseCase;

    /**
     * 创建 create Login Log 对应的数据。
     *
     * @param reqDTO reqDTO 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<Boolean> createLoginLog(LoginLogCreateReqDTO reqDTO) {
        loggerUseCase.createLoginLog(reqDTO);
        return success(true);
    }

}
