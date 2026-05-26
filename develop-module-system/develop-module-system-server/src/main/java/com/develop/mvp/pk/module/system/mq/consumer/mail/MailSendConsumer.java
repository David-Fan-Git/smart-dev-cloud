package com.develop.mvp.pk.module.system.mq.consumer.mail;

import com.develop.mvp.pk.module.system.application.mail.port.inbound.MailUseCase;
import com.develop.mvp.pk.module.system.mq.message.mail.MailSendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 针对 {@link MailSendMessage} 的消费者
 *
 * @author David
 */
@Component
@Slf4j
public class MailSendConsumer {

    @Resource
    private MailUseCase mailSendService;

    /**
     * 处理 on Message 对应的业务逻辑。
     *
     * @param message message 参数
     */
    @EventListener
    @Async // Spring Event 默认在 Producer 发送的线程，通过 @Async 实现异步
    public void onMessage(MailSendMessage message) {
        log.info("[onMessage][消息内容({})]", message);
        mailSendService.doSendMail(message);
    }

}
