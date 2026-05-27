# 本机接口复测记录

本文记录本机通过 Nacos 配置启动 `develop-server` 与 `develop-gateway` 后，对接口连通性进行复测的结果。

## 1. 测试环境

```text
Nacos             http://localhost:8848/nacos/
develop-server    http://localhost:48080
develop-gateway   http://localhost:48081
```

服务注册情况：

```text
命名空间：dev
分组：DEFAULT_GROUP
服务：DEFAULT_GROUP@@develop-server，健康实例数 1
服务：DEFAULT_GROUP@@gateway-server，健康实例数 1
```

接口文档来源：

```text
develop-server   http://localhost:48080/v3/api-docs
develop-gateway  http://localhost:48081/admin-api/system/v3/api-docs
```

## 2. 接口数量

从网关 OpenAPI 文档统计：

```text
paths       322
operations  414
GET         172
POST         88
PUT          46
DELETE       63
PATCH        15
OPTIONS      15
HEAD         15
```

## 3. GET 接口复测

GET 接口按只读请求通过网关执行，避免修改本地数据库。

复测结果：

```text
GET 接口总数：172
实际执行：170
跳过路径变量接口：2
HTTP 200：170
5xx：0
连接错误：0
```

报告文件：

```text
/tmp/develop_get_api_test_report.json
```

## 4. 非 GET 接口复测

非 GET 接口采用非破坏性探测策略：

- 跳过短信、邮件、微信、小程序、支付、社交绑定、上传、回调等可能触发外部副作用的接口。
- 跳过删除、重置密码、修改状态、全部已读等破坏性变更接口。
- 跳过无保护输入、可能直接执行真实动作的接口。
- 对剩余接口发送最小化探测请求，用于验证网关路由、鉴权链路和后端稳定性。

复测结果：

```text
非 GET 接口总数：242
实际执行探测：87
  PUT：20
  POST：41
  OPTIONS：13
  HEAD：13
HTTP 200：87
5xx：0
连接错误：0
```

跳过统计：

```text
外部副作用接口：60
破坏性变更接口：41
无保护输入的变更接口：53
路径变量无法安全构造：1
```

报告文件：

```text
/tmp/develop_non_get_api_retest_report.json
```

## 5. 结论

本次本机复测中：

```text
Nacos 登录与配置可用
develop-server 和 gateway-server 已注册到 Nacos，实例健康
网关到主项目的 OpenAPI 路由可用
GET 安全复测未发现 5xx
非 GET 非破坏性探测未发现 5xx 或连接错误
```
