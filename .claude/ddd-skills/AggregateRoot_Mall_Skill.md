---
name: aggregate-root-mall-skill
description: Use when routing Mall DDD work to product, promotion, trade, or statistics child aggregate skills; not for direct production refactoring.
type: ddd-aggregate-skill
status: navigation
---

# DDD Skill: AggregateRoot_Mall_Skill

## Production Readiness Boundary

本文件当前不是直接生产级重构指南。必须先按 `DDD_Skill_Production_Readiness_Standard.md` 补齐当前事实源、字段映射、错误码、事务边界、外部契约和验证命令；否则只能用于范围识别和升级 skill。

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

## Status

这是 Mall 模块 DDD skill 的父级导航入口，不是可直接执行的生产级聚合重构指南。

Mall 已拆成多个独立业务子域。执行 DDD 重构时必须进入对应子域 skill，不能用本文件覆盖 Product、Promotion、Trade、Statistics 的具体边界。

## Child Skills

| 子域 | 使用文件 | 适用范围 |
|---|---|---|
| Product | `AggregateRoot_MallProduct_Skill.md` | 商品分类、品牌、SPU、SKU、属性、评论、收藏、浏览记录 |
| Promotion | `AggregateRoot_MallPromotion_Skill.md` | 优惠券、秒杀、满减、奖励、拼团、砍价、积分活动 |
| Trade | `AggregateRoot_MallTrade_Skill.md` | 订单、购物车、售后、配送、交易状态流转 |
| Statistics | `AggregateRoot_MallStatistics_Skill.md` | 交易、商品、用户、营销统计口径 |

## How To Use

1. 先阅读 `DDD_Skill_Production_Readiness_Standard.md`。
2. 按本轮修改目标选择唯一子域 skill。
3. 读取对应子域当前代码事实源后再决定是否升级该子域 skill。
4. 每次只处理一个子域或一个小聚合集合，不把 Mall 当作一个整体批量重构。

## Red Flags

- 试图用本文件直接设计订单、商品或营销聚合。
- 一次同时改 Product、Promotion、Trade、Statistics 多个子域。
- 子域 skill 与当前代码事实冲突时继续按文档改代码。
- 为了目录统一移动 DTO、VO、DO、Mapper、MQ、Job 或统计口径对象。

## Acceptance Criteria

- 本文件只承担导航和边界提醒。
- 具体业务规则、字段映射、错误码、事务边界和验证命令必须写在子域 skill 中。
- 修改 Mall 子域前，必须确认使用的是对应子域 skill，而不是本父级入口。
