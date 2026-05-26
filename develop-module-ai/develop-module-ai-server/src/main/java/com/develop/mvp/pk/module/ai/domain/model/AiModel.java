package com.develop.mvp.pk.module.ai.domain.model;

import com.develop.mvp.pk.module.ai.domain.model.event.AiModelCreatedEvent;
import com.develop.mvp.pk.module.ai.domain.model.event.AiModelDeletedEvent;
import com.develop.mvp.pk.module.ai.domain.model.event.DomainEvent;
import com.develop.mvp.pk.module.ai.domain.model.valueobject.AiModelId;
import com.develop.mvp.pk.module.ai.domain.model.valueobject.AiModelPlatform;
import com.develop.mvp.pk.module.ai.domain.model.valueobject.AiModelStatus;
import com.develop.mvp.pk.module.ai.domain.model.valueobject.AiModelType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class AiModel {

    private final AiModelId id;
    private Long keyId;
    private String name;
    private String model;
    private String platform;
    private Integer type;
    private Integer sort;
    private AiModelStatus status;
    private Double temperature;
    private Integer maxTokens;
    private Integer maxContexts;

    private final List<DomainEvent> events = new ArrayList<>();

    // Full constructor for factory use (package-private)
    AiModel(Long id, Long keyId, String name, String model, String platform,
            Integer type, Integer sort, AiModelStatus status,
            Double temperature, Integer maxTokens, Integer maxContexts) {
        this.id = id != null ? AiModelId.of(id) : null;
        this.keyId = keyId;
        this.name = Objects.requireNonNull(name, "模型名称不能为空");
        this.model = Objects.requireNonNull(model, "模型标志不能为空");
        this.platform = AiModelPlatform.of(platform).platform();
        this.type = AiModelType.of(type).type();
        this.sort = sort;
        this.status = status != null ? status : AiModelStatus.ENABLED;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.maxContexts = maxContexts;
    }

    // Minimal constructor for skeleton compatibility
    public AiModel(Long id, String name) {
        this.id = id != null ? AiModelId.of(id) : null;
        this.name = Objects.requireNonNull(name, "模型名称不能为空");
        this.model = name;
        this.status = AiModelStatus.ENABLED;
        this.events.clear();
    }

    // Factory for backward compatibility
    public static AiModel of(Long id, String name) {
        return new AiModel(id, name);
    }

    // Business methods

    public void updateProfile(Long keyId, String name, String model, String platform,
                               Integer type, Integer sort, Integer status,
                               Double temperature, Integer maxTokens,
                               Integer maxContexts) {
        this.keyId = keyId;
        this.name = Objects.requireNonNull(name, "模型名称不能为空");
        this.model = Objects.requireNonNull(model, "模型标志不能为空");
        this.platform = AiModelPlatform.of(platform).platform();
        this.type = AiModelType.of(type).type();
        this.sort = sort;
        this.status = status != null ? AiModelStatus.of(status) : AiModelStatus.ENABLED;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.maxContexts = maxContexts;
    }

    public void enable() {
        this.status = this.status.enable();
    }

    public void disable() {
        this.status = this.status.disable();
    }

    public void markCreated() {
        if (this.id == null) {
            throw new IllegalStateException("模型创建事件必须包含已持久化编号");
        }
        events.add(new AiModelCreatedEvent(this.id.value(), this.name, this.model, this.platform));
    }

    public void markDeleted() {
        events.add(new AiModelDeletedEvent(
                this.id != null ? this.id.value() : null, this.name, this.model));
    }

    // Query methods

    public AiModelId modelId() { return id; }
    public Long id() { return id != null ? id.value() : null; }
    public Long keyId() { return keyId; }
    public String name() { return name; }
    public String model() { return model; }
    public String platform() { return platform; }
    public Integer type() { return type; }
    public Integer sort() { return sort; }
    public AiModelStatus status() { return status; }
    public Double temperature() { return temperature; }
    public Integer maxTokens() { return maxTokens; }
    public Integer maxContexts() { return maxContexts; }

    public boolean isEnabled() { return status.isEnabled(); }
    public boolean isDisabled() { return status.isDisabled(); }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> result = new ArrayList<>(events);
        events.clear();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AiModel that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AiModel{id=" + id + ", name=" + name + ", model=" + model + '}';
    }
}
