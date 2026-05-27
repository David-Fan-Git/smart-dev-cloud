# 本机 Nacos 2.4.3 鉴权配置记录

本文记录本机 Nacos 2.4.3 开启鉴权、初始化管理员、重启和验证的完整流程。

## 1. 当前环境

本机 Nacos 运行信息：

```text
Nacos home: /tmp/nacos
配置文件: /tmp/nacos/conf/application.properties
启动包: /tmp/nacos/target/nacos-server.jar
控制台地址: http://localhost:8848/nacos/
```

常用端口：

```text
8848  HTTP 控制台/API
9848  gRPC 客户端端口
9849  gRPC 服务端端口
7848  集群通信端口
```

检查进程和端口：

```bash
ps aux | grep -i '[n]acos\|[c]om.alibaba.nacos'
lsof -nP -iTCP:8848 -iTCP:9848 -iTCP:9849 -iTCP:7848 -sTCP:LISTEN
```

## 2. 修改鉴权配置

编辑配置文件：

```bash
/tmp/nacos/conf/application.properties
```

设置或确认以下配置：

```properties
nacos.core.auth.system.type=nacos
nacos.core.auth.enabled=true

nacos.core.auth.caching.enabled=true
nacos.core.auth.enable.userAgentAuthWhite=false

nacos.core.auth.server.identity.key=nacos-server-identity
nacos.core.auth.server.identity.value=你的随机字符串

nacos.core.auth.plugin.nacos.token.cache.enable=false
nacos.core.auth.plugin.nacos.token.expire.seconds=18000
nacos.core.auth.plugin.nacos.token.secret.key=你的Base64随机密钥
```

配置说明：

| 配置项 | 说明 |
|---|---|
| `nacos.core.auth.system.type=nacos` | 使用 Nacos 内置鉴权系统 |
| `nacos.core.auth.enabled=true` | 开启鉴权 |
| `nacos.core.auth.server.identity.key/value` | 服务端身份标识，开启鉴权后需要配置 |
| `nacos.core.auth.plugin.nacos.token.secret.key` | JWT token 签名密钥，必须使用随机密钥 |
| `nacos.core.auth.plugin.nacos.token.expire.seconds=18000` | token 过期时间，单位秒 |

生成随机 Base64 token secret：

```bash
python3 - <<'PY'
import base64, secrets
print(base64.b64encode(secrets.token_bytes(48)).decode())
PY
```

生成随机 server identity value：

```bash
python3 - <<'PY'
import secrets
print(secrets.token_urlsafe(32))
PY
```

## 3. 空用户库时初始化管理员

如果 Nacos 用户库为空，直接开启鉴权后登录会失败，常见错误：

```text
User nacos not found
```

Nacos 2.4.3 的默认管理员用户名固定为小写：

```text
nacos
```

如果需要初始化默认管理员，应先临时关闭鉴权：

```properties
nacos.core.auth.enabled=false
```

然后重启 Nacos，再调用管理员初始化接口：

```bash
curl -X POST 'http://localhost:8848/nacos/v1/auth/users/admin' \
  -d 'password=Nacos'
```

成功返回示例：

```json
{"username":"nacos","password":"Nacos"}
```

初始化后的管理员账号：

```text
用户名：nacos
密码：Nacos
```

注意：默认管理员用户名不能初始化成大写 `Nacos`。大写 `Nacos` 可以作为普通用户创建，但不会自动拥有全局管理员权限。

## 4. 重新开启鉴权

初始化管理员后，把配置改回：

```properties
nacos.core.auth.enabled=true
```

然后再次重启 Nacos。

## 5. 重启 Nacos

本机使用的重启方式是先停止旧进程，再用原启动参数启动。

查找进程：

```bash
ps aux | grep -i '[n]acos\|[c]om.alibaba.nacos'
```

停止进程：

```bash
kill <PID>
```

启动命令：

```bash
nohup /Library/Java/JavaVirtualMachines/jdk-23.0.2.jdk/Contents/Home/bin/java \
  -Xms512m -Xmx512m -Xmn256m \
  -Dnacos.standalone=true \
  -Dnacos.member.list= \
  '-Xlog:gc*:file=/tmp/nacos/logs/nacos_gc.log:time,tags:filecount=10,filesize=100m' \
  -Dloader.path=/tmp/nacos/plugins,/tmp/nacos/plugins/health,/tmp/nacos/plugins/cmdb,/tmp/nacos/plugins/selector \
  -Dnacos.home=/tmp/nacos \
  -jar /tmp/nacos/target/nacos-server.jar \
  --spring.config.additional-location=file:/tmp/nacos/conf/ \
  --logging.config=/tmp/nacos/conf/nacos-logback.xml \
  --server.max-http-header-size=524288 \
  nacos.nacos \
  >/tmp/nacos/logs/restart.out 2>&1 &
```

注意：在 zsh 下，`-Xlog:gc*:...` 参数需要加引号，否则 `*` 会被当成通配符展开。

## 6. 验证鉴权

验证配置开关：

```bash
grep '^nacos.core.auth.enabled=' /tmp/nacos/conf/application.properties
```

期望输出：

```text
nacos.core.auth.enabled=true
```

验证端口监听：

```bash
lsof -nP -iTCP:8848 -iTCP:9848 -iTCP:9849 -iTCP:7848 -sTCP:LISTEN
```

验证管理员登录：

```bash
curl -s -X POST 'http://localhost:8848/nacos/v1/auth/login' \
  -d 'username=nacos&password=Nacos'
```

成功返回示例：

```json
{
  "accessToken": "...",
  "tokenTtl": 18000,
  "globalAdmin": true,
  "username": "nacos"
}
```

验证未登录访问被拒绝：

```bash
curl -s -o /tmp/nacos_unauth.txt -w '%{http_code}' \
  'http://localhost:8848/nacos/v1/auth/users?search=blur&pageNo=1&pageSize=10'
```

期望 HTTP 状态码：

```text
403
```

## 7. 控制台登录

浏览器打开：

```text
http://localhost:8848/nacos/
```

登录账号：

```text
用户名：nacos
密码：Nacos
```

## 8. 常见问题

### User nacos not found

原因：用户库为空，尚未初始化默认管理员。

处理：临时关闭鉴权，重启后调用：

```bash
curl -X POST 'http://localhost:8848/nacos/v1/auth/users/admin' \
  -d 'password=Nacos'
```

然后重新开启鉴权并重启。

### User Nacos not found

原因：Nacos 用户名区分大小写，默认管理员用户名是小写 `nacos`，不是大写 `Nacos`。

处理：使用：

```text
用户名：nacos
密码：Nacos
```

### role 'ROLE_ADMIN' is not permitted to create

原因：Nacos 2.4.3 不允许通过普通角色创建接口手动创建 `ROLE_ADMIN`。

处理：使用 `/nacos/v1/auth/users/admin` 初始化默认管理员，该接口会自动给小写 `nacos` 用户授予全局管理员角色。

## 9. 本次验证结果

本次最终验证结果：

```text
nacos.core.auth.enabled=true
8848/9848/9849/7848 端口正常监听
username=nacos&password=Nacos 登录成功
globalAdmin=true
未认证访问用户接口返回 403
```
