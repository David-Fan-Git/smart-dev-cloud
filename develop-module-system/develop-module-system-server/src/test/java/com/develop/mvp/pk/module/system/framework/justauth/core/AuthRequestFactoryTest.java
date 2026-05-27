package com.develop.mvp.pk.module.system.framework.justauth.core;

import com.xkcoding.justauth.autoconfigure.JustAuthProperties;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.enums.AuthResponseStatus;
import me.zhyd.oauth.exception.AuthException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class AuthRequestFactoryTest {

    @Test
    void getWithoutExtendThrowsUnsupportedInsteadOfNullPointerException() {
        JustAuthProperties properties = new JustAuthProperties();
        properties.setType(new HashMap<>());
        AuthRequestFactory factory = new AuthRequestFactory(properties, mock(AuthStateCache.class));

        AuthException exception = assertThrows(AuthException.class, () -> factory.get("GITEE"));

        assertEquals(AuthResponseStatus.UNSUPPORTED.getCode(), exception.getErrorCode());
    }
}
