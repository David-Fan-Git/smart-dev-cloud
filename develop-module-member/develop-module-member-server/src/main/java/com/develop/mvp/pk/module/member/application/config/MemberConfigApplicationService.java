package com.develop.mvp.pk.module.member.application.config;

// Skill: AggregateRoot_MemberConfig_Skill — 应用服务 MemberConfigApplicationService

import com.develop.mvp.pk.module.member.domain.config.MemberConfig;
import com.develop.mvp.pk.module.member.domain.config.repository.MemberConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberConfigApplicationService {

    private final MemberConfigRepository repo;

    @Transactional
    public void saveConfig(Boolean pointTradeDeductEnable, Integer pointTradeDeductUnitPrice,
                           Integer pointTradeDeductMaxPrice, Integer pointTradeGivePoint) {
        MemberConfig config = repo.findSingle().orElse(null);
        if (config != null) {
            config.update(pointTradeDeductEnable, pointTradeDeductUnitPrice, pointTradeDeductMaxPrice, pointTradeGivePoint);
            repo.save(config);
        } else {
            repo.save(MemberConfig.create(pointTradeDeductEnable, pointTradeDeductUnitPrice,
                    pointTradeDeductMaxPrice, pointTradeGivePoint));
        }
    }

    public MemberConfig getConfig() {
        return repo.findSingle().orElse(null);
    }
}
