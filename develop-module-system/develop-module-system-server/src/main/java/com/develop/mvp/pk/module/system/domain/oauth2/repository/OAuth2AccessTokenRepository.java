package com.develop.mvp.pk.module.system.domain.oauth2.repository;

import com.develop.mvp.pk.module.system.domain.oauth2.OAuth2AccessToken;
import java.util.List;

/**
 * OAuth2 Access Token Repository 领域仓储接口。
 */
public interface OAuth2AccessTokenRepository {
    /**
     * 创建 save 对应的数据。
     *
     * @param t t 参数
     * @return 处理结果
     */
    OAuth2AccessToken save(OAuth2AccessToken t);
    /**
     * 删除 delete 对应的数据。
     *
     * @param id id 参数
     */
    void delete(Long id);
    /**
     * 查询 find By Id 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    OAuth2AccessToken findById(Long id);
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
     * 删除 delete By User Id 对应的数据。
     *
     * @param userId userId 参数
     */
    void deleteByUserId(Long userId);
    /**
     * 删除 delete By User Type And User Id 对应的数据。
     *
     * @param userType userType 参数
     * @param userId userId 参数
     */
    void deleteByUserTypeAndUserId(Integer userType, Long userId);
}
