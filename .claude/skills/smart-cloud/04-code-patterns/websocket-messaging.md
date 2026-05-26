---
name: websocket-messaging
description: WebSocket real-time messaging — listener, sender, heartbeat, reconnection, cross-module push via Feign, multi-terminal (ADMIN/MEMBER)
type: project
---

# WebSocket 消息模式

## 概述

使用 `develop-spring-boot-starter-websocket` 实现实时消息推送。服务端通过 `WebSocketMessageListener` 接收客户端消息，通过 `WebSocketMessageSender` 向客户端推送。跨模块推送通过 `WebSocketSenderApi` Feign 接口。支持管理员端（ADMIN）和会员端（MEMBER）。

## 1. 定义消息体

```java
package com.develop.mvp.pk.module.{module}.websocket.message;

import lombok.Data;
import java.time.LocalDateTime;

/** 客户端 → 服务端 */
@Data
public class {Domain}SendMessage {
    private Long toUserId;
    private String content;
    private Integer msgType;
}

/** 服务端 → 客户端 */
@Data
public class {Domain}ReceiveMessage {
    private Long fromUserId;
    private String content;
    private String nickname;
    private LocalDateTime sendTime;
}
```

## 2. 服务端消息监听器

```java
package com.develop.mvp.pk.module.{module}.websocket;

import com.develop.mvp.pk.framework.websocket.core.listener.WebSocketMessageListener;
import com.develop.mvp.pk.framework.websocket.core.sender.WebSocketMessageSender;
import com.develop.mvp.pk.framework.websocket.core.utils.WebSocketFrameworkUtils;
import com.develop.mvp.pk.module.{module}.websocket.message.{Domain}ReceiveMessage;
import com.develop.mvp.pk.module.{module}.websocket.message.{Domain}SendMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@Slf4j
public class {Domain}WebSocketMessageListener implements WebSocketMessageListener<{Domain}SendMessage> {

    @Resource
    private WebSocketMessageSender webSocketMessageSender;

    @Override
    public void onMessage(WebSocketSession session, {Domain}SendMessage message) {
        Long fromUserId = WebSocketFrameworkUtils.getLoginUserId(session);
        log.info("[onMessage][用户({}) 发送消息给({}): {}]", fromUserId, message.getToUserId(), message.getContent());

        {Domain}ReceiveMessage receiveMessage = new {Domain}ReceiveMessage();
        receiveMessage.setFromUserId(fromUserId);
        receiveMessage.setContent(message.getContent());
        receiveMessage.setNickname("用户" + fromUserId);
        receiveMessage.setSendTime(LocalDateTime.now());

        webSocketMessageSender.sendObject(
                UserTypeEnum.ADMIN.getValue(),   // 用户类型
                message.getToUserId(),            // 目标用户
                "{domain}-message-receive",       // 消息类型
                receiveMessage);                  // 消息体
    }

    @Override
    public String getType() {
        return "{domain}-message-send"; // 与客户端发送的消息类型一致
    }
}
```

## 3. 服务端主动推送

```java
@Service
@Validated
public class {Domain}ServiceImpl implements {Domain}Service {

    @Resource
    private WebSocketMessageSender webSocketMessageSender;

    /** 推送给指定用户 */
    public void notifyUser(Long userId, String content) {
        NotificationMessage msg = new NotificationMessage();
        msg.setContent(content);
        msg.setSendTime(LocalDateTime.now());

        webSocketMessageSender.sendObject(
                UserTypeEnum.ADMIN.getValue(), // 用户类型
                userId,                        // 目标用户
                "notification",                // 消息类型
                msg);                          // 消息体
    }

    /** 广播给所有在线管理员 */
    public void broadcast(String content) {
        webSocketMessageSender.sendObject(
                UserTypeEnum.ADMIN.getValue(),
                0L,             // userId=0 表示广播
                "broadcast",
                content);
    }
}
```

## 4. 跨模块推送（通过 Feign）

```java
@Service
@Validated
public class {Other}ServiceImpl implements {Other}Service {

    @Resource
    private WebSocketSenderApi webSocketSenderApi;

    public void afterBusiness(Long userId, String result) {
        CommonResult<Boolean> result = webSocketSenderApi.send(
                UserTypeEnum.ADMIN.getValue(),
                userId,
                "business-notification",
                Map.of("result", result));
    }
}
```

## 5. 心跳与重连

WebSocket 客户端自动处理心跳和重连：
- 客户端每 30 秒发送 Ping 帧
- 服务端回复 Pong 帧
- 连接断开时客户端自动重连（指数退避，最长 30 秒）
- 重连成功后自动恢复会话

## 6. 消息确认（ACK）

```java
/** 客户端收到消息后回复 ACK */
public class {Domain}AckMessage {
    private String messageId;   // 消息唯一 ID
    private Long receiveTime;   // 接收时间戳
}

// 服务端监听 ACK，超时未收到则标记为未送达
```

## WebSocketMessageSender 方法

```java
// 发送给指定用户
sendObject(userType, userId, messageType, data);

// 参数说明:
// userType:   UserTypeEnum.ADMIN.getValue() 或 UserTypeEnum.MEMBER.getValue()
// userId:     接收用户（0 = 广播）
// messageType: 客户端路由标识
// data:       消息体（JSON 序列化）
```

## 消息类型常量

```java
public interface WebSocketMessageTypes {
    String NOTIFICATION = "notification";
    String BROADCAST = "broadcast";
    String ORDER_UPDATE = "order-update";
    String TASK_ASSIGN = "task-assign";
}
```

## 关键点

1. **`WebSocketMessageListener<T>`** — 实现 `getType()` + `onMessage()`，`@Component` 注册
2. **`WebSocketMessageSender.sendObject()`** — 服务端主动推送入口
3. **`UserTypeEnum`** — 区分管理员（ADMIN）和会员（MEMBER）
4. **`messageType`** — 客户端服务端约定的协议类型
5. **`WebSocketFrameworkUtils.getLoginUserId(session)`** — 从 Session 获取当前用户
6. **广播** — `userId = 0L`
7. **跨模块推送** — 使用 `WebSocketSenderApi` Feign 客户端
8. **心跳** — 客户端自动 Ping/Pong，断开自动重连

## 常见错误

- `getType()` 返回的类型与客户端不匹配 — 监听器收不到消息
- `onMessage()` 中抛出未捕获异常 — WebSocket 连接断开
- 给不存在的用户推送 — `sendObject()` 静默处理，需加日志确认
- 消息体含无法序列化的字段（循环引用）— 只含简单 POJO
- `UserTypeEnum` 用错 — ADMIN 和 MEMBER 不可混用
- 大消息体（超过 1MB）— 应拆分或走 MQ
