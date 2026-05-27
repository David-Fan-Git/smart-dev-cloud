# 本机启动项目查看记录

本文记录在 macOS 上查看当前本机启动项目、端口、进程和日志的常用命令。

## 1. 查看当前监听端口

查看所有正在监听的 TCP 端口：

```bash
lsof -nP -iTCP -sTCP:LISTEN
```

只查看当前项目相关端口：

```bash
lsof -nP -iTCP:8848 -iTCP:9848 -iTCP:9849 -iTCP:7848 -iTCP:48080 -iTCP:48081 -sTCP:LISTEN
```

当前项目常用端口：

```text
8848   Nacos 控制台/API
9848   Nacos gRPC 客户端端口
9849   Nacos gRPC 服务端端口
7848   Nacos 集群通信端口
48080  develop-server 主项目
48081  develop-gateway 网关
```

## 2. 查看 Java 项目进程

查看所有 Java 进程：

```bash
ps aux | grep java
```

只查看当前项目相关进程：

```bash
ps aux | grep -E '[n]acos|[d]evelop-server|[d]evelop-gateway|spring-boot:run'
```

如果本机 JDK 工具可用，也可以使用：

```bash
jps -l
```

## 3. 根据端口反查进程

查看主项目 `develop-server`：

```bash
lsof -nP -iTCP:48080 -sTCP:LISTEN
```

查看网关 `develop-gateway`：

```bash
lsof -nP -iTCP:48081 -sTCP:LISTEN
```

查看 Nacos：

```bash
lsof -nP -iTCP:8848 -sTCP:LISTEN
```

输出中的 `PID` 就是进程 ID，例如：

```text
COMMAND   PID   USER   ...
java      23337 david  ...
```

根据 PID 查看完整启动命令：

```bash
ps -p <PID> -o pid,command
```

示例：

```bash
ps -p 23337 -o pid,command
```

## 4. 启动顺序要求

网关和主项目启动时需要连接 Nacos，用于服务注册和配置加载。因此启动顺序建议是：

```text
1. 先启动 Nacos
2. 确认 Nacos 8848 端口可用，且鉴权账号可以登录
3. 再启动 develop-server 主项目
4. 最后启动 develop-gateway 网关
```

如果 Nacos 没有启动、鉴权配置错误、账号密码错误，或者 Nacos 配置拉取失败，`develop-server` 和 `develop-gateway` 可能会启动失败或注册失败。

## 5. 验证服务是否可用

验证 Nacos 登录：

```bash
curl -s -X POST 'http://localhost:8848/nacos/v1/auth/login' \
  -d 'username=nacos&password=Nacos'
```

验证主项目接口：

```bash
curl -i 'http://localhost:48080/admin-api/system/v3/api-docs'
```

验证网关转发：

```bash
curl -i 'http://localhost:48081/admin-api/system/v3/api-docs'
```

## 6. 查看日志

当前本机常用日志文件：

```bash
tail -f /tmp/nacos/logs/restart.out
tail -f /tmp/develop-server-retest.log
tail -f /tmp/develop-gateway.log
```

也可以查看项目默认日志目录：

```bash
ls -la ~/logs
```

常见日志文件：

```bash
tail -f ~/logs/develop-server.log
tail -f ~/logs/develop-gateway.log
```

## 7. 停止服务

先根据端口查 PID：

```bash
lsof -nP -iTCP:48080 -sTCP:LISTEN
```

再停止进程：

```bash
kill <PID>
```

示例：

```bash
kill 23337
```

一般不要直接使用 `kill -9`，除非普通 `kill` 无法停止进程。

## 8. 当前项目启动状态示例

一次正常启动后，可能看到类似状态：

```text
Nacos            8848/9848/9849/7848
Develop Server   48080
Develop Gateway  48081
```

其中：

```text
48080  develop-server 主项目
48081  develop-gateway 网关
```
