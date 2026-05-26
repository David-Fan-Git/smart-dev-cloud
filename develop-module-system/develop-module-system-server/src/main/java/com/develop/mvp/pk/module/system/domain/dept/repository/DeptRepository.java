package com.develop.mvp.pk.module.system.domain.dept.repository;
import com.develop.mvp.pk.module.system.domain.dept.Dept;
import com.develop.mvp.pk.module.system.domain.dept.valueobject.DeptId;
import java.util.*;
/**
 * Dept Repository 领域仓储接口。
 */
public interface DeptRepository {
    /**
     * 创建 save 对应的数据。
     *
     * @param dept dept 参数
     */
    void save(Dept dept);
    /**
     * 删除 delete 对应的数据。
     *
     * @param id id 参数
     */
    void delete(DeptId id);
    /**
     * 查询 find By Id 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    Dept findById(DeptId id);
    /**
     * 查询 find By Ids 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    List<Dept> findByIds(Collection<DeptId> ids);
    /**
     * 查询 find By Parent Id 对应的数据。
     *
     * @param parentId parentId 参数
     * @return 处理结果
     */
    List<Dept> findByParentId(Long parentId);
    /**
     * 查询 find By Leader User Id 对应的数据。
     *
     * @param userId userId 参数
     * @return 处理结果
     */
    List<Dept> findByLeaderUserId(Long userId);
    /**
     * 查询 find All 对应的数据。
     *
     * @return 处理结果
     */
    List<Dept> findAll();
    /**
     * 查询 find Child Ids From Cache 对应的数据。
     *
     * @param parentId parentId 参数
     * @return 处理结果
     */
    Set<Long> findChildIdsFromCache(Long parentId);
    /**
     * 执行 exists By Name 对应的业务操作。
     *
     * @param name name 参数
     * @param excludeId excludeId 参数
     * @return 处理结果
     */
    boolean existsByName(String name, Long excludeId);
}
