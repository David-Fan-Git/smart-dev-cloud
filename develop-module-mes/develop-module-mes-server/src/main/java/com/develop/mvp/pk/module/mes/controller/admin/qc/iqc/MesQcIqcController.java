package com.develop.mvp.pk.module.mes.controller.admin.qc.iqc;

import cn.hutool.core.collection.CollUtil;
import com.develop.mvp.pk.framework.apilog.core.annotation.ApiAccessLog;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageParam;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.framework.excel.core.util.ExcelUtils;
import com.develop.mvp.pk.module.mes.controller.admin.qc.iqc.vo.MesQcIqcPageReqVO;
import com.develop.mvp.pk.module.mes.controller.admin.qc.iqc.vo.MesQcIqcRespVO;
import com.develop.mvp.pk.module.mes.controller.admin.qc.iqc.vo.MesQcIqcSaveReqVO;
import com.develop.mvp.pk.module.mes.dal.dataobject.md.item.MesMdItemDO;
import com.develop.mvp.pk.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import com.develop.mvp.pk.module.mes.dal.dataobject.md.vendor.MesMdVendorDO;
import com.develop.mvp.pk.module.mes.dal.dataobject.qc.iqc.MesQcIqcDO;
import com.develop.mvp.pk.module.mes.service.md.item.MesMdItemService;
import com.develop.mvp.pk.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;
import com.develop.mvp.pk.module.mes.service.md.vendor.MesMdVendorService;
import com.develop.mvp.pk.module.mes.service.qc.iqc.MesQcIqcService;
import com.develop.mvp.pk.module.system.api.user.AdminUserApi;
import com.develop.mvp.pk.module.system.api.user.dto.AdminUserRespDTO;
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
import static com.develop.mvp.pk.framework.common.util.collection.MapUtils.findAndThen;
import static com.develop.mvp.pk.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - MES 来料检验单（IQC）")
@RestController
@RequestMapping("/mes/qc/iqc")
@Validated
public class MesQcIqcController {

    @Resource
    private MesQcIqcService iqcService;
    @Resource
    private MesMdVendorService vendorService;
    @Resource
    private MesMdItemService itemService;
    @Resource
    private MesMdUnitMeasureService unitMeasureService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建来料检验单")
    @PreAuthorize("@ss.hasPermission('mes:qc-iqc:create')")
    public CommonResult<Long> createIqc(@Valid @RequestBody MesQcIqcSaveReqVO createReqVO) {
        return success(iqcService.createIqc(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新来料检验单")
    @PreAuthorize("@ss.hasPermission('mes:qc-iqc:update')")
    public CommonResult<Boolean> updateIqc(@Valid @RequestBody MesQcIqcSaveReqVO updateReqVO) {
        iqcService.updateIqc(updateReqVO);
        return success(true);
    }

    @PutMapping("/finish")
    @Operation(summary = "完成来料检验单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:qc-iqc:finish')")
    public CommonResult<Boolean> finishIqc(@RequestParam("id") Long id) {
        iqcService.finishIqc(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除来料检验单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:qc-iqc:delete')")
    public CommonResult<Boolean> deleteIqc(@RequestParam("id") Long id) {
        iqcService.deleteIqc(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得来料检验单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:qc-iqc:query')")
    public CommonResult<MesQcIqcRespVO> getIqc(@RequestParam("id") Long id) {
        MesQcIqcDO iqc = iqcService.getIqc(id);
        return success(buildIqcRespVOList(Collections.singletonList(iqc)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得来料检验单分页")
    @PreAuthorize("@ss.hasPermission('mes:qc-iqc:query')")
    public CommonResult<PageResult<MesQcIqcRespVO>> getIqcPage(@Valid MesQcIqcPageReqVO pageReqVO) {
        PageResult<MesQcIqcDO> pageResult = iqcService.getIqcPage(pageReqVO);
        return success(new PageResult<>(buildIqcRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出来料检验单 Excel")
    @PreAuthorize("@ss.hasPermission('mes:qc-iqc:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportIqcExcel(@Valid MesQcIqcPageReqVO pageReqVO,
                               HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MesQcIqcDO> list = iqcService.getIqcPage(pageReqVO).getList();
        List<MesQcIqcRespVO> voList = buildIqcRespVOList(list);
        ExcelUtils.write(response, "来料检验单.xls", "数据", MesQcIqcRespVO.class, voList);
    }

    // ==================== 拼接 VO ====================

    private List<MesQcIqcRespVO> buildIqcRespVOList(List<MesQcIqcDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 批量查询供应商
        Map<Long, MesMdVendorDO> vendorMap = vendorService.getVendorMap(
                convertSet(list, MesQcIqcDO::getVendorId));
        // 批量查询物料
        Map<Long, MesMdItemDO> itemMap = itemService.getItemMap(
                convertSet(list, MesQcIqcDO::getItemId));
        // 批量查询计量单位（通过物料的 unitMeasureId）
        Map<Long, MesMdUnitMeasureDO> unitMeasureMap = unitMeasureService.getUnitMeasureMap(
                convertSet(itemMap.values(), MesMdItemDO::getUnitMeasureId));
        // 批量查询检测人员
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(list, MesQcIqcDO::getInspectorUserId));
        // 拼装 VO
        return BeanUtils.toBean(list, MesQcIqcRespVO.class, vo -> {
            findAndThen(vendorMap, vo.getVendorId(),
                    vendor -> vo.setVendorNickname(vendor.getNickname()));
            findAndThen(itemMap, vo.getItemId(), item -> {
                vo.setItemCode(item.getCode());
                vo.setItemName(item.getName());
                vo.setItemSpecification(item.getSpecification());
                findAndThen(unitMeasureMap, item.getUnitMeasureId(),
                        unit -> vo.setUnitName(unit.getName()));
            });
            findAndThen(userMap, vo.getInspectorUserId(),
                    user -> vo.setInspectorNickname(user.getNickname()));
        });
    }

}
