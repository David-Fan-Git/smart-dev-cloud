package com.develop.mvp.pk.module.system.controller.admin.sms;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.util.servlet.ServletUtils;
import com.develop.mvp.pk.framework.tenant.core.aop.TenantIgnore;
import com.develop.mvp.pk.module.system.application.sms.port.inbound.SmsUseCase;
import com.develop.mvp.pk.module.system.framework.sms.core.enums.SmsChannelEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

/**
 * Sms Callback Controller 控制器。
 */
@Tag(name = "管理后台 - 短信回调")
@RestController
@RequestMapping("/system/sms/callback")
public class SmsCallbackController {

    @Resource
    private SmsUseCase smsSendService;

    /**
     * 执行 receive Aliyun Sms Status 对应的业务操作。
     *
     * @param request request 参数
     * @return 处理结果
     */
    @PostMapping("/aliyun")
    @PermitAll
    @TenantIgnore
    @Operation(summary = "阿里云短信的回调", description = "参见 https://help.aliyun.com/document_detail/120998.html 文档")
    public CommonResult<Boolean> receiveAliyunSmsStatus(HttpServletRequest request) throws Throwable {
        String text = ServletUtils.getBody(request);
        smsSendService.receiveSmsStatus(SmsChannelEnum.ALIYUN.getCode(), text);
        return success(true);
    }

    /**
     * 执行 receive Tencent Sms Status 对应的业务操作。
     *
     * @param request request 参数
     * @return 处理结果
     */
    @PostMapping("/tencent")
    @PermitAll
    @TenantIgnore
    @Operation(summary = "腾讯云短信的回调", description = "参见 https://cloud.tencent.com/document/product/382/52077 文档")
    public CommonResult<Boolean> receiveTencentSmsStatus(HttpServletRequest request) throws Throwable {
        String text = ServletUtils.getBody(request);
        smsSendService.receiveSmsStatus(SmsChannelEnum.TENCENT.getCode(), text);
        return success(true);
    }


    /**
     * 执行 receive Huawei Sms Status 对应的业务操作。
     *
     * @param requestBody requestBody 参数
     * @return 处理结果
     */
    @PostMapping("/huawei")
    @PermitAll
    @TenantIgnore
    @Operation(summary = "华为云短信的回调", description = "参见 https://support.huaweicloud.com/api-msgsms/sms_05_0003.html 文档")
    public CommonResult<Boolean> receiveHuaweiSmsStatus(@RequestBody String requestBody) throws Throwable {
        smsSendService.receiveSmsStatus(SmsChannelEnum.HUAWEI.getCode(), requestBody);
        return success(true);
    }

    /**
     * 执行 receive Qiniu Sms Status 对应的业务操作。
     *
     * @param requestBody requestBody 参数
     * @return 处理结果
     */
    @PostMapping("/qiniu")
    @PermitAll
    @TenantIgnore
    @Operation(summary = "七牛云短信的回调", description = "参见 https://developer.qiniu.com/sms/5910/message-push 文档")
    public CommonResult<Boolean> receiveQiniuSmsStatus(@RequestBody String requestBody) throws Throwable {
        smsSendService.receiveSmsStatus(SmsChannelEnum.QINIU.getCode(), requestBody);
        return success(true);
    }

}