package com.develop.mvp.pk.module.system.application.dept.port.inbound;

// DDD 角色：入站端口 — 定义 Dept 聚合的用例边界，供 Controller/API/跨服务调用
// Hexagonal-Lite：入站端口接口，应用服务实现此接口

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.controller.admin.dept.vo.dept.DeptListReqVO;
import com.develop.mvp.pk.module.system.controller.admin.dept.vo.dept.DeptSaveReqVO;
import com.develop.mvp.pk.module.system.controller.admin.dept.vo.post.PostPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.dept.vo.post.PostSaveReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.dept.DeptDO;
import com.develop.mvp.pk.module.system.dal.dataobject.dept.PostDO;
import com.develop.mvp.pk.module.system.domain.dept.Dept;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * Dept 聚合的入站用例端口。
 */
public interface DeptUseCase {

    /**
     * 创建 create Dept 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @return 处理结果
     */
    Long createDept(DeptSaveReqVO createReqVO);

    /**
     * 更新 update Dept 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     */
    void updateDept(DeptSaveReqVO updateReqVO);

    /**
     * 删除 delete Dept 对应的数据。
     *
     * @param id id 参数
     */
    void deleteDept(Long id);

    /**
     * 删除 delete Dept List 对应的数据。
     *
     * @param ids ids 参数
     */
    void deleteDeptList(List<Long> ids);

    /**
     * 查询 get Dept 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    DeptDO getDept(Long id);

    /**
     * 查询 get Dept List 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    List<DeptDO> getDeptList(Collection<Long> ids);

    /**
     * 查询 get Dept List 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    List<DeptDO> getDeptList(DeptListReqVO reqVO);

    /**
     * 查询 get Dept Map 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    Map<Long, DeptDO> getDeptMap(Collection<Long> ids);

    /**
     * 查询 get Child Dept List 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    List<DeptDO> getChildDeptList(Long id);

    /**
     * 查询 get Child Dept List 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    List<DeptDO> getChildDeptList(Collection<Long> ids);

    /**
     * 查询 get Dept List By Leader User Id 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    List<DeptDO> getDeptListByLeaderUserId(Long id);

    /**
     * 查询 get Child Dept Id List From Cache 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    Set<Long> getChildDeptIdListFromCache(Long id);

    /**
     * 校验 validate Dept List 对应的业务规则。
     *
     * @param ids ids 参数
     */
    void validateDeptList(Collection<Long> ids);

    /**
     * 创建 create Post 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @return 处理结果
     */
    Long createPost(PostSaveReqVO createReqVO);

    /**
     * 更新 update Post 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     */
    void updatePost(PostSaveReqVO updateReqVO);

    /**
     * 删除 delete Post 对应的数据。
     *
     * @param id id 参数
     */
    void deletePost(Long id);

    /**
     * 删除 delete Post List 对应的数据。
     *
     * @param ids ids 参数
     */
    void deletePostList(List<Long> ids);

    /**
     * 查询 get Post List 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    List<PostDO> getPostList(Collection<Long> ids);

    /**
     * 查询 get Post List 对应的数据。
     *
     * @param ids ids 参数
     * @param statuses statuses 参数
     * @return 处理结果
     */
    List<PostDO> getPostList(Collection<Long> ids, Collection<Integer> statuses);

    /**
     * 查询 get Post Page 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    PageResult<PostDO> getPostPage(PostPageReqVO reqVO);

    /**
     * 查询 get Post 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    PostDO getPost(Long id);

    /**
     * 校验 validate Post List 对应的业务规则。
     *
     * @param ids ids 参数
     */
    void validatePostList(Collection<Long> ids);

    /**
     * 创建 create Dept Domain 对应的数据。
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
    Long createDeptDomain(Long id, String name, Long parentId, Integer sort,
                          Long leaderUserId, String phone, String email);

    /**
     * 更新 update Dept Domain 对应的数据。
     *
     * @param id id 参数
     * @param name name 参数
     * @param parentId parentId 参数
     * @param sort sort 参数
     * @param leaderUserId leaderUserId 参数
     * @param phone phone 参数
     * @param email email 参数
     */
    void updateDeptDomain(Long id, String name, Long parentId, Integer sort,
                          Long leaderUserId, String phone, String email);

    /**
     * 删除 delete Dept Domain 对应的数据。
     *
     * @param id id 参数
     */
    void deleteDeptDomain(Long id);

    /**
     * 查询 get Dept Domain 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    Dept getDeptDomain(Long id);

    /**
     * 查询 get Dept Domain List 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    List<Dept> getDeptDomainList(Collection<Long> ids);

    /**
     * 查询 get All Dept Domains 对应的数据。
     *
     * @return 处理结果
     */
    List<Dept> getAllDeptDomains();

    /**
     * 查询 get Child Dept Domain List 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    List<Dept> getChildDeptDomainList(Long id);

    /**
     * 查询 get Child Dept Domain Ids From Cache 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    Set<Long> getChildDeptDomainIdsFromCache(Long id);
}
