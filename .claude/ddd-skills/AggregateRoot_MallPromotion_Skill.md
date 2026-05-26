---
name: aggregate-root-mall-promotion-skill
description: Use when modifying or reviewing Mall Promotion banner, seckill, coupon, discount, reward, combination, bargain, or point activity boundaries.
type: ddd-aggregate-skill
status: production-review
---

# AggregateRoot Mall Promotion Skill

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

## Quick Reference

| 要做什么 | 正确位置 | 禁止位置 |
|---|---|---|
| 业务不变量 | `domain/{aggregate}` | `controller`、`convert`、`dal` |
| 用例编排和事务 | `application/{aggregate}/service` | `domain` 或 Controller |
| 入站用例契约 | `application/{aggregate}/port/inbound` | Controller 私有方法 |
| 外部能力端口 | `application/{aggregate}/port/outbound` | domain 或 infrastructure 反向定义 |
| 仓储接口 | `domain/{aggregate}/repository` | infrastructure 反向定义业务端口 |
| Mapper/DO 适配 | `infrastructure/{aggregate}/persistence` | domain/application 直接调用 |
| 对象转换 | `convert` | domain 聚合内 |

## AI Self-Check

- 已读取当前事实源，而不是只依据本 skill 猜测。
- 未改变 Controller/API/VO/DTO/权限/租户/数据权限/错误码/分页/Excel/MQ/Job/缓存/回调契约。
- domain 未依赖 Spring、MyBatis、Feign、Mapper、DO、VO、DTO 或基础设施实现。
- 标准目录、入站端口、出站端口、领域仓储和 infrastructure 适配边界没有因“当前为空”被省略。
- 已运行本文件列出的验证命令，或明确记录无法验证的原因。

## 1. Overview

本 skill 用于把 Mall Promotion 从当前“少量 DDD + 大量 legacy service/dal”的混合状态，按当前可编译外部行为逐步迁移到 `domain/application/infrastructure/convert` 分层；任何重构都必须先保留现有 Controller、API、错误码、事务、库存、Job、MQ 和跨模块集成语义。

## 2. When to Use

使用本 skill：

- 重构或验证 `develop-module-mall/develop-module-promotion-*` 的 Banner、秒杀、优惠券、限时折扣、满减送、拼团、砍价、积分商城链路。
- 拆分 promotion API 的 stable contract 与 remote Feign client。
- 从 `service/dal` 迁移营销活动、库存扣减、优惠券状态机、拼团记录、砍价记录等核心规则。
- 修改 promotion Job、MQ、WebSocket、Product/Trade/Member/System API 集成。
- 判断当前 DDD 代码是否能替代 legacy service 行为。

不要使用本 skill：

- 只改文章、装修、客服等非营销核心功能，除非变更影响 promotion 公共错误码、RPC 配置或 Job/MQ 装配。
- 只修改页面文案、SQL 初始化数据、测试夹具或无结构影响的配置。
- 未读取当前事实源就直接按旧草稿新增聚合、值对象或仓储。

## 3. Baseline Failure Findings

当前旧草稿的失败点：

- 没有 YAML frontmatter，不能被稳定识别为生产级 skill。
- 只写概念聚合，缺少 Controller、VO/DTO、DO、Mapper、Convert、Service、Application、Repository、ErrorCode、测试路径。
- 没有记录当前 API 仍带 `@FeignClient`、`SeckillActivityApi` 前缀复用 `/discount-activity`、`PointActivityApi` tag 误写“秒杀活动”等现状冲突。
- 没有固定字段模型，容易猜错 `stock/totalStock`、`configIds`、`productScopeValues`、`rules`、`HEAD_ID_GROUP` 等字段语义。
- 没有事务、Job、MQ、WebSocket 和 Product/Trade 集成契约，容易把 durable 业务流程误改成领域事件或空转发。
- 没有区分“当前 legacy service 是生产事实源”和“DDD 目标结构”，容易直接用不完整 application service 替代现有行为。

## 4. Reproducibility Contract

1. 每次只处理一个聚合或一个小链路：Banner、Seckill、Coupon、Discount、Reward、Combination、Bargain、Point 中任选其一。
2. 修改代码前必须读取本 skill、`.claude/ddd-skills/DDD_Skill_Production_Readiness_Standard.md`、`.claude/ddd-skills/Module_Structure_Standard.md` 和当前目标链路事实源。
3. 当前可编译代码的外部行为优先于本文档和草稿假设；冲突时先修订 skill，再改代码。
4. Controller 路径、HTTP 方法、权限、请求/响应 VO、API DTO、错误码、错误参数、分页、Excel、Job、MQ、库存 SQL、跨模块 API 不得在 DDD 重构中顺手改变。
5. 当前 `service/*ServiceImpl` 是多数 promotion 行为的事实源；只有对应 application/domain/infrastructure 已覆盖同等行为和测试后，才允许迁移调用方。
6. Domain 不得依赖 Spring、MyBatis、Feign、Mapper、DO、Controller VO、WebSocketSenderApi、TradeOrderApi、Product*Api、MemberUserRemoteClient 或 System remote client。
7. 库存扣减必须保留 Mapper 层 guarded update/affected-row 语义，禁止改成“先查库存再保存”。
8. 涉及 Java 代码必须执行影响范围 Maven compile/test；只改 skill 文档时至少执行文档 diff/heading 验证。

## 5. Current Source Anchors

### API module

- API constants/enums：
  - `develop-module-mall/develop-module-promotion-api/src/main/java/com/develop/mvp/pk/module/promotion/enums/ApiConstants.java`
  - `develop-module-mall/develop-module-promotion-api/src/main/java/com/develop/mvp/pk/module/promotion/enums/ErrorCodeConstants.java`
  - `develop-module-mall/develop-module-promotion-api/src/main/java/com/develop/mvp/pk/module/promotion/enums/MessageTemplateConstants.java`
  - `develop-module-mall/develop-module-promotion-api/src/main/java/com/develop/mvp/pk/module/promotion/enums/WebSocketMessageTypeConstants.java`
- Trade-facing APIs：
  - `develop-module-mall/develop-module-promotion-api/src/main/java/com/develop/mvp/pk/module/promotion/api/seckill/SeckillActivityApi.java`
  - `develop-module-mall/develop-module-promotion-api/src/main/java/com/develop/mvp/pk/module/promotion/api/coupon/CouponApi.java`
  - `develop-module-mall/develop-module-promotion-api/src/main/java/com/develop/mvp/pk/module/promotion/api/discount/DiscountActivityApi.java`
  - `develop-module-mall/develop-module-promotion-api/src/main/java/com/develop/mvp/pk/module/promotion/api/reward/RewardActivityApi.java`
  - `develop-module-mall/develop-module-promotion-api/src/main/java/com/develop/mvp/pk/module/promotion/api/combination/CombinationRecordApi.java`
  - `develop-module-mall/develop-module-promotion-api/src/main/java/com/develop/mvp/pk/module/promotion/api/bargain/BargainActivityApi.java`
  - `develop-module-mall/develop-module-promotion-api/src/main/java/com/develop/mvp/pk/module/promotion/api/bargain/BargainRecordApi.java`
  - `develop-module-mall/develop-module-promotion-api/src/main/java/com/develop/mvp/pk/module/promotion/api/point/PointActivityApi.java`
- API DTO：
  - `develop-module-mall/develop-module-promotion-api/src/main/java/com/develop/mvp/pk/module/promotion/api/seckill/dto/SeckillValidateJoinRespDTO.java`
  - `develop-module-mall/develop-module-promotion-api/src/main/java/com/develop/mvp/pk/module/promotion/api/coupon/dto/CouponRespDTO.java`
  - `develop-module-mall/develop-module-promotion-api/src/main/java/com/develop/mvp/pk/module/promotion/api/coupon/dto/CouponTemplateRespDTO.java`
  - `develop-module-mall/develop-module-promotion-api/src/main/java/com/develop/mvp/pk/module/promotion/api/coupon/dto/CouponUseReqDTO.java`
  - `develop-module-mall/develop-module-promotion-api/src/main/java/com/develop/mvp/pk/module/promotion/api/coupon/dto/CouponValidReqDTO.java`
  - `develop-module-mall/develop-module-promotion-api/src/main/java/com/develop/mvp/pk/module/promotion/api/discount/dto/DiscountProductRespDTO.java`
  - `develop-module-mall/develop-module-promotion-api/src/main/java/com/develop/mvp/pk/module/promotion/api/reward/dto/RewardActivityMatchRespDTO.java`
  - `develop-module-mall/develop-module-promotion-api/src/main/java/com/develop/mvp/pk/module/promotion/api/combination/dto/CombinationRecordCreateReqDTO.java`
  - `develop-module-mall/develop-module-promotion-api/src/main/java/com/develop/mvp/pk/module/promotion/api/combination/dto/CombinationRecordCreateRespDTO.java`
  - `develop-module-mall/develop-module-promotion-api/src/main/java/com/develop/mvp/pk/module/promotion/api/combination/dto/CombinationRecordRespDTO.java`
  - `develop-module-mall/develop-module-promotion-api/src/main/java/com/develop/mvp/pk/module/promotion/api/combination/dto/CombinationValidateJoinRespDTO.java`
  - `develop-module-mall/develop-module-promotion-api/src/main/java/com/develop/mvp/pk/module/promotion/api/bargain/dto/BargainValidateJoinRespDTO.java`
  - `develop-module-mall/develop-module-promotion-api/src/main/java/com/develop/mvp/pk/module/promotion/api/point/dto/PointValidateJoinRespDTO.java`

### Server entry points

- Admin controllers：
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/controller/admin/banner/BannerController.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/controller/admin/seckill/SeckillActivityController.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/controller/admin/seckill/SeckillConfigController.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/controller/admin/coupon/CouponTemplateController.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/controller/admin/coupon/CouponController.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/controller/admin/discount/DiscountActivityController.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/controller/admin/reward/RewardActivityController.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/controller/admin/combination/CombinationActivityController.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/controller/admin/combination/CombinationRecordController.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/controller/admin/bargain/BargainActivityController.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/controller/admin/bargain/BargainRecordController.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/controller/admin/bargain/BargainHelpController.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/controller/admin/point/PointActivityController.java`
- API implementations：
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/api/seckill/SeckillActivityApiImpl.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/api/coupon/CouponApiImpl.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/api/discount/DiscountActivityApiImpl.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/api/reward/RewardActivityApiImpl.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/api/combination/CombinationRecordApiImpl.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/api/bargain/BargainActivityApiImpl.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/api/bargain/BargainRecordApiImpl.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/api/point/PointActivityApiImpl.java`

### Current DDD layer

- Existing domain/application/infrastructure only partially cover Banner、CouponTemplate、SeckillActivity：
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/domain/banner/Banner.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/domain/banner/BannerFactory.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/domain/banner/repository/BannerRepository.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/domain/banner/valueobject/BannerId.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/application/banner/BannerApplicationService.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/infrastructure/banner/BannerRepositoryImpl.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/domain/coupon/CouponTemplate.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/domain/coupon/CouponTemplateFactory.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/domain/coupon/repository/CouponTemplateRepository.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/domain/coupon/valueobject/CouponTemplateId.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/domain/coupon/valueobject/CouponId.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/application/coupon/CouponTemplateApplicationService.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/infrastructure/coupon/CouponTemplateRepositoryImpl.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/domain/seckill/SeckillActivity.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/domain/seckill/SeckillActivityFactory.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/domain/seckill/valueobject/SeckillActivityId.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/domain/seckill/valueobject/SeckillProduct.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/domain/seckill/event/SeckillActivityStatusChangedEvent.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/domain/seckill/repository/SeckillActivityRepository.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/application/seckill/SeckillActivityApplicationService.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/infrastructure/seckill/SeckillActivityRepositoryImpl.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/domain/event/DomainEvent.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/domain/event/DomainEventPublisher.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/infrastructure/event/SpringDomainEventPublisher.java`

### Legacy service facts

- `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/service/banner/BannerServiceImpl.java`
- `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/service/seckill/SeckillConfigServiceImpl.java`
- `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/service/seckill/SeckillActivityServiceImpl.java`
- `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/service/coupon/CouponTemplateServiceImpl.java`
- `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/service/coupon/CouponServiceImpl.java`
- `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/service/discount/DiscountActivityServiceImpl.java`
- `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/service/reward/RewardActivityServiceImpl.java`
- `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/service/combination/CombinationActivityServiceImpl.java`
- `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/service/combination/CombinationRecordServiceImpl.java`
- `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/service/bargain/BargainActivityServiceImpl.java`
- `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/service/bargain/BargainRecordServiceImpl.java`
- `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/service/bargain/BargainHelpServiceImpl.java`
- `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/service/point/PointActivityServiceImpl.java`

### Persistence facts

- DO：
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/dataobject/banner/BannerDO.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/dataobject/seckill/SeckillActivityDO.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/dataobject/seckill/SeckillProductDO.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/dataobject/seckill/SeckillConfigDO.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/dataobject/coupon/CouponTemplateDO.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/dataobject/coupon/CouponDO.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/dataobject/discount/DiscountActivityDO.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/dataobject/discount/DiscountProductDO.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/dataobject/reward/RewardActivityDO.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/dataobject/combination/CombinationActivityDO.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/dataobject/combination/CombinationProductDO.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/dataobject/combination/CombinationRecordDO.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/dataobject/bargain/BargainActivityDO.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/dataobject/bargain/BargainRecordDO.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/dataobject/bargain/BargainHelpDO.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/dataobject/point/PointActivityDO.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/dataobject/point/PointProductDO.java`
- Mapper：
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/mysql/banner/BannerMapper.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/mysql/seckill/SeckillActivityMapper.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/mysql/seckill/SeckillProductMapper.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/mysql/seckill/SeckillConfigMapper.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/mysql/coupon/CouponTemplateMapper.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/mysql/coupon/CouponMapper.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/mysql/discount/DiscountActivityMapper.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/mysql/discount/DiscountProductMapper.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/mysql/reward/RewardActivityMapper.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/mysql/combination/CombinationActivityMapper.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/mysql/combination/CombinationProductMapper.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/mysql/combination/CombinationRecordMapper.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/mysql/bargain/BargainActivityMapper.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/mysql/bargain/BargainRecordMapper.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/mysql/bargain/BargainHelpMapper.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/mysql/point/PointActivityMapper.java`
  - `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/dal/mysql/point/PointProductMapper.java`
- Convert：`develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/convert/` 下的 Banner、Seckill、Coupon、Discount、Reward、Combination、Bargain、Point 转换类；迁移前必须逐个读取目标转换类当前方法。

### Jobs, MQ and RPC

- `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/job/coupon/CouponExpireJob.java` calls `CouponService#expireCoupon()` and has tenant/job semantics.
- `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/job/combination/CombinationRecordExpireJob.java` calls `CombinationRecordService#expireCombinationRecord()` and has tenant/job semantics.
- `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/mq/consumer/coupon/CouponTakeByRegisterConsumer.java` handles registration coupon grants.
- `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/framework/rpc/config/RpcConfiguration.java` scans Product APIs, TradeOrderApi, WebSocketSenderApi, Member/System remote clients.

### Tests

- `develop-module-mall/develop-module-promotion-server/src/test/java/com/develop/mvp/pk/module/promotion/domain/banner/BannerTest.java`
- `develop-module-mall/develop-module-promotion-server/src/test/java/com/develop/mvp/pk/module/promotion/domain/coupon/CouponTemplateTest.java`
- `develop-module-mall/develop-module-promotion-server/src/test/java/com/develop/mvp/pk/module/promotion/domain/seckill/SeckillActivityTest.java`
- `develop-module-mall/develop-module-promotion-server/src/test/java/com/develop/mvp/pk/module/promotion/application/banner/BannerApplicationServiceTest.java`
- `develop-module-mall/develop-module-promotion-server/src/test/java/com/develop/mvp/pk/module/promotion/application/coupon/CouponTemplateApplicationServiceTest.java`
- `develop-module-mall/develop-module-promotion-server/src/test/java/com/develop/mvp/pk/module/promotion/application/seckill/SeckillActivityApplicationServiceTest.java`
- `develop-module-mall/develop-module-promotion-server/src/test/java/com/develop/mvp/pk/module/promotion/infrastructure/event/SpringDomainEventPublisherTest.java`
- `develop-module-mall/develop-module-promotion-server/src/test/java/com/develop/mvp/pk/module/promotion/infrastructure/seckill/SeckillActivityRepositoryImplTest.java`
- `develop-module-mall/develop-module-promotion-server/src/test/resources/application-unit-test.yaml`
- `develop-module-mall/develop-module-promotion-server/src/test/resources/sql/clean.sql`
- `develop-module-mall/develop-module-promotion-server/src/test/resources/sql/create_tables.sql`

## 6. Fixed Data Model

### Shared activity fields

| Concept | Current DO fields | Type / nullable / default | Meaning | Mapping rule |
|---|---|---|---|---|
| Activity id | `id` | `Long`; nullable before insert, non-null after persistence | DB primary key | Domain value object wraps id; create may receive null until persistence assigns id |
| Status | `status` | `Integer`; non-null in persisted activities; current default comes from create service/VO | `CommonStatusEnum` / `PromotionActivityStatusEnum` depending chain | Domain exposes intent methods like close/enable; API keeps integer enum |
| Time window | `startTime/endTime` | `LocalDateTime`; non-null for activities that can join/order | Participation window | Domain validates start before end, application compares now for join |
| Product identity | `spuId/skuId` | `Long`; non-null for product-bound activities/products | Product module ids | Product APIs validate existence outside domain |
| Stock | `stock/totalStock` | `Integer`; non-null, defaults from create request/product sum; must never go below 0 | Remaining and total activity/product stock | Infrastructure preserves Mapper guarded updates |
| Scope | `productScope/productScopeValues` | `Integer` + `List<Long>`; values can be empty/null only when scope is ALL by current semantics | `PromotionProductScopeEnum` and selected ids | Domain value object may wrap scope; converter handles LongList type handler |
| Config ids | `configIds` | `List<Long>` with `LongListTypeHandler`; non-empty for Seckill activity/product | Seckill time slot ids | Conflict checks compare intersections; do not flatten to comma string |
| Reward rules | `rules` | `List<RewardActivityDO.Rule>` with `JacksonTypeHandler`; non-empty for Reward | Threshold and benefit JSON | Preserve nested fields and coupon grants explicitly |
| Group head marker | `CombinationRecordDO.HEAD_ID_GROUP` | constant `Long 0L`; never null | Marks group head record | Do not replace with null or self id |

### Aggregate-specific model

| Aggregate | DO / DTO fields to preserve | Notes |
|---|---|---|
| Banner | `BannerDO.id/title/picUrl/status/sort/position/url/memo` and controller VO fields | Existing DDD covers Banner; legacy service remains behavior reference for controller parity |
| SeckillActivity | `SeckillActivityDO.id/spuId/name/status/remark/startTime/endTime/sort/configIds/totalLimitCount/singleLimitCount/stock/totalStock` | `configIds` uses `LongListTypeHandler`; stock must update activity and product tables |
| SeckillProduct | `SeckillProductDO.id/activityId/configIds/spuId/skuId/seckillPrice/stock/activityStatus/activityStartTime/activityEndTime` | Product rows duplicate activity time/status for query performance; preserve mapping |
| SeckillConfig | `SeckillConfigDO` fields define seckill time slots and enabled/disabled state | Validate config existence/status/time before seckill join or conflict decisions |
| CouponTemplate | `id/name/description/status/totalCount/takeLimitCount/takeType/usePrice/productScope/productScopeValues/validityType/validStartTime/validEndTime/fixedStartTerm/fixedEndTerm/discountType/discountPercent/discountPrice/discountLimitPrice/takeCount/useCount` | `TAKE_LIMIT_COUNT_MAX=-1` and `TOTAL_COUNT_MAX=-1` mean unlimited |
| Coupon | `id/templateId/name/status/userId/takeType/usePrice/validStartTime/validEndTime/productScope/productScopeValues/discountType/discountPercent/discountPrice/discountLimitPrice/useOrderId/useTime` | Status machine is UNUSED → USED/EXPIRE and USED → UNUSED/EXPIRE for return |
| DiscountActivity | `DiscountActivityDO` plus `DiscountProductDO` product rows | Same SPU cannot overlap active discount activities |
| RewardActivity | `id/name/status/startTime/endTime/remark/conditionType/productScope/productScopeValues/rules` | `rules` is Jackson JSON; `Rule.giveCouponTemplateCounts` grants coupons after payment |
| CombinationActivity | Activity fields plus `CombinationProductDO`, limits, virtual group config | Order/payment callbacks and expiry job define record lifecycle |
| CombinationRecord | `id/activityId/combinationPrice/spuId/spuName/picUrl/skuId/count/userId/nickname/avatar/headId/status/orderId/userSize/userCount/virtualGroup/expireTime/startTime/endTime` | `HEAD_ID_GROUP=0L` marks group head; status must align with `CombinationRecordStatusEnum` |
| BargainActivity | Activity product/price/stock/help constraints | Stock update uses `BargainActivityMapper#updateStock` affected rows |
| BargainRecord | User participation, price progress, order binding and status | Join/order validation is in `BargainRecordServiceImpl` |
| BargainHelp | Helper user, record id, reduced price | Prevent self-help, repeated help and over-limit help |
| PointActivity | Activity and `PointProductDO` rows with points/cash price/stock/count | `count` is single-purchase limit; stock update affects product and activity tables |

## 7. Required Method Signatures and Capabilities

Keep stable API signatures until a separate API migration plan exists:

- `SeckillActivityApi#updateSeckillStockDecr(Long id, Long skuId, Integer count)`
- `SeckillActivityApi#updateSeckillStockIncr(Long id, Long skuId, Integer count)`
- `SeckillActivityApi#validateJoinSeckill(Long activityId, Long skuId, Integer count)`
- `CouponApi#getCouponListByUserId(Long userId, Integer status)`
- `CouponApi#useCoupon(CouponUseReqDTO useReqDTO)`
- `CouponApi#returnUsedCoupon(Long id)`
- `CouponApi#takeCouponsByAdmin(Map<Long, Integer> giveCoupons, Long userId)`
- `CouponApi#invalidateCouponsByAdmin(List<Long> giveCouponIds, Long userId)`
- `DiscountActivityApi#getMatchDiscountProductListBySkuIds(Collection<Long> skuIds)` returns `CommonResult<List<DiscountProductRespDTO>>`
- `RewardActivityApi#getMatchRewardActivityListBySpuIds(Collection<Long> spuIds)` returns `CommonResult<List<RewardActivityMatchRespDTO>>`
- `CombinationRecordApi#validateCombinationRecord(Long userId, Long activityId, Long headId, Long skuId, Integer count)` returns `CommonResult<Boolean>`
- `CombinationRecordApi#createCombinationRecord(CombinationRecordCreateReqDTO reqDTO)` returns `CommonResult<CombinationRecordCreateRespDTO>`
- `CombinationRecordApi#getCombinationRecordByOrderId(Long userId, Long orderId)` returns `CommonResult<CombinationRecordRespDTO>`
- `CombinationRecordApi#validateJoinCombination(Long userId, Long activityId, Long headId, Long skuId, Integer count)` returns `CommonResult<CombinationValidateJoinRespDTO>`; `headId` request parameter is optional
- `BargainActivityApi#updateBargainActivityStock(Long id, Integer count)` returns `CommonResult<Boolean>`
- `BargainRecordApi#validateJoinBargain(Long userId, Long bargainRecordId, Long skuId)` returns `CommonResult<BargainValidateJoinRespDTO>`
- `BargainRecordApi#updateBargainRecordOrderId(Long id, Long orderId)` returns `CommonResult<Boolean>`; current request parameter name is misspelled as `@RequestParam("oderId")` and must be treated as existing external behavior until an API migration fixes it
- `PointActivityApi#validateJoinPointActivity(Long activityId, Long skuId, Integer count)`
- `PointActivityApi#updatePointStockDecr(Long id, Long skuId, Integer count)`
- `PointActivityApi#updatePointStockIncr(Long id, Long skuId, Integer count)`

Target DDD capabilities, not mandatory class names:

- Aggregate methods express rules: create/update/close/delete/validateJoin/increaseStock/decreaseStock/use/return/expire/startGroup/joinGroup/helpBargain.
- Domain repository interfaces live under `domain/{aggregate}/repository` and accept domain ids/value objects where useful.
- Infrastructure repositories preserve Mapper SQL semantics and handle DO/domain conversion.
- Application services own transactions, external API calls, tenant/job/MQ orchestration and cross-aggregate coordination.

## 8. Business Rules

### Banner

- PR-BAN-01：update/delete/browse 前必须校验 Banner 存在；错误码 `BANNER_NOT_EXISTS`。
- PR-BAN-02：Controller 路径和权限保持 `/promotion/banner` 及现有 `promotion:banner:*` 语义。

### Seckill

- PR-SEC-01：创建/更新秒杀活动必须校验商品、SKU、秒杀时段配置存在并保留当前 Product API 与 SeckillConfig 规则。
- PR-SEC-02：同一 SPU 在启用秒杀活动中不能存在秒杀时段 `configIds` 交集；冲突错误码 `SECKILL_ACTIVITY_SPU_CONFLICTS`。
- PR-SEC-03：关闭状态活动不能更新；错误码 `SECKILL_ACTIVITY_UPDATE_FAIL_STATUS_CLOSED`。
- PR-SEC-04：活动未关闭且未结束不能删除；错误码 `SECKILL_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED_OR_END`。
- PR-SEC-05：库存减少必须先校验活动和商品行，再通过 `SeckillProductMapper#updateStockDecr` 与 `SeckillActivityMapper#updateStockDecr` affected rows 防超卖；失败错误码 `SECKILL_ACTIVITY_UPDATE_STOCK_FAIL`。
- PR-SEC-06：库存回滚增加必须同时更新 product 和 activity stock，不能只回滚其中一张表。
- PR-SEC-07：下单前校验必须覆盖活动存在、启用、活动时间、秒杀时段时间、商品存在、单次限购；对应错误码保留 `SECKILL_JOIN_ACTIVITY_*`。

### CouponTemplate and Coupon

- PR-COU-01：模板商品范围必须通过 `ProductSpuApi#validateSpuList` 或 `ProductCategoryApi#validateCategoryList` 校验，不能放进 domain 直接调用远程 API。
- PR-COU-02：用户领取型模板总发放数不能小于已领取数；错误码 `COUPON_TEMPLATE_TOTAL_COUNT_TOO_SMALL`。
- PR-COU-03：领取模板必须校验领取方式、库存/剩余数量、固定有效期是否过期；错误码 `COUPON_TEMPLATE_CANNOT_TAKE`、`COUPON_TEMPLATE_NOT_ENOUGH`、`COUPON_TEMPLATE_EXPIRED`。
- PR-COU-04：每人限领通过已领取数量过滤，不能只依赖前端或请求参数。
- PR-COU-05：使用优惠券前必须校验存在、状态 UNUSED、有效期包含当前时间；错误码 `COUPON_NOT_EXISTS`、`COUPON_STATUS_NOT_UNUSED`、`COUPON_VALID_TIME_NOT_NOW`。
- PR-COU-06：优惠券退还时，未过期恢复 UNUSED，已过期置 EXPIRE；必须使用 id+status CAS 语义防重复退还。
- PR-COU-07：过期 Job 批量处理失败要记录并继续处理其它券，不得因单券异常中断整批。
- PR-COU-08：注册赠券 MQ consumer 语义必须保留，不能被同步 controller 逻辑替代。

### Discount

- PR-DIS-01：同一 SPU 不能同时参与多个启用的限时折扣活动；错误码 `DISCOUNT_ACTIVITY_SPU_CONFLICTS`。
- PR-DIS-02：关闭状态不能更新，启用状态不能删除，重复关闭要报对应 `DISCOUNT_ACTIVITY_*` 错误。
- PR-DIS-03：商品折扣配置必须保持 `DiscountProductRespDTO` 对 Trade 价格计算的字段语义。

### Reward

- PR-REW-01：满减送冲突必须同时考虑时间重叠和商品范围交集。
- PR-REW-02：`ALL` 与任何范围冲突；`CATEGORY` 与 `CATEGORY` 看分类交集；`SPU` 与 `SPU` 看 SPU 交集；`CATEGORY` 与 `SPU` 通过 Product category 关系判断。
- PR-REW-03：规则 JSON `RewardActivityDO.Rule` 中 `limit/discountPrice/freeDelivery/point/giveCouponTemplateCounts` 语义不得丢失。
- PR-REW-04：关闭/更新/删除错误码保持 `REWARD_ACTIVITY_*`。

### Combination

- PR-COM-01：同一 SPU 不能同时参与多个启用拼团活动；错误码 `COMBINATION_ACTIVITY_SPU_CONFLICTS`。
- PR-COM-02：关闭状态不能更新；未关闭或未结束不能删除。
- PR-COM-03：创建拼团记录必须校验活动、商品、团长记录、人数、单次/总次数、未支付订单等当前规则。
- PR-COM-04：`HEAD_ID_GROUP=0L` 是团长标记，不能改成 null 或自引用。
- PR-COM-05：拼团过期 Job 要区分成功/失败/虚拟成团，必要时调用 `TradeOrderApi` 取消订单并用 `WebSocketSenderApi` 推送结果。

### Bargain

- PR-BAR-01：同一 SPU 不能同时参与多个启用砍价活动；错误码 `BARGAIN_ACTIVITY_SPU_CONFLICTS`。
- PR-BAR-02：砍价活动参与必须校验存在、启用、库存、活动时间。
- PR-BAR-03：库存扣减必须使用 `BargainActivityMapper#updateStock` affected rows；失败错误码 `BARGAIN_ACTIVITY_STOCK_NOT_ENOUGH`。
- PR-BAR-04：砍价记录下单必须校验砍价成功、未绑定订单。
- PR-BAR-05：助力必须防止非进行中记录、自助力、超限、重复助力和并发冲突。

### Point

- PR-POI-01：同一 SPU 不能同时参与多个启用积分商城活动；错误码 `POINT_ACTIVITY_SPU_CONFLICTS`。
- PR-POI-02：加入积分活动必须校验启用、商品存在、单次限购、库存；对应 `POINT_ACTIVITY_JOIN_*` 和 `POINT_ACTIVITY_UPDATE_STOCK_FAIL` 错误。
- PR-POI-03：库存扣减和回滚必须同时更新 `PointProductMapper` 与 `PointActivityMapper`。

## 9. Error Code Contract

| Scenario | ErrorCodeConstants | Throwing layer |
|---|---|---|
| Banner missing | `BANNER_NOT_EXISTS` | application/service before update/delete/query detail |
| Discount missing/conflict/closed/delete/close | `DISCOUNT_ACTIVITY_NOT_EXISTS`, `DISCOUNT_ACTIVITY_SPU_CONFLICTS`, `DISCOUNT_ACTIVITY_UPDATE_FAIL_STATUS_CLOSED`, `DISCOUNT_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED`, `DISCOUNT_ACTIVITY_CLOSE_FAIL_STATUS_CLOSED` | application/service |
| Coupon template invalid | `COUPON_TEMPLATE_NOT_EXISTS`, `COUPON_TEMPLATE_TOTAL_COUNT_TOO_SMALL`, `COUPON_TEMPLATE_NOT_ENOUGH`, `COUPON_TEMPLATE_USER_ALREADY_TAKE`, `COUPON_TEMPLATE_EXPIRED`, `COUPON_TEMPLATE_CANNOT_TAKE` | application/service |
| Coupon invalid use/return | `COUPON_NOT_EXISTS`, `COUPON_DELETE_FAIL_USED`, `COUPON_STATUS_NOT_UNUSED`, `COUPON_VALID_TIME_NOT_NOW`, `COUPON_STATUS_NOT_USED` | API/application/service |
| Reward conflict/lifecycle | `REWARD_ACTIVITY_NOT_EXISTS`, `REWARD_ACTIVITY_SPU_CONFLICTS`, `REWARD_ACTIVITY_UPDATE_FAIL_STATUS_CLOSED`, `REWARD_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED`, `REWARD_ACTIVITY_CLOSE_FAIL_STATUS_CLOSED`, `REWARD_ACTIVITY_SCOPE_EXISTS` | application/service |
| Point lifecycle/join/stock | `POINT_ACTIVITY_NOT_EXISTS`, `POINT_ACTIVITY_SPU_CONFLICTS`, `POINT_ACTIVITY_UPDATE_FAIL_STATUS_CLOSED`, `POINT_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED_OR_END`, `POINT_ACTIVITY_CLOSE_FAIL_STATUS_CLOSED`, `POINT_ACTIVITY_JOIN_ACTIVITY_STATUS_CLOSED`, `POINT_ACTIVITY_JOIN_ACTIVITY_SINGLE_LIMIT_COUNT_EXCEED`, `POINT_ACTIVITY_JOIN_ACTIVITY_PRODUCT_NOT_EXISTS`, `POINT_ACTIVITY_UPDATE_STOCK_FAIL` | API/application/service |
| Seckill lifecycle/join/stock/config | `SECKILL_ACTIVITY_NOT_EXISTS`, `SECKILL_ACTIVITY_SPU_CONFLICTS`, `SECKILL_ACTIVITY_UPDATE_FAIL_STATUS_CLOSED`, `SECKILL_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED_OR_END`, `SECKILL_ACTIVITY_CLOSE_FAIL_STATUS_CLOSED`, `SECKILL_ACTIVITY_UPDATE_STOCK_FAIL`, `SECKILL_JOIN_ACTIVITY_TIME_ERROR`, `SECKILL_JOIN_ACTIVITY_STATUS_CLOSED`, `SECKILL_JOIN_ACTIVITY_SINGLE_LIMIT_COUNT_EXCEED`, `SECKILL_JOIN_ACTIVITY_PRODUCT_NOT_EXISTS`, `SECKILL_CONFIG_NOT_EXISTS`, `SECKILL_CONFIG_TIME_CONFLICTS`, `SECKILL_CONFIG_DISABLE` | API/application/service |
| Combination activity/record | `COMBINATION_ACTIVITY_NOT_EXISTS`, `COMBINATION_ACTIVITY_SPU_CONFLICTS`, `COMBINATION_ACTIVITY_STATUS_DISABLE_NOT_UPDATE`, `COMBINATION_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED_OR_END`, `COMBINATION_ACTIVITY_STATUS_DISABLE`, `COMBINATION_JOIN_ACTIVITY_PRODUCT_NOT_EXISTS`, `COMBINATION_ACTIVITY_UPDATE_STOCK_FAIL`, `COMBINATION_RECORD_*` | API/application/service/job |
| Bargain activity/record/help | `BARGAIN_ACTIVITY_*`, `BARGAIN_RECORD_*`, `BARGAIN_HELP_*` | API/application/service |

High-risk parameter contract:

| Error code | Parameters |
|---|---|
| `DISCOUNT_ACTIVITY_SPU_CONFLICTS` | one display value for conflicting activity/product, preserve current service argument order |
| `COUPON_TEMPLATE_TOTAL_COUNT_TOO_SMALL` | current `takeCount` as the single `{}` argument |
| `REWARD_ACTIVITY_SCOPE_EXISTS` | first conflicting activity display value, second textual conflict reason |
| `SECKILL_ACTIVITY_SPU_CONFLICTS` | no public placeholder argument in current constant text |
| `SECKILL_ACTIVITY_UPDATE_STOCK_FAIL` | no argument; thrown when product or activity affected rows is 0 |
| `POINT_ACTIVITY_UPDATE_STOCK_FAIL` | no argument; thrown when product or activity affected rows is 0 |
| `COMBINATION_RECORD_FAILED_SINGLE_LIMIT_COUNT_EXCEED` | no argument in current constant text |
| `BARGAIN_ACTIVITY_STOCK_NOT_ENOUGH` | no argument; thrown when stock affected rows is 0 |
| `BARGAIN_HELP_CREATE_FAIL_CONFLICT` | no argument; preserves retry-oriented message |

Rules:

- 错误码数字、常量名、中文文案和参数顺序不得因 DDD 重构改变。
- Placeholder `{}` 参数必须按当前 service 抛出顺序传递；不确定时先读取对应 `*ServiceImpl` 抛错语句，不得猜测。
- Domain 可抛领域异常，但 application/service 边界必须映射为当前 `ServiceException`/`ErrorCodeConstants` 外部语义。
- API implementation 继续返回 `CommonResult.success(...)`，失败由异常机制处理。

## 10. Transaction Contract

| Use case | Current transaction expectation | Must preserve |
|---|---|---|
| Admin create/update/close/delete activity | `@Transactional(rollbackFor = Exception.class)` in legacy services; current partial DDD application uses plain `@Transactional` | 写主表和子表必须同事务 |
| Seckill stock decr/incr | `@Transactional(rollbackFor = Exception.class)` | product stock 与 activity stock 同事务；任一失败回滚 |
| Point stock decr/incr | `@Transactional(rollbackFor = Exception.class)` | product stock 与 activity stock 同事务 |
| Bargain stock update | mapper atomic update; service method participates in caller transaction where applicable | affected rows fail must throw |
| Coupon use/return/take/admin grant | transactional service methods | status、useOrderId、useTime、template take/use count 保持一致 |
| Coupon batch take | `REQUIRES_NEW` exists for batch item behavior in current service | 单用户/单券失败边界按当前代码保持 |
| Coupon expire job | batch loop with per-item error logging | 单个 coupon 异常不阻断整批 |
| Combination record create/pay/expire | transactional around record, order id/status and notifications | 订单取消和 WebSocket side effect 按当前顺序保留 |
| MQ register coupon grant | consumer delegates service transaction | 消息消费不得绕过领取限制 |

Migration rule: application service owns transaction boundary; repository implementation should not become the only transaction owner except current temporary DDD code already does so. When moving logic, prefer `@Transactional(rollbackFor = Exception.class)` on application use case.

## 11. Integration Contract

- Product module：`ProductSpuApi`、`ProductSkuApi`、`ProductCategoryApi` are application/infrastructure ports for validation and category lookup; domain must receive validated facts, not call APIs.
- Trade module：`TradeOrderApi` participates in combination expiry/cancellation and order-linked validation; do not replace with local assumptions.
- Member module：`MemberUserRemoteClient` is scanned in promotion RPC config and may provide user info for records/messages.
- System module：`AdminUserRemoteClient`、`SocialClientRemoteClient` are conditionally enabled by `develop.rpc.remote.system.enabled`; preserve conditional configuration.
- Infra websocket：`WebSocketSenderApi` publishes combination-related user notifications; preserve message type constants and payload semantics.
- Job：`CouponExpireJob` and `CombinationRecordExpireJob` are tenant-aware scheduled entry points; job classes should only trigger application use cases after migration.
- MQ：`CouponTakeByRegisterConsumer` must remain idempotent at service boundary through coupon take rules.
- Tenant/data permission：promotion tables inherit framework behavior; do not remove tenant/job annotations or permission checks from controllers/jobs.
- OpenAPI/Swagger：current `@Tag`/`@Operation` are external documentation contracts even when text is imperfect.

## 12. Mapping Rules

- Controller VO stays in `controller/.../vo`; domain/application must not accept Controller VO as core method contract after migration.
- API DTO stays in `promotion-api`; do not move DTO into server domain.
- DO stays in `dal/dataobject`; domain must not import DO.
- Mapper stays infrastructure/DAL only; domain/application should access persistence through repository ports or legacy service during transition.
- Convert layer maps:
  - Admin VO ↔ application command/query/result.
  - DO ↔ domain aggregate/value object.
  - DO/domain/application result ↔ API DTO.
- JSON/list type-handler fields (`configIds`, `productScopeValues`, `rules`) must be converted explicitly; do not rely on shallow bean copy when field meaning differs.
- Redundant snapshot fields in Coupon、SeckillProduct、CombinationRecord must remain snapshots; do not recompute them on read from current product/user data unless current service does so.

## 13. Current Conflict Notes

- All promotion stable APIs currently include `@FeignClient(name = ApiConstants.NAME)`. Target structure should split `remote/*RemoteClient`, but this must be done as an API local/remote batch, not hidden inside aggregate migration.
- `SeckillActivityApi` currently sets `PREFIX = ApiConstants.PREFIX + "/discount-activity"` even though it is a seckill API. Treat this as current external path until an explicit API migration changes it.
- `PointActivityApi` currently has `@Tag(name = "RPC 服务 - 秒杀活动")`; do not “fix” documentation text during behavior-preserving DDD migration unless API doc migration is explicitly in scope.
- Current DDD coverage is incomplete: Banner、CouponTemplate、SeckillActivity have partial application/domain/repository code; Discount、Reward、Combination、Bargain、Point、Coupon instance remain primarily legacy service/dal.
- Some current DDD transactions use plain `@Transactional`; target is `rollbackFor = Exception.class`, but changing rollback semantics should be verified per use case.
- Existing legacy service line numbers in older drafts may be stale. Always re-read current file before using a specific method anchor.
- Article/Diy/KeFu share the promotion module and error code file but are not part of this high-risk Mall Promotion skill scope unless touched by RPC/config/shared convert changes.

## 14. Acceptance Criteria

### Architecture AC

- AC-PROM-01：Domain classes import no Spring, MyBatis, Feign, Mapper, DO, Controller VO or remote clients.
- AC-PROM-02：Application services orchestrate transactions, external APIs, repositories and domain methods; they do not contain raw Mapper SQL decisions.
- AC-PROM-03：Infrastructure repositories implement persistence and preserve Mapper guarded updates.
- AC-PROM-04：Controller/API implementation delegates to application/service boundary and preserves route, permission, request/response and `CommonResult` semantics.
- AC-PROM-05：API local/remote split, if performed, results in stable contract + remote Feign adapter without changing method signatures.

### Behavior AC

- AC-PROM-06：Seckill/Point stock decr/incr update product and activity stock atomically.
- AC-PROM-07：Bargain stock decrement remains affected-row guarded.
- AC-PROM-08：Coupon use/return/expire state machine and CAS semantics are preserved.
- AC-PROM-09：Reward scope conflict algorithm handles ALL/CATEGORY/SPU combinations.
- AC-PROM-10：Combination expiry preserves virtual group/order cancellation/WebSocket behavior.
- AC-PROM-11：All lifecycle close/update/delete error codes match `ErrorCodeConstants`.
- AC-PROM-12：Product/Trade/WebSocket/Member/System integrations remain behind application/infrastructure ports.

### Verification AC

- AC-PROM-13：Target module compiles with `mvn compile -pl develop-module-mall/develop-module-promotion-server -am -DskipTests`.
- AC-PROM-14：Existing targeted tests pass or failures are documented with exact failing tests and reason.
- AC-PROM-15：Document-only skill changes pass `git diff --check` and heading verification.

## 15. Verification Commands

Skill-only change:

```bash
git diff --check -- .claude/ddd-skills/AggregateRoot_MallPromotion_Skill.md
grep -n "^## " .claude/ddd-skills/AggregateRoot_MallPromotion_Skill.md
```

API contract/local-remote change:

```bash
mvn compile -pl develop-module-mall/develop-module-promotion-api -am -DskipTests
mvn compile -pl develop-module-mall/develop-module-promotion-server -am -DskipTests
```

Single aggregate code change examples:

```bash
mvn test -pl develop-module-mall/develop-module-promotion-server -Dtest=BannerTest,BannerApplicationServiceTest
mvn test -pl develop-module-mall/develop-module-promotion-server -Dtest=CouponTemplateTest,CouponTemplateApplicationServiceTest
mvn test -pl develop-module-mall/develop-module-promotion-server -Dtest=SeckillActivityTest,SeckillActivityApplicationServiceTest,SeckillActivityRepositoryImplTest
mvn compile -pl develop-module-mall/develop-module-promotion-server -am -DskipTests
```

When adding tests for currently legacy chains, prefer focused service/application tests around the original behavior: coupon use/return/expire, seckill stock decr/incr, point stock decr/incr, reward conflict, combination expire, bargain help.

## 16. Quick Reference

| 要做什么 | 正确位置 | 禁止位置 |
|---|---|---|
| 活动业务不变量 | `domain/{aggregate}` | Controller、Mapper、Convert |
| 用例事务和跨模块调用 | `application/{aggregate}` | Domain、Controller |
| Product/Trade/WebSocket 适配 | `infrastructure/{aggregate}` 或 application port implementation | Domain |
| Mapper guarded stock update | Mapper + infrastructure repository | Domain 先查后存 |
| API DTO | `develop-module-promotion-api/.../dto` | server domain |
| Controller VO | `controller/.../vo` | domain/application public core API |
| DO/Mapper | `dal` and infrastructure implementation | domain |
| Job entry | `job/*` calls application use case | Job 内复制业务流程 |
| MQ consumer | `mq/*` calls application/service use case | MQ 内写领域规则 |
| API Feign adapter after split | `api/.../remote/*RemoteClient` | stable CommonApi interface |

## 17. Common Mistakes

| Mistake | Consequence | Fix |
|---|---|---|
| 直接按旧草稿创建所有聚合 | 生成空抽象，行为不等价 | 每次只迁移一个链路，先读 legacy service |
| “修正” Seckill API prefix | Trade 调用路径回归 | 单独 API migration 计划处理 |
| 库存先查后改 | 并发超卖 | 保留 Mapper affected-row guarded update |
| Domain 调 Product API | 领域层依赖远程技术 | application 获取事实后传入 domain |
| 删除 Job/MQ 入口 | 优惠券过期/注册赠券/拼团过期失效 | Job/MQ 只瘦身，不删除行为 |
| Bean copy 规则 JSON/list | `rules/configIds/productScopeValues` 映射丢失 | 显式转换并测试 |
| 用 DDD 部分实现替换完整 legacy service | 丢失错误码、事务或集成 | 先补齐测试和 parity checklist |

## 18. Rationalization Table

| Excuse | Reality |
|---|---|
| “只是 skill 文档，不需要事实锚点” | 生产级 skill 的核心价值就是让无上下文 AI 不猜路径、不猜规则。 |
| “API 前缀明显写错，顺手改了” | 当前路径可能已有调用方依赖；没有迁移计划就不能改外部契约。 |
| “领域事件可以替代 Job/MQ” | Coupon expire、Combination expire、register coupon 都有 durable 入口和失败处理语义。 |
| “库存逻辑放领域里更 DDD” | 并发安全依赖数据库 affected rows；领域表达意图，基础设施保证原子性。 |
| “现有 DDD 代码已经有 application service” | 当前只覆盖少数链路，不能代表 legacy service 完整行为。 |
| “编译过就够了” | 营销模块高风险在库存、状态机、冲突校验和外部回调，必须有行为验证。 |

## 19. Red Flags

出现以下情况立即停止本批重构：

- 计划一次迁移 Promotion 全部营销聚合。
- 未读 `service/*ServiceImpl` 就修改 application/domain。
- 修改 Controller 路径、权限、VO 字段、API DTO 或 Feign 路径。
- Domain import Spring、Mapper、DO、Feign、Product API、Trade API、WebSocket API。
- 库存扣减没有 affected-row 检查。
- Coupon 状态更新没有 id+status CAS 语义。
- 删除或绕过 CouponExpireJob、CombinationRecordExpireJob、CouponTakeByRegisterConsumer。
- 只验证编译，不验证原业务链路。
- 用“当前代码看起来有 bug”为理由直接修复外部契约而不写迁移计划。

## 20. Rollback Conditions

必须回滚或停止当前批次：

1. `develop-module-promotion-api` 或 `develop-module-promotion-server` 编译失败。
2. 任一 Controller/API 调用路径、权限、DTO、错误码、错误参数回归。
3. Seckill/Point/Bargain 库存出现超卖或只更新一张表。
4. Coupon use/return/expire 出现重复使用、重复退还、错误过期。
5. Reward/Discount/Seckill/Combination/Bargain/Point 商品冲突校验遗漏。
6. Combination expiry 不再取消订单或不再发送必要 WebSocket 通知。
7. Product/Trade/Member/System/Infra integration 装配失败。
8. 发现本 skill 与当前代码事实冲突但尚未修订 skill。

## 21. AI Self-Check

完成任何 Mall Promotion 相关修改前逐项确认：

- [ ] 是否只处理一个聚合或一个明确链路？
- [ ] 是否读取了当前目标 Controller、VO/DTO、DO、Mapper、Convert、Service/Application、ErrorCode 和测试？
- [ ] 是否保持 API/Controller 外部契约不变？
- [ ] 是否保留错误码常量、参数和抛出语义？
- [ ] 是否把跨模块 API 调用放在 application/infrastructure，不在 domain？
- [ ] 是否保留库存 affected-row 语义？
- [ ] 是否保留 Coupon 状态机和 CAS？
- [ ] 是否保留 Job/MQ/WebSocket 入口和失败处理？
- [ ] 是否新增或复用能证明当前链路行为的测试？
- [ ] 是否运行了影响范围 Maven compile/test 或明确记录无法执行原因？
