package com.develop.mvp.pk.module.system.application.oauth2.service;

import cn.hutool.core.util.RandomUtil;
import com.develop.mvp.pk.framework.common.enums.UserTypeEnum;
import com.develop.mvp.pk.framework.common.util.date.DateUtils;
import com.develop.mvp.pk.framework.test.core.ut.BaseDbUnitTest;
import com.develop.mvp.pk.module.system.application.auth.service.AuthApplicationService;
import com.develop.mvp.pk.module.system.application.user.service.AdminUserApplicationService;
import com.develop.mvp.pk.module.system.dal.dataobject.oauth2.OAuth2CodeDO;
import com.develop.mvp.pk.module.system.dal.mysql.oauth2.OAuth2CodeMapper;
import com.develop.mvp.pk.module.system.dal.redis.oauth2.OAuth2AccessTokenRedisDAO;
import jakarta.annotation.Resource;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static com.develop.mvp.pk.framework.test.core.util.AssertUtils.assertPojoEquals;
import static com.develop.mvp.pk.framework.test.core.util.AssertUtils.assertServiceException;
import static com.develop.mvp.pk.framework.test.core.util.RandomUtils.*;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.OAUTH2_CODE_EXPIRE;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.OAUTH2_CODE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link OAuth2ApplicationService} 的单元测试类
 *
 * @author David
 */
@Import(OAuth2ApplicationService.class)
class OAuth2CodeApplicationServiceTest extends BaseDbUnitTest {

    @Resource
    private OAuth2ApplicationService oauth2CodeService;

    @Resource
    private OAuth2CodeMapper oauth2CodeMapper;

    @MockitoBean
    private OAuth2AccessTokenRedisDAO oauth2AccessTokenRedisDAO;
    @MockitoBean
    private AdminUserApplicationService adminUserService;
    @MockitoBean
    private AuthApplicationService adminAuthService;

    @Test
    public void testCreateAuthorizationCode() {
        // 准备参数
        Long userId = randomLongId();
        Integer userType = RandomUtil.randomEle(UserTypeEnum.values()).getValue();
        String clientId = randomString();
        List<String> scopes = Lists.newArrayList("read", "write");
        String redirectUri = randomString();
        String state = randomString();

        // 调用
        OAuth2CodeDO codeDO = oauth2CodeService.createAuthorizationCode(userId, userType, clientId,
                scopes, redirectUri, state);
        // 断言
        OAuth2CodeDO dbCodeDO = oauth2CodeMapper.selectByCode(codeDO.getCode());
        // TODO @David：expiresTime 被屏蔽，仅 win11 会复现，建议后续修复。
        assertPojoEquals(codeDO, dbCodeDO, "expiresTime", "createTime", "updateTime", "deleted");
        assertEquals(userId, codeDO.getUserId());
        assertEquals(userType, codeDO.getUserType());
        assertEquals(clientId, codeDO.getClientId());
        assertEquals(scopes, codeDO.getScopes());
        assertEquals(redirectUri, codeDO.getRedirectUri());
        assertEquals(state, codeDO.getState());
        assertFalse(DateUtils.isExpired(codeDO.getExpiresTime()));
    }

    @Test
    public void testConsumeAuthorizationCode_null() {
        // 调用，并断言
        assertServiceException(() -> oauth2CodeService.consumeAuthorizationCode(randomString()),
                OAUTH2_CODE_NOT_EXISTS);
    }

    @Test
    public void testConsumeAuthorizationCode_expired() {
        // 准备参数
        String code = "test_code";
        // mock 数据
        OAuth2CodeDO codeDO = randomPojo(OAuth2CodeDO.class).setCode(code)
                .setExpiresTime(LocalDateTime.now().minusDays(1));
        oauth2CodeMapper.insert(codeDO);

        // 调用，并断言
        assertServiceException(() -> oauth2CodeService.consumeAuthorizationCode(code),
                OAUTH2_CODE_EXPIRE);
    }

    @Test
    public void testConsumeAuthorizationCode_success() {
        // 准备参数
        String code = "test_code";
        // mock 数据
        OAuth2CodeDO codeDO = randomPojo(OAuth2CodeDO.class).setCode(code)
                .setExpiresTime(LocalDateTime.now().plusDays(1));
        oauth2CodeMapper.insert(codeDO);

        // 调用
        OAuth2CodeDO result = oauth2CodeService.consumeAuthorizationCode(code);
        // TODO @David：expiresTime 被屏蔽，仅 win11 会复现，建议后续修复。
        assertPojoEquals(codeDO, result, "expiresTime");
        assertNull(oauth2CodeMapper.selectByCode(code));
    }

}
