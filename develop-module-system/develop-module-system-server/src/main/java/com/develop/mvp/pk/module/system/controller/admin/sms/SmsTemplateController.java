package com.develop.mvp.pk.module.system.controller.admin.sms;

import com.develop.mvp.pk.framework.apilog.core.annotation.ApiAccessLog;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageParam;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.framework.excel.core.util.ExcelUtils;
import com.develop.mvp.pk.module.system.controller.admin.sms.vo.template.SmsTemplatePageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.sms.vo.template.SmsTemplateRespVO;
import com.develop.mvp.pk.module.system.controller.admin.sms.vo.template.SmsTemplateSaveReqVO;
import com.develop.mvp.pk.module.system.controller.admin.sms.vo.template.SmsTemplateSendReqVO;
import com.develop.mvp.pk.module.system.application.sms.port.inbound.SmsUseCase;
import com.develop.mvp.pk.module.system.dal.dataobject.sms.SmsTemplateDO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.develop.mvp.pk.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

/**
 * Sms Template Controller 控制器。
 */
@Tag(name = "管理后台 - 短信模板")
@RestController
@RequestMapping("/system/sms-template")
public class SmsTemplateController {

    @Resource
    private SmsUseCase smsTemplateService;
    @Resource
    private SmsUseCase smsSendService;

    /**
     * 创建 create Sms Template 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @return 处理结果
     */
    @PostMapping("/create")
    @Operation(summary = "创建短信模板")
    @PreAuthorize("@ss.hasPermission('system:sms-template:create')")
    public CommonResult<Long> createSmsTemplate(@Valid @RequestBody SmsTemplateSaveReqVO createReqVO) {
        return success(smsTemplateService.createSmsTemplate(createReqVO));
    }

    /**
     * 更新 update Sms Template 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     * @return 处理结果
     */
    @PutMapping("/update")
    @Operation(summary = "更新短信模板")
    @PreAuthorize("@ss.hasPermission('system:sms-template:update')")
    public CommonResult<Boolean> updateSmsTemplate(@Valid @RequestBody SmsTemplateSaveReqVO updateReqVO) {
        smsTemplateService.updateSmsTemplate(updateReqVO);
        return success(true);
    }

    /**
     * 删除 delete Sms Template 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除短信模板")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('system:sms-template:delete')")
    public CommonResult<Boolean> deleteSmsTemplate(@RequestParam("id") Long id) {
        smsTemplateService.deleteSmsTemplate(id);
        return success(true);
    }

    /**
     * 删除 delete Sms Template List 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @Operation(summary = "批量删除短信模板")
    @PreAuthorize("@ss.hasPermission('system:sms-template:delete')")
    public CommonResult<Boolean> deleteSmsTemplateList(@RequestParam("ids") List<Long> ids) {
        smsTemplateService.deleteSmsTemplateList(ids);
        return success(true);
    }

    /**
     * 查询 get Sms Template 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @GetMapping("/get")
    @Operation(summary = "获得短信模板")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:sms-template:query')")
    public CommonResult<SmsTemplateRespVO> getSmsTemplate(@RequestParam("id") Long id) {
        SmsTemplateDO template = smsTemplateService.getSmsTemplate(id);
        return success(BeanUtils.toBean(template, SmsTemplateRespVO.class));
    }

    /**
     * 查询 get Sms Template Page 对应的数据。
     *
     * @param pageVO pageVO 参数
     * @return 处理结果
     */
    @GetMapping("/page")
    @Operation(summary = "获得短信模板分页")
    @PreAuthorize("@ss.hasPermission('system:sms-template:query')")
    public CommonResult<PageResult<SmsTemplateRespVO>> getSmsTemplatePage(@Valid SmsTemplatePageReqVO pageVO) {
        PageResult<SmsTemplateDO> pageResult = smsTemplateService.getSmsTemplatePage(pageVO);
        return success(BeanUtils.toBean(pageResult, SmsTemplateRespVO.class));
    }

    /**
     * 执行 export Sms Template Excel 对应的业务操作。
     *
     * @param exportReqVO exportReqVO 参数
     * @param response response 参数
     */
    @GetMapping("/export-excel")
    @Operation(summary = "导出短信模板 Excel")
    @PreAuthorize("@ss.hasPermission('system:sms-template:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSmsTemplateExcel(@Valid SmsTemplatePageReqVO exportReqVO,
                                       HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<SmsTemplateDO> list = smsTemplateService.getSmsTemplatePage(exportReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "短信模板.xls", "数据", SmsTemplateRespVO.class,
                BeanUtils.toBean(list, SmsTemplateRespVO.class));
    }

    /**
     * 发送 send Sms 对应的消息。
     *
     * @param sendReqVO sendReqVO 参数
     * @return 处理结果
     */
    @PostMapping("/send-sms")
    @Operation(summary = "发送短信")
    @PreAuthorize("@ss.hasPermission('system:sms-template:send-sms')")
    public CommonResult<Long> sendSms(@Valid @RequestBody SmsTemplateSendReqVO sendReqVO) {
        return success(smsSendService.sendSingleSmsToAdmin(sendReqVO.getMobile(), null,
                sendReqVO.getTemplateCode(), sendReqVO.getTemplateParams()));
    }

}
