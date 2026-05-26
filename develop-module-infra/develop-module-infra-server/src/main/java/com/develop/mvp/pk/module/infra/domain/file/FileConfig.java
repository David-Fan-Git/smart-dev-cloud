package com.develop.mvp.pk.module.infra.domain.file;

// DDD 角色：文件存储配置聚合根
// 规则 R01：只能有一个 Master 文件配置
// 规则 R02：Master 配置不可删除

import com.develop.mvp.pk.module.infra.domain.event.DomainEvent;
import com.develop.mvp.pk.module.infra.domain.file.event.FileConfigCreatedEvent;
import com.develop.mvp.pk.module.infra.domain.file.event.FileConfigDeletedEvent;
import com.develop.mvp.pk.module.infra.domain.file.event.FileConfigMasterChangedEvent;
import com.develop.mvp.pk.module.infra.domain.file.valueobject.FileConfigId;
import com.develop.mvp.pk.module.infra.domain.file.valueobject.FileConfigName;
import com.develop.mvp.pk.module.infra.framework.file.core.client.FileClientConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class FileConfig {

    private final FileConfigId id;
    private final FileConfigName name;
    private Integer storage;
    private Boolean master;
    private FileClientConfig config;
    private String remark;

    private final List<DomainEvent> events = new ArrayList<>();

    public FileConfig(FileConfigId id, FileConfigName name, Integer storage, Boolean master,
                      FileClientConfig config, String remark) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "fileConfigName 不能为空");
        this.storage = storage;
        this.master = master != null ? master : false;
        this.config = config;
        this.remark = remark;
    }

    // ── 业务方法 ──

    /** 更新文件配置 */
    public void updateProfile(Integer storage, String name, FileClientConfig config, String remark) {
        this.storage = storage;
        this.config = config;
        this.remark = remark;
    }

    /** 规则 R01：设置为 Master */
    public void setAsMaster() {
        if (!this.master) {
            this.master = true;
            events.add(new FileConfigMasterChangedEvent(this.id.value(), this.name.value()));
        }
    }

    /** 取消 Master 标记 */
    public void clearMaster() {
        this.master = false;
    }

    /** 规则 R02：是否为 Master */
    public boolean isMaster() {
        return Boolean.TRUE.equals(this.master);
    }

    public void markCreated() {
        if (this.id == null) {
            throw new IllegalStateException("文件配置创建事件必须包含已持久化编号");
        }
        events.add(new FileConfigCreatedEvent(this.id.value(), this.name.value()));
    }

    public void markDeleted() {
        events.add(new FileConfigDeletedEvent(this.id.value(), this.name.value()));
    }

    // ── 查询方法 ──

    public FileConfigId id() { return id; }
    public FileConfigName name() { return name; }
    public Integer storage() { return storage; }
    public Boolean master() { return master; }
    public FileClientConfig config() { return config; }
    public String remark() { return remark; }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> result = new ArrayList<>(events);
        events.clear();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileConfig that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "FileConfig{id=" + id + ", name=" + name + '}';
    }
}
