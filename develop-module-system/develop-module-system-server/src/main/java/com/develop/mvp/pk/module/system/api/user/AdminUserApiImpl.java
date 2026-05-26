package com.develop.mvp.pk.module.system.api.user;

import com.develop.mvp.pk.module.system.application.dept.port.inbound.DeptUseCase;
// Skill: AggregateRoot_User_Validation_Skill — 接口层 AdminUserApiImpl (Feign RPC)
// DDD 角色：Feign RPC 端点，调用 UserApplicationService

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.datapermission.core.annotation.DataPermission;
import com.develop.mvp.pk.framework.datapermission.core.util.DataPermissionUtils;
import com.develop.mvp.pk.module.system.api.user.dto.AdminUserRespDTO;
import com.develop.mvp.pk.module.system.application.user.port.inbound.UserUseCase;
import com.develop.mvp.pk.module.system.dal.dataobject.dept.DeptDO;
import com.develop.mvp.pk.module.system.domain.user.User;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * Admin User Api Impl 模块 API 实现。
 */
@RestController
@Validated
public class AdminUserApiImpl implements AdminUserApi {

    @Resource
    private UserUseCase userApplicationService;
    @Resource
    private DeptUseCase deptUseCase;

    /**
     * 查询 get User 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @Override
    @DataPermission(enable = false)
    public CommonResult<AdminUserRespDTO> getUser(Long id) {
        User user = userApplicationService.getUser(id);
        return success(toDTO(user));
    }

    /**
     * 查询 get User List By Subordinate 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<List<AdminUserRespDTO>> getUserListBySubordinate(Long id) {
        User user = userApplicationService.getUser(id);
        if (user == null) return success(Collections.emptyList());
        ArrayList<Long> deptIds = new ArrayList<>();
        DeptDO dept = deptUseCase.getDept(user.deptId());
        if (dept == null) return success(Collections.emptyList());
        if (ObjUtil.notEqual(dept.getLeaderUserId(), id)) return success(Collections.emptyList());
        deptIds.add(dept.getId());
        List<DeptDO> childDeptList = deptUseCase.getChildDeptList(dept.getId());
        if (CollUtil.isNotEmpty(childDeptList)) {
            deptIds.addAll(convertSet(childDeptList, DeptDO::getId));
        }
        List<User> users = userApplicationService.getUserListByDeptIds(deptIds);
        users.removeIf(item -> ObjUtil.equal(item.id().value(), id));
        return success(users.stream().map(this::toDTO).collect(Collectors.toList()));
    }

    /**
     * 查询 get User List 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<List<AdminUserRespDTO>> getUserList(Collection<Long> ids) {
        return DataPermissionUtils.executeIgnore(() -> {
            List<User> users = userApplicationService.getUserList(ids);
            return success(users.stream().map(this::toDTO).collect(Collectors.toList()));
        });
    }

    /**
     * 查询 get User List By Dept Ids 对应的数据。
     *
     * @param deptIds deptIds 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<List<AdminUserRespDTO>> getUserListByDeptIds(Collection<Long> deptIds) {
        List<User> users = userApplicationService.getUserListByDeptIds(deptIds);
        return success(users.stream().map(this::toDTO).collect(Collectors.toList()));
    }

    /**
     * 查询 get User List By Post Ids 对应的数据。
     *
     * @param postIds postIds 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<List<AdminUserRespDTO>> getUserListByPostIds(Collection<Long> postIds) {
        List<User> users = userApplicationService.getUserListByPostIds(postIds);
        return success(users.stream().map(this::toDTO).collect(Collectors.toList()));
    }

    /**
     * 校验 validate User List 对应的业务规则。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<Boolean> validateUserList(Collection<Long> ids) {
        userApplicationService.validateUserList(ids);
        return success(true);
    }

    /** User 领域对象 → AdminUserRespDTO */
    private AdminUserRespDTO toDTO(User user) {
        if (user == null) return null;
        AdminUserRespDTO dto = new AdminUserRespDTO();
        dto.setId(user.id().value());
        dto.setNickname(user.profile().nickname());
        dto.setStatus(user.status().code());
        dto.setDeptId(user.deptId());
        dto.setPostIds(new HashSet<>(user.postIds()));
        dto.setMobile(user.mobile().isPresent() ? user.mobile().value() : null);
        dto.setAvatar(user.profile().avatar());
        return dto;
    }
}
