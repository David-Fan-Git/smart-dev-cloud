package com.develop.mvp.pk.module.pay.controller.admin.refund;

import cn.hutool.core.collection.CollectionUtil;
import com.develop.mvp.pk.framework.apilog.core.annotation.ApiAccessLog;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.framework.excel.core.util.ExcelUtils;
import com.develop.mvp.pk.module.pay.application.app.PayAppApplicationService;
import com.develop.mvp.pk.module.pay.application.refund.PayRefundApplicationService;
import com.develop.mvp.pk.module.pay.controller.admin.refund.vo.*;
import com.develop.mvp.pk.module.pay.convert.refund.PayRefundConvert;
import com.develop.mvp.pk.module.pay.dal.dataobject.refund.PayRefundDO;
import com.develop.mvp.pk.module.pay.domain.app.PayApp;
import com.develop.mvp.pk.module.pay.domain.refund.PayRefund;
import com.develop.mvp.pk.module.pay.service.refund.PayRefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.develop.mvp.pk.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - 退款订单")
@RestController
@RequestMapping("/pay/refund")
@Validated
public class PayRefundController {

    @Resource
    private PayRefundApplicationService refundApplicationService;
    @Resource
    private PayAppApplicationService appApplicationService;
    @Resource
    private PayRefundService refundService;

    @GetMapping("/get")
    @Operation(summary = "获得退款订单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pay:refund:query')")
    public CommonResult<PayRefundDetailsRespVO> getRefund(@RequestParam("id") Long id) {
        PayRefund refund = refundApplicationService.get(id);
        if (refund == null) {
            return success(new PayRefundDetailsRespVO());
        }

        // 拼接数据
        PayApp app = appApplicationService.get(refund.getAppId());
        PayRefundDetailsRespVO resp = BeanUtils.toBean(refund, PayRefundDetailsRespVO.class);
        if (app != null) {
            resp.setAppName(app.getName());
        }
        return success(resp);
    }

    @GetMapping("/page")
    @Operation(summary = "获得退款订单分页")
    @PreAuthorize("@ss.hasPermission('pay:refund:query')")
    public CommonResult<PageResult<PayRefundPageItemRespVO>> getRefundPage(@Valid PayRefundPageReqVO pageVO) {
        PageResult<PayRefund> pageResult = refundApplicationService.getPage(
                pageVO.getAppId(), pageVO.getChannelCode(), pageVO.getMerchantOrderId(),
                pageVO.getMerchantRefundId(), pageVO.getStatus(),
                pageVO.getPageNo(), pageVO.getPageSize());
        if (CollectionUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }

        // 处理应用ID数据
        Map<Long, PayApp> appMap = appApplicationService.getList(
                convertList(pageResult.getList(), PayRefund::getAppId)).stream()
                .collect(java.util.stream.Collectors.toMap(PayApp::getId, a -> a));
        return success(BeanUtils.toBean(pageResult, PayRefundPageItemRespVO.class, item -> {
            if (appMap.containsKey(item.getAppId())) {
                item.setAppName(appMap.get(item.getAppId()).getName());
            }
        }));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出退款订单 Excel")
    @PreAuthorize("@ss.hasPermission('pay:refund:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportRefundExcel(@Valid PayRefundExportReqVO exportReqVO,
                                  HttpServletResponse response) throws IOException {
        List<PayRefundDO> list = refundService.getRefundList(exportReqVO);

        // 拼接返回
        Map<Long, String> appNameMap = appApplicationService.getList(convertList(list, PayRefundDO::getAppId)).stream()
                .collect(java.util.stream.Collectors.toMap(PayApp::getId, PayApp::getName));
        List<PayRefundExcelVO> excelList = PayRefundConvert.INSTANCE.convertList(list, appNameMap);
        // 导出 Excel
        ExcelUtils.write(response, "退款订单.xls", "数据", PayRefundExcelVO.class, excelList);
    }

}
