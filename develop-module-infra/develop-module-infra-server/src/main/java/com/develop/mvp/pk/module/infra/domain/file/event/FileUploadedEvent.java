package com.develop.mvp.pk.module.infra.domain.file.event;

// DDD 角色：文件上传完成后发布
import com.develop.mvp.pk.module.infra.domain.event.DomainEvent;

import java.time.LocalDateTime;

public record FileUploadedEvent(Long id, String name, String path, String url, LocalDateTime occurredAt) implements DomainEvent {
    public FileUploadedEvent(Long id, String name, String path, String url) {
        this(id, name, path, url, LocalDateTime.now());
    }
}
