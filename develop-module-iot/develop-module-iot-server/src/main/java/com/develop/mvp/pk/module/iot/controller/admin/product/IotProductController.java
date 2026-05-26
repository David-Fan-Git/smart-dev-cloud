package com.develop.mvp.pk.module.iot.controller.admin.product;

import com.develop.mvp.pk.framework.apilog.core.annotation.ApiAccessLog;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageParam;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.collection.MapUtils;
import com.develop.mvp.pk.framework.excel.core.util.ExcelUtils;
import com.develop.mvp.pk.module.iot.application.product.command.CreateIotProductCommand;
import com.develop.mvp.pk.module.iot.application.product.command.UpdateIotProductCommand;
import com.develop.mvp.pk.module.iot.application.product.command.UpdateIotProductStatusCommand;
import com.develop.mvp.pk.module.iot.application.product.port.inbound.IotProductUseCase;
import com.develop.mvp.pk.module.iot.application.product.query.IotProductPageQuery;
import com.develop.mvp.pk.module.iot.application.product.result.IotProductResult;
import com.develop.mvp.pk.module.iot.controller.admin.product.vo.product.IotProductPageReqVO;
import com.develop.mvp.pk.module.iot.controller.admin.product.vo.product.IotProductRespVO;
import com.develop.mvp.pk.module.iot.controller.admin.product.vo.product.IotProductSaveReqVO;
import com.develop.mvp.pk.module.iot.dal.dataobject.product.IotProductCategoryDO;
import com.develop.mvp.pk.module.iot.service.product.IotProductCategoryService;
import com.develop.mvp.pk.module.iot.service.product.IotProductService;
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
import java.util.List;
import java.util.Map;

import static com.develop.mvp.pk.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - IoT 产品")
@RestController
@RequestMapping("/iot/product")
@Validated
public class IotProductController {

    @Resource
    private IotProductUseCase productUseCase;
    @Resource
    private IotProductService productService;
    @Resource
    private IotProductCategoryService categoryService;

    @PostMapping("/create")
    @Operation(summary = "创建产品")
    @PreAuthorize("@ss.hasPermission('iot:product:create')")
    public CommonResult<Long> createProduct(@Valid @RequestBody IotProductSaveReqVO createReqVO) {
        return success(productUseCase.createProduct(new CreateIotProductCommand(createReqVO.getName(),
                createReqVO.getProductKey(), createReqVO.getRegisterEnabled(), createReqVO.getCategoryId(),
                createReqVO.getIcon(), createReqVO.getPicUrl(), createReqVO.getDescription(),
                createReqVO.getDeviceType(), createReqVO.getNetType(), createReqVO.getProtocolType(),
                createReqVO.getSerializeType())));
    }

    @PutMapping("/update")
    @Operation(summary = "更新产品")
    @PreAuthorize("@ss.hasPermission('iot:product:update')")
    public CommonResult<Boolean> updateProduct(@Valid @RequestBody IotProductSaveReqVO updateReqVO) {
        productUseCase.updateProduct(new UpdateIotProductCommand(updateReqVO.getId(), updateReqVO.getName(),
                updateReqVO.getRegisterEnabled(), updateReqVO.getCategoryId(), updateReqVO.getIcon(),
                updateReqVO.getPicUrl(), updateReqVO.getDescription(), updateReqVO.getDeviceType(),
                updateReqVO.getNetType(), updateReqVO.getProtocolType(), updateReqVO.getSerializeType()));
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新产品状态")
    @Parameter(name = "id", description = "编号", required = true)
    @Parameter(name = "status", description = "状态", required = true)
    @PreAuthorize("@ss.hasPermission('iot:product:update')")
    public CommonResult<Boolean> updateProductStatus(@RequestParam("id") Long id,
                                                     @RequestParam("status") Integer status) {
        productUseCase.updateProductStatus(new UpdateIotProductStatusCommand(id, status));
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除产品")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:product:delete')")
    public CommonResult<Boolean> deleteProduct(@RequestParam("id") Long id) {
        productUseCase.deleteProduct(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得产品")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:product:query')")
    public CommonResult<IotProductRespVO> getProduct(@RequestParam("id") Long id) {
        IotProductResult product = productUseCase.getProduct(id);
        if (product == null) {
            return success(null);
        }
        IotProductCategoryDO category = categoryService.getProductCategory(product.categoryId());
        return success(toRespVO(product, category));
    }

    @GetMapping("/get-by-key")
    @Operation(summary = "通过 ProductKey 获得产品")
    @Parameter(name = "productKey", description = "产品Key", required = true, example = "abc123")
    @PreAuthorize("@ss.hasPermission('iot:product:query')")
    public CommonResult<IotProductRespVO> getProductByKey(@RequestParam("productKey") String productKey) {
        IotProductResult product = productUseCase.getProductByProductKey(productKey);
        if (product == null) {
            return success(null);
        }
        IotProductCategoryDO category = categoryService.getProductCategory(product.categoryId());
        return success(toRespVO(product, category));
    }

    @GetMapping("/page")
    @Operation(summary = "获得产品分页")
    @PreAuthorize("@ss.hasPermission('iot:product:query')")
    public CommonResult<PageResult<IotProductRespVO>> getProductPage(@Valid IotProductPageReqVO pageReqVO) {
        PageResult<IotProductResult> pageResult = productUseCase.getProductPage(new IotProductPageQuery(
                pageReqVO.getName(), pageReqVO.getProductKey(), pageReqVO.getPageNo(), pageReqVO.getPageSize()));
        Map<Long, IotProductCategoryDO> categoryMap = categoryService.getProductCategoryMap(
                convertList(pageResult.getList(), IotProductResult::categoryId));
        return success(new PageResult<>(convertList(pageResult.getList(), product -> {
            IotProductRespVO respVO = toRespVO(product, null);
            MapUtils.findAndThen(categoryMap, product.categoryId(), category -> respVO.setCategoryName(category.getName()));
            return respVO;
        }), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出产品 Excel")
    @PreAuthorize("@ss.hasPermission('iot:product:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProductExcel(@Valid IotProductPageReqVO exportReqVO,
                                   HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        CommonResult<PageResult<IotProductRespVO>> result = getProductPage(exportReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "产品.xls", "数据", IotProductRespVO.class,
                result.getData().getList());
    }

    @PostMapping("/sync-property-table")
    @Operation(summary = "同步产品属性表结构到 TDengine")
    @PreAuthorize("@ss.hasPermission('iot:product:update')")
    public CommonResult<Boolean> syncProductPropertyTable() {
        productUseCase.syncProductPropertyTable();
        return success(true);
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获取产品的精简信息列表", description = "主要用于前端的下拉选项")
    @Parameter(name = "deviceType", description = "设备类型", example = "1")
    public CommonResult<List<IotProductRespVO>> getProductSimpleList(
            @RequestParam(value = "deviceType", required = false) Integer deviceType) {
        List<IotProductResult> list = productUseCase.getProductList(deviceType);
        return success(convertList(list, product ->
                new IotProductRespVO().setId(product.id()).setName(product.name()).setStatus(product.status())
                        .setDeviceType(product.deviceType()).setProductKey(product.productKey())));
    }

    private IotProductRespVO toRespVO(IotProductResult product, IotProductCategoryDO category) {
        IotProductRespVO respVO = new IotProductRespVO()
                .setId(product.id()).setName(product.name()).setProductKey(product.productKey())
                .setProductSecret(product.productSecret()).setRegisterEnabled(product.registerEnabled())
                .setCategoryId(product.categoryId()).setIcon(product.icon()).setPicUrl(product.picUrl())
                .setDescription(product.description()).setStatus(product.status()).setDeviceType(product.deviceType())
                .setNetType(product.netType()).setProtocolType(product.protocolType())
                .setSerializeType(product.serializeType());
        if (category != null) {
            respVO.setCategoryName(category.getName());
        }
        return respVO;
    }

}