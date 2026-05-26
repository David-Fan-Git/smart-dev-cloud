---
name: starter-websocket
description: WebSocket messaging with JSON message format, type-based dispatch, login handshake auth, multi-node broadcast (local/Redis/RocketMQ/RabbitMQ/Kafka), and cross-module push via Feign
type: project
---

# develop-spring-boot-starter-websocket

## Overview

WebSocket 实时消息模块。提供 JSON 格式的消息收发、按 `type` 字段派发的监听器模式、登录用户握手鉴权，以及多节点广播能力（支持 local/Redis/RocketMQ/RabbitMQ/Kafka 五种发送模式）。支持跨模块消息推送（通过 `WebSocketSenderApi` Feign 接口）。所有 Bean 通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册 **1 个自动配置类**。

**Package base:** `com.develop.mvp.pk.framework.websocket`

## AutoConfiguration Registration

```
# develop-spring-boot-starter-websocket AutoConfiguration.imports
com.develop.mvp.pk.framework.websocket.config.DevelopWebSocketAutoConfiguration
```

Note: There is only one auto-configuration class. All WebSocket senders (local, Redis, RocketMQ, RabbitMQ, Kafka) are conditionally created within this single class.

## Core Components

### 1. JsonWebSocketMessage

```java
public class JsonWebSocketMessage {
    private String type;         // Message type identifier (e.g., "notification:new", "chat:message")
    private Object content;      // Message content (any JSON-serializable object)
}
```

All WebSocket messages use this unified JSON format.

### 2. WebSocketMessageListener\<T\>

```java
public interface WebSocketMessageListener<T> {
    void onMessage(WebSocketSession session, T message);  // Handle incoming message
    String getType();                                      // Message type this listener handles
}
```

Each listener registers for a specific `type` value. Multiple listeners for different types can coexist.

### 3. JsonWebSocketMessageHandler

- Implements `TextWebSocketHandler`
- On message received:
  1. Deserializes JSON payload to `JsonWebSocketMessage`
  2. Routes to the matching `WebSocketMessageListener` by `type` field
  3. Handles unknown types: returns error message to client
- Error handling: catches exceptions per listener, prevents cascade failures

### 4. WebSocketSessionManager

Manages all WebSocket sessions on the current node:

```java
public interface WebSocketSessionManager {
    void addSession(WebSocketSession session);
    void removeSession(WebSocketSession session);
    WebSocketSession getSession(String sessionId);
    Set<WebSocketSession> getSessionByUserId(Long userId);  // Multi-session per user
    int getSessionCount();
}
```

- `WebSocketSessionManagerImpl`: default implementation using ConcurrentHashMap
- Indexes by both `sessionId` and `userId`
- `WebSocketSessionHandlerDecorator`: decorates sessions with metadata (userId, tenantId, etc.)

### 5. LoginUserHandshakeInterceptor

- Implements `HandshakeInterceptor`
- During WebSocket handshake:
  1. Extracts token from query parameter or header
  2. Calls `OAuth2TokenCommonApi.checkAccessToken()` for validation
  3. On success: stores `LoginUser` in `WebSocketSession.attributes`
  4. On failure: returns 401 status and rejects the connection
- Token parameter fallback: uses `develop.security.token-parameter` (since WebSocket cannot set custom headers from browser)

### 6. Multi-Node Broadcast

Configured via `develop.websocket.senderType` (not `sender-type`):

| Mode | Underlying Mechanism | Use Case |
|---|---|---|
| `local` | Direct session iteration | Single node (dev, simple deployment) |
| `redis` | Redis Pub/Sub | Multi-node, no additional MQ dependency |
| `rocketmq` | RocketMQ broadcast | Multi-node, RocketMQ already in stack |
| `rabbitmq` | RabbitMQ fanout exchange | Multi-node, RabbitMQ already in stack |
| `kafka` | Kafka topic broadcast | Multi-node, Kafka already in stack |

How multi-node broadcast works:
1. Node A sends message via `WebSocketSenderApi.send(WebSocketSendReqVO)`
2. The sender implementation publishes to the shared channel (Redis/RocketMQ/etc.)
3. All nodes receive the message
4. Each node's consumer checks if the target user's session is on the current node (via `WebSocketSessionManager`)
5. If yes, delivers to the local session; if no, ignores

### 7. WebSocketSenderApi

Feign interface allowing cross-module push:

```java
@FeignClient(name = "system-server", contextId = "websocketSenderApi")
public interface WebSocketSenderApi {
    @PostMapping("/rpc-api/websocket/send")
    CommonResult<Boolean> send(@Valid @RequestBody WebSocketSendReqVO reqVO);
}
```

Allows any business module to push WebSocket messages without direct WebSocket dependency.

### 8. WebSocketFrameworkUtils

```java
public class WebSocketFrameworkUtils {
    public static Long getLoginUserId(WebSocketSession session);      // Get user ID from session
    public static LoginUser getLoginUser(WebSocketSession session);   // Get full LoginUser
    public static String getIp(WebSocketSession session);             // Get client IP
}
```

## Configuration Properties

```yaml
develop:
  websocket:
    enable: true                          # Enable WebSocket (default: true)
    path: /ws                             # WebSocket endpoint path (default: /ws, not /ws/{token})
    sender-type: local                    # Broadcast mode: local, redis, rocketmq, rabbitmq, kafka
    max-session-count: 10000              # Max concurrent connections per node (memory bound)
```

## Code Examples

```java
// 1. Register message listener
@Component
public class ChatMessageListener implements WebSocketMessageListener<ChatMessage> {
    @Override
    public String getType() {
        return "chat:message";
    }
    @Override
    public void onMessage(WebSocketSession session, ChatMessage message) {
        SendResponse response = chatService.handleMessage(message);
        session.sendMessage(new TextMessage(JsonUtils.toJsonString(response)));
    }
}

// 2. Server-side push
@Service
public class NotificationService {
    @Resource
    private WebSocketSessionManager sessionManager;

    public void notifyUser(Long userId, NotificationVO notification) {
        Set<WebSocketSession> sessions = sessionManager.getSessionByUserId(userId);
        JsonWebSocketMessage message = new JsonWebSocketMessage("notification:new", notification);
        for (WebSocketSession session : sessions) {
            synchronized (session) {  // Thread-safe send
                session.sendMessage(new TextMessage(JsonUtils.toJsonString(message)));
            }
        }
    }
}

// 3. Cross-module push (via Feign)
@Service
public class OrderService {
    @Resource
    private WebSocketSenderApi webSocketSenderApi;

    public void orderCreated(OrderDO order) {
        WebSocketSendReqVO reqVO = new WebSocketSendReqVO();
        reqVO.setUserId(order.getUserId());
        reqVO.setType("order:created");
        reqVO.setContent(order);
        // Async send to avoid blocking order processing
        CompletableFuture.runAsync(() -> webSocketSenderApi.send(reqVO));
    }
}
```

## Production Concerns

| Concern | Recommendation |
|---|---|
| Session memory | `max-session-count` per node; each session ~1-5 KB (WebSocket + user context). Set based on available heap |
| Heartbeat | Client sends ping every 30s; configure WebSocket container `setHeartbeatTime(30000)` |
| Connection leakage | `WebSocketSessionManager.removeSession()` must be called on `afterConnectionClosed` |
| Cross-module push latency | Feign call adds network RTT; batch pushes for high-frequency notifications |
| Multi-node consistency | Redis/RocketMQ/RabbitMQ/Kafka broadcast is at-most-once; idempotent message handling recommended |
| Authentication failure during handshake | Return 401; client should implement reconnection with exponential backoff |

## 注意事项

- WebSocket 连接数受限于单节点内存（`max-session-count`），大规模部署建议按用户 ID 范围水平切分
- 广播模式下（Redis/RocketMQ/RabbitMQ/Kafka），所有节点接收所有消息，但只有持有目标用户 Session 的节点实际投递。这是通过 `WebSocketSessionManager.getSessionByUserId()` 检查实现的
- `LoginUserHandshakeInterceptor` 握手阶段若 token 验证失败，需返回 401 状态码并关闭连接；**不可** 在握手成功后才发现未认证
- 心跳机制建议客户端每 30 秒发送 ping 帧；服务端 `WebSocketConfigurer` 通过 `setHeartbeatTime(30000)` 检测断连——30秒无心跳视为断开
- `WebSocketSenderApi` 的 Feign 调用建议异步处理（`@Async` 或 `CompletableFuture.runAsync`），避免阻塞业务主流程
- 同一用户多端登录时，`getSessionByUserId(userId)` 返回多个 `WebSocketSession` 实例；推送消息时应遍历所有 session 确保送达全部终端
- `senderType` 属性的默认值为 `local`（单节点模式），多节点部署需显式配置为 `redis`、`rocketmq`、`rabbitmq` 或 `kafka`
- WebSocket endpoint 路径默认是 `/ws`（**不包含** `{token}` 路径变量），Token 通过查询参数传递（`ws://host/ws?token=xxx`）
