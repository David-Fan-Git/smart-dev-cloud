package com.develop.mvp.pk.module.system.api.sms;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.module.system.api.sms.dto.send.SmsSendSingleToUserReqDTO;
import com.develop.mvp.pk.module.system.application.sms.port.inbound.SmsUseCase;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

/**
 * Sms Send Api Impl 模块 API 实现。
 */
@RestController // 提供 RESTful API 接口，给 Feign 调用
@Validated
public class SmsSendApiImpl implements SmsSendApi {

    @Resource
    private SmsUseCase smsSendService;

    /**
     * 发送 send Single Sms To Admin 对应的消息。
     *
     * @param reqDTO reqDTO 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<Long> sendSingleSmsToAdmin(SmsSendSingleToUserReqDTO reqDTO) {
        return success(smsSendService.sendSingleSmsToAdmin(reqDTO.getMobile(), reqDTO.getUserId(),
                reqDTO.getTemplateCode(), reqDTO.getTemplateParams()));
    }

    /**
     * 发送 send Single Sms To Member 对应的消息。
     *
     * @param reqDTO reqDTO 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<Long> sendSingleSmsToMember(SmsSendSingleToUserReqDTO reqDTO) {
        return success(smsSendService.sendSingleSmsToMember(reqDTO.getMobile(), reqDTO.getUserId(),
                reqDTO.getTemplateCode(), reqDTO.getTemplateParams()));
    }

}
