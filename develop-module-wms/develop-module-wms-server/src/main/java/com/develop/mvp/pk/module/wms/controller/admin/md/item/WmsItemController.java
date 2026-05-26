package com.develop.mvp.pk.module.wms.controller.admin.md.item;

import cn.hutool.core.collection.CollUtil;
import com.develop.mvp.pk.framework.apilog.core.annotation.ApiAccessLog;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageParam;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.collection.MapUtils;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.framework.excel.core.util.ExcelUtils;
import com.develop.mvp.pk.module.wms.controller.admin.md.item.vo.item.WmsItemListReqVO;
import com.develop.mvp.pk.module.wms.controller.admin.md.item.vo.item.WmsItemPageReqVO;
import com.develop.mvp.pk.module.wms.controller.admin.md.item.vo.item.WmsItemRespVO;
import com.develop.mvp.pk.module.wms.controller.admin.md.item.vo.item.WmsItemSaveReqVO;
import com.develop.mvp.pk.module.wms.controller.admin.md.item.vo.sku.WmsItemSkuRespVO;
import com.develop.mvp.pk.module.wms.dal.dataobject.md.item.WmsItemBrandDO;
import com.develop.mvp.pk.module.wms.dal.dataobject.md.item.WmsItemCategoryDO;
import com.develop.mvp.pk.module.wms.dal.dataobject.md.item.WmsItemDO;
import com.develop.mvp.pk.module.wms.dal.dataobject.md.item.WmsItemSkuDO;
import com.develop.mvp.pk.module.wms.service.md.item.WmsItemBrandService;
import com.develop.mvp.pk.module.wms.service.md.item.WmsItemCategoryService;
import com.develop.mvp.pk.module.wms.service.md.item.WmsItemService;
import com.develop.mvp.pk.module.wms.service.md.item.WmsItemSkuService;
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
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.*;

@Tag(name = "管理后台 - WMS 商品")
@RestController
@RequestMapping("/wms/item")
@Validated
public class WmsItemController {

    @Resource
    private WmsItemService itemService;
    @Resource
    private WmsItemCategoryService categoryService;
    @Resource
    private WmsItemBrandService brandService;
    @Resource
    private WmsItemSkuService itemSkuService;

    @PostMapping("/create")
    @Operation(summary = "创建商品")
    @PreAuthorize("@ss.hasPermission('wms:item:create')")
    public CommonResult<Long> createItem(@Valid @RequestBody WmsItemSaveReqVO createReqVO) {
        return success(itemService.createItem(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新商品")
    @PreAuthorize("@ss.hasPermission('wms:item:update')")
    public CommonResult<Boolean> updateItem(@Valid @RequestBody WmsItemSaveReqVO updateReqVO) {
        itemService.updateItem(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除商品")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:item:delete')")
    public CommonResult<Boolean> deleteItem(@RequestParam("id") Long id) {
        itemService.deleteItem(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得商品")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:item:query')")
    public CommonResult<WmsItemRespVO> getItem(@RequestParam("id") Long id) {
        WmsItemDO item = itemService.getItem(id);
        return success(buildItemRespVO(item));
    }

    @GetMapping("/page")
    @Operation(summary = "获得商品分页")
    @PreAuthorize("@ss.hasPermission('wms:item:query')")
    public CommonResult<PageResult<WmsItemRespVO>> getItemPage(@Valid WmsItemPageReqVO pageReqVO) {
        PageResult<WmsItemDO> pageResult = itemService.getItemPage(pageReqVO);
        return success(new PageResult<>(buildItemRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得商品精简列表")
    @PreAuthorize("@ss.hasPermission('wms:item:query')")
    public CommonResult<List<WmsItemRespVO>> getItemSimpleList(@Valid WmsItemListReqVO listReqVO) {
        List<WmsItemDO> list = itemService.getItemList(listReqVO);
        return success(buildItemRespVOList(list));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出商品 Excel")
    @PreAuthorize("@ss.hasPermission('wms:item:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportItemExcel(@Valid WmsItemPageReqVO pageReqVO,
                                HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsItemDO> list = itemService.getItemPage(pageReqVO).getList();
        ExcelUtils.write(response, "商品.xls", "数据", WmsItemRespVO.class,
                buildItemRespVOList(list));
    }

    // ==================== 拼接 VO ====================

    private WmsItemRespVO buildItemRespVO(WmsItemDO item) {
        if (item == null) {
            return null;
        }
        return getFirst(buildItemRespVOList(Collections.singletonList(item)));
    }

    private List<WmsItemRespVO> buildItemRespVOList(List<WmsItemDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 查询关联数据
        Map<Long, WmsItemCategoryDO> categoryMap = categoryService.getItemCategoryMap(
                convertSet(list, WmsItemDO::getCategoryId));
        Map<Long, WmsItemBrandDO> brandMap = brandService.getItemBrandMap(
                convertSet(list, WmsItemDO::getBrandId));
        Map<Long, List<WmsItemSkuDO>> skuMap = itemSkuService.getItemSkuMultiMap(
                convertSet(list, WmsItemDO::getId));
        // 拼接 VO
        return BeanUtils.toBean(list, WmsItemRespVO.class, vo -> {
            MapUtils.findAndThen(categoryMap, vo.getCategoryId(), category ->
                    vo.setCategoryName(category.getName()));
            MapUtils.findAndThen(brandMap, vo.getBrandId(), brand ->
                    vo.setBrandName(brand.getName()));
            vo.setSkus(BeanUtils.toBean(skuMap.getOrDefault(vo.getId(), Collections.emptyList()),
                    WmsItemSkuRespVO.class));
        });
    }

}
