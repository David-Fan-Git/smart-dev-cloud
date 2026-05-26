package com.develop.mvp.pk.module.system.controller.admin.user;

import com.develop.mvp.pk.module.system.application.dept.port.inbound.DeptUseCase;
// Skill: AggregateRoot_User_Validation_Skill — 接口层 UserProfileController
// DDD 角色：接口层，调用 UserApplicationService

import cn.hutool.core.collection.CollUtil;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.datapermission.core.annotation.DataPermission;
import com.develop.mvp.pk.module.system.application.user.port.inbound.UserUseCase;
import com.develop.mvp.pk.module.system.controller.admin.user.vo.profile.UserProfileRespVO;
import com.develop.mvp.pk.module.system.controller.admin.user.vo.profile.UserProfileUpdatePasswordReqVO;
import com.develop.mvp.pk.module.system.controller.admin.user.vo.profile.UserProfileUpdateReqVO;
import com.develop.mvp.pk.module.system.convert.user.UserConvert;
import com.develop.mvp.pk.module.system.dal.dataobject.dept.DeptDO;
import com.develop.mvp.pk.module.system.dal.dataobject.dept.PostDO;
import com.develop.mvp.pk.module.system.dal.dataobject.permission.RoleDO;
import com.develop.mvp.pk.module.system.domain.user.User;
import com.develop.mvp.pk.module.system.application.permission.port.inbound.PermissionUseCase;
import com.develop.mvp.pk.module.system.application.permission.port.inbound.RoleUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;
import static com.develop.mvp.pk.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * User Profile Controller 控制器。
 */
@Tag(name = "管理后台 - 用户个人中心")
@RestController
@RequestMapping("/system/user/profile")
@Validated
@Slf4j
public class UserProfileController {

    @Resource
    private UserUseCase userApplicationService;
    @Resource
    private DeptUseCase deptUseCase;
    @Resource
    private DeptUseCase postUseCase;
    @Resource
    private PermissionUseCase permissionService;
    @Resource
    private RoleUseCase roleService;

    /**
     * 查询 get User Profile 对应的数据。
     *
     * @return 处理结果
     */
    @GetMapping("/get")
    @Operation(summary = "获得登录用户信息")
    @DataPermission(enable = false)
    public CommonResult<UserProfileRespVO> getUserProfile() {
        User user = userApplicationService.getUser(getLoginUserId());
        List<RoleDO> userRoles = roleService.getRoleListFromCache(
                permissionService.getUserRoleIdListByUserId(user.id().value()));
        DeptDO dept = user.deptId() != null ? deptUseCase.getDept(user.deptId()) : null;
        List<PostDO> posts = CollUtil.isNotEmpty(user.postIds())
                ? postUseCase.getPostList(user.postIds()) : null;
        return success(UserConvert.INSTANCE.convertUser(user, userRoles, dept, posts));
    }

    /**
     * 更新 update User Profile 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    @PutMapping("/update")
    @Operation(summary = "修改用户个人信息")
    public CommonResult<Boolean> updateUserProfile(@Valid @RequestBody UserProfileUpdateReqVO reqVO) {
        userApplicationService.updateProfile(
                getLoginUserId(), reqVO.getEmail(), reqVO.getMobile(),
                reqVO.getNickname(), reqVO.getAvatar(), reqVO.getSex(), null);
        return success(true);
    }

    /**
     * 更新 update User Profile Password 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    @PutMapping("/update-password")
    @Operation(summary = "修改用户个人密码")
    public CommonResult<Boolean> updateUserProfilePassword(
            @Valid @RequestBody UserProfileUpdatePasswordReqVO reqVO) {
        userApplicationService.changePassword(
                getLoginUserId(), reqVO.getOldPassword(), reqVO.getNewPassword());
        return success(true);
    }
}
