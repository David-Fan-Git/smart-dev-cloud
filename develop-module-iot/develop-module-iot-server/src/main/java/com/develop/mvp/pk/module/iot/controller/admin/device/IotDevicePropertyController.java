package com.develop.mvp.pk.module.iot.controller.admin.device;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.module.iot.application.property.port.inbound.IotDevicePropertyUseCase;
import com.develop.mvp.pk.module.iot.application.property.query.IotDeviceLatestPropertyQuery;
import com.develop.mvp.pk.module.iot.application.property.result.IotDeviceLatestPropertyResult;
import com.develop.mvp.pk.module.iot.controller.admin.device.vo.property.IotDevicePropertyDetailRespVO;
import com.develop.mvp.pk.module.iot.controller.admin.device.vo.property.IotDevicePropertyHistoryListReqVO;
import com.develop.mvp.pk.module.iot.controller.admin.device.vo.property.IotDevicePropertyRespVO;
import com.develop.mvp.pk.module.iot.dal.dataobject.device.IotDeviceDO;
import com.develop.mvp.pk.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import com.develop.mvp.pk.module.iot.dal.dataobject.thingmodel.model.ThingModelProperty;
import com.develop.mvp.pk.module.iot.enums.thingmodel.IotThingModelTypeEnum;
import com.develop.mvp.pk.module.iot.service.device.IotDeviceService;
import com.develop.mvp.pk.module.iot.service.device.property.IotDevicePropertyService;
import com.develop.mvp.pk.module.iot.service.thingmodel.IotThingModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - IoT 设备属性")
@RestController
@RequestMapping("/iot/device/property")
@Validated
public class IotDevicePropertyController {

    @Resource
    private IotDevicePropertyUseCase devicePropertyUseCase;
    @Resource
    private IotDevicePropertyService devicePropertyService;
    @Resource
    private IotThingModelService thingModelService;
    @Resource
    private IotDeviceService deviceService;

    @GetMapping("/get-latest")
    @Operation(summary = "获取设备属性最新属性")
    @Parameter(name = "deviceId", description = "设备编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device:property-query')")
    public CommonResult<List<IotDevicePropertyDetailRespVO>> getLatestDeviceProperties(
            @RequestParam("deviceId") Long deviceId) {
        // 1.1 获取设备信息
        IotDeviceDO device = deviceService.getDevice(deviceId);
        Assert.notNull(device, "设备不存在");
        List<IotDeviceLatestPropertyResult> properties = devicePropertyUseCase.getLatestProperties(
                new IotDeviceLatestPropertyQuery(deviceId));
        List<IotThingModelDO> thingModels = thingModelService.getThingModelListByProductIdAndType(
                device.getProductId(), IotThingModelTypeEnum.PROPERTY.getType());

        return success(convertList(thingModels, thingModel -> {
            ThingModelProperty thingModelProperty = thingModel.getProperty();
            Assert.notNull(thingModelProperty, "属性不能为空");
            IotDevicePropertyDetailRespVO result = new IotDevicePropertyDetailRespVO()
                    .setName(thingModel.getName()).setDataType(thingModelProperty.getDataType())
                    .setDataSpecs(thingModelProperty.getDataSpecs())
                    .setDataSpecsList(thingModelProperty.getDataSpecsList());
            result.setIdentifier(thingModel.getIdentifier());
            properties.stream().filter(property -> thingModel.getIdentifier().equals(property.identifier()))
                    .findFirst().ifPresent(property -> result.setValue(property.value())
                            .setUpdateTime(LocalDateTimeUtil.toEpochMilli(property.updateTime())));
            return result;
        }));
    }

    @GetMapping("/history-list")
    @Operation(summary = "获取设备属性历史数据列表")
    @PreAuthorize("@ss.hasPermission('iot:device:property-query')")
    public CommonResult<List<IotDevicePropertyRespVO>> getHistoryDevicePropertyList(
            @Valid IotDevicePropertyHistoryListReqVO listReqVO) {
        return success(devicePropertyService.getHistoryDevicePropertyList(listReqVO));
    }

}