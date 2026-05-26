package com.develop.mvp.pk.module.system.application.oauth2.port.inbound;

// DDD 角色：入站端口 — 定义 OAuth2 聚合的用例边界，供 Controller/API/跨服务调用
// Hexagonal-Lite：入站端口接口，应用服务实现此接口

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.controller.admin.oauth2.vo.client.OAuth2ClientPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.oauth2.vo.client.OAuth2ClientSaveReqVO;
import com.develop.mvp.pk.module.system.controller.admin.oauth2.vo.token.OAuth2AccessTokenPageReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import com.develop.mvp.pk.module.system.dal.dataobject.oauth2.OAuth2ApproveDO;
import com.develop.mvp.pk.module.system.dal.dataobject.oauth2.OAuth2ClientDO;
import com.develop.mvp.pk.module.system.dal.dataobject.oauth2.OAuth2CodeDO;
import com.develop.mvp.pk.module.system.domain.oauth2.OAuth2AccessToken;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
/**
 * OAuth2 聚合的入站用例端口。
 */
public interface OAuth2UseCase {

    // ========== OAuth2 客户端 CRUD ==========

    /**
     * 创建 create OAuth2 Client 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @return 处理结果
     */
    Long createOAuth2Client(OAuth2ClientSaveReqVO createReqVO);

    /**
     * 更新 update OAuth2 Client 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     */
    void updateOAuth2Client(OAuth2ClientSaveReqVO updateReqVO);

    /**
     * 删除 delete OAuth2 Client 对应的数据。
     *
     * @param id id 参数
     */
    void deleteOAuth2Client(Long id);

    /**
     * 删除 delete OAuth2 Client List 对应的数据。
     *
     * @param ids ids 参数
     */
    void deleteOAuth2ClientList(List<Long> ids);

    /**
     * 查询 get OAuth2 Client 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    OAuth2ClientDO getOAuth2Client(Long id);

    /**
     * 查询 get OAuth2 Client From Cache 对应的数据。
     *
     * @param clientId clientId 参数
     * @return 处理结果
     */
    OAuth2ClientDO getOAuth2ClientFromCache(String clientId);

    /**
     * 查询 get OAuth2 Client Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    PageResult<OAuth2ClientDO> getOAuth2ClientPage(OAuth2ClientPageReqVO pageReqVO);

    /**
     * 执行 valid OAuth Client From Cache 对应的业务操作。
     *
     * @param clientId clientId 参数
     * @return 处理结果
     */
    OAuth2ClientDO validOAuthClientFromCache(String clientId);

    /**
     * 执行 valid OAuth Client From Cache 对应的业务操作。
     *
     * @param clientId clientId 参数
     * @param clientSecret clientSecret 参数
     * @param authorizedGrantType authorizedGrantType 参数
     * @param scopes scopes 参数
     * @param redirectUri redirectUri 参数
     * @return 处理结果
     */
    OAuth2ClientDO validOAuthClientFromCache(String clientId, String clientSecret, String authorizedGrantType,
                                              Collection<String> scopes, String redirectUri);

    // ========== 访问令牌 ==========

    /**
     * 创建 create Access Token 对应的数据。
     *
     * @param userId userId 参数
     * @param userType userType 参数
     * @param clientId clientId 参数
     * @param scopes scopes 参数
     * @return 处理结果
     */
    OAuth2AccessTokenDO createAccessToken(Long userId, Integer userType, String clientId, List<String> scopes);

    /**
     * 处理 refresh Access Token 对应的认证流程。
     *
     * @param refreshToken refreshToken 参数
     * @param clientId clientId 参数
     * @return 处理结果
     */
    OAuth2AccessTokenDO refreshAccessToken(String refreshToken, String clientId);

    /**
     * 查询 get Access Token 对应的数据。
     *
     * @param accessToken accessToken 参数
     * @return 处理结果
     */
    OAuth2AccessTokenDO getAccessToken(String accessToken);

    /**
     * 校验 check Access Token 对应的业务规则。
     *
     * @param accessToken accessToken 参数
     * @return 处理结果
     */
    OAuth2AccessTokenDO checkAccessToken(String accessToken);

    /**
     * 删除 remove Access Token 对应的数据。
     *
     * @param accessToken accessToken 参数
     * @return 处理结果
     */
    OAuth2AccessTokenDO removeAccessToken(String accessToken);

    /**
     * 删除 remove Access Token 对应的数据。
     *
     * @param userId userId 参数
     * @param userType userType 参数
     */
    void removeAccessToken(Long userId, Integer userType);

    /**
     * 查询 get Access Token Page 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    PageResult<OAuth2AccessTokenDO> getAccessTokenPage(OAuth2AccessTokenPageReqVO reqVO);

    /**
     * 执行 clean Refresh Token 对应的业务操作。
     *
     * @param exceedDay exceedDay 参数
     * @param deleteLimit deleteLimit 参数
     * @return 处理结果
     */
    Integer cleanRefreshToken(Integer exceedDay, Integer deleteLimit);

    /**
     * 执行 clean Access Token 对应的业务操作。
     *
     * @param exceedDay exceedDay 参数
     * @param deleteLimit deleteLimit 参数
     * @return 处理结果
     */
    Integer cleanAccessToken(Integer exceedDay, Integer deleteLimit);

    // ========== 授权码 ==========

    /**
     * 创建 create Authorization Code 对应的数据。
     *
     * @param userId userId 参数
     * @param userType userType 参数
     * @param clientId clientId 参数
     * @param scopes scopes 参数
     * @param redirectUri redirectUri 参数
     * @param state state 参数
     * @return 处理结果
     */
    OAuth2CodeDO createAuthorizationCode(Long userId, Integer userType, String clientId,
                                          List<String> scopes, String redirectUri, String state);

    /**
     * 执行 consume Authorization Code 对应的业务操作。
     *
     * @param code code 参数
     * @return 处理结果
     */
    OAuth2CodeDO consumeAuthorizationCode(String code);

    // ========== 审批 ==========

    /**
     * 校验 check For Pre Approval 对应的业务规则。
     *
     * @param userId userId 参数
     * @param userType userType 参数
     * @param clientId clientId 参数
     * @param requestedScopes requestedScopes 参数
     * @return 处理结果
     */
    boolean checkForPreApproval(Long userId, Integer userType, String clientId, Collection<String> requestedScopes);

    /**
     * 更新 update After Approval 对应的数据。
     *
     * @param userId userId 参数
     * @param userType userType 参数
     * @param clientId clientId 参数
     * @param requestedScopes requestedScopes 参数
     * @return 处理结果
     */
    boolean updateAfterApproval(Long userId, Integer userType, String clientId, Map<String, Boolean> requestedScopes);

    /**
     * 查询 get Approve List 对应的数据。
     *
     * @param userId userId 参数
     * @param userType userType 参数
     * @param clientId clientId 参数
     * @return 处理结果
     */
    List<OAuth2ApproveDO> getApproveList(Long userId, Integer userType, String clientId);

    // ========== 授权模式 ==========

    /**
     * 执行 grant Implicit 对应的业务操作。
     *
     * @param userId userId 参数
     * @param userType userType 参数
     * @param clientId clientId 参数
     * @param scopes scopes 参数
     * @return 处理结果
     */
    OAuth2AccessTokenDO grantImplicit(Long userId, Integer userType, String clientId, List<String> scopes);

    /**
     * 执行 grant Authorization Code For Code 对应的业务操作。
     *
     * @param userId userId 参数
     * @param userType userType 参数
     * @param clientId clientId 参数
     * @param scopes scopes 参数
     * @param redirectUri redirectUri 参数
     * @param state state 参数
     * @return 处理结果
     */
    String grantAuthorizationCodeForCode(Long userId, Integer userType, String clientId, List<String> scopes,
                                          String redirectUri, String state);

    /**
     * 执行 grant Authorization Code For Access Token 对应的业务操作。
     *
     * @param clientId clientId 参数
     * @param code code 参数
     * @param redirectUri redirectUri 参数
     * @param state state 参数
     * @return 处理结果
     */
    OAuth2AccessTokenDO grantAuthorizationCodeForAccessToken(String clientId, String code, String redirectUri, String state);

    /**
     * 执行 grant Password 对应的业务操作。
     *
     * @param username username 参数
     * @param password password 参数
     * @param clientId clientId 参数
     * @param scopes scopes 参数
     * @return 处理结果
     */
    OAuth2AccessTokenDO grantPassword(String username, String password, String clientId, List<String> scopes);

    /**
     * 执行 grant Refresh Token 对应的业务操作。
     *
     * @param refreshToken refreshToken 参数
     * @param clientId clientId 参数
     * @return 处理结果
     */
    OAuth2AccessTokenDO grantRefreshToken(String refreshToken, String clientId);

    /**
     * 执行 grant Client Credentials 对应的业务操作。
     *
     * @param clientId clientId 参数
     * @param scopes scopes 参数
     * @return 处理结果
     */
    OAuth2AccessTokenDO grantClientCredentials(String clientId, List<String> scopes);

    /**
     * 执行 revoke Token 对应的业务操作。
     *
     * @param clientId clientId 参数
     * @param accessToken accessToken 参数
     * @return 处理结果
     */
    boolean revokeToken(String clientId, String accessToken);

    // ========== Token 领域对象操作 ==========

    /**
     * 创建 create Token 对应的数据。
     *
     * @param id id 参数
     * @param accessToken accessToken 参数
     * @param refreshToken refreshToken 参数
     * @param userId userId 参数
     * @param userType userType 参数
     * @param clientId clientId 参数
     * @param scopes scopes 参数
     * @param expiresTime expiresTime 参数
     * @return 处理结果
     */
    OAuth2AccessToken createToken(Long id, String accessToken, String refreshToken, Long userId, Integer userType,
                                   String clientId, List<String> scopes, LocalDateTime expiresTime);

    /**
     * 删除 delete Token 对应的数据。
     *
     * @param id id 参数
     */
    void deleteToken(Long id);

    /**
     * 查询 get Token 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    OAuth2AccessToken getToken(Long id);

    /**
     * 查询 find By Access Token 对应的数据。
     *
     * @param accessToken accessToken 参数
     * @return 处理结果
     */
    OAuth2AccessToken findByAccessToken(String accessToken);

    /**
     * 查询 find By Refresh Token 对应的数据。
     *
     * @param refreshToken refreshToken 参数
     * @return 处理结果
     */
    OAuth2AccessToken findByRefreshToken(String refreshToken);

    /**
     * 删除 delete By User Type And User Id 对应的数据。
     *
     * @param userType userType 参数
     * @param userId userId 参数
     */
    void deleteByUserTypeAndUserId(Integer userType, Long userId);
}
