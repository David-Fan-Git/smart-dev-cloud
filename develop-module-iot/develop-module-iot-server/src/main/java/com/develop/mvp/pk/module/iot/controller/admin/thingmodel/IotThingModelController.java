package com.develop.mvp.pk.module.iot.controller.admin.thingmodel;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.iot.application.thingmodel.command.CreateIotThingModelCommand;
import com.develop.mvp.pk.module.iot.application.thingmodel.command.UpdateIotThingModelCommand;
import com.develop.mvp.pk.module.iot.application.thingmodel.port.inbound.IotThingModelUseCase;
import com.develop.mvp.pk.module.iot.application.thingmodel.query.IotThingModelListQuery;
import com.develop.mvp.pk.module.iot.application.thingmodel.query.IotThingModelPageQuery;
import com.develop.mvp.pk.module.iot.application.thingmodel.result.IotThingModelResult;
import com.develop.mvp.pk.module.iot.application.thingmodel.result.IotThingModelTslResult;
import com.develop.mvp.pk.module.iot.controller.admin.thingmodel.vo.*;
import com.develop.mvp.pk.module.iot.dal.dataobject.thingmodel.model.ThingModelEvent;
import com.develop.mvp.pk.module.iot.dal.dataobject.thingmodel.model.ThingModelProperty;
import com.develop.mvp.pk.module.iot.dal.dataobject.thingmodel.model.ThingModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertList;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.filterList;

@Tag(name = "管理后台 - IoT 产品物模型")
@RestController
@RequestMapping("/iot/thing-model")
@Validated
public class IotThingModelController {

    @Resource
    private IotThingModelUseCase thingModelUseCase;

    @PostMapping("/create")
    @Operation(summary = "创建产品物模型")
    @PreAuthorize("@ss.hasPermission('iot:thing-model:create')")
    public CommonResult<Long> createThingModel(@Valid @RequestBody IotThingModelSaveReqVO createReqVO) {
        return success(thingModelUseCase.createThingModel(new CreateIotThingModelCommand(createReqVO.getProductId(),
                createReqVO.getProductKey(), createReqVO.getIdentifier(), createReqVO.getName(),
                createReqVO.getDescription(), createReqVO.getType(), createReqVO.getProperty(), createReqVO.getEvent(),
                createReqVO.getService())));
    }

    @PutMapping("/update")
    @Operation(summary = "更新产品物模型")
    @PreAuthorize("@ss.hasPermission('iot:thing-model:update')")
    public CommonResult<Boolean> updateThingModel(@Valid @RequestBody IotThingModelSaveReqVO updateReqVO) {
        thingModelUseCase.updateThingModel(new UpdateIotThingModelCommand(updateReqVO.getId(), updateReqVO.getProductId(),
                updateReqVO.getProductKey(), updateReqVO.getIdentifier(), updateReqVO.getName(),
                updateReqVO.getDescription(), updateReqVO.getType(), updateReqVO.getProperty(), updateReqVO.getEvent(),
                updateReqVO.getService()));
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除产品物模型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:thing-model:delete')")
    public CommonResult<Boolean> deleteThingModel(@RequestParam("id") Long id) {
        thingModelUseCase.deleteThingModel(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得产品物模型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:thing-model:query')")
    public CommonResult<IotThingModelRespVO> getThingModel(@RequestParam("id") Long id) {
        return success(toRespVO(thingModelUseCase.getThingModel(id)));
    }

    @GetMapping("/get-tsl")
    @Operation(summary = "获得产品物模型 TSL")
    @Parameter(name = "productId", description = "产品 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:thing-model:query')")
    public CommonResult<IotThingModelTSLRespVO> getThingModelTsl(@RequestParam("productId") Long productId) {
        IotThingModelTslResult tsl = thingModelUseCase.getTsl(productId);
        if (tsl == null) {
            return success(null);
        }
        return success(new IotThingModelTSLRespVO().setProductId(tsl.productId()).setProductKey(tsl.productKey())
                .setProperties(convertList(tsl.properties(), ThingModelProperty.class::cast))
                .setEvents(convertList(tsl.events(), ThingModelEvent.class::cast))
                .setServices(convertList(tsl.services(), ThingModelService.class::cast)));
    }

    @GetMapping("/list")
    @Operation(summary = "获得产品物模型列表")
    @PreAuthorize("@ss.hasPermission('iot:thing-model:query')")
    public CommonResult<List<IotThingModelRespVO>> getThingModelListByProductId(@Valid IotThingModelListReqVO reqVO) {
        List<IotThingModelResult> list = thingModelUseCase.getThingModelList(new IotThingModelListQuery(
                reqVO.getProductId(), reqVO.getIdentifier(), reqVO.getName(), reqVO.getType()));
        return success(convertList(list, this::toRespVO));
    }

    @GetMapping("/page")
    @Operation(summary = "获得产品物模型分页")
    @PreAuthorize("@ss.hasPermission('iot:thing-model:query')")
    public CommonResult<PageResult<IotThingModelRespVO>> getThingModelPage(@Valid IotThingModelPageReqVO pageReqVO) {
        PageResult<IotThingModelResult> pageResult = thingModelUseCase.getThingModelPage(new IotThingModelPageQuery(
                pageReqVO.getProductId(), pageReqVO.getIdentifier(), pageReqVO.getName(), pageReqVO.getType(),
                pageReqVO.getPageNo(), pageReqVO.getPageSize()));
        return success(new PageResult<>(convertList(pageResult.getList(), this::toRespVO), pageResult.getTotal()));
    }

    private IotThingModelRespVO toRespVO(IotThingModelResult thingModel) {
        if (thingModel == null) {
            return null;
        }
        return new IotThingModelRespVO().setId(thingModel.id()).setProductId(thingModel.productId())
                .setProductKey(thingModel.productKey()).setIdentifier(thingModel.identifier())
                .setName(thingModel.name()).setDescription(thingModel.description()).setType(thingModel.type())
                .setProperty((ThingModelProperty) thingModel.property()).setEvent((ThingModelEvent) thingModel.event())
                .setService((ThingModelService) thingModel.service());
    }

}
