# develop-module-statistics-api

## 模块定位

statistics 模块 API，暴露给其它模块调用

## 基本信息

| 项目 | 内容 |
|---|---|
| 模块路径 | `develop-module-mall/develop-module-statistics-api` |
| Maven Artifact | `develop-module-statistics-api` |
| Packaging | `jar` |
| Java 源文件数量 | 3 |
| 模块说明 | statistics 模块 API，暴露给其它模块调用 |

## 子模块结构

本模块没有声明 Maven 子模块。

## 主要目录职责

| 目录 | 说明 |
|---|---|
| `api` | 跨模块 API、DTO、枚举、RPC / CommonApi 契约。 |
| `enums` | 模块内枚举、错误码、状态值等。 |

## 关键依赖

未发现需要在 README 中强调的直接业务 API 或 Starter 依赖。

## 架构职责

- 本模块提供跨模块稳定契约，包括 DTO、枚举、CommonApi / RPC API 和调用适配接口。
- API 模块应避免依赖业务实现层，保持轻量、稳定、可被其他模块安全引用。
- 修改契约时需要检查所有调用方兼容性，并同步本地/远程调用适配。

## 构建与验证

```bash
# 编译该模块及其依赖
mvn compile -pl develop-module-mall/develop-module-statistics-api -am

# 运行该模块测试
mvn test -pl develop-module-mall/develop-module-statistics-api

# 打包该模块及其依赖
mvn clean package -pl develop-module-mall/develop-module-statistics-api -am -Dmaven.test.skip=true
```

## 维护建议

- 修改跨模块契约时，必须检查所有调用方兼容性。
- DTO、枚举和 API 接口应保持稳定，不应依赖 server 内部实现类。
- 涉及远程调用时，应同步检查 local / remote 适配器和 Feign 契约。
