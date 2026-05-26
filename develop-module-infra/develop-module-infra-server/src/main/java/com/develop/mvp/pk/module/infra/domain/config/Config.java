package com.develop.mvp.pk.module.infra.domain.config;

// DDD 角色：系统配置聚合根
// 规则 R01：SYSTEM 类型的配置不可删除（由应用层校验）
// 规则 R02：配置键在全局不可重复（由应用层校验）
// 规则 R03：不可见的配置不允许返回给前端（由应用层校验）

import com.develop.mvp.pk.module.infra.domain.config.event.*;
import com.develop.mvp.pk.module.infra.domain.config.valueobject.*;
import com.develop.mvp.pk.module.infra.domain.event.DomainEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Config {

    private final ConfigId id;
    private final ConfigKey key;
    private String value;
    private String name;
    private String category;
    private ConfigType type;
    private ConfigVisible visible;
    private String remark;

    private final List<DomainEvent> events = new ArrayList<>();

    public Config(ConfigId id, ConfigKey key, String value, String name, String category,
           ConfigType type, ConfigVisible visible, String remark) {
        // 新建配置在入库前还没有数据库编号；持久化重建后的配置必须带有 id。
        this.id = id;
        this.key = Objects.requireNonNull(key, "configKey 不能为空");
        this.value = value;
        this.name = name;
        this.category = category;
        this.type = type != null ? type : ConfigType.CUSTOM;
        this.visible = visible != null ? visible : ConfigVisible.VISIBLE;
        this.remark = remark;
    }

    // ── 业务方法 ──

    /** 更新配置值 */
    public void updateValue(String newValue) {
        this.value = newValue;
    }

    /** 更新配置基本信息 */
    public void updateProfile(String value, String name, String category, ConfigVisible visible, String remark) {
        this.value = value;
        this.name = name;
        this.category = category;
        this.visible = visible != null ? visible : this.visible;
        this.remark = remark;
        events.add(new ConfigUpdatedEvent(this.id.value(), this.key.value()));
    }

    /** 规则 R01：校验是否为系统配置（不可删除） */
    public boolean isSystemType() {
        return this.type.isSystem();
    }

    /** 标记删除 */
    public void markDeleted() {
        events.add(new ConfigDeletedEvent(this.id.value(), this.key.value()));
    }

    /** 标记创建 */
    public void markCreated() {
        events.add(new ConfigCreatedEvent(this.id.value(), this.key.value()));
    }

    /** 规则 R03：校验是否可见 */
    public boolean isVisible() {
        return this.visible.isVisible();
    }

    // ── 查询方法 ──

    public ConfigId id() { return id; }
    public ConfigKey key() { return key; }
    public String value() { return value; }
    public String name() { return name; }
    public String category() { return category; }
    public ConfigType type() { return type; }
    public ConfigVisible visible() { return visible; }
    public String remark() { return remark; }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> result = new ArrayList<>(events);
        events.clear();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Config that)) return false;
        if (id == null || that.id == null) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() { return id != null ? Objects.hash(id) : System.identityHashCode(this); }

    @Override
    public String toString() {
        return "Config{id=" + id + ", key=" + key + '}';
    }
}
