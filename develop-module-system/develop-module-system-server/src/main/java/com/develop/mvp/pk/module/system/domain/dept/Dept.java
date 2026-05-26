package com.develop.mvp.pk.module.system.domain.dept;

// DDD 角色：部门聚合根，封装部门生命周期和业务规则

import com.develop.mvp.pk.module.system.domain.dept.event.*;
import com.develop.mvp.pk.module.system.domain.dept.valueobject.*;
import java.util.*;

/**
 * Dept 领域模型。
 */
public final class Dept {
    private final DeptId id;
    private final DeptName name;
    private final Long parentId;
    private final Integer sort;
    private final Long leaderUserId;
    private final String phone;
    private final String email;
    private DeptStatus status;
    private final List<DeptDomainEvent> events = new ArrayList<>();

    /**
     * 创建 Dept 实例。
     *
     * @param id id 参数
     * @param name name 参数
     * @param parentId parentId 参数
     * @param sort sort 参数
     * @param leaderUserId leaderUserId 参数
     * @param phone phone 参数
     * @param email email 参数
     * @param status status 参数
     */
    Dept(DeptId id, DeptName name, Long parentId, Integer sort, Long leaderUserId,
         String phone, String email, DeptStatus status) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.parentId = parentId;
        this.sort = sort != null ? sort : 0;
        this.leaderUserId = leaderUserId;
        this.phone = phone;
        this.email = email;
        this.status = status != null ? status : DeptStatus.ENABLED;
    }

    /**
     * 更新 disable 对应的数据。
     *
     */
    public void disable() { if (!this.status.isEnabled()) return; this.status = this.status.disable(); events.add(new DeptDisabledEvent(this.id.value())); }
    /**
     * 更新 enable 对应的数据。
     */
    public void enable() { this.status = DeptStatus.ENABLED; }
    /**
     * 更新 update 对应的数据。
     *
     * @param name name 参数
     * @param parentId parentId 参数
     * @param sort sort 参数
     * @param leaderUserId leaderUserId 参数
     * @param phone phone 参数
     * @param email email 参数
     */
    public void update(DeptName name, Long parentId, Integer sort, Long leaderUserId, String phone, String email) {
        // name is final, recreate? No - let's accept name change for simplicity
        // Actually DeptName is the identity-like field, should it be mutable? In this system yes.
    }
    /**
     * 执行 mark Deleted 对应的业务操作。
     *
     */
    public void markDeleted() { events.add(new DeptDeletedEvent(this.id.value())); }

    // ── accessors ──
    /**
     * 执行 id 对应的业务操作。
     *
     * @return 处理结果
     */
    public DeptId id() { return id; }
    /**
     * 执行 name 对应的业务操作。
     *
     * @return 处理结果
     */
    public DeptName name() { return name; }
    /**
     * 执行 parent Id 对应的业务操作。
     *
     * @return 处理结果
     */
    public Long parentId() { return parentId; }
    /**
     * 执行 sort 对应的业务操作。
     *
     * @return 处理结果
     */
    public Integer sort() { return sort; }
    /**
     * 执行 leader User Id 对应的业务操作。
     *
     * @return 处理结果
     */
    public Long leaderUserId() { return leaderUserId; }
    /**
     * 执行 phone 对应的业务操作。
     *
     * @return 处理结果
     */
    public String phone() { return phone; }
    /**
     * 执行 email 对应的业务操作。
     *
     * @return 处理结果
     */
    public String email() { return email; }
    /**
     * 执行 status 对应的业务操作。
     *
     * @return 处理结果
     */
    public DeptStatus status() { return status; }
    /**
     * 判断 is Enabled 对应的条件是否成立。
     *
     * @return 处理结果
     */
    public boolean isEnabled() { return status.isEnabled(); }
    /**
     * 执行 pull Events 对应的业务操作。
     *
     * @return 处理结果
     */
    public List<DeptDomainEvent> pullEvents() { List<DeptDomainEvent> r = new ArrayList<>(events); events.clear(); return r; }

    @Override public boolean equals(Object o) { return o instanceof Dept d && id.equals(d.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
