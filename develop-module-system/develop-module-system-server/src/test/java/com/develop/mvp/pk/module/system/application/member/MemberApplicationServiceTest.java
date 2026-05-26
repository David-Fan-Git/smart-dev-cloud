package com.develop.mvp.pk.module.system.application.member;

import com.develop.mvp.pk.module.system.application.member.port.outbound.MemberUserGateway;
import com.develop.mvp.pk.module.system.application.member.service.MemberApplicationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MemberApplicationServiceTest {

    private final MemberApplicationService memberApplicationService = new MemberApplicationService(new FakeMemberUserGateway());

    @Test
    void getMemberUserMobile_whenIdIsNull_returnsNull() {
        assertNull(memberApplicationService.getMemberUserMobile(null));
    }

    @Test
    void getMemberUserMobile_whenUserExists_returnsMobile() {
        assertEquals("15601691300", memberApplicationService.getMemberUserMobile(1L));
    }

    @Test
    void getMemberUserEmail_whenUserExists_returnsEmail() {
        assertEquals("member@test.com", memberApplicationService.getMemberUserEmail(1L));
    }

    @Test
    void getMemberUserEmail_whenUserMissing_returnsNull() {
        assertNull(memberApplicationService.getMemberUserEmail(2L));
    }

    private static final class FakeMemberUserGateway implements MemberUserGateway {

        @Override
        public Object getMemberUser(Long id) {
            return id.equals(1L) ? new FakeMemberUser("15601691300", "member@test.com") : null;
        }
    }

    private record FakeMemberUser(String mobile, String email) {
        public String getMobile() {
            return mobile;
        }

        public String getEmail() {
            return email;
        }
    }
}
