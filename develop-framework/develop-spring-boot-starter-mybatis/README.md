# develop-spring-boot-starter-mybatis

## 模块定位

数据库连接池、多数据源、事务、MyBatis 拓展

## 基本信息

| 项目 | 内容 |
|---|---|
| 模块路径 | `develop-framework/develop-spring-boot-starter-mybatis` |
| Maven Artifact | `develop-spring-boot-starter-mybatis` |
| Packaging | `jar` |
| Java 源文件数量 | 24 |
| 模块说明 | 数据库连接池、多数据源、事务、MyBatis 拓展 |

## 子模块结构

本模块没有声明 Maven 子模块。

## 主要目录职责

| 目录 | 说明 |
|---|---|
| `framework` | 模块内 Spring 配置、拦截器、扩展点。 |
| `enums` | 模块内枚举、错误码、状态值等。 |

## 关键依赖

| 依赖 | 说明 |
|---|---|
| `develop-spring-boot-starter-security` | 认证授权、登录用户上下文、操作日志 |
| `easy-trans-spring-boot-starter` | 外部框架或组件依赖。 |

## 架构职责

- 本模块是框架 Starter，面向业务模块提供可复用技术能力或自动配置。
- Starter 应保持业务无关，只暴露稳定配置、拦截器、工具类、模板类或扩展点。
- 修改 Starter 时需要关注所有依赖该 Starter 的业务模块，避免引入跨模块业务耦合。

## 构建与验证

```bash
# 编译该模块及其依赖
mvn compile -pl develop-framework/develop-spring-boot-starter-mybatis -am

# 运行该模块测试
mvn test -pl develop-framework/develop-spring-boot-starter-mybatis

# 打包该模块及其依赖
mvn clean package -pl develop-framework/develop-spring-boot-starter-mybatis -am -Dmaven.test.skip=true
```

## 维护建议

- 修改 Starter 能力时，优先保证配置项、自动配置条件和默认行为向调用方清晰可控。
- 框架模块不应引入具体业务模块依赖；需要扩展业务行为时优先通过接口、SPI 或配置完成。
- 至少运行当前 Starter 的 `mvn compile`，必要时补充依赖该 Starter 的业务模块编译验证。
