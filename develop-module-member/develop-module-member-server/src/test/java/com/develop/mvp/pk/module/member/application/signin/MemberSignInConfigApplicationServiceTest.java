package com.develop.mvp.pk.module.member.application.signin;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.module.member.domain.signin.MemberSignInConfig;
import com.develop.mvp.pk.module.member.domain.signin.repository.MemberSignInConfigRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MemberSignInConfigApplicationServiceTest {

    @Test
    void createSignInConfig_withEnableStatusPersistsEnable() {
        StubMemberSignInConfigRepository repository = new StubMemberSignInConfigRepository();
        MemberSignInConfigApplicationService applicationService = new MemberSignInConfigApplicationService(repository);

        applicationService.createSignInConfig(1, 10, 20, CommonStatusEnum.ENABLE.getStatus());

        assertEquals(CommonStatusEnum.ENABLE.getStatus(), repository.saved.status());
    }

    @Test
    void createSignInConfig_withDisableStatusPersistsDisable() {
        StubMemberSignInConfigRepository repository = new StubMemberSignInConfigRepository();
        MemberSignInConfigApplicationService applicationService = new MemberSignInConfigApplicationService(repository);

        applicationService.createSignInConfig(1, 10, 20, CommonStatusEnum.DISABLE.getStatus());

        assertEquals(CommonStatusEnum.DISABLE.getStatus(), repository.saved.status());
    }

    private static final class StubMemberSignInConfigRepository implements MemberSignInConfigRepository {
        private MemberSignInConfig saved;

        @Override
        public MemberSignInConfig save(MemberSignInConfig config) {
            saved = config;
            return MemberSignInConfig.reconstitute(100L, config.day(), config.point(), config.experience(), config.status());
        }

        @Override public void delete(Long id) {}
        @Override public MemberSignInConfig findById(Long id) { return null; }
        @Override public MemberSignInConfig findByDay(Integer day) { return null; }
        @Override public List<MemberSignInConfig> findAll() { return List.of(); }
        @Override public List<MemberSignInConfig> findByStatus(Integer status) { return List.of(); }
    }
}
