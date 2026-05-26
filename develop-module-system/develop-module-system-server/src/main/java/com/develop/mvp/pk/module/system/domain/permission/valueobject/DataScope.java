package com.develop.mvp.pk.module.system.domain.permission.valueobject;

// Skill: AggregateRoot_Role_Menu_Skill — 值对象 DataScope
// 封装角色的数据权限范围

import com.develop.mvp.pk.module.system.enums.permission.DataScopeEnum;

import java.util.*;

/**
 * Data Scope 值对象。
 */
public final class DataScope {
    public static final DataScope ALL = new DataScope(DataScopeEnum.ALL.getScope(), Collections.emptySet());
    private final Integer scope;
    private final Set<Long> deptIds;
    /**
     * 创建 DataScope 实例。
     *
     * @param scope scope 参数
     * @param deptIds deptIds 参数
     */
    private DataScope(Integer scope, Set<Long> deptIds) {
        this.scope = Objects.requireNonNull(scope);
        this.deptIds = deptIds != null ? Collections.unmodifiableSet(new HashSet<>(deptIds)) : Collections.emptySet();
    }
    /**
     * 执行 all 对应的业务操作。
     *
     * @return 处理结果
     */
    public static DataScope all() { return ALL; }
    /**
     * 执行 of 对应的业务操作。
     *
     * @param scope scope 参数
     * @param deptIds deptIds 参数
     * @return 处理结果
     */
    public static DataScope of(Integer scope, Set<Long> deptIds) { return new DataScope(scope, deptIds); }
    /**
     * 执行 scope 对应的业务操作。
     *
     * @return 处理结果
     */
    public Integer scope() { return scope; }
    /**
     * 执行 dept Ids 对应的业务操作。
     *
     * @return 处理结果
     */
    public Set<Long> deptIds() { return deptIds; }
    /**
     * 判断 is All 对应的条件是否成立。
     *
     * @return 处理结果
     */
    public boolean isAll() { return scope.equals(DataScopeEnum.ALL.getScope()); }
    /**
     * 判断 is Dept Custom 对应的条件是否成立。
     *
     * @return 处理结果
     */
    public boolean isDeptCustom() { return scope.equals(DataScopeEnum.DEPT_CUSTOM.getScope()); }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataScope d)) return false;
        return scope.equals(d.scope) && deptIds.equals(d.deptIds);
    }
    @Override public int hashCode() { return Objects.hash(scope, deptIds); }
}
