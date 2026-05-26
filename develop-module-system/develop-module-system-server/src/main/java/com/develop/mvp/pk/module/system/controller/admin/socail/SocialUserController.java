package com.develop.mvp.pk.module.system.controller.admin.socail;

import com.develop.mvp.pk.framework.common.enums.UserTypeEnum;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.system.api.social.dto.SocialUserBindReqDTO;
import com.develop.mvp.pk.module.system.controller.admin.socail.vo.user.SocialUserBindReqVO;
import com.develop.mvp.pk.module.system.controller.admin.socail.vo.user.SocialUserPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.socail.vo.user.SocialUserRespVO;
import com.develop.mvp.pk.module.system.controller.admin.socail.vo.user.SocialUserUnbindReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.social.SocialUserDO;
import com.develop.mvp.pk.module.system.application.social.port.inbound.SocialUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertList;
import static com.develop.mvp.pk.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * Social User Controller 控制器。
 */
@Tag(name = "管理后台 - 社交用户")
@RestController
@RequestMapping("/system/social-user")
@Validated
public class SocialUserController {

    @Resource
    private SocialUseCase socialUseCase;

    /**
     * 执行 social Bind 对应的业务操作。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    @PostMapping("/bind")
    @Operation(summary = "社交绑定，使用 code 授权码")
    public CommonResult<Boolean> socialBind(@RequestBody @Valid SocialUserBindReqVO reqVO) {
        socialUseCase.bindSocialUser(new SocialUserBindReqDTO().setSocialType(reqVO.getType())
                        .setCode(reqVO.getCode()).setState(reqVO.getState())
                        .setUserId(getLoginUserId()).setUserType(UserTypeEnum.ADMIN.getValue()));
        return CommonResult.success(true);
    }

    /**
     * 执行 social Unbind 对应的业务操作。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    @DeleteMapping("/unbind")
    @Operation(summary = "取消社交绑定")
    public CommonResult<Boolean> socialUnbind(@RequestBody SocialUserUnbindReqVO reqVO) {
        socialUseCase.unbindSocialUser(getLoginUserId(), UserTypeEnum.ADMIN.getValue(), reqVO.getType(), reqVO.getOpenid());
        return CommonResult.success(true);
    }

    /**
     * 查询 get Bind Social User List 对应的数据。
     *
     * @return 处理结果
     */
    @GetMapping("/get-bind-list")
    @Operation(summary = "获得绑定社交用户列表")
    public CommonResult<List<SocialUserRespVO>> getBindSocialUserList() {
        List<SocialUserDO> list = socialUseCase.getSocialUserList(getLoginUserId(), UserTypeEnum.ADMIN.getValue());
        return success(convertList(list, socialUser -> new SocialUserRespVO() // 返回精简信息
                .setId(socialUser.getId()).setType(socialUser.getType()).setOpenid(socialUser.getOpenid())
                .setNickname(socialUser.getNickname()).setAvatar(socialUser.getNickname())));
    }

    // ==================== 社交用户 CRUD ====================

    /**
     * 查询 get Social User 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @GetMapping("/get")
    @Operation(summary = "获得社交用户")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:social-user:query')")
    public CommonResult<SocialUserRespVO> getSocialUser(@RequestParam("id") Long id) {
        SocialUserDO socialUser = socialUseCase.getSocialUser(id);
        return success(BeanUtils.toBean(socialUser, SocialUserRespVO.class));
    }

    /**
     * 查询 get Social User Page 对应的数据。
     *
     * @param pageVO pageVO 参数
     * @return 处理结果
     */
    @GetMapping("/page")
    @Operation(summary = "获得社交用户分页")
    @PreAuthorize("@ss.hasPermission('system:social-user:query')")
    public CommonResult<PageResult<SocialUserRespVO>> getSocialUserPage(@Valid SocialUserPageReqVO pageVO) {
        PageResult<SocialUserDO> pageResult = socialUseCase.getSocialUserPage(pageVO);
        return success(BeanUtils.toBean(pageResult, SocialUserRespVO.class));
    }

}
