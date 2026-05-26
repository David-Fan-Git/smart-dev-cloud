package com.develop.mvp.pk.module.system.application.oauth2.service;

import com.develop.mvp.pk.framework.common.enums.UserTypeEnum;
import com.develop.mvp.pk.framework.test.core.ut.BaseMockitoUnitTest;
import com.develop.mvp.pk.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import com.develop.mvp.pk.module.system.dal.dataobject.oauth2.OAuth2CodeDO;
import com.develop.mvp.pk.module.system.application.auth.service.AuthApplicationService;
import com.develop.mvp.pk.module.system.dal.dataobject.user.AdminUserDO;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

import java.util.List;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static com.develop.mvp.pk.framework.test.core.util.AssertUtils.assertPojoEquals;
import static com.develop.mvp.pk.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * {@link OAuth2ApplicationService} 的单元测试
 *
 * @author David
 */
public class OAuth2GrantApplicationServiceTest extends BaseMockitoUnitTest {

    @Spy
    @InjectMocks
    private OAuth2ApplicationService oauth2GrantService;

    @org.mockito.Mock
    private AuthApplicationService adminAuthService;

    @Test
    public void testGrantImplicit() {
        // 准备参数
        Long userId = randomLongId();
        Integer userType = randomEle(UserTypeEnum.values()).getValue();
        String clientId = randomString();
        List<String> scopes = Lists.newArrayList("read", "write");
        // mock 方法
        OAuth2AccessTokenDO accessTokenDO = randomPojo(OAuth2AccessTokenDO.class);
        doReturn(accessTokenDO).when(oauth2GrantService).createAccessToken(eq(userId), eq(userType),
                eq(clientId), eq(scopes));

        // 调用，并断言
        assertPojoEquals(accessTokenDO, oauth2GrantService.grantImplicit(
                userId, userType, clientId, scopes));
    }

    @Test
    public void testGrantAuthorizationCodeForCode() {
        // 准备参数
        Long userId = randomLongId();
        Integer userType = randomEle(UserTypeEnum.values()).getValue();
        String clientId = randomString();
        List<String> scopes = Lists.newArrayList("read", "write");
        String redirectUri = randomString();
        String state = randomString();
        // mock 方法
        OAuth2CodeDO codeDO = randomPojo(OAuth2CodeDO.class);
        doReturn(codeDO).when(oauth2GrantService).createAuthorizationCode(eq(userId), eq(userType),
                eq(clientId), eq(scopes), eq(redirectUri), eq(state));

        // 调用，并断言
        assertEquals(codeDO.getCode(), oauth2GrantService.grantAuthorizationCodeForCode(userId, userType,
                clientId, scopes, redirectUri, state));
    }

    @Test
    public void testGrantAuthorizationCodeForAccessToken() {
        // 准备参数
        String clientId = randomString();
        String code = randomString();
        List<String> scopes = Lists.newArrayList("read", "write");
        String redirectUri = randomString();
        String state = randomString();
        // mock 方法（code）
        OAuth2CodeDO codeDO = randomPojo(OAuth2CodeDO.class, o -> {
            o.setClientId(clientId);
            o.setRedirectUri(redirectUri);
            o.setState(state);
            o.setScopes(scopes);
        });
        doReturn(codeDO).when(oauth2GrantService).consumeAuthorizationCode(eq(code));
        // mock 方法（创建令牌）
        OAuth2AccessTokenDO accessTokenDO = randomPojo(OAuth2AccessTokenDO.class);
        doReturn(accessTokenDO).when(oauth2GrantService).createAccessToken(eq(codeDO.getUserId()), eq(codeDO.getUserType()),
                eq(codeDO.getClientId()), eq(codeDO.getScopes()));

        // 调用，并断言
        assertPojoEquals(accessTokenDO, oauth2GrantService.grantAuthorizationCodeForAccessToken(
                clientId, code, redirectUri, state));
    }

    @Test
    public void testGrantPassword() {
        // 准备参数
        String username = randomString();
        String password = randomString();
        String clientId = randomString();
        List<String> scopes = Lists.newArrayList("read", "write");
        // mock 方法(认证)
        AdminUserDO user = randomPojo(AdminUserDO.class);
        when(adminAuthService.authenticate(eq(username), eq(password))).thenReturn(user);
        // mock 方法（访问令牌）
        OAuth2AccessTokenDO accessTokenDO = randomPojo(OAuth2AccessTokenDO.class);
        doReturn(accessTokenDO).when(oauth2GrantService).createAccessToken(eq(user.getId()), eq(UserTypeEnum.ADMIN.getValue()),
                eq(clientId), eq(scopes));

        // 调用，并断言
        assertPojoEquals(accessTokenDO, oauth2GrantService.grantPassword(
                username, password, clientId, scopes));
    }

    @Test
    public void testGrantRefreshToken() {
        // 准备参数
        String refreshToken = randomString();
        String clientId = randomString();
        // mock 方法
        OAuth2AccessTokenDO accessTokenDO = randomPojo(OAuth2AccessTokenDO.class);
        doReturn(accessTokenDO).when(oauth2GrantService).refreshAccessToken(eq(refreshToken), eq(clientId));

        // 调用，并断言
        assertPojoEquals(accessTokenDO, oauth2GrantService.grantRefreshToken(
                refreshToken, clientId));
    }

    @Test
    public void testRevokeToken_clientIdError() {
        // 准备参数
        String clientId = randomString();
        String accessToken = randomString();
        // mock 方法
        OAuth2AccessTokenDO accessTokenDO = randomPojo(OAuth2AccessTokenDO.class);
        doReturn(accessTokenDO).when(oauth2GrantService).getAccessToken(eq(accessToken));

        // 调用，并断言
        assertFalse(oauth2GrantService.revokeToken(clientId, accessToken));
    }

    @Test
    public void testRevokeToken_success() {
        // 准备参数
        String clientId = randomString();
        String accessToken = randomString();
        // mock 方法（访问令牌）
        OAuth2AccessTokenDO accessTokenDO = randomPojo(OAuth2AccessTokenDO.class).setClientId(clientId);
        doReturn(accessTokenDO).when(oauth2GrantService).getAccessToken(eq(accessToken));
        // mock 方法（移除）
        doReturn(accessTokenDO).when(oauth2GrantService).removeAccessToken(eq(accessToken));

        // 调用，并断言
        assertTrue(oauth2GrantService.revokeToken(clientId, accessToken));
    }

}
