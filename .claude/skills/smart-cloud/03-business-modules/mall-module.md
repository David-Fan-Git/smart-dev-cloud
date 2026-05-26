---
name: mall-module
description: E-commerce platform — product (SPU/SKU/category/brand), promotion (coupon/seckill/combination/bargain), trade (cart/order/after-sale/delivery), statistics (sales/rankings)
type: project
---

# develop-module-mall

## 概述

商城模块。完整的 B2C 电商功能，按领域拆分为四个独立子模块：商品 (product)、营销 (promotion)、交易 (trade)、统计 (statistics)。

- **包路径**: 按子模块划分为 `com.develop.mvp.pk.module.{product|promotion|trade|statistics}`
- **服务名**: 各子模块独立
- **错误码区间**: product=[1-008-000-000 ~ 1-009-000-000), trade=[1-011-000-000 ~ 1-012-000-000), promotion=[1-013-000-000 ~ 1-014-000-000)
- **多租户**: 所有 Entity 继承 `TenantBaseDO`

## 模块结构

```
develop-module-mall/
├── develop-module-product/             # 商品域
│   ├── develop-module-product-api/     # SPU/SKU/分类/品牌 Feign 接口
│   └── develop-module-product-server/  # 实现层
├── develop-module-promotion/           # 营销域
│   ├── develop-module-promotion-api/   # 优惠券/秒杀/拼团等 Feign 接口
│   └── develop-module-promotion-server/
├── develop-module-trade/               # 交易域
│   ├── develop-module-trade-api/       # 订单/购物车/售后 Feign 接口
│   └── develop-module-trade-server/
└── develop-module-statistics/          # 统计域
    ├── develop-module-statistics-api/
    └── develop-module-statistics-server/
```

## 1. 商品域 (develop-module-product)

### Controllers
| Controller | 路由 | 方法 |
|---|---|---|
| `ProductSpuController` | `/product/spu` | CRUD + `/page`、上下架、规格组合 |
| `ProductSkuController` | `/product/sku` | SKU 列表（价格、库存、规格值） |
| `ProductCategoryController` | `/product/category` | 树形 CRUD + `/list` |
| `ProductBrandController` | `/product/brand` | CRUD + `/page`、`/list-all-simple` |
| `ProductPropertyController` | `/product/property` | 规格属性 CRUD（颜色、尺寸等） |
| `ProductCommentController` | `/product/comment` | CRUD + `/page` |

### 关键 Services
| Service | 职责 |
|---|---|
| `ProductSpuService` | SPU 业务（含 SKU 组合、上下架） |
| `ProductSkuService` | SKU 库存管理、价格管理 |
| `ProductCategoryService` | 分类树管理 |
| `ProductCommentService` | 评论管理 |

### Feign API
| API | 主要方法 |
|---|---|
| `ProductSpuApi` | `getSpu`, `validateSpuList` |
| `ProductSkuApi` | `getSku`, `getSkuList`, `validateSkuList` |
| `ProductCategoryApi` | `getCategory`, `getCategoryList` |
| `ProductCommentApi` | `getCommentCountBySpuIds` |

## 2. 营销域 (develop-module-promotion)

### Controllers
| Controller | 路由 | 方法 |
|---|---|---|
| `CouponTemplateController` | `/promotion/coupon-template` | 优惠券模板 CRUD + `/page` |
| `CouponController` | `/promotion/coupon` | 用户领券/核销 |
| `DiscountActivityController` | `/promotion/discount-activity` | 限时折扣活动 |
| `SeckillActivityController` | `/promotion/seckill-activity` | 秒杀活动 |
| `SeckillConfigController` | `/promotion/seckill-config` | 秒杀时段配置 |
| `CombinationActivityController` | `/promotion/combination-activity` | 拼团活动 |
| `CombinationRecordController` | `/promotion/combination-record` | 拼团记录 |
| `BargainActivityController` | `/promotion/bargain-activity` | 砍价活动 |
| `BargainRecordController` | `/promotion/bargain-record` | 砍价记录 |
| `PointActivityController` | `/promotion/point-activity` | 积分兑换活动 |
| `RewardActivityController` | `/promotion/reward-activity` | 满减/满送活动 |
| `BannerController` | `/promotion/banner` | 轮播图管理 |
| `RecommendController` | `/promotion/recommend` | 推荐商品/广告位 |

### Feign API
| API | 主要方法 |
|---|---|
| `CouponApi` | `getCoupon`, `useCoupon` |
| `SeckillActivityApi` | `getSeckillActivity` |
| `CombinationRecordApi` | `getCombinationRecord` |
| `BargainActivityApi` | `getBargainActivity` |
| `DiscountActivityApi` | `getDiscountActivity` |
| `RewardActivityApi` | `getRewardActivity` |
| `PointActivityApi` | `getPointActivity` |

## 3. 交易域 (develop-module-trade)

### Controllers
| Controller | 路由 | 方法 |
|---|---|---|
| `CartController` | `/trade/cart` | 购物车增删改查、选中计算 |
| `TradeOrderController` | `/trade/order` | 订单创建、查询、支付、取消、收货 |
| `AfterSaleController` | `/trade/after-sale` | 售后（退款/退货） |
| `TradeDeliveryController` | `/trade/delivery` | 物流配送管理 |
| `TradeConfigController` | `/trade/config` | 交易配置（自动收货时间、售后期限等） |

### 关键 Services
| Service | 职责 |
|---|---|
| `TradeOrderService` | 订单核心逻辑（创建→支付→发货→收货） |
| `CartService` | 购物车管理 |
| `AfterSaleService` | 售后处理 |

### Feign API
| API | 主要方法 |
|---|---|
| `TradeOrderApi` | `getOrder`, `createOrder`, `cancelOrder`, `updateOrderPaid` |

## 4. 统计域 (develop-module-statistics)

主要提供数据看板：销售趋势、TOP 排行、用户统计、交易统计、商品分析。

## 数据库表

### Product 表
`product_spu`, `product_sku`, `product_category`, `product_brand`, `product_property`, `product_property_value`, `product_comment`

### Promotion 表
`promotion_coupon_template`, `promotion_coupon`, `promotion_discount_activity`, `promotion_discount_product`, `promotion_seckill_activity`, `promotion_seckill_config`, `promotion_combination_activity`, `promotion_combination_record`, `promotion_bargain_activity`, `promotion_bargain_record`, `promotion_point_activity`, `promotion_reward_activity`, `promotion_banner`, `promotion_recommend`

### Trade 表
`trade_order`, `trade_order_item`, `trade_after_sale`, `trade_after_sale_log`, `trade_cart`, `trade_delivery`, `trade_config`, `trade_brokerage_record`

## 依赖的 Starter

- `develop-spring-boot-starter-web`
- `develop-spring-boot-starter-security`
- `develop-spring-boot-starter-mybatis`
- `develop-spring-boot-starter-redis`
- `develop-spring-boot-starter-biz-tenant`
- `develop-module-pay-api` (交易域依赖支付模块)

## 关键点

- 商城拆分为 4 个独立子模块，各有独立的 api/server
- 错误码：product [1-008], trade [1-011], promotion [1-013]
- 交易依赖支付模块完成支付回调
- SPU/SKU 分离设计（SPU 描述商品，SKU 定义具体规格和价格）
- 秒杀等营销活动使用 Redis 做库存扣减（高并发场景）
