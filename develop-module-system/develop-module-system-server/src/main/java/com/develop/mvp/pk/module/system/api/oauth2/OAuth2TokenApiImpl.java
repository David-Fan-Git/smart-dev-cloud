package com.develop.mvp.pk.module.system.api.oauth2;

import com.develop.mvp.pk.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.framework.tenant.core.aop.TenantIgnore;
import com.develop.mvp.pk.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenCheckRespDTO;
import com.develop.mvp.pk.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenCreateReqDTO;
import com.develop.mvp.pk.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenRespDTO;
import com.develop.mvp.pk.module.system.application.oauth2.port.inbound.OAuth2UseCase;
import com.develop.mvp.pk.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

/**
 * OAuth2 Token Api Impl 模块 API 实现。
 */
@RestController // 提供 RESTful API 接口，给 Feign 调用
@Validated
public class OAuth2TokenApiImpl implements OAuth2TokenCommonApi {

    @Resource
    private OAuth2UseCase oauth2TokenService;

    /**
     * 创建 create Access Token 对应的数据。
     *
     * @param reqDTO reqDTO 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<OAuth2AccessTokenRespDTO> createAccessToken(OAuth2AccessTokenCreateReqDTO reqDTO) {
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.createAccessToken(
                reqDTO.getUserId(), reqDTO.getUserType(), reqDTO.getClientId(), reqDTO.getScopes());
        return success(BeanUtils.toBean(accessTokenDO, OAuth2AccessTokenRespDTO.class));
    }

    /**
     * 校验 check Access Token 对应的业务规则。
     *
     * @param accessToken accessToken 参数
     * @return 处理结果
     */
    @Override
    @TenantIgnore // 访问令牌校验时，无需传递租户编号；主要解决上传文件的场景，前端不会传递 tenant-id
    public CommonResult<OAuth2AccessTokenCheckRespDTO> checkAccessToken(String accessToken) {
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.checkAccessToken(accessToken);
        return success(BeanUtils.toBean(accessTokenDO, OAuth2AccessTokenCheckRespDTO.class));
    }

    /**
     * 删除 remove Access Token 对应的数据。
     *
     * @param accessToken accessToken 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<OAuth2AccessTokenRespDTO> removeAccessToken(String accessToken) {
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.removeAccessToken(accessToken);
        return success(BeanUtils.toBean(accessTokenDO, OAuth2AccessTokenRespDTO.class));
    }

    /**
     * 执行 refresh Access Token 对应的业务操作。
     *
     * @param refreshToken refreshToken 参数
     * @param clientId clientId 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<OAuth2AccessTokenRespDTO> refreshAccessToken(String refreshToken, String clientId) {
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.refreshAccessToken(refreshToken, clientId);
        return success(BeanUtils.toBean(accessTokenDO, OAuth2AccessTokenRespDTO.class));
    }

}
