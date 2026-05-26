# develop-framework 核心 Starter 注释补充设计

**日期：** 2026-05-26
**范围：** `develop-framework` 核心 Starter 源码注释
**状态：** 待审阅

## 背景

`develop-framework` 是 Smart Cloud 后端工程的共享框架层，向业务模块和运行单元提供 Web、安全、MyBatis、Redis、MQ、RPC、租户、数据权限等横向能力。当前模块已有 README 说明整体架构，但初学者直接进入源码时，容易在 Spring Boot 自动配置、Filter/AOP/Interceptor 调用顺序、上下文传递、默认 Bean 生效条件等位置迷失。

本次工作目标是补充关键流程注释，让读者能按“启动装配 → 请求进入 → 框架增强 → 业务调用 → 数据/RPC/MQ/缓存能力”的路径理解核心 Starter，而不是对所有源码做机械逐行解释。

## 目标

1. 帮助初学者看懂核心 Starter 的职责边界和主流程。
2. 解释关键类在 Spring Boot 启动、请求链路、上下文传递或技术扩展中的位置。
3. 保持代码行为不变，只补充注释，不做重构、不改接口、不调整目录。
4. 避免无意义注释，重点解释“为什么存在、何时生效、前后依赖、流程顺序”。

## 覆盖范围

优先处理以下 8 类核心 Starter：

| Starter | 注释重点 |
|---|---|
| `develop-spring-boot-starter-web` | 自动配置、Web MVC 扩展、过滤器、全局异常、请求日志、OpenAPI/Knife4j 入口 |
| `develop-spring-boot-starter-security` | 安全自动配置、认证过滤器、权限判断、登录用户上下文、操作日志链路 |
| `develop-spring-boot-starter-mybatis` | MyBatis 自动配置、数据权限/租户协作、基础 Mapper、事务与数据访问扩展 |
| `develop-spring-boot-starter-redis` | Redis/Redisson/Cache 自动配置、缓存默认策略、分布式能力入口 |
| `develop-spring-boot-starter-mq` | 消息抽象、生产/消费入口、不同 MQ 实现的职责边界 |
| `develop-spring-boot-starter-rpc` | Feign/RPC 自动配置、远程调用扩展、请求头透传、调用边界 |
| `develop-spring-boot-starter-biz-tenant` | 租户上下文、租户过滤、请求链路中的租户识别与传播 |
| `develop-spring-boot-starter-biz-data-permission` | 数据权限注解、AOP 拦截、权限规则组合、与 MyBatis 查询改写的协作 |

不在本次优先范围：

- `develop-common` 中普通工具类的全面注释。
- 每个 Starter 的所有类级注释补齐。
- README、SVG、架构图内容重写。
- 任何业务模块或 DDD 聚合代码。

## 文件选择标准

每个核心 Starter 只选择“读流程必须经过”的类：

1. `*AutoConfiguration`：说明 Starter 如何被 Spring Boot 装配。
2. `Filter`、`Interceptor`、`Advisor`、`Aspect`：说明请求或方法调用如何被增强。
3. `ContextHolder`、`Context`、`FrameworkService`：说明跨层状态如何传递。
4. `Template`、`Client`、`Producer`、`Consumer`：说明对外提供能力的入口。
5. 条件装配或顺序敏感类：说明为什么需要条件、顺序或默认 Bean。

如果某个类只是 DTO、枚举、简单属性对象、Lombok 数据类或普通工具方法，则默认不补注释，除非它是理解流程的必要入口。

## 注释风格

### 类级注释

类级注释应回答四个问题：

1. 它解决什么问题？
2. 它在启动链路、请求链路或调用链路中的位置是什么？
3. 它依赖哪些前置组件，又会影响哪些后续组件？
4. 初学者阅读时应该重点看哪些方法或顺序？

### 方法级注释

只给关键方法添加注释，说明：

- 该方法在流程中的触发时机。
- 重要条件判断背后的原因。
- 上下文写入、清理、透传或恢复的必要性。
- 与 Spring Boot、Spring Security、MyBatis、Redis、MQ、Feign 等框架机制的衔接点。

### 不添加的注释

- 不解释 getter/setter、字段赋值、构造器简单注入。
- 不把方法名翻译成中文。
- 不写“这是某某方法”这类无信息注释。
- 不新增多段长注释；优先使用简洁 JavaDoc 或一行必要说明。

## 实施顺序

1. 先扫描 8 个核心 Starter 的主流程入口类。
2. 按 Starter 分组补充注释，每次只处理同一类流程，避免上下文混乱。
3. 优先处理启动装配类，再处理请求/调用链路类，最后处理上下文和扩展点。
4. 每批修改后检查 diff，确保只有注释变化。
5. 完成后运行 Maven 编译验证。

## 验证方式

优先执行：

```bash
mvn compile -pl develop-framework -am
```

如果全量 framework 编译受环境、依赖下载或历史问题影响失败，则缩小到受影响 Starter 编译验证，并记录失败命令、失败原因和已验证范围。

同时检查：

- `git diff` 中不应出现业务逻辑变更。
- 不应修改方法签名、Bean 名称、注解参数或配置键。
- 注释应与当前源码一致，不写未验证的行为。

## 验收标准

1. 核心 Starter 的主流程入口类具备清晰注释。
2. 初学者能通过注释理解自动配置、拦截、上下文传递、RPC/MQ/缓存/数据访问入口的流程。
3. 代码行为保持不变，编译验证完成或明确报告无法验证原因。
4. 注释专业、简洁、可维护，不制造重复噪音。
