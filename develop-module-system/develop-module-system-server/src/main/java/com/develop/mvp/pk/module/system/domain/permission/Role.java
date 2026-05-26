package com.develop.mvp.pk.module.system.domain.permission;

// Skill: AggregateRoot_Role_Menu_Skill — 聚合根 Role
// DDD 角色：角色聚合根，封装角色身份、状态、数据范围，内部持有菜单关联
// 验收标准 AC01：无 MyBatis/Spring 注解

import com.develop.mvp.pk.module.system.domain.permission.event.*;
import com.develop.mvp.pk.module.system.domain.permission.valueobject.*;
import com.develop.mvp.pk.module.system.domain.user.event.DomainEvent;

import java.util.*;

/**
 * Role 领域模型。
 */
public final class Role {
    private final RoleId id;
    private RoleName name;
    private RoleCode code;
    private Integer sort;
    private RoleStatus status;
    private final RoleType type;
    private String remark;
    private final Long tenantId;
    private DataScope dataScope;
    private final Set<Long> menuIds;
    private final List<DomainEvent> events = new ArrayList<>();

    /**
     * 创建 Role 实例。
     *
     * @param id id 参数
     * @param name name 参数
     * @param code code 参数
     * @param sort sort 参数
     * @param status status 参数
     * @param type type 参数
     * @param remark remark 参数
     * @param tenantId tenantId 参数
     * @param dataScope dataScope 参数
     * @param menuIds menuIds 参数
     */
    Role(RoleId id, RoleName name, RoleCode code, Integer sort, RoleStatus status,
         RoleType type, String remark, Long tenantId, DataScope dataScope, Set<Long> menuIds) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.code = Objects.requireNonNull(code);
        this.sort = sort != null ? sort : 0;
        this.status = status != null ? status : RoleStatus.ENABLED;
        this.type = type != null ? type : RoleType.CUSTOM;
        this.remark = remark;
        this.tenantId = tenantId;
        this.dataScope = dataScope != null ? dataScope : DataScope.all();
        this.menuIds = menuIds != null ? new HashSet<>(menuIds) : new HashSet<>();
    }

    // ── 业务方法 ──

    /**
     * 更新 change Base Info 对应的数据。
     *
     * @param name name 参数
     * @param code code 参数
     * @param sort sort 参数
     * @param status status 参数
     * @param remark remark 参数
     */
    public void changeBaseInfo(String name, String code, Integer sort, Integer status, String remark) {
        this.name = RoleName.of(name);
        this.code = RoleCode.of(code);
        this.sort = sort != null ? sort : 0;
        this.status = status != null ? RoleStatus.of(status) : RoleStatus.ENABLED;
        this.remark = remark;
    }

    /** 规则 RR05：系统角色不可删除/修改 */
    public boolean isSystem() { return type.isSystem(); }

    /**
     * 更新 change Data Scope 对应的数据。
     *
     * @param newDataScope newDataScope 参数
     */
    public void changeDataScope(DataScope newDataScope) {
        this.dataScope = Objects.requireNonNull(newDataScope);
    }

    /**
     * 执行 sync Menus 对应的业务操作。
     *
     * @param newMenuIds newMenuIds 参数
     */
    public void syncMenus(Set<Long> newMenuIds) {
        menuIds.clear();
        if (newMenuIds != null) menuIds.addAll(newMenuIds);
    }

    /**
     * 执行 mark Deleted 对应的业务操作。
     */
    public void markDeleted() {
        if (isSystem()) throw new IllegalStateException("系统角色不能删除");
        events.add(new RoleDeletedEvent(this.id.value(), this.code.value()));
    }

    // ── 查询方法 ──

    /**
     * 执行 id 对应的业务操作。
     *
     * @return 处理结果
     */
    public RoleId id() { return id; }
    /**
     * 执行 name 对应的业务操作。
     *
     * @return 处理结果
     */
    public RoleName name() { return name; }
    /**
     * 执行 code 对应的业务操作。
     *
     * @return 处理结果
     */
    public RoleCode code() { return code; }
    /**
     * 执行 sort 对应的业务操作。
     *
     * @return 处理结果
     */
    public Integer sort() { return sort; }
    /**
     * 执行 status 对应的业务操作。
     *
     * @return 处理结果
     */
    public RoleStatus status() { return status; }
    /**
     * 执行 type 对应的业务操作。
     *
     * @return 处理结果
     */
    public RoleType type() { return type; }
    /**
     * 执行 remark 对应的业务操作。
     *
     * @return 处理结果
     */
    public String remark() { return remark; }
    /**
     * 执行 tenant Id 对应的业务操作。
     *
     * @return 处理结果
     */
    public Long tenantId() { return tenantId; }
    /**
     * 执行 data Scope 对应的业务操作。
     *
     * @return 处理结果
     */
    public DataScope dataScope() { return dataScope; }
    /**
     * 执行 menu Ids 对应的业务操作。
     *
     * @return 处理结果
     */
    public Set<Long> menuIds() { return Collections.unmodifiableSet(menuIds); }
    /**
     * 判断 is Enabled 对应的条件是否成立。
     *
     * @return 处理结果
     */
    public boolean isEnabled() { return status.isEnabled(); }
    /**
     * 判断 has Id 对应的条件是否成立。
     *
     * @param otherId otherId 参数
     * @return 处理结果
     */
    public boolean hasId(Long otherId) { return id != null && id.value().equals(otherId); }

    /**
     * 执行 pull Events 对应的业务操作。
     *
     * @return 处理结果
     */
    public List<DomainEvent> pullEvents() { List<DomainEvent> r = new ArrayList<>(events); events.clear(); return r; }

    @Override public boolean equals(Object o) { return o instanceof Role r && Objects.equals(id, r.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
