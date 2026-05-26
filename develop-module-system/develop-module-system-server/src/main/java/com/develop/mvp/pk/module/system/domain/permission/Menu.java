package com.develop.mvp.pk.module.system.domain.permission;

// Skill: AggregateRoot_Role_Menu_Skill — 聚合根 Menu
// DDD 角色：菜单聚合根，树形结构，封装菜单属性与层级约束
// 验收标准 AC02：无 MyBatis/Spring 注解

import com.develop.mvp.pk.module.system.domain.permission.event.*;
import com.develop.mvp.pk.module.system.domain.permission.valueobject.*;
import com.develop.mvp.pk.module.system.domain.user.event.DomainEvent;

import java.util.*;

/**
 * Menu 领域模型。
 */
public final class Menu {
    private final MenuId id;
    private final MenuName name;
    private final MenuPermission permission;
    private final MenuType type;
    private final Integer sort;
    private final MenuId parentId;
    private final String path;
    private final String icon;
    private final String component;
    private final String componentName;
    private final Integer status;
    private final Boolean visible;
    private final Boolean keepAlive;
    private final Boolean alwaysShow;
    private final List<DomainEvent> events = new ArrayList<>();

    /**
     * 创建 Menu 实例。
     *
     * @param id id 参数
     * @param name name 参数
     * @param permission permission 参数
     * @param type type 参数
     * @param sort sort 参数
     * @param parentId parentId 参数
     * @param path path 参数
     * @param icon icon 参数
     * @param component component 参数
     * @param componentName componentName 参数
     * @param status status 参数
     * @param visible visible 参数
     * @param keepAlive keepAlive 参数
     * @param alwaysShow alwaysShow 参数
     */
    Menu(MenuId id, MenuName name, MenuPermission permission, MenuType type, Integer sort,
         MenuId parentId, String path, String icon, String component, String componentName,
         Integer status, Boolean visible, Boolean keepAlive, Boolean alwaysShow) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.permission = permission != null ? permission : MenuPermission.empty();
        this.type = Objects.requireNonNull(type);
        this.sort = sort != null ? sort : 0;
        this.parentId = Objects.requireNonNull(parentId);
        this.path = path;
        this.icon = icon;
        this.component = type.isButton() ? "" : component;
        this.componentName = type.isButton() ? "" : componentName;
        this.status = status != null ? status : 0;
        this.visible = visible != null ? visible : true;
        this.keepAlive = keepAlive != null ? keepAlive : false;
        this.alwaysShow = alwaysShow != null ? alwaysShow : false;
    }

    // ── 业务方法 ──

    /** 规则 MR01/MR02：校验父菜单合法性（由应用层调用） */
    public void validateParentAgainst(Menu parent) {
        if (parentId.isRoot()) return;
        if (parent == null) throw new IllegalArgumentException("父菜单不存在");
        if (!parent.type().isDirOrMenu()) throw new IllegalArgumentException("父菜单必须是目录或菜单类型");
        if (parentId.equals(id)) throw new IllegalArgumentException("不能设置自己为父菜单");
    }

    /** 规则 MR05：是否有子菜单（由应用层检查） */

    public void markDeleted() {
        events.add(new MenuDeletedEvent(this.id.value()));
    }

    /** 规则 MR06：按钮类型清空组件属性（构造时已处理） */

    // ── 查询方法 ──

    public MenuId id() { return id; }
    /**
     * 执行 name 对应的业务操作。
     *
     * @return 处理结果
     */
    public MenuName name() { return name; }
    /**
     * 执行 permission 对应的业务操作。
     *
     * @return 处理结果
     */
    public MenuPermission permission() { return permission; }
    /**
     * 执行 type 对应的业务操作。
     *
     * @return 处理结果
     */
    public MenuType type() { return type; }
    /**
     * 执行 sort 对应的业务操作。
     *
     * @return 处理结果
     */
    public Integer sort() { return sort; }
    /**
     * 执行 parent Id 对应的业务操作。
     *
     * @return 处理结果
     */
    public MenuId parentId() { return parentId; }
    /**
     * 执行 path 对应的业务操作。
     *
     * @return 处理结果
     */
    public String path() { return path; }
    /**
     * 执行 icon 对应的业务操作。
     *
     * @return 处理结果
     */
    public String icon() { return icon; }
    /**
     * 执行 component 对应的业务操作。
     *
     * @return 处理结果
     */
    public String component() { return component; }
    /**
     * 执行 component Name 对应的业务操作。
     *
     * @return 处理结果
     */
    public String componentName() { return componentName; }
    /**
     * 执行 status 对应的业务操作。
     *
     * @return 处理结果
     */
    public Integer status() { return status; }
    /**
     * 执行 visible 对应的业务操作。
     *
     * @return 处理结果
     */
    public Boolean visible() { return visible; }
    /**
     * 执行 keep Alive 对应的业务操作。
     *
     * @return 处理结果
     */
    public Boolean keepAlive() { return keepAlive; }
    /**
     * 执行 always Show 对应的业务操作。
     *
     * @return 处理结果
     */
    public Boolean alwaysShow() { return alwaysShow; }

    /**
     * 执行 pull Events 对应的业务操作。
     *
     * @return 处理结果
     */
    public List<DomainEvent> pullEvents() { List<DomainEvent> r = new ArrayList<>(events); events.clear(); return r; }

    @Override public boolean equals(Object o) { return o instanceof Menu m && id.equals(m.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
