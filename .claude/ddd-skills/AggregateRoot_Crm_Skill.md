---
name: aggregate-root-crm-skill
description: Use when triaging CRM aggregate DDD skill scope or upgrading one CRM aggregate skill at a time; do not use for production refactoring until the target aggregate has current-code anchors and contracts.
type: ddd-aggregate-skill
status: draft
---

# DDD Skill: AggregateRoot_Crm_Skill

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

## 1. 技能名称

`AggregateRoot_Crm_Skill` — CRM（客户关系管理）模块的多聚合根领域建模与重构技能

## 2. 适用场景

本技能针对 **CRM 模块** 的多个聚合根，覆盖以下业务操作：

**客户（Customer）聚合：**
- 创建、更新、删除客户
- 客户成交状态变更
- 客户锁定/解锁
- 客户转移（含同时转移合同/商机/联系人）
- 客户放入公海 / 领取公海客户
- 自动定时放入公海
- 客户批量导入
- 客户跟进记录更新
- 客户拥有者数量上限校验、锁定数量上限校验

**线索（Clue）聚合：**
- 创建、更新、删除线索
- 线索转化为客户（含跟进记录复制）
- 线索转移
- 线索跟进

**联系人（Contact）聚合：**
- 创建、更新、删除联系人
- 联系人转移
- 联系人跟进
- 联系人-商机关联管理
- 客户批量放入公海时联动清空联系人负责人

**商机（Business）聚合：**
- 创建、更新、删除商机
- 商机状态流转（阶段推进/结束）
- 商机转移
- 商机-商品关联
- 商机跟进
- 联系人-商机关联

**合同（Contract）聚合：**
- 创建、更新、删除合同
- 合同提交审批（BPM工作流）
- 合同审核结果更新
- 合同转移
- 合同-商品关联（产品项差价计算）
- 合同跟进

**回款（Receivable）聚合：**
- 创建、更新、删除回款
- 回款提交审批（BPM工作流）
- 回款审核结果更新
- 回款金额上限校验（不可超过可用回款金额）

**CRM权限（Permission）聚合：**
- 数据权限创建/批量创建
- 数据权限转移（负责人变更）
- 数据权限删除（负责人 / 指定级别 / 自我退出）
- 数据权限校验（是否有某级别权限）

**跟进记录（FollowUpRecord）聚合：**
- 跟进记录创建/批量创建
- 跟进记录删除（按业务类型+业务编号）

**产品（Product）与产品分类（ProductCategory）** 为独立基础数据聚合

## 3. DDD 构造块

### 3.1 聚合根列表

| 聚合根 | 类名 | 角色说明 |
|--------|------|---------|
| 客户 | `Customer` | CRM核心聚合，包含客户信息、成交状态、锁定状态、公海状态 |
| 线索 | `Clue` | 潜在客户线索，可转化为客户 |
| 联系人 | `Contact` | 客户联系人，可关联商机 |
| 商机 | `Business` | 销售机会，含商品项、状态阶段、管道金额 |
| 合同 | `Contract` | 销售合同，含商品项、金额、审核状态、BPM审批流 |
| 回款 | `Receivable` | 回款记录，关联合同和回款计划 |
| CRM权限 | `CrmPermission` | 数据级权限控制（负责人/只读/读写） |
| 跟进记录 | `FollowUpRecord` | 各类业务的跟进记录 |

### 3.2 聚合边界

- **Customer**：客户基本信息 + 成交/锁定/公海状态。不包含：Clue（线索）、Contact（联系人）、Business（商机）、Contract（合同）— 通过ID引用
- **Clue**：线索信息 + 转化状态。可转化为 Customer
- **Contact**：联系人信息 + 上级关系。可关联 Business（多对多）
- **Business**：商机信息 + 商品项列表（BusinessProduct）。不包含：Contract、Receivable
- **Contract**：合同信息 + 商品项列表（ContractProduct）+ BPM审批流引用。不包含：ReceivablePlan
- **Receivable**：回款信息 + 审核状态 + BPM审批流引用。关联 Contract
- **CrmPermission**：数据权限，按(bizType, bizId, userId)唯一

### 3.3 值对象（Value Objects）

| 值对象 | 类名 | 封装字段 | 不可变 | 自校验 |
|--------|------|---------|--------|--------|
| 客户名称 | `CustomerName` | `String value` | ✅ | 非空、非空白 |
| 成交状态 | `DealStatus` | `Boolean value` | ✅ | 非空 |
| 锁定状态 | `LockStatus` | `Boolean value` | ✅ | 非空 |
| 跟进状态 | `FollowUpStatus` | `Boolean value` | ✅ | 非空 |
| 公海状态 | `PoolStatus` | `Boolean inPool` | ✅ | — |
| 客户限额配置 | `CustomerLimitConfig` | type, maxCount, dealCountEnabled | ✅ | 非空 |
| 线索转化状态 | `TransformStatus` | `Boolean value` | ✅ | 非空 |
| 商机阶段 | `BusinessStatusRef` | statusTypeId, statusId, endStatus | ✅ | 状态校验 |
| 商机商品项 | `BusinessProductItem` | productId, price, count, totalPrice | ✅ | 正数校验 |
| 合同审核状态 | `AuditStatus` | `Integer code` | ✅ | DRAFT/PROCESS/APPROVE/REJECT |
| 合同商品项 | `ContractProductItem` | productId, price, count, discount, totalPrice | ✅ | 正数校验 |
| 回款金额 | `ReceivablePrice` | `BigDecimal value` | ✅ | 正数、不超过可用额度 |
| 回款审核状态 | `ReceivableAuditStatus` | `Integer code` | ✅ | DRAFT/PROCESS/APPROVE/REJECT |
| 权限级别 | `CrmPermissionLevel` | `Integer level` | ✅ | OWNER/WRITE/READ |
| 业务类型 | `BizType` | `Integer type` | ✅ | CRM预定义类型之一 |
| 客户创建来源 | `CustomerCreateSource` | `Integer source` | ✅ | 枚举值校验 |
| 跟进内容 | `FollowUpContent` | `String content` | ✅ | 非空 |
| 产品金额 | `Money` | `BigDecimal value` | ✅ | 非负、精度2位 |
| 折扣百分比 | `DiscountPercent` | `BigDecimal value` | ✅ | 0-100范围 |

### 3.4 仓储接口（Repository，领域层）

每个聚合根对应一个 Repository 接口，定义在 `domain/{aggregate}/repository/` 包：

```java
// 客户
public interface CustomerRepository {
    Customer save(Customer customer);
    void delete(CustomerId id);
    Customer findById(CustomerId id);
    List<Customer> findByIds(Collection<CustomerId> ids);
    PageResult<Customer> findPage(CustomerPageQuery query);
    boolean existsByName(CustomerName name);
    long countByOwnerUserId(Long ownerUserId);
    long countByDealStatusAndOwnerUserId(DealStatus dealStatus, Long ownerUserId);
    long countByLockStatusAndOwnerUserId(LockStatus lockStatus, Long ownerUserId);
    long countByCustomerId(Long customerId); // 用于删除校验引用
    List<Customer> findByAutoPoolConfig(PoolConfig config); // 自动公海
    void updateOwnerUserId(CustomerId id, Long ownerUserId);
    void updateBatch(List<Customer> customers);
    void updateDealStatus(CustomerId id, DealStatus status);
    void updateFollowUp(CustomerId id, FollowUpData data);
    void updateLockStatus(CustomerId id, LockStatus status);
    PageResult<Customer> findPutPoolRemindPage(CustomerPageQuery query, PoolConfig config);
    PageResult<Customer> findPageByCustomerId(CustomerPageQuery query);
    long countByTodayContact(Long userId);
    long countByFollow(Long userId);
}

// 线索
public interface ClueRepository {
    Clue save(Clue clue);
    void delete(ClueId id);
    Clue findById(ClueId id);
    PageResult<Clue> findPage(CluePageQuery query);
    void updateTransformStatus(ClueId id, TransformStatus status, CustomerId customerId);
    void updateFollowUp(ClueId id, FollowUpData data);
    long countByFollow(Long userId);
}

// 联系人
public interface ContactRepository {
    Contact save(Contact contact);
    void delete(ContactId id);
    Contact findById(ContactId id);
    List<Contact> findByIds(Collection<ContactId> ids);
    List<Contact> findByCustomerId(CustomerId customerId);
    PageResult<Contact> findPage(ContactPageQuery query);
    PageResult<Contact> findPageByCustomerId(ContactPageQuery query);
    PageResult<Contact> findPageByBusinessId(ContactPageQuery query, Set<Long> contactIds);
    long countByCustomerId(CustomerId customerId);
    void updateOwnerUserIdByCustomerId(CustomerId customerId, Long ownerUserId);
    void updateBatch(List<Contact> contacts);
    void updateFollowUp(ContactId id, FollowUpData data);
    void updateContactNextTime(Collection<ContactId> ids, LocalDateTime nextTime);
    List<Contact> findByCustomerIdAndOwnerUserId(CustomerId customerId, Long ownerUserId);
}

// 商机
public interface BusinessRepository {
    Business save(Business business);
    void delete(BusinessId id);
    Business findById(BusinessId id);
    PageResult<Business> findPage(BusinessPageQuery query);
    PageResult<Business> findPageByCustomerId(BusinessPageQuery query);
    PageResult<Business> findPageByContactId(BusinessPageQuery query, Set<Long> businessIds);
    long countByCustomerId(CustomerId customerId);
    long countByStatusTypeId(Long statusTypeId);
    long countByBusinessId(BusinessId businessId); // 合同关联校验
    void updateStatus(BusinessId id, Long statusId, Integer endStatus);
    void updateOwnerUserId(BusinessId id, Long ownerUserId);
    void updateFollowUp(BusinessId id, FollowUpData data);
    void updateContactNextTime(Collection<BusinessId> ids, LocalDateTime nextTime);
    List<Business> findByCustomerIdAndOwnerUserId(CustomerId customerId, Long ownerUserId);
    PageResult<Business> findPageByDate(FunnelQuery query);
    List<BusinessProduct> findProductsByBusinessId(BusinessId businessId);
}

// 合同
public interface ContractRepository {
    Contract save(Contract contract);
    void delete(ContractId id);
    Contract findById(ContractId id);
    Contract findByNo(String no);
    PageResult<Contract> findPage(ContractPageQuery query);
    PageResult<Contract> findPageByCustomerId(ContractPageQuery query);
    PageResult<Contract> findPageByBusinessId(ContractPageQuery query);
    long countByCustomerId(CustomerId customerId);
    long countByBusinessId(BusinessId businessId);
    long countByContactId(ContactId contactId);
    long countByAudit(Long userId);
    long countByRemind(Long userId, ContractConfig config);
    void submitAudit(ContractId id, String processInstanceId);
    void updateAuditStatus(ContractId id, AuditStatus status);
    void updateOwnerUserId(ContractId id, Long ownerUserId);
    void updateFollowUp(ContractId id, LocalDateTime contactLastTime);
    List<Contract> findByCustomerIdAndOwnerUserId(CustomerId customerId, Long ownerUserId);
    List<ContractProduct> findProductsByContractId(ContractId contractId);
}

// 回款
public interface ReceivableRepository {
    Receivable save(Receivable receivable);
    void delete(ReceivableId id);
    Receivable findById(ReceivableId id);
    Receivable findByNo(String no);
    PageResult<Receivable> findPage(ReceivablePageQuery query);
    long countByContractId(ContractId contractId);
    BigDecimal sumByContractId(ContractId contractId); // 计算已回款总额
    void submitAudit(ReceivableId id, String processInstanceId);
    void updateAuditStatus(ReceivableId id, AuditStatus status);
}

// CRM权限
public interface CrmPermissionRepository {
    CrmPermission save(CrmPermission permission);
    void saveBatch(List<CrmPermission> permissions);
    void delete(CrmPermissionId id);
    void deleteByIds(Collection<Long> ids);
    void deleteByBiz(Integer bizType, Long bizId);
    void deleteByBizAndLevel(Integer bizType, Long bizId, Integer level);
    void updateLevel(CrmPermissionId id, Integer level);
    void updateBatch(List<CrmPermission> permissions);
    CrmPermission findById(CrmPermissionId id);
    CrmPermission findByBizAndUser(Integer bizType, Long bizId, Long userId);
    List<CrmPermission> findByBiz(Integer bizType, Long bizId);
    List<CrmPermission> findByBizAndUserIds(Integer bizType, Collection<Long> bizIds);
    List<CrmPermission> findByBizTypeAndUserId(Integer bizType, Long userId);
    boolean existsByBizAndUser(Integer bizType, Long bizId, Long userId);
    boolean hasPermission(Integer bizType, Long bizId, Long userId, Integer level);
    long countByBizAndUsers(Integer bizType, Collection<Long> bizIds, Collection<Long> userIds);
}

// 跟进记录
public interface FollowUpRecordRepository {
    FollowUpRecord save(FollowUpRecord record);
    void saveBatch(List<FollowUpRecord> records);
    void deleteByBiz(Integer bizType, Long bizId);
    List<FollowUpRecord> findByBiz(Integer bizType, Collection<Long> bizIds);
}
```

### 3.5 领域服务（Domain Service）

| 领域服务 | 职责 | 原因 |
|---------|------|------|
| `CustomerUniquenessChecker` | 检查客户名称是否唯一 | 跨聚合查询 |
| `CustomerOwnerLimitChecker` | 检查用户拥有的客户/锁定客户数量是否超限 | 需要查询限额配置+当前数量 |
| `CustomerPoolDomainService` | 公海领取/放入的领域逻辑 | 涉及多个聚合（客户+联系人+权限） |
| `ClueTransformDomainService` | 线索转客户的领域编排 | 跨线索+客户+跟进记录聚合 |
| `ContractPriceCalculator` | 合同总价计算（产品总价-折扣） | 金额计算是领域核心逻辑 |
| `BusinessPriceCalculator` | 商机总价计算 | 同上 |
| `ReceivablePriceValidator` | 回款金额校验（不超过可用额度） | 需要查询合同已回款总额 |
| `CrmPermissionValidator` | 数据权限校验 | 跨聚合的权限查询 |
| `BizTypeTransferDomainService` | 客户转移时同步转移关联业务 | 涉及客户+联系人+商机+合同多个聚合 |

### 3.6 领域事件（Domain Events）

| 事件 | 触发时机 | 携带数据 | 消费者 |
|------|---------|---------|--------|
| `CustomerCreatedEvent` | 客户创建后 | customerId, name, ownerUserId | 操作日志 |
| `CustomerDealStatusChangedEvent` | 成交状态变更后 | customerId, dealStatus | 统计更新 |
| `CustomerLockedEvent` / `CustomerUnlockedEvent` | 锁定/解锁后 | customerId, userId | 操作日志 |
| `CustomerPutPoolEvent` | 客户放入公海后 | customerId, name | 通知、操作日志 |
| `CustomerReceivedEvent` | 客户从公海领出后 | customerId, ownerUserId | 通知、操作日志 |
| `CustomerDeletedEvent` | 客户删除后 | customerId | 清理联系人权限 |
| `CustomerTransferredEvent` | 客户转移后 | customerId, oldOwner, newOwner | 操作日志 |
| `ClueTransformedEvent` | 线索转客户后 | clueId, customerId | 操作日志 |
| `ClueDeletedEvent` | 线索删除后 | clueId | 清理跟进记录 |
| `ContactDeletedEvent` | 联系人删除后 | contactId | 清理商机关联 |
| `BusinessStatusChangedEvent` | 商机阶段变更后 | businessId, oldStatus, newStatus | 漏斗统计 |
| `BusinessDeletedEvent` | 商机删除后 | businessId | 清理商品关联 |
| `ContractSubmittedEvent` | 合同提交审批后 | contractId, processInstanceId | BPM流程跟踪 |
| `ContractAuditedEvent` | 合同审批完成后 | contractId, auditStatus | 通知 |
| `ContractDeletedEvent` | 合同删除后 | contractId | 清理商品关联 |
| `ReceivableSubmittedEvent` | 回款提交审批后 | receivableId, processInstanceId | BPM流程跟踪 |
| `ReceivableAuditedEvent` | 回款审批完成后 | receivableId, auditStatus | 更新合同已回款金额 |
| `PermissionChangedEvent` | 数据权限转移/变更后 | bizType, bizId, userId, level | 操作日志 |

**注意**：当前代码直接通过 mapper 操作数据，领域事件尚未实现。重构时需引入事件发布机制。

### 3.7 工厂（Factory）

| 工厂 | 职责 |
|------|------|
| `CustomerFactory` | 创建 Customer 聚合（含初始化状态：未成交、未锁定、未跟进、负责人时间戳） |
| `ClueFactory` | 创建 Clue 聚合（含默认未转化状态） |
| `ContactFactory` | 创建 Contact 聚合（可选关联商机） |
| `BusinessFactory` | 创建 Business 聚合（含默认第一阶段状态、商机商品项） |
| `ContractFactory` | 创建 Contract 聚合（含合同编号、审核状态草稿、合同商品项、总价计算） |
| `ReceivableFactory` | 创建 Receivable 聚合（含回款编号、审核状态草稿、回款计划关联） |

## 4. 职责边界

### 4.1 各聚合根必须负责的规则

#### Customer 聚合

| 规则编号 | 规则描述 | 对应原代码位置 |
|---------|---------|-------------|
| R-C01 | 创建时默认状态：未成交、未锁定、未跟进 | `initCustomer()` L119-122 |
| R-C02 | 创建/转移时校验负责人是否超限（拥有客户数量） | `validateCustomerExceedOwnerLimit()` L613-627 |
| R-C03 | 锁定客户时校验锁定数量上限 | `validateCustomerExceedLockLimit()` L634-646 |
| R-C04 | 成交状态变更必须与当前状态不同 | `updateCustomerDealStatus()` L153-154 |
| R-C05 | 锁定/解锁必须与当前状态不同 | `lockCustomer()` L259-261 |
| R-C06 | 删除前校验是否被联系人/商机/合同引用 | `validateCustomerReference()` L551-561 |
| R-C07 | 放入公海前校验是否有负责人且未锁定 | `validateCustomerOwnerExists()` L573-585, `validateCustomerIsLocked()` L595-599 |
| R-C08 | 领取公海前校验无负责人、未锁定、未成交 | `receiveCustomer()` L387-406 |
| R-C09 | 转移时若勾选"同时转移"，则同步转移联系人/商机/合同 | `transferCustomer()` L206-221, `transfer()` L233-249 |
| R-C10 | 更新时不可修改负责人 | `updateCustomer()` L131 |
| R-C11 | 导入时客户名称不可为空，已存在且不允许更新时报错 | `validateCustomerForCreate()` L539-544, `importCustomerList()` L330-334 |
| R-C12 | 自动放入公海受公海池配置控制（启用开关、天数等） | `autoPutCustomerPool()` L435-453 |

#### Clue 聚合

| 规则编号 | 规则描述 | 对应原代码位置 |
|---------|---------|-------------|
| R-CL01 | 创建时设置负责人并创建OWNER级数据权限 | `createClue()` L70-88 |
| R-CL02 | 线索转化后不可重复转化 | `transformClue()` L186-188 |
| R-CL03 | 线索转化为客户时，复制跟进记录到客户 | `transformClue()` L194-201 |
| R-CL04 | 删除线索时同步删除跟进记录和数据权限 | `deleteClue()` L141-155 |
| R-CL05 | 更新后保持原有负责人不变（操作日志避免"删除负责人"） | `updateClue()` L107 |

#### Contact 聚合

| 规则编号 | 规则描述 | 对应原代码位置 |
|---------|---------|-------------|
| R-CO01 | 创建时可关联指定商机 | `createContact()` L91-94 |
| R-CO02 | 删除时校验是否关联合同 | `deleteContact()` L155-157 |
| R-CO03 | 删除时同步删除商机关联和数据权限 | `deleteContact()` L163-165 |
| R-CO04 | 客户放入公海时，该客户下联系人负责人同步清空 | `putCustomerPool()` L464 |
| R-CO05 | 更新后保持原有负责人不变 | `updateContact()` L117 |

#### Business 聚合

| 规则编号 | 规则描述 | 对应原代码位置 |
|---------|---------|-------------|
| R-B01 | 创建时默认使用商机状态类型的第一阶段 | `createBusiness()` L98 |
| R-B02 | 商机总价 = 商品总价 - 折扣 | `calculateTotalPrice()` L215-219 |
| R-B03 | 已结束的商机不可再变更状态 | `updateBusinessStatus()` L229-231 |
| R-B04 | 状态变更不能与当前相同 | `updateBusinessStatus()` L238-241 |
| R-B05 | 创建时若传了contactId，需创建联系人-商机关联 | `createBusiness()` L113-116 |
| R-B06 | 删除时校验是否有关联合同 | `validateContractExists()` L280-284 |
| R-B07 | 更新时不可修改负责人和状态类型 | `updateBusiness()` L129 |

#### Contract 聚合

| 规则编号 | 规则描述 | 对应原代码位置 |
|---------|---------|-------------|
| R-CT01 | 创建时自动生成合同编号（Redis自增） | `createContract()` L109 |
| R-CT02 | 合同编号全局唯一 | `createContract()` L110-111 |
| R-CT03 | 合同总价 = 产品总价 - 折扣金额 | `calculateTotalPrice()` L218-222 |
| R-CT04 | 产品项总价 = 单价 * 数量 | `validateContractProducts()` L210-216 |
| R-CT05 | 只有草稿/审批中状态的合同可编辑 | `updateContract()` L145-148 |
| R-CT06 | 只有草稿状态的合同可提交审批 | `submitContract()` L295-297 |
| R-CT07 | 只有审批中状态的合同可更新审批结果 | `updateContractAuditStatus()` L316-319 |
| R-CT08 | 删除时校验是否被回款引用 | `deleteContract()` L233-235 |
| R-CT09 | 合同审批流程标识为 `crm-contract-audit` | `BPM_PROCESS_DEFINITION_KEY` L69 |
| R-CT10 | 签约联系人、签约人、客户、商机关联需校验存在性 | `validateRelationDataExists()` L188-208 |

#### Receivable 聚合

| 规则编号 | 规则描述 | 对应原代码位置 |
|---------|---------|-------------|
| R-R01 | 回款编号全局唯一（Redis自增） | `createReceivable()` L91 |
| R-R02 | 回款金额不可超过合同可用回款额度 | `createReceivable()` — `validateReceivablePriceExceedsLimit()` |
| R-R03 | 默认审核状态为草稿 | `createReceivable()` L98 |
| R-R04 | 只有草稿状态的回款可提交审批 | 同 Contract 逻辑 |
| R-R05 | 只有审批中状态的回款可更新审批结果 | 同 Contract 逻辑 |
| R-R06 | 回款审批流程标识为 `crm-receivable-audit` | `BPM_PROCESS_DEFINITION_KEY` L60 |

#### CrmPermission 聚合

| 规则编号 | 规则描述 | 对应原代码位置 |
|---------|---------|-------------|
| R-P01 | 同一(bizType, bizId, userId)不可重复创建权限 | `validatePermissionNotExists()` L196-204 |
| R-P02 | 转移时只有负责人可操作（或超管） | `transferPermission()` L210-216 |
| R-P03 | 转移时若新用户已有权限则升级为OWNER，否则新增 | `transferPermission()` L225-245 |
| R-P04 | 转移时旧负责人可降级（保留READ/WRITE）或删除 | `transferPermission()` L239-244 |
| R-P05 | 删除权限时操作人必须是负责人 | `deletePermissionBatch()` L280-286 |
| R-P06 | 用户不可自我删除负责人权限 | `deleteSelfPermission()` L300-302 |
| R-P07 | 同时添加至联系人/商机/合同时需校验被添加人是否已有权限 | `createBizTypePermissions()` L140-149 |

#### FollowUpRecord 聚合

| 规则编号 | 规则描述 | 对应原代码位置 |
|---------|---------|-------------|
| R-F01 | 跟进记录按(bizType, bizId)关联业务 | `createFollowUpRecord()` |
| R-F02 | 线索转客户时跟进记录批量复制（bizType从线索变为客户） | `transformClue()` L194-201 |

### 4.2 严禁外泄的职责（不可放在各聚合内）

| 禁止行为 | 原因 | 应由谁处理 |
|---------|------|----------|
| 直接操作数据库/调用 Mapper | 破坏持久化无关性 | Repository 实现 |
| 调用 BPM 工作流 API 创建审批流程 | 基础设施关注点 | ApplicationService |
| 调用 AdminUserApi 校验用户存在性 | User 是独立聚合 | 应用层 |
| 调用文件服务上传/下载 | 基础设施关注点 | 应用层/基础设施 |
| Excel 导入导出逻辑 | 基础设施/应用关注点 | 应用层 Service |
| 记录操作日志（LogRecord 注解） | 基础设施关注点 | AOP 切面或应用层 |
| 生成 Redis 自增编号 | 基础设施关注点 | 应用层传入 |
| 数据权限注解解析（@CrmPermission） | 基础设施关注点 | AOP 切面 |
| 对象属性差异对比（操作日志 Diff） | 基础设施关注点 | 操作日志组件 |

## 5. 依赖与协作

### 5.1 领域层依赖（向内）

各聚合根仅依赖：
- 自身值对象
- 自身仓储接口
- 相关领域服务接口（如 CustomerOwnerLimitChecker）
- 领域事件发布器

### 5.2 跨聚合协作（仅通过 ID 引用）

| 源聚合 | 目标聚合 | 引用方式 | 协作场景 |
|-------|---------|---------|---------|
| Customer | CrmPermission | bizType=CRM_CUSTOMER, bizId=customerId | 数据权限管理 |
| Customer | Contact | customerId | 联系人列表/批量清空负责人 |
| Customer | Business | customerId | 商机列表/删除校验 |
| Customer | Contract | customerId | 合同列表/删除校验 |
| Clue | Customer | 转化后customerId | 线索→客户转化 |
| Clue | FollowUpRecord | bizType=CRM_CLUE, bizId=clueId | 跟进记录复制 |
| Contact | Business | contactId→ContactBusiness关联表 | 商机关联 |
| Contact | Contract | signContactId | 签约联系人 |
| Business | Contact | ContactBusiness关联 | 联系人列表 |
| Business | Contract | businessId | 合同列表/删除校验 |
| Contract | Receivable | contractId | 回款列表/删除校验 |
| Customer | CustomerLimitConfig | ownerUserId | 限额校验 |

### 5.3 基础设施依赖（向外，通过接口倒置）

```
领域层定义接口                              基础设施层实现
─────────────                              ──────────────
CustomerRepository              ←──        CustomerRepositoryImpl (委托 CrmCustomerMapper)
ClueRepository                  ←──        ClueRepositoryImpl (委托 CrmClueMapper)
ContactRepository               ←──        ContactRepositoryImpl (委托 CrmContactMapper)
BusinessRepository              ←──        BusinessRepositoryImpl (委托 CrmBusinessMapper + CrmBusinessProductMapper)
ContractRepository              ←──        ContractRepositoryImpl (委托 CrmContractMapper + CrmContractProductMapper)
ReceivableRepository            ←──        ReceivableRepositoryImpl (委托 CrmReceivableMapper)
CrmPermissionRepository         ←──        CrmPermissionRepositoryImpl (委托 CrmPermissionMapper)
FollowUpRecordRepository        ←──        FollowUpRecordRepositoryImpl (委托 CrmFollowUpRecordMapper)
CustomerOwnerLimitChecker       ←──        CustomerOwnerLimitCheckerImpl (委托 CrmCustomerLimitConfigMapper + CrmCustomerMapper)
BizTypeTransferDomainService    ←──        BizTypeTransferDomainServiceImpl (编排 Contact/Business/Contract 服务)
DomainEventPublisher            ←──        SpringDomainEventPublisher (委托 Spring ApplicationEventPublisher)
```

## 6. 不变式与约束（Invariants）

### Customer 聚合

| 编号 | 不变式 | 类型 | 验证点 |
|------|--------|------|--------|
| I-C01 | `name` 在全局不可重复 | 跨聚合唯一性 | 创建/导入时 |
| I-C02 | 同一用户的客户拥有数量不得超过限额配置（含成交客户统计开关） | 跨聚合约束 | 创建/转移时 |
| I-C03 | 同一用户的锁定客户数量不得超过锁定限额配置 | 跨聚合约束 | 锁定客户时 |
| I-C04 | 已锁定客户不可放入公海 | 聚合内部 | 放入公海时 |
| I-C05 | 已有负责人的客户不可再被领取 | 聚合内部 | 领取公海时 |
| I-C06 | 已成交客户不可被领取公海 | 聚合内部 | 领取公海时 |
| I-C07 | `dealStatus` 不可回退（只允许未成交→成交单向） | 聚合内部 | 状态变更时 |
| I-C08 | 删除时必须确保不被联系人/商机/合同引用 | 跨聚合约束 | 删除时 |
| I-C09 | 公海池的自动放入受配置控制（启用、天数阈值、通知开关） | 应用层约束 | 定时任务执行时 |

### Clue 聚合

| 编号 | 不变式 | 类型 | 验证点 |
|------|--------|------|--------|
| I-CL01 | 已转化的线索不可再转化 | 聚合内部 | 转化时 |
| I-CL02 | 转化后必须关联已创建的客户ID | 跨聚合约束 | 转化后 |

### Contact 聚合

| 编号 | 不变式 | 类型 | 验证点 |
|------|--------|------|--------|
| I-CO01 | 删除时必须确保不被合同引用 | 跨聚合约束 | 删除时 |
| I-CO02 | `parentId` 可空，若提供则必须引用存在的联系人 | 聚合内部 | 创建/更新时 |

### Business 聚合

| 编号 | 不变式 | 类型 | 验证点 |
|------|--------|------|--------|
| I-B01 | `statusId` 必须属于 `statusTypeId` 定义的阶段集合 | 跨聚合约束 | 创建/更新时 |
| I-B02 | 已结束的商机（`endStatus` 非空）不可再变更状态 | 聚合内部 | 状态变更时 |
| I-B03 | 删除时必须确保不被合同引用 | 跨聚合约束 | 删除时 |

### Contract 聚合

| 编号 | 不变式 | 类型 | 验证点 |
|------|--------|------|--------|
| I-CT01 | `no` 在全局唯一 | 聚合内部 | 创建时 |
| I-CT02 | 只有 `DRAFT` 状态的合同可编辑或提交审批 | 聚合内部 | 编辑/提交时 |
| I-CT03 | 只有 `PROCESS` 状态的合同可更新审批结果 | 聚合内部 | 审批回调时 |
| I-CT04 | 删除时必须确保不被回款引用 | 跨聚合约束 | 删除时 |
| I-CT05 | `totalPrice = totalProductPrice - discountPrice` | 聚合内部 | 创建/更新时 |

### Receivable 聚合

| 编号 | 不变式 | 类型 | 验证点 |
|------|--------|------|--------|
| I-R01 | `no` 在全局唯一 | 聚合内部 | 创建时 |
| I-R02 | 回款金额 <= 合同总金额 - 已回款总额 | 跨聚合约束 | 创建时 |
| I-R03 | `auditStatus` 从 DRAFT→PROCESS→APPROVE/REJECT 单向流转 | 聚合内部 | 状态变更时 |

### CrmPermission 聚合

| 编号 | 不变式 | 类型 | 验证点 |
|------|--------|------|--------|
| I-P01 | 同一(bizType, bizId, userId)组合唯一 | 聚合内部 | 创建时 |
| I-P02 | 每个(bizType, bizId)有且仅有一个OWNER | 聚合内部 | 转移时 |
| I-P03 | `level` 只能是 OWNER(1)/WRITE(2)/READ(3) | 聚合内部 | 创建/更新时 |

## 7. 验收标准

| 编号 | 验收标准 | 验证方法 |
|------|---------|---------|
| AC01 | 所有聚合根类不包含 MyBatis（`@TableName`, `@TableId`）或 Spring 注解 | 代码审查 |
| AC02 | 所有值对象为不可变类（final class, final 字段, 无 setter），构造方法自校验 | 代码审查 |
| AC03 | 所有仓储接口定义在 `domain/{aggregate}/repository/` 包，不 import 任何 MyBatis/Spring 类 | 代码审查 |
| AC04 | 所有仓储实现在 `infrastructure/{aggregate}/` 包，负责 DO↔领域模型映射 | 代码审查 |
| AC05 | Customer 的 `ownerUserId` 和 `ownerTime` 通过工厂方法构造，不在 Service 中手动 set | 代码审查 |
| AC06 | 客户拥有者数量/锁定数量校验通过 CustomerOwnerLimitChecker 领域服务完成 | 代码审查 |
| AC07 | 公海放入/领取的编排逻辑在 CustomerPoolDomainService 领域服务中 | 代码审查 |
| AC08 | 线索转客户的编排逻辑在 ClueTransformDomainService 中 | 代码审查 |
| AC09 | 合同/回款提交审批的 BPM API 调用在 ApplicationService 中，不在聚合根内 | 代码审查 |
| AC10 | CrmPermission 聚合的 transfer 方法封装完整的转移业务规则（R-P02~R-P04） | 代码审查 |
| AC11 | CRUD 操作日志通过领域事件发布，而非在 Service 中直接 LogRecord 注解 | 代码审查 |
| AC12 | 更新操作避免修改 ownerUserId（在应用层显式排除） | 代码审查 |
| AC13 | Customer 的成交/锁定/跟进状态通过行为方法变更（`markDeal()`, `lock()`, `unlock()`），而非直接 setter | 代码审查 |
| AC14 | Controller 调用 ApplicationService，ApplicationService 编排聚合操作 | 代码审查 |
| AC15 | 所有使用 `@CrmPermission` 注解的权限校验由 AOP 切面处理 | 代码审查 |

## 8. 目录结构规划（重构后）

```
develop-module-crm/develop-module-crm-server/src/main/java/com/develop/mvp/pk/module/crm/
├── domain/
│   ├── customer/
│   │   ├── Customer.java                         # 聚合根
│   │   ├── CustomerFactory.java                  # 工厂
│   │   ├── valueobject/
│   │   │   ├── CustomerName.java
│   │   │   ├── DealStatus.java
│   │   │   ├── LockStatus.java
│   │   │   ├── FollowUpStatus.java
│   │   │   ├── CustomerCreateSource.java
│   │   │   └── ContactNextTime.java
│   │   ├── event/
│   │   │   ├── CustomerCreatedEvent.java
│   │   │   ├── CustomerDealStatusChangedEvent.java
│   │   │   ├── CustomerLockedEvent.java
│   │   │   ├── CustomerPutPoolEvent.java
│   │   │   ├── CustomerReceivedEvent.java
│   │   │   ├── CustomerDeletedEvent.java
│   │   │   └── CustomerTransferredEvent.java
│   │   ├── service/
│   │   │   ├── CustomerUniquenessChecker.java
│   │   │   ├── CustomerOwnerLimitChecker.java
│   │   │   └── CustomerPoolDomainService.java
│   │   └── repository/
│   │       ├── CustomerRepository.java
│   │       └── CustomerPageQuery.java
│   ├── clue/
│   │   ├── Clue.java
│   │   ├── ClueFactory.java
│   │   ├── valueobject/
│   │   │   ├── TransformStatus.java
│   │   │   └── ClueSource.java
│   │   ├── event/
│   │   │   ├── ClueCreatedEvent.java
│   │   │   ├── ClueTransformedEvent.java
│   │   │   └── ClueDeletedEvent.java
│   │   ├── service/
│   │   │   └── ClueTransformDomainService.java
│   │   └── repository/
│   │       ├── ClueRepository.java
│   │       └── CluePageQuery.java
│   ├── contact/
│   │   ├── Contact.java
│   │   ├── ContactFactory.java
│   │   ├── valueobject/
│   │   │   └── ContactInfo.java
│   │   ├── event/
│   │   │   ├── ContactCreatedEvent.java
│   │   │   ├── ContactDeletedEvent.java
│   │   │   └── ContactTransferredEvent.java
│   │   └── repository/
│   │       ├── ContactRepository.java
│   │       └── ContactPageQuery.java
│   ├── business/
│   │   ├── Business.java
│   │   ├── BusinessFactory.java
│   │   ├── BusinessProduct.java                # 聚合内部实体
│   │   ├── valueobject/
│   │   │   ├── BusinessStatusRef.java
│   │   │   ├── BusinessProductItem.java
│   │   │   └── DiscountPercent.java
│   │   ├── event/
│   │   │   ├── BusinessCreatedEvent.java
│   │   │   ├── BusinessStatusChangedEvent.java
│   │   │   └── BusinessDeletedEvent.java
│   │   ├── service/
│   │   │   └── BusinessPriceCalculator.java
│   │   └── repository/
│   │       ├── BusinessRepository.java
│   │       └── BusinessPageQuery.java
│   ├── contract/
│   │   ├── Contract.java
│   │   ├── ContractFactory.java
│   │   ├── ContractProduct.java                # 聚合内部实体
│   │   ├── valueobject/
│   │   │   ├── AuditStatus.java
│   │   │   ├── ContractProductItem.java
│   │   │   └── Money.java
│   │   ├── event/
│   │   │   ├── ContractCreatedEvent.java
│   │   │   ├── ContractSubmittedEvent.java
│   │   │   ├── ContractAuditedEvent.java
│   │   │   └── ContractDeletedEvent.java
│   │   ├── service/
│   │   │   └── ContractPriceCalculator.java
│   │   └── repository/
│   │       ├── ContractRepository.java
│   │       └── ContractPageQuery.java
│   ├── receivable/
│   │   ├── Receivable.java
│   │   ├── ReceivableFactory.java
│   │   ├── valueobject/
│   │   │   ├── ReceivablePrice.java
│   │   │   └── ReceivableAuditStatus.java
│   │   ├── event/
│   │   │   ├── ReceivableCreatedEvent.java
│   │   │   ├── ReceivableSubmittedEvent.java
│   │   │   ├── ReceivableAuditedEvent.java
│   │   │   └── ReceivableDeletedEvent.java
│   │   ├── service/
│   │   │   └── ReceivablePriceValidator.java
│   │   └── repository/
│   │       ├── ReceivableRepository.java
│   │       └── ReceivablePageQuery.java
│   ├── permission/
│   │   ├── CrmPermission.java
│   │   ├── valueobject/
│   │   │   ├── CrmPermissionLevel.java
│   │   │   └── BizType.java
│   │   ├── event/
│   │   │   └── PermissionChangedEvent.java
│   │   ├── service/
│   │   │   └── CrmPermissionValidator.java
│   │   └── repository/
│   │       └── CrmPermissionRepository.java
│   └── followup/
│       ├── FollowUpRecord.java
│       ├── valueobject/
│       │   └── FollowUpContent.java
│       └── repository/
│           └── FollowUpRecordRepository.java
├── application/
│   ├── customer/CustomerApplicationService.java
│   ├── clue/ClueApplicationService.java
│   ├── contact/ContactApplicationService.java
│   ├── business/BusinessApplicationService.java
│   ├── contract/ContractApplicationService.java
│   ├── receivable/ReceivableApplicationService.java
│   ├── permission/PermissionApplicationService.java
│   └── followup/FollowUpRecordApplicationService.java
├── infrastructure/
│   ├── customer/CustomerRepositoryImpl.java
│   ├── clue/ClueRepositoryImpl.java
│   ├── contact/ContactRepositoryImpl.java
│   ├── business/BusinessRepositoryImpl.java
│   ├── contract/ContractRepositoryImpl.java
│   ├── receivable/ReceivableRepositoryImpl.java
│   ├── permission/CrmPermissionRepositoryImpl.java
│   └── followup/FollowUpRecordRepositoryImpl.java
├── controller/
│   ├── admin/customer/CustomerController.java
│   ├── admin/clue/ClueController.java
│   ├── admin/contact/ContactController.java
│   ├── admin/business/BusinessController.java
│   ├── admin/contract/ContractController.java
│   ├── admin/receivable/ReceivableController.java
│   ├── admin/permission/PermissionController.java
│   └── admin/followup/FollowUpRecordController.java
├── dal/
│   ├── dataobject/           # 保留（DO 映射）
│   ├── mysql/                # 保留（Mapper 接口）
│   └── redis/                # 保留（Redis DAO）
└── convert/                  # 保留（转换器）
```

## 9. 回滚条件

如果以下任一情况发生，应回滚当前修改并重新分析：

1. `@CrmPermission` 注解的 AOP 权限校验失效，导致未授权用户可以操作
2. 客户名称唯一性校验被绕过，出现重复客户名称
3. 公海领取/放入逻辑违反业务规则（如已锁定客户被放入公海）
4. 合同/回款审批流程状态流转出错（如已审批通过的合同可以再次审批）
5. 数据权限转移时负责人变更未正确同步
6. 线索转客户时跟进记录未正确复制
7. 删除校验（引用检查）被绕过，导致引用数据被级联非预期删除

## 10. 分步执行计划

**阶段 1**：创建所有域的值对象（CustomerName, DealStatus, LockStatus, AuditStatus, CrmPermissionLevel, BizType, Money, DiscountPercent 等）

**阶段 2**：创建独立的聚合根（逐个进行）
- 2a：CrmPermission 聚合（最底层依赖，先做）
- 2b：Customer 聚合（核心聚合）
- 2c：FollowUpRecord 聚合
- 2d：Clue 聚合
- 2e：Contact 聚合
- 2f：Business 聚合（含 BusinessProduct 内部实体）
- 2g：Contract 聚合（含 ContractProduct 内部实体）
- 2h：Receivable 聚合

**阶段 3**：为每个聚合创建领域服务接口 + 仓储接口

**阶段 4**：创建领域事件类

**阶段 5**：为每个聚合创建工厂类

**阶段 6**：实现基础设施层（RepositoryImpl）

**阶段 7**：创建应用层（ApplicationService），从现有 ServiceImpl 迁移编排逻辑

**阶段 8**：精简原有 Service，适配 Controller 调用新的 ApplicationService

**阶段 9**：更新测试，确保回归通过

**阶段 10**：验证所有 @CrmPermission 注解正常工作，审批流程正确流转
