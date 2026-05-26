package com.develop.mvp.pk.module.system.controller.admin.oauth2;

import com.develop.mvp.pk.module.system.application.dept.port.inbound.DeptUseCase;
import cn.hutool.core.collection.CollUtil;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.system.controller.admin.oauth2.vo.user.OAuth2UserInfoRespVO;
import com.develop.mvp.pk.module.system.controller.admin.oauth2.vo.user.OAuth2UserUpdateReqVO;
import com.develop.mvp.pk.module.system.controller.admin.user.vo.profile.UserProfileUpdateReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.dept.DeptDO;
import com.develop.mvp.pk.module.system.dal.dataobject.dept.PostDO;
import com.develop.mvp.pk.module.system.dal.dataobject.user.AdminUserDO;
import com.develop.mvp.pk.module.system.application.user.port.inbound.AdminUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;
import static com.develop.mvp.pk.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 提供给外部应用调用为主
 *
 * 1. 在 getUserInfo 方法上，添加 @PreAuthorize("@ss.hasScope('user.read')") 注解，声明需要满足 scope = user.read
 * 2. 在 updateUserInfo 方法上，添加 @PreAuthorize("@ss.hasScope('user.write')") 注解，声明需要满足 scope = user.write
 *
 * @author David
 */
@Tag(name = "管理后台 - OAuth2.0 用户")
@RestController
@RequestMapping("/system/oauth2/user")
@Validated
@Slf4j
public class OAuth2UserController {

    @Resource
    private AdminUserUseCase userService;
    @Resource
    private DeptUseCase deptUseCase;
    @Resource
    private DeptUseCase postUseCase;

    /**
     * 查询 get User Info 对应的数据。
     *
     * @return 处理结果
     */
    @GetMapping("/get")
    @Operation(summary = "获得用户基本信息")
    @PreAuthorize("@ss.hasScope('user.read')") //
    public CommonResult<OAuth2UserInfoRespVO> getUserInfo() {
        // 获得用户基本信息
        AdminUserDO user = userService.getUser(getLoginUserId());
        OAuth2UserInfoRespVO resp = BeanUtils.toBean(user, OAuth2UserInfoRespVO.class);
        // 获得部门信息
        if (user.getDeptId() != null) {
            DeptDO dept = deptUseCase.getDept(user.getDeptId());
            resp.setDept(BeanUtils.toBean(dept, OAuth2UserInfoRespVO.Dept.class));
        }
        // 获得岗位信息
        if (CollUtil.isNotEmpty(user.getPostIds())) {
            List<PostDO> posts = postUseCase.getPostList(user.getPostIds());
            resp.setPosts(BeanUtils.toBean(posts, OAuth2UserInfoRespVO.Post.class));
        }
        return success(resp);
    }

    /**
     * 更新 update User Info 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    @PutMapping("/update")
    @Operation(summary = "更新用户基本信息")
    @PreAuthorize("@ss.hasScope('user.write')")
    public CommonResult<Boolean> updateUserInfo(@Valid @RequestBody OAuth2UserUpdateReqVO reqVO) {
        // 这里将 UserProfileUpdateReqVO =》UserProfileUpdateReqVO 对象，实现接口的复用。
        // 主要是，AdminUserApplicationService 没有自己的 BO 对象，所以复用只能这么做
        userService.updateUserProfile(getLoginUserId(), BeanUtils.toBean(reqVO, UserProfileUpdateReqVO.class));
        return success(true);
    }

}
