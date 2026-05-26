package com.develop.mvp.pk.module.system.application.permission.port.inbound;

// DDD 角色：入站端口 — 定义 Role 聚合的用例边界，供 Controller/API/跨服务调用
// Hexagonal-Lite：入站端口接口，应用服务实现此接口

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.controller.admin.permission.vo.role.RolePageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.permission.vo.role.RoleSaveReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.permission.RoleDO;
import com.develop.mvp.pk.module.system.domain.permission.Role;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Role 聚合的入站用例端口。
 */
public interface RoleUseCase {

    /**
     * 创建 create Role 对应的数据。
     *
     * @param name name 参数
     * @param code code 参数
     * @param sort sort 参数
     * @param status status 参数
     * @param remark remark 参数
     * @param type type 参数
     * @return 处理结果
     */
    Role createRole(String name, String code, Integer sort, Integer status, String remark, Integer type);

    /**
     * 更新 update Role 对应的数据。
     *
     * @param id id 参数
     * @param name name 参数
     * @param code code 参数
     * @param sort sort 参数
     * @param status status 参数
     * @param remark remark 参数
     * @return 处理结果
     */
    Role updateRole(Long id, String name, String code, Integer sort, Integer status, String remark);

    /**
     * 更新 update Role Data Scope 对应的数据。
     *
     * @param id id 参数
     * @param dataScope dataScope 参数
     * @param dataScopeDeptIds dataScopeDeptIds 参数
     * @return 处理结果
     */
    Role updateRoleDataScope(Long id, Integer dataScope, Set<Long> dataScopeDeptIds);

    /**
     * 删除 delete Role 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    Role deleteRole(Long id);

    /**
     * 查询 get Role 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    Role getRole(Long id);

    /**
     * 校验 validate Role Duplicate 对应的业务规则。
     *
     * @param name name 参数
     * @param code code 参数
     * @param id id 参数
     */
    void validateRoleDuplicate(String name, String code, Long id);

    /**
     * 校验 validate Role For Update 对应的业务规则。
     *
     * @param id id 参数
     * @return 处理结果
     */
    Role validateRoleForUpdate(Long id);

    // ---- Additional methods from RoleApplicationService ----

    /**
     * 创建 create Role 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @param type type 参数
     * @return 处理结果
     */
    Long createRole(RoleSaveReqVO createReqVO, Integer type);

    /**
     * 更新 update Role 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     */
    void updateRole(RoleSaveReqVO updateReqVO);

    /**
     * 删除 delete Role List 对应的数据。
     *
     * @param ids ids 参数
     */
    void deleteRoleList(List<Long> ids);

    /**
     * 查询 get Role DO 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    RoleDO getRoleDO(Long id);

    /**
     * 查询 get Role From Cache 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    RoleDO getRoleFromCache(Long id);

    /**
     * 查询 get Role List By Status 对应的数据。
     *
     * @param statuses statuses 参数
     * @return 处理结果
     */
    List<RoleDO> getRoleListByStatus(Collection<Integer> statuses);

    /**
     * 查询 get Role List 对应的数据。
     *
     * @return 处理结果
     */
    List<RoleDO> getRoleList();

    /**
     * 查询 get Role List 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    List<RoleDO> getRoleList(Collection<Long> ids);

    /**
     * 查询 get Role List From Cache 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    List<RoleDO> getRoleListFromCache(Collection<Long> ids);

    /**
     * 查询 get Role Page 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    PageResult<RoleDO> getRolePage(RolePageReqVO reqVO);

    /**
     * 判断 has Any Super Admin 对应的条件是否成立。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    boolean hasAnySuperAdmin(Collection<Long> ids);

    /**
     * 校验 validate Role List 对应的业务规则。
     *
     * @param ids ids 参数
     */
    void validateRoleList(Collection<Long> ids);
}
