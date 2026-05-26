# API Contract Local/Remote Refactor Design

## Goal

全项目推进 API 契约统一：每个跨模块调用面向一套稳定 `XxxApi` / `XxxCommonApi` 契约，并通过本地实现与 `remote` 远程适配隔离调用方式差异。

“全部推进”表示覆盖全部业务 API 模块并形成连续迁移队列，不表示一次性大批量改完整仓库。每次实现仍按模块、上下文或 API 契约集合分批落地，避免当前大量未提交改动继续扩大成不可验证状态。

## Relationship to Existing Specs

本设计延续并细化以下既有标准与规格：

- `.claude/ddd-skills/Module_Structure_Standard.md`
- `docs/superpowers/specs/2026-05-24-full-project-api-contract-migration-design.md`
- `docs/superpowers/specs/2026-05-24-api-contract-pilot-design.md`

既有全项目迁移规格强调 `XxxApi` 保持稳定契约名、`remote/XxxRemoteClient` 承载 Feign 身份、不要一次性批量转换。本设计在此基础上确认全项目覆盖范围和分批执行顺序。

## Scope

覆盖所有后端业务 API 模块：

1. `system`
2. `infra`
3. `member`
4. `bpm`
5. `pay`
6. `mall/product`
7. `mall/promotion`
8. `mall/trade`
9. `mall/statistics`
10. `crm`
11. `erp`
12. `iot`
13. `mes`
14. `wms`
15. `ai`
16. `report`
17. `mp`

本设计只覆盖 API 契约、远程适配、调用方依赖方向、扫描配置和验证队列；不直接推进 DDD 聚合迁移、Controller 行为调整、DTO 语义重写、数据库结构调整或特殊运行单元收口。

## Target Architecture

每个真实跨模块 API 契约集合最终保持如下形态：

```text
develop-module-{name}-api/
  src/main/java/.../module/{name}/api/{business}/
    XxxApi.java
    dto/
    remote/
      XxxRemoteClient.java
```

调用方向统一为：

```text
调用方业务代码
  -> XxxApi 稳定契约
      -> 本地实现：server 模块中的 XxxApiImpl
      -> 远程实现：api 模块 remote/XxxRemoteClient
```

原则：

- `XxxApi` / `XxxCommonApi` 是业务调用方唯一依赖的稳定契约。
- `remote/XxxRemoteClient` 只做 Feign/RPC 远程适配，通常继承对应契约，不重复声明契约方法。
- Server 侧 `api/{business}/XxxApiImpl` 承接本地实现，不反向依赖 remote client。
- 扫描配置控制远程 client 是否生效；业务服务不直接依赖 remote client。
- 不强制为所有契约创建 `local/` 包。只有在模块存在明确本地/远程切换装配点时，才设计并补齐 local 适配。

## Migration Batches

### Batch 1: Finish `member`

`member` 是已试点模块，继续补齐剩余 API 契约并验证样板稳定性。

重点：

- 检查 `address/config/level/point/user` 是否均符合稳定契约 + remote client 结构。
- 确认调用方继续注入 `MemberXxxApi`。
- 确认远程扫描配置只引用 `MemberXxxRemoteClient`。
- `user` 最后处理，因为消费面最广且可能包含默认 helper 行为。

### Batch 2: Foundation Modules: `system + infra`

基础模块影响最大，必须在 member 模式稳定后推进。

重点：

- `system`：dept、dict、logger、mail、notify、permission、sms、social、user 等契约逐组迁移或校验。
- `infra`：config、file、websocket 等契约逐组迁移或校验。
- 已经存在 remote client 的契约优先做一致性审计、扫描配置校验和编译验证。
- 不把 framework common DTO 或 server 内部对象为了目录对称移动到 API 模块。

### Batch 3: Transaction Chain: `pay + mall/product + mall/promotion + mall/trade`

交易链路按业务上下文推进，避免一次性影响订单、支付、营销、商品多个关键路径。

重点：

- `pay`：order、refund、transfer、wallet 等契约。
- `mall/product`：category、comment、sku、spu 等契约。
- `mall/promotion`：coupon、seckill、discount、reward、combination、bargain 等契约。
- `mall/trade`：order 等契约。
- 每个上下文单独验证，不能把 mall 父目录当作一个模糊整体处理。

### Batch 4: Extension Modules: `bpm + crm + erp + report + mp`

业务扩展模块按真实跨模块契约存在情况推进。

重点：

- 有真实 API 契约和调用方的模块进入迁移。
- 仅包含枚举、常量或事件消息的 API 模块，不创建臆测 remote client。
- 对事件监听、消息 DTO、审批/CRM/ERP 状态回调保持原有语义，不为结构统一改变事件边界。

### Batch 5: Special Modules: `iot + mes + wms + ai`

特殊模块需要先识别边界再迁移。

重点：

- `iot` 必须区分 `iot-api / iot-core / iot-server / iot-gateway` 的职责，不简单照搬普通模块。
- `iot-core` 中现有 `CommonApi` 或技术共享能力需要先判定归属：API 契约、server domain/infrastructure、gateway infrastructure 或共享基础包。
- `mes/wms/ai` 只迁移真实跨模块调用契约；枚举或内部技术 API 不创建远程适配。

## Per-Batch Execution Model

每批执行前先做只读审计：

1. 列出该批所有 `api/{business}/XxxApi.java` 或历史 `XxxCommonApi.java`。
2. 列出 server 侧实现类和所有调用方。
3. 找出 Feign 扫描配置、自动配置和 remote client 注册点。
4. 判断哪些契约已经符合标准，哪些需要迁移，哪些不应迁移。

每个 API 契约集合按以下步骤实现：

1. 保持稳定契约名和方法签名不变。
2. 从契约中移除 Feign 身份，仅在需要迁移时新增或修正 `remote/XxxRemoteClient`。
3. 让 remote client 继承对应契约，并配置唯一 `contextId`。
4. 更新远程扫描配置，让它引用 remote client。
5. 确认业务调用方继续依赖契约接口。
6. 确认 server 侧本地实现不依赖 remote client。
7. 不移动无关 DTO、enums、message、VO、DO、Mapper 或领域对象。
8. 编译验证当前 API 模块和受影响 consumer 模块。

## Constraints

- 不改 Controller 路径、HTTP 方法、权限、租户、数据权限、错误码、分页语义、DTO 字段或返回包装。
- 不改业务语义，不借 API 迁移重写领域规则或应用编排。
- 不把应用服务、事务边界、Mapper、DO、仓储实现或领域决策放入 API 模块。
- 不为了“目录对称”移动只在 server 内使用的对象。
- 不创建 speculative `local/` 包；local 适配必须基于明确装配需求。
- 不做跨批次大提交；即使目标是全项目推进，也要保持小批次、可验证、可回退。
- 当前未提交修改不主动清理、不回滚；新改动集中在当前批次范围内。

## Validation Strategy

### Structure Checks

每个契约集合完成后检查：

- `api/{business}/XxxApi.java` 是唯一业务契约。
- `remote/XxxRemoteClient.java` 只做远程适配。
- Server 侧 `api/{business}/XxxApiImpl` 只承接本地实现。
- 调用方注入契约，不注入 remote client 或 server implementation。
- API 模块没有应用编排、领域规则、Mapper、DO 或持久化逻辑。

### Compile Checks

按最小影响范围执行 Maven 编译：

```bash
mvn compile -pl develop-module-member/develop-module-member-api -am
mvn compile -pl develop-module-member/develop-module-member-server -am
```

其他模块按实际路径替换。批次完成后执行该批相关模块 compile；最后再视工作区状态执行全量：

```bash
mvn compile
```

### Test Checks

- 只改契约和 remote 适配时，compile 是最低要求。
- 修改实现类注入、调用链或扫描配置时，补跑对应模块测试。
- 如果测试因历史环境或无关未提交改动失败，记录首个失败模块和错误摘要，不扩大修复范围。

## Stop Conditions

遇到以下情况停止实现并回到设计或计划阶段：

- 发现某个 API 契约实际承担应用编排、事务或领域规则。
- 迁移会破坏外部 RPC/Feign 路径、contextId、DTO 兼容或默认 helper 行为。
- 特殊模块边界不清，尤其是 `iot-core` 与 `iot-gateway` 的职责归属不明。
- 编译错误跨越当前批次，说明批次范围过大或已有工作区状态不可验证。
- 既有标准、spec、skill 与当前代码事实冲突。

## Acceptance Criteria

全项目 API 契约 local/remote 重构达到阶段性验收时必须满足：

- 每个已处理模块都有明确的 API 契约集合清单和处理状态。
- 每个已迁移契约保留稳定业务接口，remote client 位于 `remote/` 并继承契约。
- 业务调用方继续面向稳定契约编程。
- 远程扫描配置只注册 remote client，不把稳定契约直接当 Feign client 注册。
- 没有因迁移改变 DTO 字段、请求路径、返回包装、错误码或业务规则。
- 没有新增无意义中转层、空抽象或 speculative local 包。
- 每批都有对应 Maven compile/test 验证结果，或有明确的不可验证原因。
- 当前工作区的无关未提交改动没有被混入迁移提交或验证结论。

## Next Step

进入实现计划阶段时，第一份计划应从 `member` 批次开始，先审计并完成剩余 `point/user` 以及已存在 `level` remote 样板的一致性校验。之后按本设计的 Batch 2-5 顺序继续推进。
