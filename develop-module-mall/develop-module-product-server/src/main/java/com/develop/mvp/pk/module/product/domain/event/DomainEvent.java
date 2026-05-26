package com.develop.mvp.pk.module.product.domain.event;

import java.time.LocalDateTime;

/**
 * 商品域领域事件基接口
 */
public interface DomainEvent {
    LocalDateTime occurredAt();
}
