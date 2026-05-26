package com.develop.mvp.pk.module.mes.controller.admin.pro.card;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.develop.mvp.pk.framework.apilog.core.annotation.ApiAccessLog;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageParam;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.collection.MapUtils;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.framework.excel.core.util.ExcelUtils;
import com.develop.mvp.pk.module.mes.controller.admin.pro.card.vo.MesProCardPageReqVO;
import com.develop.mvp.pk.module.mes.controller.admin.pro.card.vo.MesProCardRespVO;
import com.develop.mvp.pk.module.mes.controller.admin.pro.card.vo.MesProCardSaveReqVO;
import com.develop.mvp.pk.module.mes.dal.dataobject.md.item.MesMdItemDO;
import com.develop.mvp.pk.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import com.develop.mvp.pk.module.mes.dal.dataobject.pro.card.MesProCardDO;
import com.develop.mvp.pk.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import com.develop.mvp.pk.module.mes.service.md.item.MesMdItemService;
import com.develop.mvp.pk.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;
import com.develop.mvp.pk.module.mes.service.pro.card.MesProCardService;
import com.develop.mvp.pk.module.mes.service.pro.workorder.MesProWorkOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.develop.mvp.pk.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - MES 生产流转卡")
@RestController
@RequestMapping("/mes/pro/card")
@Validated
public class MesProCardController {

    @Resource
    private MesProCardService cardService;
    @Resource
    private MesProWorkOrderService workOrderService;
    @Resource
    private MesMdItemService itemService;
    @Resource
    private MesMdUnitMeasureService unitMeasureService;

    @PostMapping("/create")
    @Operation(summary = "创建生产流转卡")
    @PreAuthorize("@ss.hasPermission('mes:pro-card:create')")
    public CommonResult<Long> createCard(@Valid @RequestBody MesProCardSaveReqVO createReqVO) {
        return success(cardService.createCard(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新生产流转卡")
    @PreAuthorize("@ss.hasPermission('mes:pro-card:update')")
    public CommonResult<Boolean> updateCard(@Valid @RequestBody MesProCardSaveReqVO updateReqVO) {
        cardService.updateCard(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除生产流转卡")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:pro-card:delete')")
    public CommonResult<Boolean> deleteCard(@RequestParam("id") Long id) {
        cardService.deleteCard(id);
        return success(true);
    }

    @PutMapping("/submit")
    @Operation(summary = "提交生产流转卡")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:pro-card:update')")
    public CommonResult<Boolean> submitCard(@RequestParam("id") Long id) {
        cardService.submitCard(id);
        return success(true);
    }

    @PutMapping("/finish")
    @Operation(summary = "完成生产流转卡")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:pro-card:finish')")
    public CommonResult<Boolean> finishCard(@RequestParam("id") Long id) {
        cardService.finishCard(id);
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消生产流转卡")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:pro-card:update')")
    public CommonResult<Boolean> cancelCard(@RequestParam("id") Long id) {
        cardService.cancelCard(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得生产流转卡")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:pro-card:query')")
    public CommonResult<MesProCardRespVO> getCard(@RequestParam("id") Long id) {
        MesProCardDO card = cardService.getCard(id);
        if (card == null) {
            return success(null);
        }
        return success(buildCardRespVOList(ListUtil.of(card)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得生产流转卡分页")
    @PreAuthorize("@ss.hasPermission('mes:pro-card:query')")
    public CommonResult<PageResult<MesProCardRespVO>> getCardPage(@Valid MesProCardPageReqVO pageReqVO) {
        PageResult<MesProCardDO> pageResult = cardService.getCardPage(pageReqVO);
        return success(new PageResult<>(buildCardRespVOList(pageResult.getList()), pageResult.getTotal()));
    }


    @GetMapping("/export-excel")
    @Operation(summary = "导出生产流转卡 Excel")
    @PreAuthorize("@ss.hasPermission('mes:pro-card:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCardExcel(@Valid MesProCardPageReqVO pageReqVO,
                                HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MesProCardDO> list = cardService.getCardPage(pageReqVO).getList();
        List<MesProCardRespVO> voList = buildCardRespVOList(list);
        ExcelUtils.write(response, "生产流转卡.xls", "数据", MesProCardRespVO.class, voList);
    }

    // ==================== 拼接 VO ====================

    private List<MesProCardRespVO> buildCardRespVOList(List<MesProCardDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 批量获取关联数据
        Map<Long, MesProWorkOrderDO> workOrderMap = workOrderService.getWorkOrderMap(
                convertSet(list, MesProCardDO::getWorkOrderId));
        Map<Long, MesMdItemDO> itemMap = itemService.getItemMap(
                convertSet(list, MesProCardDO::getItemId));
        Map<Long, MesMdUnitMeasureDO> unitMeasureMap = unitMeasureService.getUnitMeasureMap(
                convertSet(itemMap.values(), MesMdItemDO::getUnitMeasureId));
        // 2. 拼接 VO
        return BeanUtils.toBean(list, MesProCardRespVO.class, vo -> {
            MapUtils.findAndThen(workOrderMap, vo.getWorkOrderId(), workOrder ->
                    vo.setWorkOrderCode(workOrder.getCode()).setWorkOrderName(workOrder.getName()));
            MapUtils.findAndThen(itemMap, vo.getItemId(), item -> {
                vo.setItemCode(item.getCode()).setItemName(item.getName())
                  .setSpecification(item.getSpecification()).setUnitMeasureId(item.getUnitMeasureId());
                MapUtils.findAndThen(unitMeasureMap, item.getUnitMeasureId(),
                        unitMeasure -> vo.setUnitMeasureName(unitMeasure.getName()));
            });
        });
    }

}
