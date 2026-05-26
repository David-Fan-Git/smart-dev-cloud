package com.develop.mvp.pk.module.system.application.oauth2.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.enums.UserTypeEnum;
import com.develop.mvp.pk.framework.common.exception.ServiceException;
import com.develop.mvp.pk.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.date.DateUtils;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.framework.common.util.string.StrUtils;
import com.develop.mvp.pk.framework.security.core.LoginUser;
import com.develop.mvp.pk.framework.tenant.core.context.TenantContextHolder;
import com.develop.mvp.pk.framework.tenant.core.util.TenantUtils;
import com.develop.mvp.pk.module.system.application.auth.port.inbound.AuthUseCase;
import com.develop.mvp.pk.module.system.application.oauth2.port.inbound.OAuth2UseCase;
import com.develop.mvp.pk.module.system.application.user.port.inbound.AdminUserUseCase;
import com.develop.mvp.pk.module.system.controller.admin.oauth2.vo.client.OAuth2ClientPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.oauth2.vo.client.OAuth2ClientSaveReqVO;
import com.develop.mvp.pk.module.system.controller.admin.oauth2.vo.token.OAuth2AccessTokenPageReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import com.develop.mvp.pk.module.system.dal.dataobject.oauth2.OAuth2ApproveDO;
import com.develop.mvp.pk.module.system.dal.dataobject.oauth2.OAuth2ClientDO;
import com.develop.mvp.pk.module.system.dal.dataobject.oauth2.OAuth2CodeDO;
import com.develop.mvp.pk.module.system.dal.dataobject.oauth2.OAuth2RefreshTokenDO;
import com.develop.mvp.pk.module.system.dal.dataobject.user.AdminUserDO;
import com.develop.mvp.pk.module.system.dal.mysql.oauth2.OAuth2AccessTokenMapper;
import com.develop.mvp.pk.module.system.dal.mysql.oauth2.OAuth2ApproveMapper;
import com.develop.mvp.pk.module.system.dal.mysql.oauth2.OAuth2ClientMapper;
import com.develop.mvp.pk.module.system.dal.mysql.oauth2.OAuth2CodeMapper;
import com.develop.mvp.pk.module.system.dal.mysql.oauth2.OAuth2RefreshTokenMapper;
import com.develop.mvp.pk.module.system.dal.redis.RedisKeyConstants;
import com.develop.mvp.pk.module.system.dal.redis.oauth2.OAuth2AccessTokenRedisDAO;
import com.develop.mvp.pk.module.system.domain.oauth2.OAuth2AccessToken;
import com.develop.mvp.pk.module.system.domain.oauth2.repository.OAuth2AccessTokenRepository;
import com.develop.mvp.pk.module.system.enums.ErrorCodeConstants;
import com.google.common.annotations.VisibleForTesting;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception0;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertSet;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.*;

/**
 * OAuth2 Application Service 应用服务。
 */
public class OAuth2ApplicationService implements OAuth2UseCase {

	private static final Integer CODE_TIMEOUT = 5 * 60;

	private static final Integer APPROVE_TIMEOUT = 30 * 24 * 60 * 60;

	private OAuth2AccessTokenRepository tokenRepo;

	private final OAuth2ClientMapper oauth2ClientMapper;

	private final OAuth2AccessTokenMapper oauth2AccessTokenMapper;

	private final OAuth2RefreshTokenMapper oauth2RefreshTokenMapper;

	private final OAuth2AccessTokenRedisDAO oauth2AccessTokenRedisDAO;

	private final OAuth2CodeMapper oauth2CodeMapper;

	private final OAuth2ApproveMapper oauth2ApproveMapper;

	private final AdminUserUseCase adminUserService;

	private final AuthUseCase adminAuthService;

	/**
	 * 创建 OAuth2ApplicationService 实例。
	 */
	public OAuth2ApplicationService() {

		this.oauth2ClientMapper = null;
		this.oauth2AccessTokenMapper = null;
		this.oauth2RefreshTokenMapper = null;
		this.oauth2AccessTokenRedisDAO = null;
		this.oauth2CodeMapper = null;
		this.oauth2ApproveMapper = null;
		this.adminUserService = null;
		this.adminAuthService = null;
	}

	/**
	 * 创建 OAuth2ApplicationService 实例。
	 *
	 * @param oauth2ClientMapper        oauth2ClientMapper 参数
	 * @param oauth2AccessTokenMapper   oauth2AccessTokenMapper 参数
	 * @param oauth2RefreshTokenMapper  oauth2RefreshTokenMapper 参数
	 * @param oauth2AccessTokenRedisDAO oauth2AccessTokenRedisDAO 参数
	 * @param oauth2CodeMapper          oauth2CodeMapper 参数
	 * @param oauth2ApproveMapper       oauth2ApproveMapper 参数
	 * @param adminUserService          adminUserService 参数
	 * @param adminAuthService          adminAuthService 参数
	 */
	@Autowired
	public OAuth2ApplicationService(OAuth2ClientMapper oauth2ClientMapper, OAuth2AccessTokenMapper oauth2AccessTokenMapper, OAuth2RefreshTokenMapper oauth2RefreshTokenMapper, OAuth2AccessTokenRedisDAO oauth2AccessTokenRedisDAO, OAuth2CodeMapper oauth2CodeMapper, OAuth2ApproveMapper oauth2ApproveMapper, AdminUserUseCase adminUserService, AuthUseCase adminAuthService) {

		this.oauth2ClientMapper = oauth2ClientMapper;
		this.oauth2AccessTokenMapper = oauth2AccessTokenMapper;
		this.oauth2RefreshTokenMapper = oauth2RefreshTokenMapper;
		this.oauth2AccessTokenRedisDAO = oauth2AccessTokenRedisDAO;
		this.oauth2CodeMapper = oauth2CodeMapper;
		this.oauth2ApproveMapper = oauth2ApproveMapper;
		this.adminUserService = adminUserService;
		this.adminAuthService = adminAuthService;
	}

	/**
	 * 设置 set Token Repo 对应的数据。
	 *
	 * @param tokenRepo tokenRepo 参数
	 */
	public void setTokenRepo(OAuth2AccessTokenRepository tokenRepo) {

		this.tokenRepo = tokenRepo;
	}

	/**
	 * 创建 create OAuth2 Client 对应的数据。
	 *
	 * @param createReqVO createReqVO 参数
	 * @return 处理结果
	 */
	public Long createOAuth2Client(@Valid OAuth2ClientSaveReqVO createReqVO) {

		validateClientIdExists(null, createReqVO.getClientId());
		OAuth2ClientDO client = BeanUtils.toBean(createReqVO, OAuth2ClientDO.class);
		oauth2ClientMapper.insert(client);
		return client.getId();
	}

	/**
	 * 更新 update OAuth2 Client 对应的数据。
	 *
	 * @param updateReqVO updateReqVO 参数
	 */
	@CacheEvict(cacheNames = RedisKeyConstants.OAUTH_CLIENT, allEntries = true)
	public void updateOAuth2Client(@Valid OAuth2ClientSaveReqVO updateReqVO) {

		validateOAuth2ClientExists(updateReqVO.getId());
		validateClientIdExists(updateReqVO.getId(), updateReqVO.getClientId());
		OAuth2ClientDO updateObj = BeanUtils.toBean(updateReqVO, OAuth2ClientDO.class);
		oauth2ClientMapper.updateById(updateObj);
	}

	/**
	 * 删除 delete OAuth2 Client 对应的数据。
	 *
	 * @param id id 参数
	 */
	@CacheEvict(cacheNames = RedisKeyConstants.OAUTH_CLIENT, allEntries = true)
	public void deleteOAuth2Client(Long id) {

		validateOAuth2ClientExists(id);
		oauth2ClientMapper.deleteById(id);
	}

	/**
	 * 删除 delete OAuth2 Client List 对应的数据。
	 *
	 * @param ids ids 参数
	 */
	@CacheEvict(cacheNames = RedisKeyConstants.OAUTH_CLIENT, allEntries = true)
	public void deleteOAuth2ClientList(List<Long> ids) {

		oauth2ClientMapper.deleteByIds(ids);
	}

	/**
	 * 校验 validate OAuth2 Client Exists 对应的业务规则。
	 *
	 * @param id id 参数
	 */
	private void validateOAuth2ClientExists(Long id) {

		if (oauth2ClientMapper.selectById(id) == null) {
			throw exception(OAUTH2_CLIENT_NOT_EXISTS);
		}
	}

	/**
	 * 校验 validate Client Id Exists 对应的业务规则。
	 *
	 * @param id       id 参数
	 * @param clientId clientId 参数
	 */
	@VisibleForTesting
	void validateClientIdExists(Long id, String clientId) {

		OAuth2ClientDO client = oauth2ClientMapper.selectByClientId(clientId);
		if (client == null) {
			return;
		}
		if (id == null || !client.getId().equals(id)) {
			throw exception(OAUTH2_CLIENT_EXISTS);
		}
	}

	/**
	 * 查询 get OAuth2 Client 对应的数据。
	 *
	 * @param id id 参数
	 * @return 处理结果
	 */
	public OAuth2ClientDO getOAuth2Client(Long id) {

		return oauth2ClientMapper.selectById(id);
	}

	/**
	 * 查询 get OAuth2 Client From Cache 对应的数据。
	 *
	 * @param clientId clientId 参数
	 * @return 处理结果
	 */
	@Cacheable(cacheNames = RedisKeyConstants.OAUTH_CLIENT, key = "#clientId", unless = "#result == null")
	public OAuth2ClientDO getOAuth2ClientFromCache(String clientId) {

		return oauth2ClientMapper.selectByClientId(clientId);
	}

	/**
	 * 查询 get OAuth2 Client Page 对应的数据。
	 *
	 * @param pageReqVO pageReqVO 参数
	 * @return 处理结果
	 */
	public PageResult<OAuth2ClientDO> getOAuth2ClientPage(OAuth2ClientPageReqVO pageReqVO) {

		return oauth2ClientMapper.selectPage(pageReqVO);
	}

	/**
	 * 执行 valid OAuth Client From Cache 对应的业务操作。
	 *
	 * @param clientId clientId 参数
	 * @return 处理结果
	 */
	public OAuth2ClientDO validOAuthClientFromCache(String clientId) {

		return validOAuthClientFromCache(clientId, null, null, null, null);
	}

	/**
	 * 执行 valid OAuth Client From Cache 对应的业务操作。
	 *
	 * @param clientId            clientId 参数
	 * @param clientSecret        clientSecret 参数
	 * @param authorizedGrantType authorizedGrantType 参数
	 * @param scopes              scopes 参数
	 * @param redirectUri         redirectUri 参数
	 * @return 处理结果
	 */
	public OAuth2ClientDO validOAuthClientFromCache(String clientId, String clientSecret, String authorizedGrantType, Collection<String> scopes, String redirectUri) {

		OAuth2ClientDO client = getSelf().getOAuth2ClientFromCache(clientId);
		if (client == null) {
			throw exception(OAUTH2_CLIENT_NOT_EXISTS);
		}
		if (CommonStatusEnum.isDisable(client.getStatus())) {
			throw exception(OAUTH2_CLIENT_DISABLE);
		}
		if (StrUtil.isNotEmpty(clientSecret) && ObjectUtil.notEqual(client.getSecret(), clientSecret)) {
			throw exception(OAUTH2_CLIENT_CLIENT_SECRET_ERROR);
		}
		if (StrUtil.isNotEmpty(authorizedGrantType) && !CollUtil.contains(client.getAuthorizedGrantTypes(), authorizedGrantType)) {
			throw exception(OAUTH2_CLIENT_AUTHORIZED_GRANT_TYPE_NOT_EXISTS);
		}
		if (CollUtil.isNotEmpty(scopes) && !CollUtil.containsAll(client.getScopes(), scopes)) {
			throw exception(OAUTH2_CLIENT_SCOPE_OVER);
		}
		if (StrUtil.isNotEmpty(redirectUri) && !StrUtils.startWithAny(redirectUri, client.getRedirectUris())) {
			throw exception(OAUTH2_CLIENT_REDIRECT_URI_NOT_MATCH, redirectUri);
		}
		return client;
	}

	/**
	 * 创建 create Access Token 对应的数据。
	 *
	 * @param userId   userId 参数
	 * @param userType userType 参数
	 * @param clientId clientId 参数
	 * @param scopes   scopes 参数
	 * @return 处理结果
	 */
	@Transactional(rollbackFor = Exception.class)
	public OAuth2AccessTokenDO createAccessToken(Long userId, Integer userType, String clientId, List<String> scopes) {

		OAuth2ClientDO clientDO = validOAuthClientFromCache(clientId);
		OAuth2RefreshTokenDO refreshTokenDO = createOAuth2RefreshToken(userId, userType, clientDO, scopes);
		return createOAuth2AccessToken(refreshTokenDO, clientDO);
	}

	/**
	 * 执行 refresh Access Token 对应的业务操作。
	 *
	 * @param refreshToken refreshToken 参数
	 * @param clientId     clientId 参数
	 * @return 处理结果
	 */
	@Transactional(noRollbackFor = ServiceException.class)
	public OAuth2AccessTokenDO refreshAccessToken(String refreshToken, String clientId) {

		OAuth2RefreshTokenDO refreshTokenDO = oauth2RefreshTokenMapper.selectByRefreshToken(refreshToken);
		if (refreshTokenDO == null) {
			throw exception0(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), "无效的刷新令牌");
		}
		OAuth2ClientDO clientDO = validOAuthClientFromCache(clientId);
		if (ObjectUtil.notEqual(clientId, refreshTokenDO.getClientId())) {
			throw exception0(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), "刷新令牌的客户端编号不正确");
		}
		List<OAuth2AccessTokenDO> accessTokenDOs = oauth2AccessTokenMapper.selectListByRefreshToken(refreshToken);
		if (CollUtil.isNotEmpty(accessTokenDOs)) {
			oauth2AccessTokenMapper.deleteByIds(convertSet(accessTokenDOs, OAuth2AccessTokenDO::getId));
			oauth2AccessTokenRedisDAO.deleteList(convertSet(accessTokenDOs, OAuth2AccessTokenDO::getAccessToken));
		}
		if (DateUtils.isExpired(refreshTokenDO.getExpiresTime())) {
			oauth2RefreshTokenMapper.deleteById(refreshTokenDO.getId());
			throw exception0(GlobalErrorCodeConstants.UNAUTHORIZED.getCode(), "刷新令牌已过期");
		}
		return createOAuth2AccessToken(refreshTokenDO, clientDO);
	}

	/**
	 * 查询 get Access Token 对应的数据。
	 *
	 * @param accessToken accessToken 参数
	 * @return 处理结果
	 */
	public OAuth2AccessTokenDO getAccessToken(String accessToken) {

		OAuth2AccessTokenDO accessTokenDO = oauth2AccessTokenRedisDAO.get(accessToken);
		if (accessTokenDO != null) {
			return accessTokenDO;
		}
		accessTokenDO = oauth2AccessTokenMapper.selectByAccessToken(accessToken);
		if (accessTokenDO == null) {
			OAuth2RefreshTokenDO refreshTokenDO = oauth2RefreshTokenMapper.selectByRefreshToken(accessToken);
			if (refreshTokenDO != null && !DateUtils.isExpired(refreshTokenDO.getExpiresTime())) {
				accessTokenDO = convertToAccessToken(refreshTokenDO);
			}
		}
		if (accessTokenDO != null && !DateUtils.isExpired(accessTokenDO.getExpiresTime())) {
			oauth2AccessTokenRedisDAO.set(accessTokenDO);
		}
		return accessTokenDO;
	}

	/**
	 * 校验 check Access Token 对应的业务规则。
	 *
	 * @param accessToken accessToken 参数
	 * @return 处理结果
	 */
	public OAuth2AccessTokenDO checkAccessToken(String accessToken) {

		OAuth2AccessTokenDO accessTokenDO = getAccessToken(accessToken);
		if (accessTokenDO == null) {
			throw exception0(GlobalErrorCodeConstants.UNAUTHORIZED.getCode(), "访问令牌不存在");
		}
		if (DateUtils.isExpired(accessTokenDO.getExpiresTime())) {
			throw exception0(GlobalErrorCodeConstants.UNAUTHORIZED.getCode(), "访问令牌已过期");
		}
		return accessTokenDO;
	}

	/**
	 * 删除 remove Access Token 对应的数据。
	 *
	 * @param accessToken accessToken 参数
	 * @return 处理结果
	 */
	@Transactional(rollbackFor = Exception.class)
	public OAuth2AccessTokenDO removeAccessToken(String accessToken) {

		OAuth2AccessTokenDO accessTokenDO = oauth2AccessTokenMapper.selectByAccessToken(accessToken);
		if (accessTokenDO == null) {
			return null;
		}
		oauth2AccessTokenMapper.deleteById(accessTokenDO.getId());
		oauth2AccessTokenRedisDAO.delete(accessToken);
		oauth2RefreshTokenMapper.deleteByRefreshToken(accessTokenDO.getRefreshToken());
		oauth2AccessTokenRedisDAO.delete(accessTokenDO.getRefreshToken());
		return accessTokenDO;
	}

	/**
	 * 删除 remove Access Token 对应的数据。
	 *
	 * @param userId   userId 参数
	 * @param userType userType 参数
	 */
	public void removeAccessToken(Long userId, Integer userType) {

		List<OAuth2AccessTokenDO> accessTokens = oauth2AccessTokenMapper.selectListByUserIdAndUserType(userId, userType);
		if (CollUtil.isEmpty(accessTokens)) {
			return;
		}
		accessTokens.forEach(accessToken -> {
			oauth2AccessTokenMapper.deleteById(accessToken.getId());
			oauth2AccessTokenRedisDAO.delete(accessToken.getAccessToken());
			oauth2RefreshTokenMapper.deleteByRefreshToken(accessToken.getRefreshToken());
			oauth2AccessTokenRedisDAO.delete(accessToken.getRefreshToken());
		});
	}

	/**
	 * 查询 get Access Token Page 对应的数据。
	 *
	 * @param reqVO reqVO 参数
	 * @return 处理结果
	 */
	public PageResult<OAuth2AccessTokenDO> getAccessTokenPage(OAuth2AccessTokenPageReqVO reqVO) {

		return oauth2AccessTokenMapper.selectPage(reqVO);
	}

	/**
	 * 创建 create OAuth2 Access Token 对应的数据。
	 *
	 * @param refreshTokenDO refreshTokenDO 参数
	 * @param clientDO       clientDO 参数
	 * @return 处理结果
	 */
	private OAuth2AccessTokenDO createOAuth2AccessToken(OAuth2RefreshTokenDO refreshTokenDO, OAuth2ClientDO clientDO) {

		OAuth2AccessTokenDO accessTokenDO = new OAuth2AccessTokenDO().setAccessToken(generateAccessToken()).setUserId(refreshTokenDO.getUserId()).setUserType(refreshTokenDO.getUserType()).setUserInfo(buildUserInfo(refreshTokenDO.getUserId(), refreshTokenDO.getUserType())).setClientId(clientDO.getClientId()).setScopes(refreshTokenDO.getScopes()).setRefreshToken(refreshTokenDO.getRefreshToken()).setExpiresTime(LocalDateTime.now().plusSeconds(clientDO.getAccessTokenValiditySeconds()));
		Long tenantId = refreshTokenDO.getTenantId();
		if (tenantId == null) {
			tenantId = TenantContextHolder.getTenantId();
		}
		accessTokenDO.setTenantId(tenantId);
		oauth2AccessTokenMapper.insert(accessTokenDO);
		oauth2AccessTokenRedisDAO.set(accessTokenDO);
		return accessTokenDO;
	}

	/**
	 * 创建 create OAuth2 Refresh Token 对应的数据。
	 *
	 * @param userId   userId 参数
	 * @param userType userType 参数
	 * @param clientDO clientDO 参数
	 * @param scopes   scopes 参数
	 * @return 处理结果
	 */
	private OAuth2RefreshTokenDO createOAuth2RefreshToken(Long userId, Integer userType, OAuth2ClientDO clientDO, List<String> scopes) {

		OAuth2RefreshTokenDO refreshToken = new OAuth2RefreshTokenDO().setRefreshToken(generateRefreshToken()).setUserId(userId).setUserType(userType).setClientId(clientDO.getClientId()).setScopes(scopes).setExpiresTime(LocalDateTime.now().plusSeconds(clientDO.getRefreshTokenValiditySeconds()));
		oauth2RefreshTokenMapper.insert(refreshToken);
		return refreshToken;
	}

	/**
	 * 转换 convert To Access Token 对应的数据对象。
	 *
	 * @param refreshTokenDO refreshTokenDO 参数
	 * @return 处理结果
	 */
	private OAuth2AccessTokenDO convertToAccessToken(OAuth2RefreshTokenDO refreshTokenDO) {

		OAuth2AccessTokenDO accessTokenDO = BeanUtils.toBean(refreshTokenDO, OAuth2AccessTokenDO.class).setAccessToken(refreshTokenDO.getRefreshToken());
		TenantUtils.execute(refreshTokenDO.getTenantId(), () -> accessTokenDO.setUserInfo(buildUserInfo(refreshTokenDO.getUserId(), refreshTokenDO.getUserType())));
		return accessTokenDO;
	}

	/**
	 * 构建 build User Info 对应的数据对象。
	 *
	 * @param userId   userId 参数
	 * @param userType userType 参数
	 * @return 处理结果
	 */
	private Map<String, String> buildUserInfo(Long userId, Integer userType) {

		if (userId == null || userId <= 0) {
			return Collections.emptyMap();
		}
		if (userType.equals(UserTypeEnum.ADMIN.getValue())) {
			AdminUserDO user = adminUserService.getUser(userId);
			return MapUtil.builder(LoginUser.INFO_KEY_NICKNAME, user.getNickname()).put(LoginUser.INFO_KEY_DEPT_ID, StrUtil.toStringOrNull(user.getDeptId())).build();
		}
		if (userType.equals(UserTypeEnum.MEMBER.getValue())) {
			return Collections.emptyMap();
		}
		throw new IllegalArgumentException("未知用户类型：" + userType);
	}

	/**
	 * 执行 clean Refresh Token 对应的业务操作。
	 *
	 * @param exceedDay   exceedDay 参数
	 * @param deleteLimit deleteLimit 参数
	 * @return 处理结果
	 */
	public Integer cleanRefreshToken(Integer exceedDay, Integer deleteLimit) {

		int count = 0;
		LocalDateTime expireDate = LocalDateTime.now().minusDays(exceedDay);
		for (int i = 0; i < Short.MAX_VALUE; i++) {
			int deleteCount = oauth2RefreshTokenMapper.deleteByExpiresTimeLt(expireDate, deleteLimit);
			count += deleteCount;
			if (deleteCount < deleteLimit) {
				break;
			}
		}
		return count;
	}

	/**
	 * 执行 clean Access Token 对应的业务操作。
	 *
	 * @param exceedDay   exceedDay 参数
	 * @param deleteLimit deleteLimit 参数
	 * @return 处理结果
	 */
	public Integer cleanAccessToken(Integer exceedDay, Integer deleteLimit) {

		int count = 0;
		LocalDateTime expireDate = LocalDateTime.now().minusDays(exceedDay);
		for (int i = 0; i < Short.MAX_VALUE; i++) {
			int deleteCount = oauth2AccessTokenMapper.deleteByExpiresTimeLt(expireDate, deleteLimit);
			count += deleteCount;
			if (deleteCount < deleteLimit) {
				break;
			}
		}
		return count;
	}

	/**
	 * 创建 create Authorization Code 对应的数据。
	 *
	 * @param userId      userId 参数
	 * @param userType    userType 参数
	 * @param clientId    clientId 参数
	 * @param scopes      scopes 参数
	 * @param redirectUri redirectUri 参数
	 * @param state       state 参数
	 * @return 处理结果
	 */
	public OAuth2CodeDO createAuthorizationCode(Long userId, Integer userType, String clientId, List<String> scopes, String redirectUri, String state) {

		OAuth2CodeDO codeDO = new OAuth2CodeDO().setCode(generateCode()).setUserId(userId).setUserType(userType).setClientId(clientId).setScopes(scopes).setExpiresTime(LocalDateTime.now().plusSeconds(CODE_TIMEOUT)).setRedirectUri(redirectUri).setState(state);
		oauth2CodeMapper.insert(codeDO);
		return codeDO;
	}

	/**
	 * 处理 consume Authorization Code 对应的业务逻辑。
	 *
	 * @param code code 参数
	 * @return 处理结果
	 */
	public OAuth2CodeDO consumeAuthorizationCode(String code) {

		OAuth2CodeDO codeDO = oauth2CodeMapper.selectByCode(code);
		if (codeDO == null) {
			throw exception(OAUTH2_CODE_NOT_EXISTS);
		}
		if (DateUtils.isExpired(codeDO.getExpiresTime())) {
			throw exception(OAUTH2_CODE_EXPIRE);
		}
		oauth2CodeMapper.deleteById(codeDO.getId());
		return codeDO;
	}

	/**
	 * 校验 check For Pre Approval 对应的业务规则。
	 *
	 * @param userId          userId 参数
	 * @param userType        userType 参数
	 * @param clientId        clientId 参数
	 * @param requestedScopes requestedScopes 参数
	 * @return 处理结果
	 */
	@Transactional
	public boolean checkForPreApproval(Long userId, Integer userType, String clientId, Collection<String> requestedScopes) {

		OAuth2ClientDO clientDO = validOAuthClientFromCache(clientId);
		Assert.notNull(clientDO, "客户端不能为空");
		if (CollUtil.containsAll(clientDO.getAutoApproveScopes(), requestedScopes)) {
			LocalDateTime expireTime = LocalDateTime.now().plusSeconds(APPROVE_TIMEOUT);
			for (String scope : requestedScopes) {
				saveApprove(userId, userType, clientId, scope, true, expireTime);
			}
			return true;
		}
		List<OAuth2ApproveDO> approveDOs = getApproveList(userId, userType, clientId);
		Set<String> scopes = convertSet(approveDOs, OAuth2ApproveDO::getScope, OAuth2ApproveDO::getApproved);
		return CollUtil.containsAll(scopes, requestedScopes);
	}

	/**
	 * 更新 update After Approval 对应的数据。
	 *
	 * @param userId          userId 参数
	 * @param userType        userType 参数
	 * @param clientId        clientId 参数
	 * @param requestedScopes requestedScopes 参数
	 * @return 处理结果
	 */
	@Transactional
	public boolean updateAfterApproval(Long userId, Integer userType, String clientId, Map<String, Boolean> requestedScopes) {

		if (CollUtil.isEmpty(requestedScopes)) {
			return true;
		}
		boolean success = false;
		LocalDateTime expireTime = LocalDateTime.now().plusSeconds(APPROVE_TIMEOUT);
		for (Map.Entry<String, Boolean> entry : requestedScopes.entrySet()) {
			if (entry.getValue()) {
				success = true;
			}
			saveApprove(userId, userType, clientId, entry.getKey(), entry.getValue(), expireTime);
		}
		return success;
	}

	/**
	 * 查询 get Approve List 对应的数据。
	 *
	 * @param userId   userId 参数
	 * @param userType userType 参数
	 * @param clientId clientId 参数
	 * @return 处理结果
	 */
	public List<OAuth2ApproveDO> getApproveList(Long userId, Integer userType, String clientId) {

		List<OAuth2ApproveDO> approveDOs = oauth2ApproveMapper.selectListByUserIdAndUserTypeAndClientId(userId, userType, clientId);
		approveDOs.removeIf(o -> DateUtils.isExpired(o.getExpiresTime()));
		return approveDOs;
	}

	/**
	 * 创建 save Approve 对应的数据。
	 *
	 * @param userId     userId 参数
	 * @param userType   userType 参数
	 * @param clientId   clientId 参数
	 * @param scope      scope 参数
	 * @param approved   approved 参数
	 * @param expireTime expireTime 参数
	 */
	@VisibleForTesting
	void saveApprove(Long userId, Integer userType, String clientId, String scope, Boolean approved, LocalDateTime expireTime) {

		OAuth2ApproveDO approveDO = new OAuth2ApproveDO().setUserId(userId).setUserType(userType).setClientId(clientId).setScope(scope).setApproved(approved).setExpiresTime(expireTime);
		if (oauth2ApproveMapper.update(approveDO) == 1) {
			return;
		}
		oauth2ApproveMapper.insert(approveDO);
	}

	/**
	 * 执行 grant Implicit 对应的业务操作。
	 *
	 * @param userId   userId 参数
	 * @param userType userType 参数
	 * @param clientId clientId 参数
	 * @param scopes   scopes 参数
	 * @return 处理结果
	 */
	public OAuth2AccessTokenDO grantImplicit(Long userId, Integer userType, String clientId, List<String> scopes) {

		return createAccessToken(userId, userType, clientId, scopes);
	}

	/**
	 * 执行 grant Authorization Code For Code 对应的业务操作。
	 *
	 * @param userId      userId 参数
	 * @param userType    userType 参数
	 * @param clientId    clientId 参数
	 * @param scopes      scopes 参数
	 * @param redirectUri redirectUri 参数
	 * @param state       state 参数
	 * @return 处理结果
	 */
	public String grantAuthorizationCodeForCode(Long userId, Integer userType, String clientId, List<String> scopes, String redirectUri, String state) {

		return createAuthorizationCode(userId, userType, clientId, scopes, redirectUri, state).getCode();
	}

	/**
	 * 执行 grant Authorization Code For Access Token 对应的业务操作。
	 *
	 * @param clientId    clientId 参数
	 * @param code        code 参数
	 * @param redirectUri redirectUri 参数
	 * @param state       state 参数
	 * @return 处理结果
	 */
	public OAuth2AccessTokenDO grantAuthorizationCodeForAccessToken(String clientId, String code, String redirectUri, String state) {

		OAuth2CodeDO codeDO = consumeAuthorizationCode(code);
		Assert.notNull(codeDO, "授权码不能为空");
		if (!StrUtil.equals(clientId, codeDO.getClientId())) {
			throw exception(ErrorCodeConstants.OAUTH2_GRANT_CLIENT_ID_MISMATCH);
		}
		if (!StrUtil.equals(redirectUri, codeDO.getRedirectUri())) {
			throw exception(ErrorCodeConstants.OAUTH2_GRANT_REDIRECT_URI_MISMATCH);
		}
		state = StrUtil.nullToDefault(state, "");
		if (!StrUtil.equals(state, codeDO.getState())) {
			throw exception(ErrorCodeConstants.OAUTH2_GRANT_STATE_MISMATCH);
		}
		return createAccessToken(codeDO.getUserId(), codeDO.getUserType(), codeDO.getClientId(), codeDO.getScopes());
	}

	/**
	 * 执行 grant Password 对应的业务操作。
	 *
	 * @param username username 参数
	 * @param password password 参数
	 * @param clientId clientId 参数
	 * @param scopes   scopes 参数
	 * @return 处理结果
	 */
	public OAuth2AccessTokenDO grantPassword(String username, String password, String clientId, List<String> scopes) {

		AdminUserDO user = adminAuthService.authenticate(username, password);
		Assert.notNull(user, "用户不能为空！");
		return createAccessToken(user.getId(), UserTypeEnum.ADMIN.getValue(), clientId, scopes);
	}

	/**
	 * 执行 grant Refresh Token 对应的业务操作。
	 *
	 * @param refreshToken refreshToken 参数
	 * @param clientId     clientId 参数
	 * @return 处理结果
	 */
	public OAuth2AccessTokenDO grantRefreshToken(String refreshToken, String clientId) {

		return refreshAccessToken(refreshToken, clientId);
	}

	/**
	 * 执行 grant Client Credentials 对应的业务操作。
	 *
	 * @param clientId clientId 参数
	 * @param scopes   scopes 参数
	 * @return 处理结果
	 */
	public OAuth2AccessTokenDO grantClientCredentials(String clientId, List<String> scopes) {

		return createAccessToken(0L, UserTypeEnum.ADMIN.getValue(), clientId, scopes);
	}

	/**
	 * 执行 revoke Token 对应的业务操作。
	 *
	 * @param clientId    clientId 参数
	 * @param accessToken accessToken 参数
	 * @return 处理结果
	 */
	public boolean revokeToken(String clientId, String accessToken) {

		OAuth2AccessTokenDO accessTokenDO = getAccessToken(accessToken);
		if (accessTokenDO == null || ObjectUtil.notEqual(clientId, accessTokenDO.getClientId())) {
			return false;
		}
		return removeAccessToken(accessToken) != null;
	}

	/**
	 * 创建 create Token 对应的数据。
	 *
	 * @param id           id 参数
	 * @param accessToken  accessToken 参数
	 * @param refreshToken refreshToken 参数
	 * @param userId       userId 参数
	 * @param userType     userType 参数
	 * @param clientId     clientId 参数
	 * @param scopes       scopes 参数
	 * @param expiresTime  expiresTime 参数
	 * @return 处理结果
	 */
	@Transactional
	public OAuth2AccessToken createToken(Long id, String accessToken, String refreshToken, Long userId, Integer userType, String clientId, List<String> scopes, LocalDateTime expiresTime) {

		OAuth2AccessToken token = OAuth2AccessToken.of(id, accessToken, refreshToken, clientId).userId(userId).userType(userType).scopes(scopes).expiresTime(expiresTime);
		return tokenRepo.save(token);
	}

	/**
	 * 删除 delete Token 对应的数据。
	 *
	 * @param id id 参数
	 */
	@Transactional
	public void deleteToken(Long id) {

		tokenRepo.delete(id);
	}

	/**
	 * 查询 get Token 对应的数据。
	 *
	 * @param id id 参数
	 * @return 处理结果
	 */
	public OAuth2AccessToken getToken(Long id) {

		return tokenRepo.findById(id);
	}

	/**
	 * 查询 find By Access Token 对应的数据。
	 *
	 * @param accessToken accessToken 参数
	 * @return 处理结果
	 */
	public OAuth2AccessToken findByAccessToken(String accessToken) {

		return tokenRepo.findByAccessToken(accessToken);
	}

	/**
	 * 查询 find By Refresh Token 对应的数据。
	 *
	 * @param refreshToken refreshToken 参数
	 * @return 处理结果
	 */
	public OAuth2AccessToken findByRefreshToken(String refreshToken) {

		return tokenRepo.findByRefreshToken(refreshToken);
	}

	/**
	 * 删除 delete By User Type And User Id 对应的数据。
	 *
	 * @param userType userType 参数
	 * @param userId   userId 参数
	 */
	@Transactional
	public void deleteByUserTypeAndUserId(Integer userType, Long userId) {

		tokenRepo.deleteByUserTypeAndUserId(userType, userId);
	}

	/**
	 * 查询 get Self 对应的数据。
	 *
	 * @return 处理结果
	 */
	private OAuth2ApplicationService getSelf() {

		return SpringUtil.getBean(getClass());
	}

	/**
	 * 执行 generate Access Token 对应的业务操作。
	 *
	 * @return 处理结果
	 */
	private static String generateAccessToken() {

		return IdUtil.fastSimpleUUID();
	}

	/**
	 * 执行 generate Refresh Token 对应的业务操作。
	 *
	 * @return 处理结果
	 */
	private static String generateRefreshToken() {

		return IdUtil.fastSimpleUUID();
	}

	/**
	 * 执行 generate Code 对应的业务操作。
	 *
	 * @return 处理结果
	 */
	private static String generateCode() {

		return IdUtil.fastSimpleUUID();
	}

}
