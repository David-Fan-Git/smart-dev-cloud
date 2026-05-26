---
name: service-layer
description: Service interface + implementation with validation, transactions, read-write splitting, batch ops, and orchestration
type: project
---

# Service 层模式

## 概述

Service 层承载所有业务逻辑。每个业务模块由 Interface + Impl 组成。使用 validate 方法做前置校验，通过 `ServiceExceptionUtil.exception()` 抛出业务异常。支持事务、读写分离、批量操作、复杂编排。

## 目录结构

```
service/{domain}/
├── {Domain}Service.java           # 接口
└── impl/
    └── {Domain}ServiceImpl.java   # 实现
```

## 标准接口

```java
package com.develop.mvp.pk.module.{module}.service.{domain};

import com.develop.mvp.pk.module.{module}.controller.admin.{domain}.vo.{Domain}PageReqVO;
import com.develop.mvp.pk.module.{module}.controller.admin.{domain}.vo.{Domain}SaveReqVO;
import com.develop.mvp.pk.module.{module}.dal.dataobject.{domain}.{Domain}DO;
import com.develop.mvp.pk.framework.common.pojo.PageResult;

import java.util.List;

public interface {Domain}Service {

    Long create{Domain}({Domain}SaveReqVO createReqVO);

    void update{Domain}({Domain}SaveReqVO updateReqVO);

    void delete{Domain}(Long id);

    void delete{Domain}List(List<Long> ids);

    {Domain}DO get{Domain}(Long id);

    PageResult<{Domain}DO> get{Domain}Page({Domain}PageReqVO pageReqVO);

    List<{Domain}DO> get{Domain}List();

    List<{Domain}DO> get{Domain}List({Domain}ListReqVO listReqVO);
}
```

## 标准实现

```java
package com.develop.mvp.pk.module.{module}.service.{domain}.impl;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.{module}.controller.admin.{domain}.vo.{Domain}PageReqVO;
import com.develop.mvp.pk.module.{module}.controller.admin.{domain}.vo.{Domain}SaveReqVO;
import com.develop.mvp.pk.module.{module}.dal.dataobject.{domain}.{Domain}DO;
import com.develop.mvp.pk.module.{module}.dal.mysql.{domain}.{Domain}Mapper;
import com.develop.mvp.pk.module.{module}.service.{domain}.{Domain}Service;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.develop.mvp.pk.module.{module}.enums.ErrorCodeConstants.*;

@Service
@Validated
public class {Domain}ServiceImpl implements {Domain}Service {

    @Resource
    private {Domain}Mapper {domain}Mapper;

    @Override
    public Long create{Domain}({Domain}SaveReqVO createReqVO) {
        // 1. 前置业务校验
        validate{Domain}ForCreate(createReqVO);
        // 2. VO → DO 转换
        {Domain}DO entity = BeanUtils.toBean(createReqVO, {Domain}DO.class);
        // 3. 插入数据库
        {domain}Mapper.insert(entity);
        // 4. 返回主键
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update{Domain}({Domain}SaveReqVO updateReqVO) {
        // 1. 存在性校验 + 业务校验
        validate{Domain}ForUpdate(updateReqVO);
        // 2. VO → DO 转换（MyBatis Plus 动态 SQL 只更新非 null 字段）
        {Domain}DO entity = BeanUtils.toBean(updateReqVO, {Domain}DO.class);
        // 3. 更新数据库
        {domain}Mapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete{Domain}(Long id) {
        // 1. 存在性校验
        validate{Domain}Exists(id);
        // 2. 检查是否存在关联数据（如子节点）
        if ({domain}Mapper.selectCount({Domain}DO::getParentId, id) > 0) {
            throw ServiceExceptionUtil.exception({DOMAIN}_EXISTS_CHILDREN);
        }
        // 3. 逻辑删除（@TableLogic 自动处理）
        {domain}Mapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete{Domain}List(List<Long> ids) {
        for (Long id : ids) {
            delete{Domain}(id);
        }
    }

    @Override
    public {Domain}DO get{Domain}(Long id) {
        return validate{Domain}Exists(id);
    }

    @Override
    public PageResult<{Domain}DO> get{Domain}Page({Domain}PageReqVO pageReqVO) {
        return {domain}Mapper.selectPage(pageReqVO);
    }

    @Override
    public List<{Domain}DO> get{Domain}List() {
        return {domain}Mapper.selectList();
    }

    @Override
    public List<{Domain}DO> get{Domain}List({Domain}ListReqVO listReqVO) {
        return {domain}Mapper.selectList(listReqVO);
    }

    // ========== 校验方法 ==========

    private {Domain}DO validate{Domain}Exists(Long id) {
        if (id == null) {
            return null;
        }
        {Domain}DO entity = {domain}Mapper.selectById(id);
        if (entity == null) {
            throw ServiceExceptionUtil.exception({DOMAIN}_NOT_EXISTS);
        }
        return entity;
    }

    private void validate{Domain}ForCreate({Domain}SaveReqVO vo) {
        // 唯一性校验
        if ({domain}Mapper.selectByName(vo.getName()) != null) {
            throw ServiceExceptionUtil.exception({DOMAIN}_NAME_DUPLICATE, vo.getName());
        }
        // 父级存在性校验（树形结构）
        if (vo.getParentId() != null && vo.getParentId() > 0) {
            validate{Domain}Exists(vo.getParentId());
        }
        // 状态枚举校验
        if (vo.getStatus() != null) {
            CommonStatusEnum.validate(vo.getStatus());
        }
    }

    private void validate{Domain}ForUpdate({Domain}SaveReqVO vo) {
        validate{Domain}Exists(vo.getId());
        validate{Domain}ForCreate(vo);
    }
}
```

## @Transactional 事务指南

```java
// 写操作（create/update/delete）使用事务
@Transactional(rollbackFor = Exception.class) // 默认只回滚 RuntimeException，Exception 也需要回滚

// 只读事务优化（默认 Propagation.SUPPORTS + readOnly=true）
@Transactional(rollbackFor = Exception.class) // 写操作

// 使用 propagation 控制事务传播行为
@Transactional(propagation = Propagation.REQUIRES_NEW) // 新事务（日志记录等独立事务场景）
```

## 读写分离（@Master / @Slave）

```java
// 在 Mapper 方法上使用 @Master/@Slave 指定数据源
// 写操作默认使用 @Master

// Service 层面调用，MyBatis Plus 动态数据源根据方法上的注解自动路由
@Master  // 强制使用主库
public {Domain}DO get{Domain}(Long id) {
    return {domain}Mapper.selectById(id);
}

// 默认走从库（如果配置了从库）
public PageResult<{Domain}DO> get{Domain}Page({Domain}PageReqVO pageReqVO) {
    return {domain}Mapper.selectPage(pageReqVO);
}
```

## 关联 RPC 调用编排

```java
@Override
public void complexBusinessProcess(Long orderId) {
    // 1. 本地事务：保存订单
    OrderDO order = saveOrder(orderId);

    // 2. 跨模块 Feign 调用：扣减库存（支付模块）
    CommonResult<Long> result = productSkuApi.updateStock(order.getSkuId(), -order.getCount());
    if (result.isError()) {
        throw ServiceExceptionUtil.exception(ORDER_STOCK_INSUFFICIENT);
    }

    // 3. 异步消息发送：通知配送
    orderCreatedPublisher.sendMessage(order);
}
```

## 关键点

1. **`@Service` + `@Validated`** — Impl 类上始终标注
2. **`@Resource`** — 注入 Mapper 和其他 Service（不用 `@Autowired`）
3. **`BeanUtils.toBean()`** — VO→DO 转换，基于 Hutool BeanUtil
4. **`@Transactional(rollbackFor = Exception.class)`** — 写操作必须加事务
5. **`validate{Domain}Exists()`** — 返回实体对象避免二次查询
6. **校验分离** — 存在性校验、创建前校验、更新前校验三个方法各司其职
7. **`PageResult`** — 由 `BaseMapperX.selectPage()` 直接返回
8. **逻辑删除** — `deleteById()` 自动转为 UPDATE `deleted=1`

## 常见错误

- Service 接口中声明了受检异常 — 所有异常都是运行时异常
- 忘记加 `@Transactional` — 部分写入时发生异常无法回滚
- Update 时使用 `BeanUtils.toBean()` 覆盖了全部字段 — `null` 字段会覆盖数据库已有值。MyBatis Plus 动态 SQL 只更新非 null 字段，注意检查
- Get 方法直接 `mapper.selectById()` 而非走 `validateDomainExists` — 丢失存在性校验
- Service 返回 VO 而非 DO — Controller 层负责 DO→VO 转换
- 批量操作时未加事务 — 部分失败导致数据不一致
