package com.develop.mvp.pk.module.member.domain.group;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MemberGroupTest {

    @Test
    void enable_setsCommonEnableStatus() {
        MemberGroup group = MemberGroup.create("普通会员");

        group.enable();

        assertEquals(CommonStatusEnum.ENABLE.getStatus(), group.status());
    }

    @Test
    void disable_setsCommonDisableStatus() {
        MemberGroup group = MemberGroup.create("普通会员");

        group.disable();

        assertEquals(CommonStatusEnum.DISABLE.getStatus(), group.status());
    }
}
