---
name: aggregate-root-mall-product-skill
description: Use when modifying or reviewing Mall Product category, brand, SPU, SKU, property, comment, favorite, or browse history DDD migration boundaries.
type: ddd-aggregate-skill
status: production-review
---

# AggregateRoot Mall Product Skill

## AI Execution Contract

- **Scope:** 每次只处理本文件声明的一个聚合、一个子域或一个最小闭环；多聚合文件必须先拆分到目标子聚合后再实现。
- **Must Read:** 修改前读取本 skill 的 Current Source Anchors，以及对应 Controller、VO/DTO、DO、Mapper、Convert、Service/Application、Repository、ErrorCode、测试文件。
- **Must Preserve:** Controller 路径、HTTP 方法、VO/DTO 字段、CommonApi/Feign/RPC 契约、权限、租户、数据权限、错误码、分页、Excel、MQ、Job、缓存、第三方回调和 OpenAPI 可见行为。
- **Allowed Changes:** 只在目标聚合的 `domain`、`application`、`infrastructure`、`convert`、入口适配和对应测试内做最小必要修改，并按标准骨架补齐端口或 package 边界。
- **Forbidden Changes:** 禁止批量改无关聚合；禁止把新核心业务写入旧 `service/dal`；禁止让 domain 依赖 Spring、MyBatis、Feign、Mapper、DO、Controller VO、RPC client 或基础设施实现。
- **Dependency Rules:** domain 只依赖领域对象和值对象；application 编排用例、事务和端口；infrastructure 适配 Mapper/DO/外部系统；controller/job/mq 只做入口。
- **Verification Gate:** 完成前运行本 skill 的 Verification Commands；无法运行时写明命令、阻塞原因和未验证风险。
- **Stop Conditions:** 事实源缺失、skill 与当前代码冲突、外部契约可能变化、字段/错误码/事务需要猜测、验证失败时停止并先修订 skill 或缩小范围。

## Standard Skeleton Contract

目标聚合必须固定以下职责边界；Java 空目录用职责明确的接口或 `package-info.java` 固定，禁止 `Temp`/`Placeholder`/`Dummy`：

```text
domain/{aggregate}/model,valueobject,event,service,repository
application/{aggregate}/command,query,dto|result,port/inbound,port/outbound,service
infrastructure/{aggregate}/persistence,external,rpc,cache,messaging
convert/
controller/ job/ mq/ framework/
```

旧 `service/dal` 是迁移源，不是新核心业务最终落位。

## Overview

Mall Product covers product category, brand, SPU, SKU, property, property value, comment, favorite, and browse history behavior. This skill is the production refactoring contract for moving product code from legacy `service/dal` into `domain/application/infrastructure/convert` without changing external API, Controller, DTO, error-code, transaction, stock, or integration behavior.

Current compilable behavior wins over this document if a conflict is found. If this skill conflicts with current code, stop implementation and update the skill first.

## When to Use

Use this skill when changing:

- Product API contracts under `develop-module-mall/develop-module-product-api`.
- Product server Controller, application, domain, infrastructure, repository, convert, legacy service, mapper, or DO code.
- Stock increment/decrement, SPU price/stock derivation, SKU property validation, category validation, brand validation, comment creation, favorite uniqueness, or browse history retention.
- Product local/remote API split for ProductCategoryApi, ProductSkuApi, ProductSpuApi, or ProductCommentApi.

## When Not to Use

Do not use this skill as the authority for promotion, trade, statistics, member, or infra behavior except where product integrates with those modules through explicit API contracts. Do not use draft method names here to rename public API or Controller routes unless the API-contract migration explicitly includes that change.

## Baseline Failure Findings

Earlier Product skill drafts were not production-safe because they:

- Had no YAML frontmatter and could not be reliably discovered or versioned.
- Mixed desired DDD design with current behavior without source anchors.
- Invented state-machine constraints not implemented by legacy services.
- Omitted exact API signatures, DTO fields, fixed DO fields, error-code parameters, transaction contracts, and local/remote API conflicts.
- Did not capture current partial DDD conflicts such as create methods passing `id` into aggregate factories and repositories checking `selectById(spu.id().value())` before insert.

## Reproducibility Contract

A clean agent must be able to reproduce Product refactoring from this skill by following only repository facts and this document:

1. Read `DDD_Skill_Production_Readiness_Standard.md` and `Module_Structure_Standard.md` first.
2. Treat legacy `service/*ServiceImpl` and public `api/*Api.java` as behavior facts until parity tests prove replacement behavior.
3. Preserve all Controller paths, permissions, request/response DTOs, OpenAPI annotations, error codes, and pagination semantics unless a separate API migration explicitly changes them.
4. Refactor one Product aggregate or one tightly coupled aggregate set at a time.
5. Compile `develop-module-mall/develop-module-product-api` and `develop-module-mall/develop-module-product-server` after each batch.

## Current Source Anchors

### API module

- `develop-module-mall/develop-module-product-api/src/main/java/com/develop/mvp/pk/module/product/api/category/ProductCategoryApi.java`
- `develop-module-mall/develop-module-product-api/src/main/java/com/develop/mvp/pk/module/product/api/sku/ProductSkuApi.java`
- `develop-module-mall/develop-module-product-api/src/main/java/com/develop/mvp/pk/module/product/api/spu/ProductSpuApi.java`
- `develop-module-mall/develop-module-product-api/src/main/java/com/develop/mvp/pk/module/product/api/comment/ProductCommentApi.java`
- `develop-module-mall/develop-module-product-api/src/main/java/com/develop/mvp/pk/module/product/api/comment/dto/ProductCommentCreateReqDTO.java`
- `develop-module-mall/develop-module-product-api/src/main/java/com/develop/mvp/pk/module/product/api/sku/dto/ProductSkuRespDTO.java`
- `develop-module-mall/develop-module-product-api/src/main/java/com/develop/mvp/pk/module/product/api/sku/dto/ProductSkuUpdateStockReqDTO.java`
- `develop-module-mall/develop-module-product-api/src/main/java/com/develop/mvp/pk/module/product/api/spu/dto/ProductSpuRespDTO.java`
- `develop-module-mall/develop-module-product-api/src/main/java/com/develop/mvp/pk/module/product/api/property/dto/ProductPropertyValueDetailRespDTO.java`
- `develop-module-mall/develop-module-product-api/src/main/java/com/develop/mvp/pk/module/product/enums/ErrorCodeConstants.java`
- `develop-module-mall/develop-module-product-api/src/main/java/com/develop/mvp/pk/module/product/enums/ProductConstants.java`
- `develop-module-mall/develop-module-product-api/src/main/java/com/develop/mvp/pk/module/product/enums/spu/ProductSpuStatusEnum.java`

### Server entry and adapters

- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/api/category/ProductCategoryApiImpl.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/api/sku/ProductSkuApiImpl.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/api/spu/ProductSpuApiImpl.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/api/comment/ProductCommentApiImpl.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/controller/admin/category/ProductCategoryController.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/controller/admin/brand/ProductBrandController.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/controller/admin/spu/ProductSpuController.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/controller/admin/property/ProductPropertyController.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/controller/admin/property/ProductPropertyValueController.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/controller/admin/comment/ProductCommentController.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/controller/admin/favorite/ProductFavoriteController.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/controller/admin/history/ProductBrowseHistoryController.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/controller/app/category/AppCategoryController.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/controller/app/spu/AppProductSpuController.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/controller/app/comment/AppProductCommentController.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/controller/app/favorite/AppFavoriteController.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/controller/app/history/AppProductBrowseHistoryController.java`

### Legacy behavior source

- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/service/category/ProductCategoryServiceImpl.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/service/brand/ProductBrandServiceImpl.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/service/spu/ProductSpuServiceImpl.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/service/sku/ProductSkuServiceImpl.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/service/property/ProductPropertyServiceImpl.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/service/property/ProductPropertyValueServiceImpl.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/service/comment/ProductCommentServiceImpl.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/service/favorite/ProductFavoriteServiceImpl.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/service/history/ProductBrowseHistoryServiceImpl.java`

### Data, mapper, and convert source

- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/dal/dataobject/category/ProductCategoryDO.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/dal/dataobject/brand/ProductBrandDO.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/dal/dataobject/spu/ProductSpuDO.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/dal/dataobject/sku/ProductSkuDO.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/dal/dataobject/property/ProductPropertyDO.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/dal/dataobject/property/ProductPropertyValueDO.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/dal/dataobject/comment/ProductCommentDO.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/dal/dataobject/favorite/ProductFavoriteDO.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/dal/dataobject/history/ProductBrowseHistoryDO.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/dal/mysql/sku/ProductSkuMapper.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/dal/mysql/spu/ProductSpuMapper.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/convert/brand/ProductBrandConvert.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/convert/comment/ProductCommentConvert.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/convert/favorite/ProductFavoriteConvert.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/convert/sku/ProductSkuConvert.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/convert/spu/ProductSpuConvert.java`

### Current partial DDD source

- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/domain/event/DomainEvent.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/domain/event/DomainEventPublisher.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/infrastructure/SpringDomainEventPublisher.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/domain/productcategory/ProductCategory.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/domain/productcategory/repository/ProductCategoryRepository.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/domain/productbrand/ProductBrand.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/domain/productbrand/repository/ProductBrandRepository.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/domain/productspu/ProductSpu.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/domain/productspu/repository/ProductSpuRepository.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/domain/productspu/repository/ProductSpuPageQuery.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/application/productcategory/ProductCategoryApplicationService.java`,
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/application/productbrand/ProductBrandApplicationService.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/application/productspu/ProductSpuApplicationService.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/infrastructure/productcategory/ProductCategoryRepositoryImpl.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/infrastructure/productbrand/ProductBrandRepositoryImpl.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/infrastructure/productspu/ProductSpuRepositoryImpl.java`

### Integration config and tests

- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/framework/rpc/config/RpcConfiguration.java` — class `RpcConfiguration`, bean name `productRpcConfiguration`, scans `MemberUserRemoteClient` and `MemberLevelRemoteClient`.
- `develop-module-mall/develop-module-product-server/src/test/resources/application-unit-test.yaml`
- `develop-module-mall/develop-module-product-server/src/test/resources/sql/create_tables.sql`
- `develop-module-mall/develop-module-product-server/src/test/resources/sql/clean.sql`

## Fixed API Contract

Current stable Product APIs still carry Feign annotations. Preserve these exact signatures until the local/remote split migrates `@FeignClient` to `remote/*RemoteClient`:

```java
@FeignClient(name = ApiConstants.NAME)
@Tag(name = "RPC 服务 - 商品分类")
public interface ProductCategoryApi {
    String PREFIX = ApiConstants.PREFIX + "/category";

    @GetMapping(PREFIX + "/valid")
    @Operation(summary = "校验部门是否合法")
    CommonResult<Boolean> validateCategoryList(@RequestParam("ids") Collection<Long> ids);
}
```

```java
@FeignClient(name = ApiConstants.NAME)
@Tag(name = "RPC 服务 - 商品 SKU")
public interface ProductSkuApi {
    String PREFIX = ApiConstants.PREFIX + "/sku";

    @GetMapping(PREFIX + "/get")
    CommonResult<ProductSkuRespDTO> getSku(@RequestParam("id") Long id);

    @GetMapping(PREFIX + "/list")
    CommonResult<List<ProductSkuRespDTO>> getSkuList(@RequestParam("ids") Collection<Long> ids);

    default Map<Long, ProductSkuRespDTO> getSkuMap(Collection<Long> ids) {
        return convertMap(getSkuList(ids).getCheckedData(), ProductSkuRespDTO::getId);
    }

    @GetMapping(PREFIX + "/list-by-spu-id")
    CommonResult<List<ProductSkuRespDTO>> getSkuListBySpuId(@RequestParam("spuIds") Collection<Long> spuIds);

    @PostMapping(PREFIX + "/update-stock")
    CommonResult<Boolean> updateSkuStock(@RequestBody @Valid ProductSkuUpdateStockReqDTO updateStockReqDTO);
}
```

```java
@FeignClient(name = ApiConstants.NAME)
@Tag(name = "RPC 服务 - 商品 SPU")
public interface ProductSpuApi {
    String PREFIX = ApiConstants.PREFIX + "/spu";

    @GetMapping(PREFIX + "/list")
    CommonResult<List<ProductSpuRespDTO>> getSpuList(@RequestParam("ids") Collection<Long> ids);

    default Map<Long, ProductSpuRespDTO> getSpuMap(Collection<Long> ids) {
        return convertMap(getSpuList(ids).getCheckedData(), ProductSpuRespDTO::getId);
    }

    @GetMapping(PREFIX + "/valid")
    CommonResult<List<ProductSpuRespDTO>> validateSpuList(@RequestParam("ids") Collection<Long> ids);

    @GetMapping(PREFIX + "/get")
    CommonResult<ProductSpuRespDTO> getSpu(@RequestParam("id") Long id);
}
```

```java
@FeignClient(name = ApiConstants.NAME)
@Tag(name = "RPC 服务 - 产品评论")
public interface ProductCommentApi {
    String PREFIX = ApiConstants.PREFIX + "/comment";

    @PostMapping(PREFIX + "/create")
    @Operation(summary = "创建评论")
    CommonResult<Long> createComment(@RequestBody @Valid ProductCommentCreateReqDTO createReqDTO);
}
```

Do not fix the current `ProductCategoryApi` OpenAPI text `校验部门是否合法` unless the task explicitly includes API documentation cleanup.

## Fixed Data Model

All Product DOs extend `BaseDO`, so `creator`, `createTime`, `updater`, `updateTime`, and `deleted` behavior is inherited and must remain mapped through MyBatis Plus.

### ProductCategoryDO: `product_category`

- `id: Long` primary key.
- `parentId: Long`; root is `ProductCategoryDO.PARENT_ID_NULL = 0L`.
- `name: String`.
- `picUrl: String`.
- `sort: Integer`.
- `status: Integer`, `CommonStatusEnum`.
- Constant `CATEGORY_LEVEL = 2`; SPU can only bind category level >= 2.

### ProductBrandDO: `product_brand`

- `id: Long` primary key.
- `name: String`, unique by service rule.
- `picUrl: String`.
- `sort: Integer`.
- `description: String`.
- `status: Integer`, `CommonStatusEnum`.

### ProductSpuDO: `product_spu`

- `id: Long` primary key.
- `name`, `keyword`, `introduction`, `description: String`.
- `categoryId: Long`, `brandId: Long`.
- `picUrl: String`.
- `sliderPicUrls: List<String>` using `JacksonTypeHandler`.
- `sort: Integer`.
- `status: Integer`, `ProductSpuStatusEnum`.
- `specType: Boolean`.
- `price`, `marketPrice`, `costPrice`, `stock: Integer`; derived from SKUs.
- `deliveryTypes: List<Integer>` using `IntegerListTypeHandler`.
- `deliveryTemplateId: Long`.
- `giveIntegral: Integer`.
- `subCommissionType: Boolean`.
- `salesCount`, `virtualSalesCount`, `browseCount: Integer`.

### ProductSkuDO: `product_sku`

- `id: Long` primary key.
- `spuId: Long`.
- `properties: List<ProductSkuDO.Property>` using `JacksonTypeHandler`.
- `price`, `marketPrice`, `costPrice: Integer` in cents.
- `barCode`, `picUrl: String`.
- `stock: Integer`.
- `weight`, `volume: Double`.
- `firstBrokeragePrice`, `secondBrokeragePrice: Integer`.
- `salesCount: Integer`.
- Nested `Property`: `propertyId`, `propertyName`, `valueId`, `valueName`.

### ProductPropertyDO: `product_property`

- `id: Long` primary key.
- `name: String`.
- `remark: String`.
- Single-spec defaults: `ID_DEFAULT = 0L`, `NAME_DEFAULT = "默认"`.

### ProductPropertyValueDO: `product_property_value`

- `id: Long` primary key.
- `propertyId: Long`.
- `name: String`.
- `remark: String`.
- Single-spec defaults: `ID_DEFAULT = 0L`, `NAME_DEFAULT = "默认"`.

### ProductCommentDO: `product_comment`

- `id: Long` primary key.
- `userId`, `orderId`, `orderItemId`, `spuId`, `skuId: Long`.
- `userNickname`, `userAvatar`, `spuName`, `skuPicUrl: String`.
- `anonymous`, `visible`, `replyStatus: Boolean`.
- `skuProperties: List<ProductSkuDO.Property>` using `JacksonTypeHandler`.
- `scores`, `descriptionScores`, `benefitScores: Integer`.
- `content: String`.
- `picUrls: List<String>` using `JacksonTypeHandler`.
- `replyUserId: Long`, `replyContent: String`, `replyTime: LocalDateTime`.
- Constant `NICKNAME_ANONYMOUS = "匿名用户"`.

### ProductFavoriteDO: `product_favorite`

- `id: Long` primary key.
- `userId: Long`.
- `spuId: Long`.

### ProductBrowseHistoryDO: `product_browse_history`

- `id: Long` primary key.
- `spuId: Long`.
- `userId: Long`.
- `userDeleted: Boolean`.

### External DTO fixed fields

| DTO | Required fields | Nullable/current defaults | Mapping source/target |
|---|---|---|---|
| `ProductCommentCreateReqDTO` | `skuId`, `descriptionScores`, `benefitScores`, `content`, `anonymous`, `userId` | `orderId`, `orderItemId`, `picUrls` nullable | API request -> `ProductCommentConvert` -> `ProductCommentDO`; service fills SPU/SKU/member fields |
| `ProductSkuRespDTO` | `id`, `spuId`, `properties`, `price`, `marketPrice`, `costPrice`, `barCode`, `picUrl`, `stock`, `weight`, `volume`, `firstBrokeragePrice`, `secondBrokeragePrice` | Mirrors SKU read model; no defaults in DTO | `ProductSkuDO` -> API response; `properties` maps to `ProductPropertyValueDetailRespDTO` |
| `ProductSkuUpdateStockReqDTO` | `items` | no default; empty-list behavior is not explicitly validated by DTO | API request -> `ProductSkuServiceImpl#updateSkuStock` |
| `ProductSkuUpdateStockReqDTO.Item` | `id`, `incrCount` | positive increments, negative decrements, zero no-ops in service | Item ids resolve to current SKU rows for SPU stock grouping |
| `ProductSpuRespDTO` | `id`, `name`, `unit`, `categoryId`, `picUrl`, `status`, `specType`, `price`, `marketPrice`, `costPrice`, `stock`, `deliveryTypes`, `deliveryTemplateId`, `giveIntegral`, `subCommissionType` | DTO contains subset of DO; many fields are nullable by Java type | `ProductSpuDO` -> API response |
| `ProductPropertyValueDetailRespDTO` | `propertyId`, `propertyName`, `valueId`, `valueName` | no default in DTO | SKU property detail response; preserve exact names |

### Controller VO fixed field groups

| VO | Field group | Required/default behavior | Mapping source/target |
|---|---|---|---|
| `ProductSpuSaveReqVO` | `id`, `name`, `keyword`, `introduction`, `description`, `categoryId`, `brandId`, `picUrl`, `sliderPicUrls`, `sort`, `specType`, `deliveryTypes`, `deliveryTemplateId`, `giveIntegral`, `subCommissionType`, `virtualSalesCount`, `salesCount`, `browseCount`, `skus` | Bean validation requires name/keyword/introduction/description/categoryId/brandId/picUrl/sort/specType/deliveryTypes/giveIntegral/subCommissionType; `id` nullable on create | Admin Controller -> legacy service/application -> `ProductSpuDO`/domain |
| `ProductSkuSaveReqVO` | `name`, `price`, `marketPrice`, `costPrice`, `barCode`, `picUrl`, `stock`, `weight`, `volume`, `firstBrokeragePrice`, `secondBrokeragePrice`, `properties` | `price`, `picUrl`, `stock` required; single-spec flow overwrites `properties` with default property/value | Nested under `ProductSpuSaveReqVO#skus`; maps to `ProductSkuDO` or domain `ProductSku` |
| `ProductSkuSaveReqVO.Property` | `propertyId`, `propertyName`, `valueId`, `valueName` | nullable by VO, but multi-spec validation requires coherent existing ids | Maps to `ProductSkuDO.Property` / `SkuProperty` |
| `ProductCategorySaveReqVO` | `id`, `parentId`, `name`, `picUrl`, `sort`, `status` | `parentId=0L` means root | Maps to `ProductCategoryDO` / `ProductCategory` |
| `ProductBrandCreateReqVO` and `ProductBrandUpdateReqVO` | `name`, `picUrl`, `sort`, `description`, `status`; update includes `id` | name uniqueness checked by service | Maps to `ProductBrandDO` / `ProductBrand` |
| `ProductCommentCreateReqVO` | admin-created comment fields | validates SKU/SPU but does not use member RPC path | Maps through `ProductCommentConvert` to `ProductCommentDO` |
| `ProductCommentUpdateVisibleReqVO` and `ProductCommentReplyReqVO` | `id` plus visible/reply fields | existing comment required | partial update of `ProductCommentDO` |
| `AppFavoriteReqVO`, `AppFavoriteBatchReqVO`, `AppFavoritePageReqVO` | user-facing favorite request/page fields | current user id comes from app security context | maps to favorite service calls |
| `AppProductBrowseHistoryDeleteReqVO` and `AppProductBrowseHistoryPageReqVO` | app browse-history request/page fields | current user id comes from app security context | maps to history service calls |

### Domain fixed model

| Domain type | Fields/capabilities | Current status |
|---|---|---|
| `ProductCategory` | id, name, parentId, picUrl, sort, status, domain events; enable/disable/profile update | Exists as partial DDD model |
| `ProductBrand` | id, name, picUrl, sort, description, status, domain events; enable/disable/profile update | Exists as partial DDD model |
| `ProductSpu` | id, name, keyword, introduction, description, categoryId, brandId, picUrl, sliderPicUrls, sort, status, specType, skus, derived prices/stock, delivery, integral, commission, sales/virtual/browse counts, domain events | Exists but not legacy-parity complete |
| `ProductSku` value object | sku id, properties, price, marketPrice, costPrice, barCode, picUrl, stock, weight, volume, salesCount | Exists under `domain/productspu/valueobject` |
| `SkuProperty` value object | propertyId, propertyName, valueId, valueName | Exists under `domain/productspu/valueobject` |
| ProductProperty/ProductPropertyValue/ProductComment/ProductFavorite/ProductBrowseHistory aggregates | Required for final target, but currently absent as full DDD aggregates | Must be introduced only after preserving legacy service behavior |

### Cross-layer mapping invariants

- `ProductSpuSaveReqVO.skus[*]` -> `ProductSkuDO`/`ProductSku`; SPU price and stock are derived after SKU validation, not copied from request.
- `ProductSkuDO.Property` <-> `ProductSkuSaveReqVO.Property` <-> `SkuProperty` must preserve `propertyId/propertyName/valueId/valueName` exactly.
- `ProductSkuRespDTO.properties[*]` exposes property/value details and must not be collapsed to ids only.
- `ProductCommentCreateReqDTO` does not carry SPU/member display fields; service fills them from SKU/SPU/member lookups.
- `ProductSpuDO.sliderPicUrls`, `ProductSkuDO.properties`, and `ProductCommentDO.picUrls/skuProperties` must remain JSON-compatible list fields.

## Required Method Signatures and Capabilities

These signatures describe capabilities that must be preserved. New DDD application service method names may differ only if Controller/API behavior and tests prove parity.

### Target DDD signatures

These names are recommended for consistency with current partial DDD code. If a future batch renames them, the parameter semantics and return contracts must remain equivalent.

#### ProductCategory target

```java
public interface ProductCategoryRepository {
    ProductCategory save(ProductCategory category);
    void delete(ProductCategoryId id);
    ProductCategory findById(ProductCategoryId id);
    Optional<ProductCategory> findByName(String name);
    List<ProductCategory> findByParentId(Long parentId);
    List<ProductCategory> findByStatus(Integer status);
    List<ProductCategory> findAll();
    long countByParentId(Long parentId);
}
```

Application capabilities must include create, update, delete, status update, get by id, list by parent, list all, list by status, parent validation, child-count validation, and bound-SPU validation before delete.

#### ProductBrand target

```java
public interface ProductBrandRepository {
    ProductBrand save(ProductBrand brand);
    void delete(ProductBrandId id);
    ProductBrand findById(ProductBrandId id);
    Optional<ProductBrand> findByName(String name);
    List<ProductBrand> findByStatus(Integer status);
    List<ProductBrand> findAll();
    long count();
}
```

Application capabilities must include create, update, delete, status update, get/list, and name uniqueness with current-id exclusion.

#### ProductSpu target

```java
public interface ProductSpuRepository {
    ProductSpu save(ProductSpu spu);
    void delete(ProductSpuId id);
    ProductSpu findById(ProductSpuId id);
    ProductSpu findByIdIncludeDeleted(ProductSpuId id);
    List<ProductSpu> findByIds(Collection<ProductSpuId> ids);
    List<ProductSpu> findByStatus(Integer status);
    PageResult<ProductSpu> findPage(ProductSpuPageQuery query);
    long countByCategoryId(Long categoryId);
    void updateStock(Long id, int incrCount);
    void updateBrowseCount(Long id, int incrCount);
    Map<Integer, Long> getTabsCount();
}
```

`ProductSpuFactory` must support both create from request semantics and reconstitution from persisted DO/SKU data. Create semantics must not require a non-null database id before insert; reconstitution may require an id.

Application capabilities must include create, update, delete, status update, recycle if exposed, stock update, browse-count update, get, ordered list, status list, admin page, app page behavior, tab counts, and category-bound count.

#### Missing target repositories

The final DDD target also needs repository interfaces for ProductSku, ProductProperty, ProductPropertyValue, ProductComment, ProductFavorite, and ProductBrowseHistory. They are currently absent. Do not invent these in a broad batch; introduce each with the aggregate/service migration that proves parity.

Minimum required capabilities when introduced:

- `ProductSkuRepository`: find by id including deleted, find by ids, find by SPU id(s), create batch, update diff by property key, delete by SPU id, update stock incr/decr with affected-row guard, update redundant property names.
- `ProductPropertyRepository`: find by id, find by name, save, delete, page/list.
- `ProductPropertyValueRepository`: find by id, find by `(propertyId, name)`, find by property ids, count by property id, save, delete, delete by property id.
- `ProductCommentRepository`: find by id, find by `(userId, orderItemId)`, save, page, visible/reply partial updates.
- `ProductFavoriteRepository`: find by `(userId, spuId)`, save, delete, page, count by user.
- `ProductBrowseHistoryRepository`: find by `(userId, spuId)`, find oldest/page count by user, save, delete, hide by user and SPU ids, page.

### Legacy service capability signatures

### Category

- `Long createCategory(ProductCategorySaveReqVO createReqVO)`.
- `void updateCategory(ProductCategorySaveReqVO updateReqVO)`.
- `void deleteCategory(Long id)`.
- `void validateCategoryList(Collection<Long> ids)`.
- `void validateCategory(Long id)`.
- `Integer getCategoryLevel(Long id)`.
- `List<ProductCategoryDO> getEnableCategoryList()` and `getEnableCategoryList(List<Long> ids)`.

### Brand

- `Long createBrand(ProductBrandCreateReqVO createReqVO)`.
- `void updateBrand(ProductBrandUpdateReqVO updateReqVO)`.
- `void deleteBrand(Long id)`.
- `void validateProductBrand(Long id)`.
- `void validateBrandNameUnique(Long id, String name)` behavior must be preserved.

### SPU and SKU

- `Long createSpu(ProductSpuSaveReqVO createReqVO)`.
- `void updateSpu(ProductSpuSaveReqVO updateReqVO)`.
- `void deleteSpu(Long id)`.
- `void updateSpuStatus(ProductSpuUpdateStatusReqVO updateReqVO)`.
- `List<ProductSpuDO> validateSpuList(Collection<Long> ids)`.
- `List<ProductSpuDO> getSpuList(Collection<Long> ids)` must preserve input order.
- `PageResult<ProductSpuDO> getSpuPage(AppProductSpuPageReqVO pageReqVO)` must include enabled child categories when category filters are used.
- `void validateSkuList(List<ProductSkuSaveReqVO> skus, Boolean specType)`.
- `void updateSkuStock(ProductSkuUpdateStockReqDTO updateStockReqDTO)`.
- `int updateSkuProperty(Long propertyId, String propertyName)`.
- `int updateSkuPropertyValue(Long propertyValueId, String propertyValueName)`.

### Property and property value

- Creating an existing property by name returns the existing id instead of throwing.
- Updating property with another row's name throws `PROPERTY_EXISTS`.
- Deleting property with values throws `PROPERTY_DELETE_FAIL_VALUE_EXISTS`.
- Creating an existing property value under the same property returns the existing id instead of throwing.
- Updating property value with another row's name under the same property throws `PROPERTY_VALUE_EXISTS`.
- Property and property-value name changes must update redundant SKU property names.

### Comment, favorite, and browse history

- `Long createComment(ProductCommentCreateReqDTO createReqDTO)` validates SKU/SPU including deleted rows, checks `(userId, orderItemId)` uniqueness, loads member user detail, and inserts comment.
- `void updateCommentVisible(ProductCommentUpdateVisibleReqVO updateReqVO)`.
- `void replyComment(ProductCommentReplyReqVO replyVO, Long userId)`.
- `Long createFavorite(Long userId, Long spuId)` enforces `(userId, spuId)` uniqueness.
- `void deleteFavorite(Long userId, Long spuId)` requires an existing favorite.
- `void createBrowseHistory(Long userId, Long spuId)` ignores null users, keeps only latest record per user/SPU, and caps each user at 100 records.
- `void hideUserBrowseHistory(Long userId, Collection<Long> spuIds)` sets `userDeleted=true`.

## Business Rules

### Category rules

- Root category id is `0L`.
- Parent validation allows root; non-root parent must exist and must itself be first-level (`parentId == 0L`).
- Category deletion requires: category exists, no child categories, and no bound SPU.
- API category validation ignores empty id collections.
- API category validation fails if any id does not exist, is disabled, or has level lower than `CATEGORY_LEVEL`.
- `getCategoryLevel` uses an upper loop bound of `Byte.MAX_VALUE` to avoid dirty-data infinite loops.

### Brand rules

- Brand creation and update require unique `name`, excluding the current id on update.
- `validateProductBrand` throws if the brand is missing or disabled.
- Brand deletion currently only checks existence; it does not check SPU binding in legacy behavior.

### SPU rules

- Create/update validates category, brand, and SKU list before writing.
- Create initializes SPU status to `ENABLE`, `salesCount` to `0`, and `browseCount` to `0` when status is null.
- Update preserves existing status instead of accepting status from the save request.
- `price`, `marketPrice`, and `costPrice` are the minimum values from SKU list; `stock` is the sum of SKU stock.
- Delete requires current status `RECYCLE`; otherwise throw `SPU_NOT_RECYCLE`.
- `validateSpuList` ignores empty collections and throws `SPU_NOT_EXISTS` or `SPU_NOT_ENABLE` with SPU name.
- `getSpuList(Collection<Long> ids)` returns results in the same order as requested ids.
- App SPU page includes children of selected enabled categories.
- Tab counts include for-sale, in-warehouse, sold-out, alert-stock, and recycle-bin counts.

### SKU rules

- Empty SKU list throws `SKU_NOT_EXISTS`.
- Single spec (`specType == false`) mutates the first SKU to use default property/value `0L/默认` and returns without multi-spec validation.
- Multi-spec validation requires all referenced property ids to exist.
- Duplicate properties inside one SKU throw `SKU_PROPERTIES_DUPLICATED`.
- Inconsistent property counts across SKUs throw `SPU_ATTR_NUMBERS_MUST_BE_EQUALS`.
- Duplicate SKU property-value combinations throw `SPU_SKU_NOT_DUPLICATE`.
- Stock increment updates SKU `stock += incrCount` and `sales_count -= incrCount`.
- Stock decrement uses affected-row guard `stock >= abs(incrCount)`; zero affected rows throws `SKU_STOCK_NOT_ENOUGH`.
- After SKU stock updates, SPU stock/sales changes are grouped by actual SKU-to-SPU mapping and applied through `ProductSpuService.updateSpuStock`.

### Property rules

- Creating a property whose name already exists returns the existing id.
- Updating a property to another property's name throws `PROPERTY_EXISTS`.
- Deleting a property with any values throws `PROPERTY_DELETE_FAIL_VALUE_EXISTS`.
- Property name changes must update redundant names inside all SKU JSON property entries.

### Property value rules

- Creating a property value whose `(propertyId, name)` already exists returns the existing id.
- Updating a property value to another value's name under the same property throws `PROPERTY_VALUE_EXISTS`.
- Property value name changes must update redundant names inside all SKU JSON property entries.

### Comment rules

- Comment creation through RPC validates SKU by `getSku(skuId, true)` and SPU by `getSpu(spuId, true)`, meaning deleted SKU/SPU rows are still accepted if found by include-deleted mapper methods.
- RPC comment creation checks one comment per `(userId, orderItemId)` and throws `COMMENT_ORDER_EXISTS` on duplicates.
- RPC comment creation loads `MemberUserApi#getUser(userId).getCheckedData()` before converting and inserting.
- Visible update and reply require existing comment; missing comment throws `COMMENT_NOT_EXISTS`.

### Favorite rules

- Favorite creation checks `(userId, spuId)`; existing favorite throws `FAVORITE_EXISTS`.
- Favorite deletion checks `(userId, spuId)`; missing favorite throws `FAVORITE_NOT_EXISTS`.

### Browse history rules

- Null `userId` means no browse history is recorded.
- Same user and SPU keeps only the latest record by deleting the old one before insert.
- Each user keeps at most `USER_STORE_MAXIMUM = 100` records; when at or over the limit, delete the oldest record before insert.
- User hiding browse history sets `userDeleted=true` for the selected user/SPU rows.

## Error Code Contract

All thrown errors must reuse `develop-module-mall/develop-module-product-api/src/main/java/com/develop/mvp/pk/module/product/enums/ErrorCodeConstants.java`.

| Constant | Code | Message | Required parameter behavior |
|---|---:|---|---|
| `CATEGORY_NOT_EXISTS` | `1_008_001_000` | 商品分类不存在 | no parameter |
| `CATEGORY_PARENT_NOT_EXISTS` | `1_008_001_001` | 父分类不存在 | no parameter |
| `CATEGORY_PARENT_NOT_FIRST_LEVEL` | `1_008_001_002` | 父分类不能是二级分类 | no parameter |
| `CATEGORY_EXISTS_CHILDREN` | `1_008_001_003` | 存在子分类，无法删除 | no parameter |
| `CATEGORY_DISABLED` | `1_008_001_004` | 商品分类({})已禁用，无法使用 | pass category name |
| `CATEGORY_HAVE_BIND_SPU` | `1_008_001_005` | 类别下存在商品，无法删除 | no parameter |
| `BRAND_NOT_EXISTS` | `1_008_002_000` | 品牌不存在 | no parameter |
| `BRAND_DISABLED` | `1_008_002_001` | 品牌已禁用 | no parameter |
| `BRAND_NAME_EXISTS` | `1_008_002_002` | 品牌名称已存在 | no parameter |
| `PROPERTY_NOT_EXISTS` | `1_008_003_000` | 属性项不存在 | no parameter |
| `PROPERTY_EXISTS` | `1_008_003_001` | 属性项的名称已存在 | no parameter |
| `PROPERTY_DELETE_FAIL_VALUE_EXISTS` | `1_008_003_002` | 属性项下存在属性值，无法删除 | no parameter |
| `PROPERTY_VALUE_NOT_EXISTS` | `1_008_004_000` | 属性值不存在 | no parameter |
| `PROPERTY_VALUE_EXISTS` | `1_008_004_001` | 属性值的名称已存在 | no parameter |
| `SPU_NOT_EXISTS` | `1_008_005_000` | 商品 SPU 不存在 | no parameter |
| `SPU_SAVE_FAIL_CATEGORY_LEVEL_ERROR` | `1_008_005_001` | 商品分类不正确，原因：必须使用第二级的商品分类及以下 | no parameter |
| `SPU_SAVE_FAIL_COUPON_TEMPLATE_NOT_EXISTS` | `1_008_005_002` | 商品 SPU 保存失败，原因：优惠劵不存在 | current product services do not throw this during normal SPU save |
| `SPU_NOT_ENABLE` | `1_008_005_003` | 商品 SPU【{}】不处于上架状态 | pass SPU name |
| `SPU_NOT_RECYCLE` | `1_008_005_004` | 商品 SPU 不处于回收站状态 | no parameter |
| `SKU_NOT_EXISTS` | `1_008_006_000` | 商品 SKU 不存在 | no parameter |
| `SKU_PROPERTIES_DUPLICATED` | `1_008_006_001` | 商品 SKU 的属性组合存在重复 | no parameter |
| `SPU_ATTR_NUMBERS_MUST_BE_EQUALS` | `1_008_006_002` | 一个 SPU 下的每个 SKU，其属性项必须一致 | no parameter |
| `SPU_SKU_NOT_DUPLICATE` | `1_008_006_003` | 一个 SPU 下的每个 SKU，必须不重复 | no parameter |
| `SKU_STOCK_NOT_ENOUGH` | `1_008_006_004` | 商品 SKU 库存不足 | no parameter |
| `COMMENT_NOT_EXISTS` | `1_008_007_000` | 商品评价不存在 | no parameter |
| `COMMENT_ORDER_EXISTS` | `1_008_007_001` | 订单的商品评价已存在 | no parameter |
| `FAVORITE_EXISTS` | `1_008_008_000` | 该商品已经被收藏 | no parameter |
| `FAVORITE_NOT_EXISTS` | `1_008_008_001` | 商品收藏不存在 | no parameter |

Do not replace `ServiceExceptionUtil.exception(...)` with plain Java exceptions in application or infrastructure paths that currently surface framework error codes.

## Transaction Contract

- `ProductSpuServiceImpl#createSpu`, `updateSpu`, `deleteSpu`, `updateSpuStock`, and `updateSpuStatus` use `@Transactional(rollbackFor = Exception.class)`.
- `ProductSkuServiceImpl#updateSkuList` and `updateSkuStock` use `@Transactional(rollbackFor = Exception.class)`.
- `ProductPropertyServiceImpl#createProperty` and `updateProperty` use `@Transactional(rollbackFor = Exception.class)`.
- DDD application services must use `@Transactional(rollbackFor = Exception.class)` on equivalent write use cases when migrated; plain `@Transactional` is not parity with this contract.
- Stock updates must keep SKU update and SPU stock aggregation in the same transaction.
- SPU create/update must keep SPU and SKU writes in the same transaction.
- Comment/favorite/history writes are currently non-transactional single-aggregate operations; add transactions only when introducing multiple writes that must roll back together.

## Integration Contract

- Product server currently scans Feign clients in `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/framework/rpc/config/RpcConfiguration.java`; the actual class is `RpcConfiguration` with configuration bean name `productRpcConfiguration`, and it scans `MemberUserRemoteClient` plus `MemberLevelRemoteClient`.
- Product comment creation depends on stable `MemberUserApi`, not a remote client directly.
- Product APIs are consumed by trade and promotion modules for product validation, stock updates, SKU/SPU/category reads, and comment creation.
- API local/remote split must keep business callers injecting stable `ProductCategoryApi`, `ProductSkuApi`, `ProductSpuApi`, or `ProductCommentApi` while moving Feign identity to `remote/*RemoteClient`.
- Product server local implementations must implement the stable API contracts and must not depend on remote product clients.
- Do not move product DTOs, DOs, VOs, mappers, message objects, or domain objects during local/remote split unless explicitly included in the migration task.

## Mapping Rules

- Controller VO mapping can stay in existing `convert/*` during incremental migration.
- Domain objects must not import Controller VO, DO, Mapper, Feign, Spring, MyBatis, or framework RPC classes.
- Infrastructure repository implementations may import Mapper and DO, but should not construct Controller VO for mapper paging if a domain query object can be mapped locally.
- ProductSku JSON property mapping must preserve `propertyId`, `propertyName`, `valueId`, and `valueName` exactly.
- ProductSpu `sliderPicUrls` and ProductComment `picUrls` must remain JSON-list fields.
- ProductSpu `deliveryTypes` must remain `IntegerListTypeHandler` compatible.
- API DTO field sets must remain stable: `ProductSkuRespDTO`, `ProductSpuRespDTO`, `ProductCommentCreateReqDTO`, `ProductSkuUpdateStockReqDTO`, and `ProductPropertyValueDetailRespDTO` are external contracts.

## Current Conflict Notes

These are known current-code conflicts that must be resolved before replacing legacy services:

1. `ProductCategoryApplicationService#createCategory`, `ProductBrandApplicationService#createBrand`, and `ProductSpuApplicationService#createSpu` accept `id` and pass it into factories. Legacy create paths create DOs with null ids and return database-generated ids.
2. `ProductSpuRepositoryImpl#save` checks `productSpuMapper.selectById(spu.id().value())`; this fails for database-generated create flows if the domain id is null.
3. Current partial DDD application services use plain `@Transactional`, while legacy multi-write methods use `rollbackFor = Exception.class`.
4. `ProductCategoryApplicationService#deleteCategory` checks child categories but does not check bound SPU count, while legacy `deleteCategory` throws `CATEGORY_HAVE_BIND_SPU` if bound SPUs exist.
5. `ProductCategoryApplicationService#createCategory` validates only parent existence, not the legacy `CATEGORY_PARENT_NOT_FIRST_LEVEL` rule.
6. `ProductSpuApplicationService#createSpu` does not call category, brand, or SKU validation equivalent to legacy `ProductSpuServiceImpl`.
7. `ProductSpuApplicationService#updateSpuStatus` ignores recycle status in the generic status update path unless `recycleSpu` is called separately; legacy status update sets the requested status directly after existence validation.
8. `ProductSpuRepositoryImpl#getTabsCount` omits alert-stock count and hard-codes tab keys; legacy `getTabsCount` uses `ProductSpuPageReqVO` constants and includes five counts.
9. `ProductSpuRepositoryImpl#findPage` constructs `ProductSpuPageReqVO` in infrastructure; this is an accepted temporary migration bridge but not the final DDD boundary.
10. ProductComment, ProductFavorite, ProductBrowseHistory, ProductProperty, and ProductPropertyValue are not yet fully represented as DDD aggregates/application services in current code.
11. Product API interfaces still include `@FeignClient`; this violates the target local/remote split but is current external behavior until that migration runs.
12. Current `ProductCategoryApi` OpenAPI summary says `校验部门是否合法`; do not silently change it during DDD refactoring.

## Acceptance Criteria

- Public API signatures and DTO field names remain unchanged unless an explicit API migration says otherwise.
- Product server compiles after each aggregate batch.
- Legacy service behavior listed in this skill is covered by migrated application/domain/infrastructure code before callers switch from legacy service to application service.
- Category deletion still rejects child categories and bound SPUs.
- Brand uniqueness behavior is preserved.
- SPU create/update still validates category, brand, and SKU list and still derives SPU price/marketPrice/costPrice/stock from SKUs.
- SKU stock decrement remains affected-row guarded and throws `SKU_STOCK_NOT_ENOUGH` on insufficient stock.
- SPU stock/sales counters remain synchronized with SKU stock updates.
- Property/property-value name changes still update redundant SKU JSON property names.
- Comment creation still validates include-deleted SKU/SPU rows, member user data, and `(userId, orderItemId)` uniqueness.
- Favorite uniqueness and missing-delete errors are preserved.
- Browse history still ignores anonymous users, deduplicates by user/SPU, and caps each user at 100 records.
- Domain layer remains pure Java and imports no Spring, MyBatis, Mapper, DO, Controller VO, Feign, Redis, or framework RPC classes.

## Verification Commands

Run document checks after editing this skill:

```bash
git diff --check -- .claude/ddd-skills/AggregateRoot_MallProduct_Skill.md
grep -n "^## " .claude/ddd-skills/AggregateRoot_MallProduct_Skill.md
```

Run code checks after Product API or Product server changes:

```bash
mvn compile -pl develop-module-mall/develop-module-product-api -am -DskipTests
mvn compile -pl develop-module-mall/develop-module-product-server -am -DskipTests
```

For stock behavior changes, add or run targeted tests that prove:

- Decrement with enough stock updates SKU and SPU stock/sales.
- Decrement with insufficient stock throws `SKU_STOCK_NOT_ENOUGH` and rolls back.
- Increment reverses stock/sales counters consistently.

## Quick Reference

| Area | Preserve |
|---|---|
| Category | root `0L`, max parent depth, bind-SPU delete guard, level >= 2 for SPU |
| Brand | name uniqueness, disabled-brand validation |
| SPU | category/brand/SKU validation, derived price/stock, recycle-only delete |
| SKU | single-spec default property, multi-spec duplicate checks, stock affected-row guard |
| Property | duplicate create returns existing id, rename updates SKU JSON names |
| Comment | include-deleted SKU/SPU lookup, member user lookup, order-item uniqueness |
| Favorite | `(userId, spuId)` uniqueness |
| History | null user ignored, user/SPU dedup, max 100 records |
| API split | stable API injected by callers, Feign only in remote client after migration |

## Common Mistakes

- Treating the previous draft's ideal SPU state machine as implemented legacy behavior.
- Removing `@FeignClient` from stable API without adding and scanning `remote/*RemoteClient`.
- Changing `ProductCategoryApi` documentation text while doing unrelated DDD migration.
- Returning unordered SPU lists from `getSpuList(Collection<Long> ids)`.
- Replacing affected-row stock decrement with in-memory validation.
- Forgetting to update SPU stock after SKU stock update.
- Forgetting to update redundant SKU property/property-value names.
- Making comment creation reject deleted SKU/SPU rows when legacy include-deleted lookup accepts them.
- Creating DDD aggregate ids before insert in a way that breaks database-generated ids.
- Adding domain imports of DO, Mapper, Controller VO, Spring, Feign, or MyBatis.

## Rationalization Table

| Excuse | Reality |
|---|---|
| "The DDD aggregate already models this better." | Legacy behavior is the public contract until parity is proven. |
| "Feign annotations are wrong, so remove them now." | Local/remote split requires stable API plus remote client migration and consumer compile checks. |
| "Stock can be validated before update." | Current anti-oversell behavior is the affected-row guarded SQL update. |
| "Comment should not use deleted SKU/SPU." | Current behavior explicitly uses include-deleted lookup. Changing it is a product decision. |
| "The OpenAPI summary typo is harmless to fix." | Public API docs are an external contract; fix only in an API-doc migration. |
| "Application service can throw IllegalStateException." | Existing callers expect framework error codes from `ServiceExceptionUtil.exception(...)`. |

## Red Flags

Stop the current refactor if any of these happen:

- Controller path, HTTP method, permission, DTO field, response wrapper, or pagination semantics would change.
- API compile requires moving DTOs or renaming stable `Product*Api` interfaces.
- Domain code needs Mapper/DO/VO/Feign/Spring imports to make progress.
- Stock behavior cannot preserve affected-row guard and SPU stock synchronization in one transaction.
- Category deletion no longer checks bound SPUs.
- Product comment behavior would change from include-deleted SKU/SPU lookup to normal lookup without explicit approval.
- Compile errors spread beyond Product module and direct API consumers.

## Rollback Conditions

Rollback the current Product batch if:

- `develop-module-product-api` or `develop-module-product-server` cannot compile after scoped fixes.
- Any public Product API signature or DTO field changes unintentionally.
- Stock decrement no longer prevents oversell by SQL affected-row guard.
- SPU create/update can persist SPU without corresponding SKU parity.
- Category/brand/SPU/comment/favorite/history error-code behavior changes without tests and explicit approval.
- A change requires deleting unrelated user modifications or broad rewrites outside the current aggregate.

## AI Self-Check

Before reporting a Product refactor complete, answer yes to every item:

- Did I read this skill plus both global standards before modifying code?
- Did I identify the exact Product aggregate or API slice for this batch?
- Did I compare migrated behavior against the listed legacy service methods?
- Did I preserve public API signatures, DTO fields, Controller behavior, and error codes?
- Did I keep domain pure and infrastructure as the only Mapper/DO adapter layer?
- Did I run the relevant Maven compile command fresh and read the result?
- Did I document any unresolved conflict rather than silently broadening scope?
