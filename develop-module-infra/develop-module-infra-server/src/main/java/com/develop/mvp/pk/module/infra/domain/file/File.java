package com.develop.mvp.pk.module.infra.domain.file;

// DDD 角色：文件聚合根
// 每次文件上传记录一条记录

import com.develop.mvp.pk.module.infra.domain.event.DomainEvent;
import com.develop.mvp.pk.module.infra.domain.file.event.FileDeletedEvent;
import com.develop.mvp.pk.module.infra.domain.file.event.FileUploadedEvent;
import com.develop.mvp.pk.module.infra.domain.file.valueobject.FileConfigId;
import com.develop.mvp.pk.module.infra.domain.file.valueobject.FileId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class File {

    private final FileId id;
    private FileConfigId configId;
    private String name;
    private String path;
    private String url;
    private String type;
    private Long size;

    private final List<DomainEvent> events = new ArrayList<>();

    public File(FileId id, FileConfigId configId, String name, String path, String url, String type, Long size) {
        this.id = id;
        this.configId = configId;
        this.name = name;
        this.path = path;
        this.url = url;
        this.type = type;
        this.size = size;
    }

    // ── 业务方法 ──

    public void markUploaded() {
        if (this.id == null) {
            throw new IllegalStateException("文件上传事件必须包含已持久化编号");
        }
        events.add(new FileUploadedEvent(this.id.value(), this.name, this.path, this.url));
    }

    public void markDeleted() {
        events.add(new FileDeletedEvent(this.id.value(), this.path));
    }

    // ── 查询方法 ──

    public FileId id() { return id; }
    public FileConfigId configId() { return configId; }
    public String name() { return name; }
    public String path() { return path; }
    public String url() { return url; }
    public String type() { return type; }
    public Long size() { return size; }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> result = new ArrayList<>(events);
        events.clear();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof File that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "File{id=" + id + ", name=" + name + '}';
    }
}
