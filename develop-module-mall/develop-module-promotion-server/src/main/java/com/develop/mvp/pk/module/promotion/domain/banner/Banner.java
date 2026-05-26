package com.develop.mvp.pk.module.promotion.domain.banner;

// Skill: AggregateRoot_Banner_Validation_Skill — 聚合根 Banner
// DDD 角色：Banner 聚合根，封装 Banner 生命周期管理
// 验收标准 AC01/AC02：无 MyBatis/Spring 注解，不注入 Mapper

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.module.promotion.domain.banner.valueobject.BannerId;
import com.develop.mvp.pk.module.promotion.domain.event.DomainEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Banner {

    // ── 聚合根标识 ──
    private final BannerId id;

    // ── 核心属性 ──
    private String title;
    private String url;
    private String picUrl;
    private Integer sort;
    private Integer status;
    private Integer position;
    private String memo;

    // ── 领域事件收集 ──
    private final List<DomainEvent> events = new ArrayList<>();

    Banner(BannerId id, String title, String url, String picUrl,
           Integer sort, Integer status, Integer position, String memo) {
        this.id = id;
        this.title = Objects.requireNonNull(title, "Banner标题不能为空");
        this.url = url;
        this.picUrl = Objects.requireNonNull(picUrl, "Banner图片不能为空");
        this.sort = sort != null ? sort : 0;
        this.status = status != null ? status : CommonStatusEnum.ENABLE.getStatus();
        this.position = position;
        this.memo = memo;
    }

    // ── 业务方法 ──

    public void updateProfile(String title, String url, String picUrl, Integer sort, Integer position, String memo) {
        this.title = Objects.requireNonNull(title, "Banner标题不能为空");
        this.url = url;
        this.picUrl = Objects.requireNonNull(picUrl, "Banner图片不能为空");
        this.sort = sort != null ? sort : this.sort;
        this.position = position;
        this.memo = memo;
    }

    public void enable() { this.status = CommonStatusEnum.ENABLE.getStatus(); }
    public void disable() { this.status = CommonStatusEnum.DISABLE.getStatus(); }

    // ── 查询方法 ──

    public BannerId id() { return id; }
    public String title() { return title; }
    public String url() { return url; }
    public String picUrl() { return picUrl; }
    public Integer sort() { return sort; }
    public Integer status() { return status; }
    public Integer position() { return position; }
    public String memo() { return memo; }

    public boolean isEnabled() { return CommonStatusEnum.ENABLE.getStatus().equals(status); }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> result = new ArrayList<>(events);
        events.clear();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Banner that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() { return "Banner{id=" + id + ", title='" + title + "'}"; }
}
