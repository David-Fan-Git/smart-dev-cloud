package com.develop.mvp.pk.module.system.api.social;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.module.system.api.social.dto.SocialUserBindReqDTO;
import com.develop.mvp.pk.module.system.api.social.dto.SocialUserRespDTO;
import com.develop.mvp.pk.module.system.api.social.dto.SocialUserUnbindReqDTO;
import com.develop.mvp.pk.module.system.application.social.port.inbound.SocialUseCase;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

/**
 * Social User Api Impl 模块 API 实现。
 */
@RestController // 提供 RESTful API 接口，给 Feign 调用
@Validated
public class SocialUserApiImpl implements SocialUserApi {

    @Resource
    private SocialUseCase socialUseCase;

    /**
     * 执行 bind Social User 对应的业务操作。
     *
     * @param reqDTO reqDTO 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<String> bindSocialUser(SocialUserBindReqDTO reqDTO) {
        return success(socialUseCase.bindSocialUser(reqDTO));
    }

    /**
     * 执行 unbind Social User 对应的业务操作。
     *
     * @param reqDTO reqDTO 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<Boolean> unbindSocialUser(SocialUserUnbindReqDTO reqDTO) {
        socialUseCase.unbindSocialUser(reqDTO.getUserId(), reqDTO.getUserType(),
                reqDTO.getSocialType(), reqDTO.getOpenid());
        return success(true);
    }

    /**
     * 查询 get Social User By User Id 对应的数据。
     *
     * @param userType userType 参数
     * @param userId userId 参数
     * @param socialType socialType 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<SocialUserRespDTO> getSocialUserByUserId(Integer userType, Long userId, Integer socialType) {
        return success(socialUseCase.getSocialUserByUserId(userType, userId, socialType));
    }

    /**
     * 查询 get Social User By Code 对应的数据。
     *
     * @param userType userType 参数
     * @param socialType socialType 参数
     * @param code code 参数
     * @param state state 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<SocialUserRespDTO> getSocialUserByCode(Integer userType, Integer socialType, String code, String state) {
        return success(socialUseCase.getSocialUserByCode(userType, socialType, code, state));
    }

}
