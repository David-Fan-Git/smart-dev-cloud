package com.develop.mvp.pk.framework.mq.redis.config;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import com.develop.mvp.pk.framework.common.enums.DocumentEnum;
import com.develop.mvp.pk.framework.mq.redis.core.RedisMQTemplate;
import com.develop.mvp.pk.framework.mq.redis.core.job.RedisPendingMessageResendJob;
import com.develop.mvp.pk.framework.mq.redis.core.job.RedisStreamMessageCleanupJob;
import com.develop.mvp.pk.framework.mq.redis.core.pubsub.AbstractRedisChannelMessageListener;
import com.develop.mvp.pk.framework.mq.redis.core.stream.AbstractRedisStreamMessageListener;
import com.develop.mvp.pk.framework.redis.config.DevelopRedisAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;
import java.util.Properties;

/**
 * Redis 消息队列消费者自动配置。
 *
 * <p>该配置在基础 Redis 自动配置完成之后生效，负责把应用中声明的 Redis 消息监听器接入运行时容器：
 * {@link AbstractRedisChannelMessageListener} 通过 Redis Pub/Sub 实现广播消费，
 * {@link AbstractRedisStreamMessageListener} 通过 Redis Stream 消费者组实现集群消费。</p>
 *
 * <p>配置类只负责创建监听容器、注册监听器以及装配 Redis Stream 的补偿任务；具体业务处理仍由各监听器的
 * {@code onMessage} 实现完成，消息发送与消费前后的横切增强复用 {@link RedisMQTemplate} 中的拦截器列表。</p>
 *
 * @author David
 */
@Slf4j
@EnableScheduling // 启用定时任务，用于 RedisPendingMessageResendJob 重发消息
@AutoConfiguration(after = DevelopRedisAutoConfiguration.class)
public class DevelopRedisMQConsumerAutoConfiguration {

    /**
     * 创建 Redis Pub/Sub 广播消费容器。
     *
     * <p>只有业务侧声明了 {@link AbstractRedisChannelMessageListener} Bean 时才会装配该容器。
     * 方法会把每个监听器绑定到其消息类型对应的 Channel，并注入同一个 {@link RedisMQTemplate}，
     * 使监听器在真正执行业务 {@code onMessage} 前后可以调用模板中的消费拦截器。</p>
     *
     * @param redisMQTemplate Redis MQ 模板，提供底层 Redis 连接工厂和拦截器列表
     * @param listeners 应用中声明的 Pub/Sub 监听器集合
     * @return Redis Pub/Sub 消息监听容器
     */
    @Bean
    @ConditionalOnBean(AbstractRedisChannelMessageListener.class) // 只有 AbstractChannelMessageListener 存在的时候，才需要注册 Redis pubsub 监听
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisMQTemplate redisMQTemplate, List<AbstractRedisChannelMessageListener<?>> listeners) {
        // 创建 RedisMessageListenerContainer 对象
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        // 设置 RedisConnection 工厂。
        container.setConnectionFactory(redisMQTemplate.getRedisTemplate().getRequiredConnectionFactory());
        // 添加监听器
        listeners.forEach(listener -> {
            listener.setRedisMQTemplate(redisMQTemplate);
            container.addMessageListener(listener, new ChannelTopic(listener.getChannel()));
            log.info("[redisMessageListenerContainer][注册 Channel({}) 对应的监听器({})]",
                    listener.getChannel(), listener.getClass().getName());
        });
        return container;
    }

    /**
     * 创建 Redis Stream 待确认消息重新投递任务。
     *
     * <p>该任务只在存在 Stream 监听器时启用，用于配合监听器集合、Redis 模板和 Redisson 客户端处理
     * Pending 消息的补偿场景；具体重发策略由 {@link RedisPendingMessageResendJob} 承担。</p>
     *
     * @param listeners 应用中声明的 Stream 监听器集合
     * @param redisTemplate Redis MQ 模板
     * @param redissonClient Redisson 客户端
     * @return Redis Stream Pending 消息重发任务
     */
    @Bean
    @ConditionalOnBean(AbstractRedisStreamMessageListener.class) // 只有 AbstractStreamMessageListener 存在的时候，才需要注册 Redis pubsub 监听
    public RedisPendingMessageResendJob redisPendingMessageResendJob(List<AbstractRedisStreamMessageListener<?>> listeners,
                                                                     RedisMQTemplate redisTemplate,
                                                                     RedissonClient redissonClient) {
        return new RedisPendingMessageResendJob(listeners, redisTemplate, redissonClient);
    }

    /**
     * 创建 Redis Stream 消息清理任务。
     *
     * <p>该任务只在存在 Stream 监听器时启用，用于把监听器定义的 Stream 纳入统一清理流程；
     * 清理边界与执行细节由 {@link RedisStreamMessageCleanupJob} 维护。</p>
     *
     * @param listeners 应用中声明的 Stream 监听器集合
     * @param redisTemplate Redis MQ 模板
     * @param redissonClient Redisson 客户端
     * @return Redis Stream 消息清理任务
     */
    @Bean
    @ConditionalOnBean(AbstractRedisStreamMessageListener.class)
    public RedisStreamMessageCleanupJob redisStreamMessageCleanupJob(List<AbstractRedisStreamMessageListener<?>> listeners,
                                                                     RedisMQTemplate redisTemplate,
                                                                     RedissonClient redissonClient) {
        return new RedisStreamMessageCleanupJob(listeners, redisTemplate, redissonClient);
    }

    /**
     * 创建 Redis Stream 集群消费容器。
     *
     * <p>容器以 {@link StreamMessageListenerContainer} 承载 Stream 监听流程，并通过
     * {@code initMethod = "start"} 随 Bean 初始化启动监听、通过 {@code destroyMethod = "stop"}
     * 在容器销毁时停止监听。启动前会校验 Redis 主版本不低于 5，因为 Stream 消费者组依赖 Redis 5 提供的能力。</p>
     *
     * <p>注册监听器时，本方法会为每个 Stream Key 尝试创建监听器声明的消费者组，并使用
     * {@link #buildConsumerName()} 生成当前进程的消费者名。读取位置使用 {@link ReadOffset#lastConsumed()}，
     * 且关闭自动 ack；监听器成功处理消息后再由自身执行 acknowledge。</p>
     *
     * <p>基础知识：<a href="https://www.geek-book.com/src/docs/redis/redis/redis.io/commands/xreadgroup.html">Redis Stream 的 xreadgroup 命令</a></p>
     *
     * @param redisMQTemplate Redis MQ 模板，提供连接工厂和监听器消费时使用的拦截器列表
     * @param listeners 应用中声明的 Stream 监听器集合
     * @return Redis Stream 消息监听容器
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnBean(AbstractRedisStreamMessageListener.class) // 只有 AbstractStreamMessageListener 存在的时候，才需要注册 Redis pubsub 监听
    public StreamMessageListenerContainer<String, ObjectRecord<String, String>> redisStreamMessageListenerContainer(
            RedisMQTemplate redisMQTemplate, List<AbstractRedisStreamMessageListener<?>> listeners) {
        RedisTemplate<String, ?> redisTemplate = redisMQTemplate.getRedisTemplate();
        checkRedisVersion(redisTemplate);
        // 第一步，创建 StreamMessageListenerContainer 容器
        // 创建 options 配置
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, ObjectRecord<String, String>> containerOptions =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .batchSize(10) // 一次性最多拉取多少条消息
                        .targetType(String.class) // 目标类型。统一使用 String，通过自己封装的 AbstractStreamMessageListener 去反序列化
                        .build();
        // 创建 container 对象
        StreamMessageListenerContainer<String, ObjectRecord<String, String>> container =
                StreamMessageListenerContainer.create(redisMQTemplate.getRedisTemplate().getRequiredConnectionFactory(), containerOptions);

        // 第二步，注册监听器，消费对应的 Stream 主题
        String consumerName = buildConsumerName();
        listeners.parallelStream().forEach(listener -> {
            log.info("[redisStreamMessageListenerContainer][开始注册 StreamKey({}) 对应的监听器({})]",
                    listener.getStreamKey(), listener.getClass().getName());
            // 创建 listener 对应的消费者分组
            try {
                redisTemplate.opsForStream().createGroup(listener.getStreamKey(), listener.getGroup());
            } catch (Exception ignore) {
            }
            // 设置 listener 对应的 redisTemplate
            listener.setRedisMQTemplate(redisMQTemplate);
            // 创建 Consumer 对象
            Consumer consumer = Consumer.from(listener.getGroup(), consumerName);
            // 设置 Consumer 消费进度，以最小消费进度为准
            StreamOffset<String> streamOffset = StreamOffset.create(listener.getStreamKey(), ReadOffset.lastConsumed());
            // 设置 Consumer 监听
            StreamMessageListenerContainer.StreamReadRequestBuilder<String> builder = StreamMessageListenerContainer.StreamReadRequest
                    .builder(streamOffset).consumer(consumer)
                    .autoAcknowledge(false) // 不自动 ack
                    .cancelOnError(throwable -> false); // 默认配置，发生异常就取消消费，显然不符合预期；因此，我们设置为 false
            container.register(builder.build(), listener);
            log.info("[redisStreamMessageListenerContainer][完成注册 StreamKey({}) 对应的监听器({})]",
                    listener.getStreamKey(), listener.getClass().getName());
        });
        return container;
    }

    /**
     * 构建消费者名字，使用本地 IP + 进程编号的方式。
     * 参考自 RocketMQ clientId 的实现
     *
     * @return 消费者名字
     */
    public static String buildConsumerName() {
        return String.format("%s@%d", SystemUtil.getHostInfo().getAddress(), SystemUtil.getCurrentPID());
    }

    /**
     * 校验 Redis 版本号是否满足 Stream 消费的最低要求。
     *
     * <p>Redis Stream 是 Redis 5 开始提供的能力，因此消费者容器启动前需要读取服务端版本并阻止低版本运行，
     * 避免监听容器启动后才因命令不支持而失败。</p>
     *
     * @param redisTemplate 用于读取 Redis 服务端信息的模板
     */
    public static void checkRedisVersion(RedisTemplate<String, ?> redisTemplate) {
        // 获得 Redis 版本
        Properties info = redisTemplate.execute((RedisCallback<Properties>) RedisServerCommands::info);
        String version = MapUtil.getStr(info, "redis_version");
        // 校验最低版本必须大于等于 5.0.0
        int majorVersion = Integer.parseInt(StrUtil.subBefore(version, '.', false));
        if (majorVersion < 5) {
            throw new IllegalStateException(StrUtil.format("您当前的 Redis 版本为 {}，小于最低要求的 5.0.0 版本！" +
                    "请参考 {} 文档进行安装。", version, DocumentEnum.REDIS_INSTALL.getUrl()));
        }
    }

}
