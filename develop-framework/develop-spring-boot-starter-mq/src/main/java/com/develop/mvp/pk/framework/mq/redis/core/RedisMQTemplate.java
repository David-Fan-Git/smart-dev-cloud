package com.develop.mvp.pk.framework.mq.redis.core;

import com.develop.mvp.pk.framework.common.util.json.JsonUtils;
import com.develop.mvp.pk.framework.mq.redis.core.interceptor.RedisMessageInterceptor;
import com.develop.mvp.pk.framework.mq.redis.core.message.AbstractRedisMessage;
import com.develop.mvp.pk.framework.mq.redis.core.pubsub.AbstractRedisChannelMessage;
import com.develop.mvp.pk.framework.mq.redis.core.stream.AbstractRedisStreamMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis MQ 操作模板类，是业务侧发送 Redis 消息的统一入口。
 *
 * <p>业务代码只需要构造继承自 {@link AbstractRedisChannelMessage} 或 {@link AbstractRedisStreamMessage} 的消息对象，
 * 再调用本模板的 {@code send} 方法即可。模板负责把消息序列化为 JSON，并根据消息类型发布到 Redis Pub/Sub Channel
 * 或 Redis Stream。</p>
 *
 * <p>模板同时维护 {@link RedisMessageInterceptor} 列表。发送消息时会在真正写入 Redis 前后调用拦截器；
 * 消费监听器也会复用这份拦截器列表，在业务消费方法执行前后补充横切增强，例如租户上下文传递。</p>
 *
 * @author David
 */
@AllArgsConstructor
public class RedisMQTemplate {

    @Getter
    private final RedisTemplate<String, ?> redisTemplate;
    /**
     * 拦截器数组
     */
    @Getter
    private final List<RedisMessageInterceptor> interceptors = new ArrayList<>();

    /**
     * 发送 Redis Pub/Sub 广播消息。
     *
     * <p>发送流程会先执行发送前拦截器，再把消息序列化为 JSON 并通过消息自身声明的 Channel 发布；
     * {@code finally} 块保证发送后拦截器会被调用，用于清理或恢复发送前设置的上下文。</p>
     *
     * @param message 继承自 {@link AbstractRedisChannelMessage} 的广播消息
     */
    public <T extends AbstractRedisChannelMessage> void send(T message) {
        try {
            sendMessageBefore(message);
            // 发送消息
            redisTemplate.convertAndSend(message.getChannel(), JsonUtils.toJsonString(message));
        } finally {
            sendMessageAfter(message);
        }
    }

    /**
     * 发送 Redis Stream 集群消费消息。
     *
     * <p>发送流程会先执行发送前拦截器，再创建 Stream Record：消息内容为 JSON 字符串，Stream Key 来自消息对象自身。
     * Redis 写入成功后返回 {@link RecordId}，调用方可以据此获得本次发布生成的记录编号。</p>
     *
     * @param message 继承自 {@link AbstractRedisStreamMessage} 的 Stream 消息
     * @return Redis Stream 生成的消息记录编号对象
     */
    public <T extends AbstractRedisStreamMessage> RecordId send(T message) {
        try {
            sendMessageBefore(message);
            // 发送消息
            return redisTemplate.opsForStream().add(StreamRecords.newRecord()
                    .ofObject(JsonUtils.toJsonString(message)) // 设置内容
                    .withStreamKey(message.getStreamKey())); // 设置 stream key
        } finally {
            sendMessageAfter(message);
        }
    }

    /**
     * 添加 Redis 消息拦截器。
     *
     * <p>自动配置会把 Spring 容器中的拦截器注册进来。模板发送消息时使用这份列表，
     * Redis 监听器消费消息前后也会从模板读取同一份列表，从而保持发送侧和消费侧的横切扩展点一致。</p>
     *
     * @param interceptor 消息拦截器
     */
    public void addInterceptor(RedisMessageInterceptor interceptor) {
        interceptors.add(interceptor);
    }

    /**
     * 在消息写入 Redis 之前执行发送侧增强。
     *
     * <p>当前实现按拦截器列表的自然顺序调用，适合在发送前补充消息上下文或设置线程上下文。</p>
     *
     * @param message 即将发送的 Redis 消息
     */
    private void sendMessageBefore(AbstractRedisMessage message) {
        // 正序
        interceptors.forEach(interceptor -> interceptor.sendMessageBefore(message));
    }

    /**
     * 在消息发送流程结束后执行发送侧增强。
     *
     * <p>当前实现按拦截器列表的倒序调用，便于与发送前增强形成进入和退出的配对关系。</p>
     *
     * @param message 已完成发送流程的 Redis 消息
     */
    private void sendMessageAfter(AbstractRedisMessage message) {
        // 倒序
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            interceptors.get(i).sendMessageAfter(message);
        }
    }

}
