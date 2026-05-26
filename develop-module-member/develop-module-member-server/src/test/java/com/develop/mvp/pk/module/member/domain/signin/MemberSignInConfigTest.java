package com.develop.mvp.pk.module.member.domain.signin;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MemberSignInConfigTest {

    @Test
    void enable_setsCommonEnableStatus() {
        MemberSignInConfig config = MemberSignInConfig.create(1, 10, 20);

        config.enable();

        assertEquals(CommonStatusEnum.ENABLE.getStatus(), config.status());
    }

    @Test
    void disable_setsCommonDisableStatus() {
        MemberSignInConfig config = MemberSignInConfig.create(1, 10, 20);

        config.disable();

        assertEquals(CommonStatusEnum.DISABLE.getStatus(), config.status());
    }
}
