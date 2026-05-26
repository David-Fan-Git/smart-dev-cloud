package com.develop.mvp.pk.module.pay.controller.admin.app;

import cn.hutool.core.collection.CollUtil;
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.pay.application.app.PayAppApplicationService;
import com.develop.mvp.pk.module.pay.controller.admin.app.vo.*;
import com.develop.mvp.pk.module.pay.application.channel.PayChannelApplicationService;
import com.develop.mvp.pk.module.pay.domain.app.PayApp;
import com.develop.mvp.pk.module.pay.domain.channel.PayChannel;
import com.develop.mvp.pk.framework.common.util.collection.CollectionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertList;

@Slf4j
@Tag(name = "管理后台 - 支付应用信息")
@RestController
@RequestMapping("/pay/app")
@Validated
public class PayAppController {

    @Resource
    private PayAppApplicationService appApplicationService;
    @Resource
    private PayChannelApplicationService channelApplicationService;

    @PostMapping("/create")
    @Operation(summary = "创建支付应用信息")
    @PreAuthorize("@ss.hasPermission('pay:app:create')")
    public CommonResult<Long> createApp(@Valid @RequestBody PayAppCreateReqVO createReqVO) {
        return success(appApplicationService.create(createReqVO.getName(), createReqVO.getAppKey(), createReqVO.getStatus(),
                createReqVO.getRemark(), createReqVO.getOrderNotifyUrl(), createReqVO.getRefundNotifyUrl(),
                createReqVO.getTransferNotifyUrl()).id());
    }

    @PutMapping("/update")
    @Operation(summary = "更新支付应用信息")
    @PreAuthorize("@ss.hasPermission('pay:app:update')")
    public CommonResult<Boolean> updateApp(@Valid @RequestBody PayAppUpdateReqVO updateReqVO) {
        appApplicationService.update(updateReqVO.getId(), updateReqVO.getName(), updateReqVO.getAppKey(),
                updateReqVO.getStatus(), updateReqVO.getRemark(), updateReqVO.getOrderNotifyUrl(),
                updateReqVO.getRefundNotifyUrl(), updateReqVO.getTransferNotifyUrl());
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新支付应用状态")
    @PreAuthorize("@ss.hasPermission('pay:app:update')")
    public CommonResult<Boolean> updateAppStatus(@Valid @RequestBody PayAppUpdateStatusReqVO updateReqVO) {
        appApplicationService.updateStatus(updateReqVO.getId(), updateReqVO.getStatus());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除支付应用信息")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('pay:app:delete')")
    public CommonResult<Boolean> deleteApp(@RequestParam("id") Long id) {
        appApplicationService.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得支付应用信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pay:app:query')")
    public CommonResult<PayAppRespVO> getApp(@RequestParam("id") Long id) {
        PayApp app = appApplicationService.get(id);
        return success(BeanUtils.toBean(app, PayAppRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得支付应用信息分页")
    @PreAuthorize("@ss.hasPermission('pay:app:query')")
    public CommonResult<PageResult<PayAppPageItemRespVO>> getAppPage(@Valid PayAppPageReqVO pageVO) {
        // 得到应用分页列表
        PageResult<PayApp> pageResult = appApplicationService.getPage(
                pageVO.getName(), pageVO.getAppKey(), pageVO.getStatus(),
                pageVO.getPageNo(), pageVO.getPageSize());
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty());
        }

        // 得到所有的应用编号，查出所有的渠道，并移除未启用的渠道
        List<PayChannel> channels = channelApplicationService.getListByAppIds(
                convertList(pageResult.getList(), PayApp::getId));
        channels.removeIf(channel -> !CommonStatusEnum.ENABLE.getStatus().equals(channel.status()));

        // 拼接后返回
        PageResult<PayAppPageItemRespVO> voResult = BeanUtils.toBean(pageResult, PayAppPageItemRespVO.class);
        Map<Long, Set<String>> appIdChannelMap = CollectionUtils.convertMultiMap2(channels, PayChannel::getAppId, PayChannel::getCode);
        voResult.getList().forEach(app -> app.setChannelCodes(appIdChannelMap.get(app.getId())));
        return success(voResult);
    }

    @GetMapping("/list")
    @Operation(summary = "获得应用列表")
    @PreAuthorize("@ss.hasPermission('pay:merchant:query')")
    public CommonResult<List<PayAppRespVO>> getAppList() {
        List<PayApp> appList = appApplicationService.getList();
        return success(BeanUtils.toBean(appList, PayAppRespVO.class));
    }

}
