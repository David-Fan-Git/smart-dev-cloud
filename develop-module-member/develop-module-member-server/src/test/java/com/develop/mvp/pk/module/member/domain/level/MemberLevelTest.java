package com.develop.mvp.pk.module.member.domain.level;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MemberLevelTest {

    @Test
    void enable_setsCommonEnableStatus() {
        MemberLevel level = MemberLevel.create("普通会员");

        level.enable();

        assertEquals(CommonStatusEnum.ENABLE.getStatus(), level.status());
    }

    @Test
    void disable_setsCommonDisableStatus() {
        MemberLevel level = MemberLevel.create("普通会员");

        level.disable();

        assertEquals(CommonStatusEnum.DISABLE.getStatus(), level.status());
    }
}
