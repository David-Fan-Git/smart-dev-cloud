---
name: job-scheduling
description: XXL-Job distributed scheduling — sharding, idempotency, @TenantIgnore/@TenantJob, failure recovery, monitoring
type: project
---

# 任务调度模式

## 概述

使用 **XXL-Job 2.4.0** 分布式任务调度引擎。任务通过 `@Component` + `@XxlJob("jobName")` 注册。支持全局单次执行（`@TenantIgnore`）和按租户循环执行（`@TenantJob`）。

## 1. 全局单次执行（@TenantIgnore）

```java
package com.develop.mvp.pk.module.{module}.job;

import com.develop.mvp.pk.framework.tenant.core.aop.TenantIgnore;
import com.develop.mvp.pk.module.{module}.service.{domain}.{Domain}Service;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class {Domain}Job {

    @Resource
    private {Domain}Service {domain}Service;

    @XxlJob("{domain}CleanJob")
    @TenantIgnore // 关键：忽略租户上下文，全局只执行一次
    public void cleanExpired() {
        log.info("[cleanExpired][开始清理过期 {领域} 数据]");
        try {
            int count = {domain}Service.cleanExpired();
            XxlJobHelper.handleSuccess("清理完成，共清理 " + count + " 条数据");
        } catch (Exception e) {
            log.error("[cleanExpired][清理失败]", e);
            XxlJobHelper.handleFail("清理失败：" + e.getMessage());
        }
    }
}
```

## 2. 按租户循环执行（@TenantJob）

```java
@XxlJob("{domain}ExpireNotification")
@TenantJob // 关键：每个激活的租户执行一次
public void sendExpireNotification() {
    log.info("[sendExpireNotification][开始检查到期提醒]");
    try {
        int count = {domain}Service.sendExpireNotifications();
        XxlJobHelper.handleSuccess("发送 " + count + " 条提醒");
    } catch (Exception e) {
        log.error("[sendExpireNotification][处理失败]", e);
        XxlJobHelper.handleFail("处理失败：" + e.getMessage());
    }
}
```

## 3. 分片任务（大数据量并行处理）

```java
@XxlJob("{domain}ShardingJob")
@TenantIgnore
public void shardingJob() {
    int shardIndex = XxlJobHelper.getShardIndex();  // 当前分片（从 0 开始）
    int shardTotal = XxlJobHelper.getShardTotal();  // 总分片数

    log.info("[shardingJob][分片 {}/{} 启动]", shardIndex + 1, shardTotal);

    // 按分片查询数据（通过 ID 取模或区间划分）
    List<{Domain}DO> list = {domain}Service.getPendingListByShard(shardIndex, shardTotal);

    int success = 0;
    for ({Domain}DO entity : list) {
        try {
            {domain}Service.process(entity);
            success++;
        } catch (Exception e) {
            log.error("[shardingJob][处理失败 id={}]", entity.getId(), e);
            // 单个失败不影响整体
        }
    }
    XxlJobHelper.handleSuccess("分片 " + shardIndex + " 成功处理 " + success + "/" + list.size() + " 条");
}
```

## 4. 幂等性保证

```java
// 任务启动时加分布式锁，确保同一时刻只有一个执行器执行
@XxlJob("{domain}IdempotentJob")
@TenantIgnore
public void idempotentJob() {
    String lockKey = "job:lock:{domain}";
    boolean locked = redisLockUtil.tryLock(lockKey, 60 * 1000); // 60 秒超时
    if (!locked) {
        log.info("[idempotentJob][其他执行器正在执行，跳过]");
        XxlJobHelper.handleSuccess("跳过（其他执行器执行中）");
        return;
    }
    try {
        // 业务逻辑
        XxlJobHelper.handleSuccess("执行完成");
    } finally {
        redisLockUtil.unlock(lockKey);
    }
}
```

## 5. 任务参数传递

```java
@XxlJob("{domain}BatchProcess")
@TenantIgnore
public void batchProcess() {
    String param = XxlJobHelper.getJobParam(); // 从调度台获取任务参数
    if (StrUtil.isNotBlank(param)) {
        JSONObject json = JSONObject.parseObject(param);
        Integer batchSize = json.getInteger("batchSize");
        String date = json.getString("date");
        {domain}Service.batchProcess(batchSize, date);
    } else {
        {domain}Service.batchProcess(100, LocalDate.now().toString());
    }
    XxlJobHelper.handleSuccess("批量处理完成");
}
```

## 6. 配置

```yaml
# application.yaml
xxl:
  job:
    enabled: true
    admin:
      addresses: ${xxl-job.admin:http://127.0.0.1:9099/xxl-job-admin}
    executor:
      appname: ${spring.application.name}
      port: ${xxl-job.port:9999}
      logretentiondays: 30
    accessToken: ${xxl-job.access-token:default-token}
```

## 关键点

1. **`@Component` + `@XxlJob("jobName")`** — 必须同时标注
2. **`@TenantIgnore`** — 全局执行一次（系统级任务，如清理过期数据）
3. **`@TenantJob`** — 每个激活的租户执行一次（如各租户的到期提醒）
4. **必须调用 `XxlJobHelper.handleSuccess()` / `handleFail()`** — 否则调度中心显示"运行中"
5. **异常必须捕获并调用 `handleFail()`** — 未捕获的异常不会被 XXL-Job 正确记录
6. **分片任务** — 大数据量处理，通过 `getShardIndex() + getShardTotal()` 分布
7. **幂等性** — 使用分布式锁保证同一任务不被重复执行
8. **`getJobParam()`** — 从调度中心获取动态配置参数

## 常见错误

- 未调用 `XxlJobHelper.handleSuccess()` — 调度中心任务永远显示"运行中"
- `@TenantJob` 路由策略配置错误 — 应为"轮询"或"分片广播"
- 异常被 `try-catch` 吞掉但未调用 `handleFail()` — 调度中心误认为执行成功
- 未使用分布式锁保证幂等性 — 集群环境下任务重复执行
- 使用了 `@Transactional` + `@TenantJob` — 事务跨租户
- 任务执行超时未设置 cron 覆盖策略 — 任务堆积
