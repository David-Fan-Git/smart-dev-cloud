package com.develop.mvp.pk.module.pay.controller.admin.order;

import cn.hutool.core.collection.CollectionUtil;
import com.develop.mvp.pk.framework.apilog.core.annotation.ApiAccessLog;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.framework.excel.core.util.ExcelUtils;
import com.develop.mvp.pk.module.pay.application.app.PayAppApplicationService;
import com.develop.mvp.pk.module.pay.application.order.PayOrderApplicationService;
import com.develop.mvp.pk.module.pay.application.wallet.PayWalletApplicationService;
import com.develop.mvp.pk.module.pay.controller.admin.order.vo.*;
import com.develop.mvp.pk.module.pay.convert.order.PayOrderConvert;
import com.develop.mvp.pk.module.pay.dal.dataobject.order.PayOrderDO;
import com.develop.mvp.pk.module.pay.dal.dataobject.order.PayOrderExtensionDO;
import com.develop.mvp.pk.module.pay.domain.app.PayApp;
import com.develop.mvp.pk.module.pay.domain.order.PayOrder;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWallet;
import com.develop.mvp.pk.module.pay.enums.PayChannelEnum;
import com.develop.mvp.pk.module.pay.enums.order.PayOrderStatusEnum;
import com.develop.mvp.pk.module.pay.framework.pay.core.client.impl.wallet.WalletPayClient;
import com.develop.mvp.pk.module.pay.service.order.PayOrderService;
import com.google.common.collect.Maps;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.develop.mvp.pk.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertList;
import static com.develop.mvp.pk.framework.common.util.servlet.ServletUtils.getClientIP;
import static com.develop.mvp.pk.framework.web.core.util.WebFrameworkUtils.getLoginUserId;
import static com.develop.mvp.pk.framework.web.core.util.WebFrameworkUtils.getLoginUserType;

@Tag(name = "管理后台 - 支付订单")
@RestController
@RequestMapping("/pay/order")
@Validated
public class PayOrderController {

    @Resource
    private PayOrderApplicationService orderApplicationService;
    @Resource
    private PayOrderService orderService;
    @Resource
    private PayAppApplicationService appApplicationService;
    @Resource
    private PayWalletApplicationService walletApplicationService;

    @GetMapping("/get")
    @Operation(summary = "获得支付订单")
    @Parameters({
            @Parameter(name = "id", description = "编号", required = true, example = "1024"),
            @Parameter(name = "sync", description = "是否同步", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('pay:order:query')")
    public CommonResult<PayOrderRespVO> getOrder(@RequestParam("id") Long id,
                                                 @RequestParam(value = "sync", required = false) Boolean sync) {
        PayOrder order = orderApplicationService.get(id);
        if (Boolean.TRUE.equals(sync) && order != null && PayOrderStatusEnum.isWaiting(order.getStatus())) {
            orderService.syncOrderQuietly(order.getId());
            order = orderApplicationService.get(id);
        }
        return success(BeanUtils.toBean(order, PayOrderRespVO.class));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得支付订单详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pay:order:query')")
    public CommonResult<PayOrderDetailsRespVO> getOrderDetail(@RequestParam("id") Long id) {
        PayOrder order = orderApplicationService.get(id);
        if (order == null) {
            return success(null);
        }

        PayApp app = appApplicationService.get(order.getAppId());
        PayOrderExtensionDO orderExtension = orderService.getOrderExtension(order.getExtensionId());
        PayOrderDetailsRespVO respVO = BeanUtils.toBean(order, PayOrderDetailsRespVO.class);
        if (orderExtension != null) {
            respVO.setExtension(PayOrderConvert.INSTANCE.convert(orderExtension));
        }
        if (app != null) {
            respVO.setAppName(app.getName());
        }
        return success(respVO);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交支付订单")
    public CommonResult<PayOrderSubmitRespVO> submitPayOrder(@RequestBody PayOrderSubmitReqVO reqVO) {
        if (Objects.equals(reqVO.getChannelCode(), PayChannelEnum.WALLET.getCode())) {
            if (reqVO.getChannelExtras() == null) {
                reqVO.setChannelExtras(Maps.newHashMapWithExpectedSize(1));
            }
            PayWallet wallet = walletApplicationService.getOrCreate(getLoginUserId(), getLoginUserType());
            reqVO.getChannelExtras().put(WalletPayClient.WALLET_ID_KEY, String.valueOf(wallet.getId()));
        }

        PayOrderSubmitRespVO respVO = orderService.submitOrder(reqVO, getClientIP());
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得支付订单分页")
    @PreAuthorize("@ss.hasPermission('pay:order:query')")
    public CommonResult<PageResult<PayOrderPageItemRespVO>> getOrderPage(@Valid PayOrderPageReqVO pageVO) {
        PageResult<PayOrderDO> pageResult = orderService.getOrderPage(pageVO);
        if (CollectionUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }

        return success(PayOrderConvert.INSTANCE.convertPage(pageResult,
                getAppNameMap(convertList(pageResult.getList(), PayOrderDO::getAppId))));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出支付订单 Excel")
    @PreAuthorize("@ss.hasPermission('pay:order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOrderExcel(@Valid PayOrderExportReqVO exportReqVO,
            HttpServletResponse response) throws IOException {
        List<PayOrderDO> list = orderService.getOrderList(exportReqVO);
        if (CollectionUtil.isEmpty(list)) {
            ExcelUtils.write(response, "支付订单.xls", "数据",
                    PayOrderExcelVO.class, new ArrayList<>());
            return;
        }

        List<PayOrderExcelVO> excelList = PayOrderConvert.INSTANCE.convertList(list,
                getAppNameMap(convertList(list, PayOrderDO::getAppId)));
        ExcelUtils.write(response, "支付订单.xls", "数据", PayOrderExcelVO.class, excelList);
    }

    private java.util.Map<Long, String> getAppNameMap(List<Long> appIds) {
        return appApplicationService.getList(appIds).stream()
                .collect(java.util.stream.Collectors.toMap(PayApp::getId, PayApp::getName));
    }

}
