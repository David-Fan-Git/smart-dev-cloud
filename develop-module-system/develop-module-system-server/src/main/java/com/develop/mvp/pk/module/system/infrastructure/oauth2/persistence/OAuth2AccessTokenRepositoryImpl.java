package com.develop.mvp.pk.module.system.infrastructure.oauth2.persistence;

import com.develop.mvp.pk.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import com.develop.mvp.pk.module.system.dal.mysql.oauth2.OAuth2AccessTokenMapper;
import com.develop.mvp.pk.module.system.domain.oauth2.OAuth2AccessToken;
import com.develop.mvp.pk.module.system.domain.oauth2.repository.OAuth2AccessTokenRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * OAuth2 Access Token Repository Impl 领域仓储实现。
 */
@Repository
public class OAuth2AccessTokenRepositoryImpl implements OAuth2AccessTokenRepository {

	private final OAuth2AccessTokenMapper mapper;

	/**
	 * 创建 OAuth2AccessTokenRepositoryImpl 实例。
	 *
	 * @param mapper mapper 参数
	 */
	public OAuth2AccessTokenRepositoryImpl(OAuth2AccessTokenMapper mapper) {

		this.mapper = mapper;
	}

	/**
	 * 创建 save 对应的数据。
	 *
	 * @param t t 参数
	 * @return 处理结果
	 */
	@Override
	public OAuth2AccessToken save(OAuth2AccessToken t) {

		OAuth2AccessTokenDO d = new OAuth2AccessTokenDO();
		d.setId(t.id());
		d.setAccessToken(t.accessToken());
		d.setRefreshToken(t.refreshToken());
		d.setUserId(t.userId());
		d.setUserType(t.userType());
		d.setClientId(t.clientId());
		d.setScopes(t.scopes());
		d.setExpiresTime(t.expiresTime());
		if (mapper.selectById(t.id()) == null)
			mapper.insert(d);
		else
			mapper.updateById(d);
		return t;
	}

	/**
	 * 删除 OAuth2 访问令牌。
	 *
	 * @param id 访问令牌编号
	 */
	@Override
	public void delete(Long id) {

		mapper.deleteById(id);
	}

	/**
	 * 根据编号查询 OAuth2 访问令牌。
	 *
	 * @param id 访问令牌编号
	 * @return 访问令牌领域对象，不存在时返回 null
	 */
	@Override
	public OAuth2AccessToken findById(Long id) {

		OAuth2AccessTokenDO d = mapper.selectById(id);
		return d != null ? toDomain(d) : null;
	}

	/**
	 * 根据访问令牌查询 OAuth2 访问令牌。
	 *
	 * @param at 访问令牌
	 * @return 访问令牌领域对象，不存在时返回 null
	 */
	@Override
	public OAuth2AccessToken findByAccessToken(String at) {

		OAuth2AccessTokenDO d = mapper.selectByAccessToken(at);
		return d != null ? toDomain(d) : null;
	}

	/**
	 * 根据刷新令牌查询 OAuth2 访问令牌。
	 *
	 * @param rt 刷新令牌
	 * @return 访问令牌领域对象，不存在时返回 null
	 */
	@Override
	public OAuth2AccessToken findByRefreshToken(String rt) {

		List<OAuth2AccessTokenDO> list = mapper.selectListByRefreshToken(rt);
		return list.isEmpty() ? null : toDomain(list.get(0));
	}

	/**
	 * 删除指定用户的 OAuth2 访问令牌。
	 *
	 * @param userId 用户编号
	 */
	@Override
	public void deleteByUserId(Long userId) {

		mapper.delete(OAuth2AccessTokenDO::getUserId, userId);
	}

	/**
	 * 删除指定用户类型和用户编号的 OAuth2 访问令牌。
	 *
	 * @param userType 用户类型
	 * @param userId 用户编号
	 */
	@Override
	public void deleteByUserTypeAndUserId(Integer userType, Long userId) {

		mapper.selectListByUserIdAndUserType(userId, userType).forEach(t -> mapper.deleteById(t.getId()));
	}

	/**
	 * 执行 to Domain 对应的业务操作。
	 *
	 * @param d d 参数
	 * @return 处理结果
	 */
	private OAuth2AccessToken toDomain(OAuth2AccessTokenDO d) {

		return OAuth2AccessToken.of(d.getId(), d.getAccessToken(), d.getRefreshToken(), d.getClientId()).userId(d.getUserId()).userType(d.getUserType()).scopes(d.getScopes()).expiresTime(d.getExpiresTime());
	}

}
