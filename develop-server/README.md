# develop-server

## 模块定位

后端主启动容器，通过 Maven 依赖按需装配业务 server 模块，本身不承载核心业务逻辑。

## 基本信息

| 项目 | 内容 |
|---|---|
| 模块路径 | `develop-server` |
| Maven Artifact | `develop-server` |
| Packaging | `jar` |
| Java 源文件数量 | 2 |
| 模块说明 | 后端 Server 的主项目，通过引入需要 develop-module-xxx 的依赖， 从而实现提供 RESTful API 给 develop-ui-admin、develop-ui-user 等前端项目。 本质上来说，它就是个空壳（容器）！ |

## 子模块结构

本模块没有声明 Maven 子模块。

## 主要目录职责

| 目录 | 说明 |
|---|---|
| `controller` | REST 控制器，包含管理端、用户端请求入口和 VO。 |

## 关键依赖

| 依赖 | 说明 |
|---|---|
| `develop-module-system-server` | 业务模块 API / server / core 依赖。 |
| `develop-module-infra-server` | 业务模块 API / server / core 依赖。 |
| `develop-spring-boot-starter-protection` | 分布式锁、幂等、限流、服务保护 |
| `spring-cloud-starter-alibaba-nacos-discovery` | Nacos 注册发现或配置中心。 |
| `spring-cloud-starter-alibaba-nacos-config` | Nacos 注册发现或配置中心。 |
| `develop-spring-boot-starter-rpc` | OpenFeign、负载均衡、跨模块/跨服务调用 |

## 架构职责

- 本模块是后端主启动容器，通过 Maven 依赖装配需要启用的业务 `server` 模块。
- 容器本身不承载核心业务逻辑，业务实现应位于各 `develop-module-*-server` 模块。
- 运行时启用哪些业务模块，由 `develop-server/pom.xml` 中声明的 server 依赖决定。

## 构建与验证

```bash
# 编译
mvn compile -pl develop-server -am

# 打包
mvn clean package -pl develop-server -am -Dmaven.test.skip=true

# 启动
mvn spring-boot:run -pl develop-server -am
```

## 维护建议

- 启停业务模块时，优先修改 `develop-server/pom.xml` 中的 server 依赖，并检查根 POM reactor 是否包含对应模块。
- 容器配置变更应同步检查 `application.yaml` 与各 profile 配置。
- 修改启动依赖后，至少运行 `mvn compile -pl develop-server -am`。
