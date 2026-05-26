package com.develop.mvp.pk.module.report.domain.goview;

import com.develop.mvp.pk.module.report.domain.goview.event.DomainEvent;
import com.develop.mvp.pk.module.report.domain.goview.event.GoViewProjectCreatedEvent;
import com.develop.mvp.pk.module.report.domain.goview.event.GoViewProjectDeletedEvent;
import com.develop.mvp.pk.module.report.domain.goview.valueobject.GoViewProjectId;
import com.develop.mvp.pk.module.report.domain.goview.valueobject.GoViewProjectStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ReportProject {

    private final GoViewProjectId id;
    private String name;
    private String picUrl;
    private String content;
    private GoViewProjectStatus status;
    private String remark;

    private final List<DomainEvent> events = new ArrayList<>();

    ReportProject(GoViewProjectId id, String name, String picUrl, String content,
                  GoViewProjectStatus status, String remark) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "项目名称不能为空");
        this.picUrl = picUrl;
        this.content = content;
        this.status = status != null ? status : GoViewProjectStatus.UNPUBLISHED;
        this.remark = remark;
    }

    ReportProject(Long id, String name, String picUrl, String content,
                  GoViewProjectStatus status, String remark) {
        this.id = id != null ? GoViewProjectId.of(id) : null;
        this.name = Objects.requireNonNull(name, "项目名称不能为空");
        this.picUrl = picUrl;
        this.content = content;
        this.status = status != null ? status : GoViewProjectStatus.UNPUBLISHED;
        this.remark = remark;
    }

    public static ReportProject of(Long id, String name) {
        return new ReportProject(id, name, null, null, null, null);
    }

    public void publish() {
        this.status = this.status.publish();
    }

    public void unpublish() {
        this.status = this.status.unpublish();
    }

    public void updateProfile(String name, String picUrl, String content, String remark) {
        this.name = Objects.requireNonNull(name, "项目名称不能为空");
        this.picUrl = picUrl;
        this.content = content;
        this.remark = remark;
    }

    public void markDeleted() {
        events.add(new GoViewProjectDeletedEvent(this.id != null ? this.id.value() : null, this.name));
    }

    // Query methods

    public GoViewProjectId id() { return id; }
    public String name() { return name; }
    public String picUrl() { return picUrl; }
    public String content() { return content; }
    public GoViewProjectStatus status() { return status; }
    public String remark() { return remark; }

    public boolean isPublished() { return status.isPublished(); }
    public boolean isUnpublished() { return status.isUnpublished(); }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> result = new ArrayList<>(events);
        events.clear();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReportProject that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "ReportProject{id=" + id + ", name=" + name + '}';
    }
}
