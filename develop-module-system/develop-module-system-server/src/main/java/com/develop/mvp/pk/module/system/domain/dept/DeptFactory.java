package com.develop.mvp.pk.module.system.domain.dept;
import com.develop.mvp.pk.module.system.domain.dept.valueobject.*;
/**
 * Dept Factory 工厂。
 */
public final class DeptFactory {
    /**
     * 创建 DeptFactory 实例。
     */
    private DeptFactory() {}
    /**
     * 创建 create 对应的数据。
     *
     * @param id id 参数
     * @param name name 参数
     * @param parentId parentId 参数
     * @param sort sort 参数
     * @param leaderUserId leaderUserId 参数
     * @param phone phone 参数
     * @param email email 参数
     * @return 处理结果
     */
    public static Dept create(Long id, String name, Long parentId, Integer sort, Long leaderUserId, String phone, String email) {
        return new Dept(DeptId.of(id), DeptName.of(name), parentId, sort, leaderUserId, phone, email, DeptStatus.ENABLED);
    }
    /**
     * 执行 reconstitute 对应的业务操作。
     *
     * @param id id 参数
     * @param name name 参数
     * @param parentId parentId 参数
     * @param sort sort 参数
     * @param leaderUserId leaderUserId 参数
     * @param phone phone 参数
     * @param email email 参数
     * @param status status 参数
     * @return 处理结果
     */
    public static Dept reconstitute(Long id, String name, Long parentId, Integer sort, Long leaderUserId, String phone, String email, Integer status) {
        return new Dept(DeptId.of(id), DeptName.of(name), parentId, sort, leaderUserId, phone, email, DeptStatus.of(status));
    }
}
