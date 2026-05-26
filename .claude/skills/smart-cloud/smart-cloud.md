---
name: smart-cloud
description: Smart Cloud (芋道 develop-cloud) 微服务快速开发平台总览 — 技术栈、架构、模块地图、编码规范、启动方式
type: project
---

# Smart Cloud — 项目总览

## 项目概要

芋道 Smart Cloud 是基于 Spring Cloud Alibaba 的微服务快速开发平台（完整版，含所有业务模块），源自 RuoYi-Vue-Pro。

- **项目名**：develop-cloud (Smart Cloud)
- **版本**：2026.04-SNAPSHOT
- **Java 版本**：Java 17+
- **基础包名**：`com.develop.mvp.pk`
- **Maven 属性**：`${develop.info.base-package}` = `com.develop.mvp.pk`

## 核心技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 基础框架 | Spring Boot | 3.5.14 |
| 微服务 | Spring Cloud | 2025.0.1 |
| 微服务 | Spring Cloud Alibaba | 2025.0.0.0 |
| 注册/配置中心 | Nacos | Alibaba Cloud |
| API 网关 | Spring Cloud Gateway | WebFlux 响应式 |
| ORM | MyBatis Plus | 3.5.16 |
| 连接池 | Druid | 1.2.28 |
| 缓存 | Redis + Redisson | 4.3.1 |
| 消息队列 | 抽象层 (Redis/RabbitMQ/Kafka/RocketMQ) | — |
| 定时任务 | XXL-Job | 2.4.0 |
| 工作流 | Flowable | 7.2.0 |
| 安全 | Spring Security + OAuth2 Token | — |
| API 文档 | SpringDoc OpenAPI + Knife4j | 2.8.17 / 4.5.0 |
| 链路追踪 | SkyWalking | 9.6.0 |
| 服务保障 | Sentinel + Lock4j | — |
| Excel | FastExcel (EasyExcel fork) | 1.3.0 |
| 对象转换 | MapStruct | 1.6.3 |
| 工具库 | Lombok / Hutool / Guava | — |
| AI | Spring AI (多模型接入) | 1.1.5 |

## 模块全景图

```
develop-cloud/
├── develop-dependencies/          # BOM — 统一版本管理
├── develop-framework/             # 17 个共享 Starter + develop-common
│   ├── develop-common             # 公共库 (CommonResult, 异常, 工具类, Feign API接口)
│   ├── develop-spring-boot-starter-web
│   ├── develop-spring-boot-starter-security
│   ├── develop-spring-boot-starter-mybatis
│   ├── develop-spring-boot-starter-redis
│   ├── develop-spring-boot-starter-rpc
│   ├── develop-spring-boot-starter-mq
│   ├── develop-spring-boot-starter-job
│   ├── develop-spring-boot-starter-protection
│   ├── develop-spring-boot-starter-websocket
│   ├── develop-spring-boot-starter-monitor
│   ├── develop-spring-boot-starter-excel
│   ├── develop-spring-boot-starter-env
│   ├── develop-spring-boot-starter-test
│   ├── develop-spring-boot-starter-biz-tenant
│   ├── develop-spring-boot-starter-biz-data-permission
│   └── develop-spring-boot-starter-biz-ip
├── develop-gateway/               # Spring Cloud Gateway 网关
├── develop-server/                # 单体启动聚合器
├── develop-module-system/         # 系统管理 (用户/角色/权限/租户/字典/OAuth2)
├── develop-module-infra/          # 基础设施 (代码生成/文件/日志/监控)
├── develop-module-member/         # 会员中心
├── develop-module-bpm/            # 工作流 (Flowable)
├── develop-module-pay/            # 支付
├── develop-module-mp/             # 微信公众号
├── develop-module-report/         # 报表
├── develop-module-mall/           # 商城 (商品/促销/交易/统计)
├── develop-module-crm/            # CRM
├── develop-module-erp/            # ERP
├── develop-module-ai/             # AI 大模型
├── develop-module-iot/            # 物联网
├── develop-module-mes/            # MES 制造执行
├── develop-module-wms/            # WMS 仓储管理
├── develop-ui/                    # 5 个前端项目
└── sql/                           # 7 种数据库初始化脚本
```

## 模块组织模式

每个业务模块遵循统一的内部结构：

```
develop-module-{name}/
├── pom.xml                              # 聚合 POM
├── develop-module-{name}-api/           # API 合约 (接口 + DTO + 枚举)
│   └── src/main/java/.../
│       ├── api/{Domain}Api.java         # Feign 接口 (可跨模块调用)
│       ├── api/{domain}/dto/            # 跨模块 DTO
│       └── enums/                       # 模块专属枚举 + 错误码常量
└── develop-module-{name}-server/        # 实现层
    └── src/main/java/.../
        ├── {Name}ServerApplication.java # 独立启动类
        ├── controller/admin/{domain}/   # 管理后台 Controller + VO
        ├── controller/app/{domain}/     # 移动端 Controller
        ├── service/{domain}/            # Service 接口 + Impl
        ├── dal/dataobject/{domain}/     # MyBatis Plus Entity (DO)
        ├── dal/mysql/{domain}/          # MyBatis Plus Mapper
        ├── dal/redis/                   # Redis DAO (可选)
        ├── api/{domain}/                # Feign API 实现 (RestController)
        ├── convert/{domain}/            # MapStruct Convert 接口
        ├── job/{domain}/                # XXL-Job 处理器
        └── framework/                   # 模块本地 Spring 配置
```

## 两种部署模式

### 微服务模式
- 启动 Nacos 注册中心 + 配置中心
- 启动 develop-gateway (网关, 端口 48080)
- 分别启动各 develop-module-*-server (各自独立端口)
- 模块间通过 Feign + Nacos 服务发现通信
- Gateway 统一鉴权 (TokenAuthenticationFilter)

### 单体模式 (开发推荐)
- `develop-server` 作为壳工程，通过 Maven 依赖聚合所有 `-server` 模块
- 一个 JVM 进程运行所有模块，端口 48080
- 排除 `spring-cloud-starter-openfeign`，禁用 Nacos 发现/配置
- `application-local.yaml` 中排除所有 `*RpcAutoConfiguration`
- 启动命令：`mvn -pl develop-server spring-boot:run`

## 编码规范速览

### 分层命名

| 层 | 类名模式 | 包路径 |
|----|----------|--------|
| Controller | `{Domain}Controller` | `controller/admin/{domain}/` |
| Service 接口 | `{Domain}Service` | `service/{domain}/` |
| Service 实现 | `{Domain}ServiceImpl` | `service/{domain}/` |
| Entity (数据对象) | `{Domain}DO` | `dal/dataobject/{domain}/` |
| Mapper | `{Domain}Mapper` | `dal/mysql/{domain}/` |
| Feign API 接口 | `{Domain}Api` | `api/{domain}/` (在 -api 模块) |
| Feign API 实现 | `{Domain}ApiImpl` | `api/{domain}/` (在 -server 模块) |
| MapStruct 转换器 | `{Domain}Convert` | `convert/{domain}/` |
| 请求 VO | `{Domain}SaveReqVO`, `{Domain}PageReqVO` | `controller/admin/{domain}/vo/` |
| 响应 VO | `{Domain}RespVO`, `{Domain}SimpleRespVO` | `controller/admin/{domain}/vo/` |
| 跨模块 DTO | `{Domain}ReqDTO`, `{Domain}RespDTO` | `api/{domain}/dto/` (在 -api 模块) |

### 核心注解规范

```java
// Controller
@Tag(name = "管理后台 - 用户")          // Swagger 分组
@RestController
@RequestMapping("/system/user")         // REST 路径
@Validated                               // 方法级参数校验
public class UserController {
    @Resource private UserService service;

    @PostMapping("/create")
    @Operation(summary = "创建用户")
    @PreAuthorize("@ss.hasPermission('system:user:create')")
    public CommonResult<Long> create(@Valid @RequestBody UserSaveReqVO vo) {
        return success(service.create(vo));
    }
}

// Service 接口
public interface UserService {
    Long create(UserSaveReqVO vo);
    void update(UserSaveReqVO vo);
    void delete(Long id);
    UserDO get(Long id);
    PageResult<UserDO> page(UserPageReqVO vo, PageParam pageParam);
}

// Service 实现
@Service @Validated
public class UserServiceImpl implements UserService {
    @Resource private UserMapper mapper;

    @Override
    public Long create(UserSaveReqVO vo) {
        validateXxx(vo);       // 业务校验
        UserDO entity = BeanUtils.toBean(vo, UserDO.class);
        mapper.insert(entity);
        return entity.getId();
    }

    private void validateXxx(UserSaveReqVO vo) {
        // 业务规则校验，失败抛出 ServiceException
    }
}

// Entity
@TableName("system_users")
@KeySequence("system_users_seq")         // Oracle/PG 序列支持
@Data
@EqualsAndHashCode(callSuper = true)
public class UserDO extends TenantBaseDO {
    @TableId private Long id;
    private String username;
    private String password;
    private String nickname;
    private Integer status;             // CommonStatusEnum
}

// Mapper
@Mapper
public interface UserMapper extends BaseMapperX<UserDO> {
    default UserDO selectByUsername(String username) {
        return selectOne(UserDO::getUsername, username);
    }
}

// Feign API 接口 (在 -api 模块)
@FeignClient(name = ApiConstants.NAME)   // = "system-server"
public interface UserApi {
    String PREFIX = ApiConstants.PREFIX + "/user";

    @GetMapping(PREFIX + "/get")
    CommonResult<UserRespDTO> getUser(@RequestParam("id") Long id);
}

// Feign API 实现 (在 -server 模块)
@RestController @Validated @Primary
public class UserApiImpl implements UserApi {
    @Resource private UserService service;

    @Override
    public CommonResult<UserRespDTO> getUser(Long id) {
        return success(BeanUtils.toBean(service.get(id), UserRespDTO.class));
    }
}
```

### Lombok 配置 (lombok.config)

```properties
config.stopBubbling = true
lombok.tostring.callsuper=CALL
lombok.equalsandhashcode.callsuper=CALL
lombok.accessors.chain=true              # setter 链式调用
```

### 通用注解说明

- 所有 Controller 和 ServiceImpl 加 `@Validated`
- 简单对象转换用 `BeanUtils.toBean()`（开发框架封装版）
- 复杂/批量转换用 MapStruct `@Mapper` 接口
- Entity 继承 `BaseDO`（非租户表）或 `TenantBaseDO`（多租户表）
- `BaseDO` 自带 `createTime`, `updateTime`, `creator`, `updater`, `deleted`（逻辑删除）
- Controller 方法全部返回 `CommonResult<T>`，通过 `success(data)` 包装

## 常用命令

```bash
# 完整构建
mvn clean package -Dmaven.test.skip=true

# 安装到本地仓库（运行前必须）
mvn clean install -Dmaven.test.skip=true

# 启动单体应用
mvn -pl develop-server spring-boot:run

# 运行单个模块测试
mvn test -pl develop-module-system/develop-module-system-server

# 运行 gateway
cd develop-gateway && mvn spring-boot:run
```

## Skills 索引

本主 Skill 关联以下子 Skills，按需查阅：

### 架构层
- [project-structure](01-architecture/project-structure.md)
- [layered-architecture](01-architecture/layered-architecture.md)
- [deployment-modes](01-architecture/deployment-modes.md)
- [naming-conventions](01-architecture/naming-conventions.md)
- [configuration](01-architecture/configuration.md)

### 框架层
- [develop-common](02-framework-starters/develop-common.md)
- [starter-web](02-framework-starters/starter-web.md)
- [starter-security](02-framework-starters/starter-security.md)
- [starter-mybatis](02-framework-starters/starter-mybatis.md)
- [starter-redis](02-framework-starters/starter-redis.md)
- [starter-tenant](02-framework-starters/starter-tenant.md)
- [starter-data-permission](02-framework-starters/starter-data-permission.md)
- [starter-excel](02-framework-starters/starter-excel.md)
- [starter-mq](02-framework-starters/starter-mq.md)
- [starter-job](02-framework-starters/starter-job.md)
- [starter-rpc](02-framework-starters/starter-rpc.md)
- [starter-protection](02-framework-starters/starter-protection.md)
- [starter-websocket](02-framework-starters/starter-websocket.md)
- [starter-monitor](02-framework-starters/starter-monitor.md)

### 业务模块
- [system-module](03-business-modules/system-module.md)
- [infra-module](03-business-modules/infra-module.md)
- [bpm-module](03-business-modules/bpm-module.md)
- [pay-module](03-business-modules/pay-module.md)
- [mall-module](03-business-modules/mall-module.md)
- [crm-module](03-business-modules/crm-module.md)
- [erp-module](03-business-modules/erp-module.md)
- [ai-module](03-business-modules/ai-module.md)
- [iot-module](03-business-modules/iot-module.md)
- [member-module](03-business-modules/member-module.md)
- [mp-module](03-business-modules/mp-module.md)
- [report-module](03-business-modules/report-module.md)
- [mes-wms-module](03-business-modules/mes-wms-module.md)

### 代码模式
- [crud-controller](04-code-patterns/crud-controller.md)
- [service-layer](04-code-patterns/service-layer.md)
- [dal-layer](04-code-patterns/dal-layer.md)
- [feign-api](04-code-patterns/feign-api.md)
- [dto-vo-conversion](04-code-patterns/dto-vo-conversion.md)
- [validation](04-code-patterns/validation.md)
- [exception-handling](04-code-patterns/exception-handling.md)
- [permission-control](04-code-patterns/permission-control.md)
- [excel-import-export](04-code-patterns/excel-import-export.md)
- [mq-pub-sub](04-code-patterns/mq-pub-sub.md)
- [job-scheduling](04-code-patterns/job-scheduling.md)
- [websocket-messaging](04-code-patterns/websocket-messaging.md)
- [code-generation](04-code-patterns/code-generation.md)

### 关键类
- 见 [05-key-classes/](05-key-classes/) 目录
