package com.develop.mvp.pk.module.member.application.signin;

// Skill: AggregateRoot_MemberSignInConfig_Skill — 应用服务 MemberSignInConfigApplicationService

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.module.member.domain.signin.MemberSignInConfig;
import com.develop.mvp.pk.module.member.domain.signin.repository.MemberSignInConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.member.enums.ErrorCodeConstants.SIGN_IN_CONFIG_EXISTS;
import static com.develop.mvp.pk.module.member.enums.ErrorCodeConstants.SIGN_IN_CONFIG_NOT_EXISTS;

@Service
@RequiredArgsConstructor
public class MemberSignInConfigApplicationService {

    private final MemberSignInConfigRepository repo;

    @Transactional
    public Long createSignInConfig(Integer day, Integer point, Integer experience, Integer status) {
        validateDayDuplicate(day, null);
        MemberSignInConfig config = MemberSignInConfig.create(day, point, experience);
        if (CommonStatusEnum.ENABLE.getStatus().equals(status)) config.enable(); else config.disable();
        config = repo.save(config);
        return config.id();
    }

    @Transactional
    public void updateSignInConfig(Long id, Integer day, Integer point, Integer experience, Integer status) {
        validateExists(id);
        validateDayDuplicate(day, id);
        MemberSignInConfig config = get(id);
        config.update(day, point, experience, status);
        repo.save(config);
    }

    @Transactional
    public void deleteSignInConfig(Long id) {
        validateExists(id);
        repo.delete(id);
    }

    public MemberSignInConfig get(Long id) {
        MemberSignInConfig c = repo.findById(id);
        if (c == null) throw exception(SIGN_IN_CONFIG_NOT_EXISTS);
        return c;
    }

    public List<MemberSignInConfig> getList() { return repo.findAll(); }

    public List<MemberSignInConfig> getListByStatus(Integer status) { return repo.findByStatus(status); }

    private void validateExists(Long id) {
        if (repo.findById(id) == null) throw exception(SIGN_IN_CONFIG_NOT_EXISTS);
    }

    private void validateDayDuplicate(Integer day, Long id) {
        MemberSignInConfig existing = repo.findByDay(day);
        if (id == null && existing != null) throw exception(SIGN_IN_CONFIG_EXISTS);
        if (id != null && existing != null && !existing.id().equals(id)) throw exception(SIGN_IN_CONFIG_EXISTS);
    }
}
