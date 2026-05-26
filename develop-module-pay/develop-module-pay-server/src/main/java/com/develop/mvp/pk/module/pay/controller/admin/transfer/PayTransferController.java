package com.develop.mvp.pk.module.pay.controller.admin.transfer;

import com.develop.mvp.pk.framework.apilog.core.annotation.ApiAccessLog;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.framework.excel.core.util.ExcelUtils;
import com.develop.mvp.pk.module.pay.application.app.PayAppApplicationService;
import com.develop.mvp.pk.module.pay.application.transfer.PayTransferApplicationService;
import com.develop.mvp.pk.module.pay.controller.admin.transfer.vo.PayTransferPageReqVO;
import com.develop.mvp.pk.module.pay.controller.admin.transfer.vo.PayTransferRespVO;
import com.develop.mvp.pk.module.pay.domain.app.PayApp;
import com.develop.mvp.pk.module.pay.domain.transfer.PayTransfer;
import io.swagger.v3.oas.annotations.Operation;
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
import java.util.Map;

import static com.develop.mvp.pk.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;
import static com.develop.mvp.pk.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - 转账单")
@RestController
@RequestMapping("/pay/transfer")
@Validated
public class PayTransferController {

    @Resource
    private PayTransferApplicationService transferApplicationService;
    @Resource
    private PayAppApplicationService appApplicationService;

    @GetMapping("/get")
    @Operation(summary = "获得转账订单")
    @PreAuthorize("@ss.hasPermission('pay:transfer:query')")
    public CommonResult<PayTransferRespVO> getTransfer(@RequestParam("id") Long id) {
        PayTransfer transfer = transferApplicationService.get(id);
        if (transfer == null) {
            return success(new PayTransferRespVO());
        }

        // 拼接数据
        PayApp app = appApplicationService.get(transfer.getAppId());
        return success(BeanUtils.toBean(transfer, PayTransferRespVO.class, transferVO -> {
            if (app != null) {
                transferVO.setAppName(app.getName());
            }
        }));
    }

    @GetMapping("/page")
    @Operation(summary = "获得转账订单分页")
    @PreAuthorize("@ss.hasPermission('pay:transfer:query')")
    public CommonResult<PageResult<PayTransferRespVO>> getTransferPage(@Valid PayTransferPageReqVO pageVO) {
        PageResult<PayTransfer> pageResult = transferApplicationService.getPage(
                pageVO.getNo(), pageVO.getAppId(), pageVO.getChannelCode(),
                pageVO.getMerchantTransferId(), pageVO.getStatus(),
                pageVO.getPageNo(), pageVO.getPageSize());

        // 拼接数据
        Map<Long, PayApp> apps = appApplicationService.getList(
                convertList(pageResult.getList(), PayTransfer::getAppId)).stream()
                .collect(java.util.stream.Collectors.toMap(PayApp::getId, a -> a));
        return success(BeanUtils.toBean(pageResult, PayTransferRespVO.class, transferVO -> {
            if (apps.containsKey(transferVO.getAppId())) {
                transferVO.setAppName(apps.get(transferVO.getAppId()).getName());
            }
        }));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出转账订单 Excel")
    @PreAuthorize("@ss.hasPermission('pay:transfer:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportTransfer(PayTransferPageReqVO pageReqVO,
                               HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PAGE_SIZE_NONE);
        PageResult<PayTransferRespVO> pageResult = getTransferPage(pageReqVO).getData();

        // 导出 Excel
        ExcelUtils.write(response, "转账订单.xls", "数据", PayTransferRespVO.class, pageResult.getList());
    }

}
