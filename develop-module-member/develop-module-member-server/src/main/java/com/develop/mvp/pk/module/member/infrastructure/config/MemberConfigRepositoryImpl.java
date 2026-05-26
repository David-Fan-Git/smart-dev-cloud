package com.develop.mvp.pk.module.member.infrastructure.config;

// Skill: AggregateRoot_MemberConfig_Skill — 仓储实现 MemberConfigRepositoryImpl

import com.develop.mvp.pk.framework.common.util.collection.CollectionUtils;
import com.develop.mvp.pk.module.member.dal.dataobject.config.MemberConfigDO;
import com.develop.mvp.pk.module.member.dal.mysql.config.MemberConfigMapper;
import com.develop.mvp.pk.module.member.domain.config.MemberConfig;
import com.develop.mvp.pk.module.member.domain.config.repository.MemberConfigRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class MemberConfigRepositoryImpl implements MemberConfigRepository {

    private final MemberConfigMapper mapper;
    public MemberConfigRepositoryImpl(MemberConfigMapper mapper) { this.mapper = mapper; }

    @Override @Transactional
    public MemberConfig save(MemberConfig c) {
        MemberConfigDO d = toDO(c);
        if (c.id() == null) { mapper.insert(d); } else { mapper.updateById(d); }
        return fromDO(d);
    }

    @Override
    public Optional<MemberConfig> findSingle() {
        MemberConfigDO d = CollectionUtils.getFirst(mapper.selectList());
        return Optional.ofNullable(fromDO(d));
    }

    // ── DO ↔ Domain 映射 ──
    private MemberConfigDO toDO(MemberConfig c) {
        return MemberConfigDO.builder()
                .id(c.id()).pointTradeDeductEnable(c.pointTradeDeductEnable())
                .pointTradeDeductUnitPrice(c.pointTradeDeductUnitPrice())
                .pointTradeDeductMaxPrice(c.pointTradeDeductMaxPrice())
                .pointTradeGivePoint(c.pointTradeGivePoint())
                .build();
    }

    private MemberConfig fromDO(MemberConfigDO d) {
        if (d == null) return null;
        return MemberConfig.reconstitute(d.getId(), d.getPointTradeDeductEnable(),
                d.getPointTradeDeductUnitPrice(), d.getPointTradeDeductMaxPrice(), d.getPointTradeGivePoint());
    }
}
