package com.develop.mvp.pk.module.member.convert.level;

import com.develop.mvp.pk.module.member.controller.admin.level.vo.level.MemberLevelSimpleRespVO;
import com.develop.mvp.pk.module.member.domain.level.MemberLevel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MemberLevelConvertTest {

    @Test
    void convertSimpleListFromDomain_includesIcon() {
        MemberLevel level = MemberLevel.reconstitute(1L, "黄金会员", 2, 100,
                90, "https://example.com/icon.png", "https://example.com/bg.png", 0);

        List<MemberLevelSimpleRespVO> result = MemberLevelConvert.INSTANCE.convertSimpleListFromDomain(List.of(level));

        assertEquals("https://example.com/icon.png", result.get(0).getIcon());
    }
}
