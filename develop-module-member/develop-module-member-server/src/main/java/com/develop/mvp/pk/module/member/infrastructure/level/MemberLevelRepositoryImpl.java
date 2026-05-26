package com.develop.mvp.pk.module.member.infrastructure.level;

// Skill: AggregateRoot_MemberLevel_Skill — 仓储实现 MemberLevelRepositoryImpl

import cn.hutool.core.collection.CollUtil;
import com.develop.mvp.pk.module.member.dal.dataobject.level.MemberLevelDO;
import com.develop.mvp.pk.module.member.dal.mysql.level.MemberLevelMapper;
import com.develop.mvp.pk.module.member.domain.level.MemberLevel;
import com.develop.mvp.pk.module.member.domain.level.repository.MemberLevelRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MemberLevelRepositoryImpl implements MemberLevelRepository {

    private final MemberLevelMapper mapper;
    public MemberLevelRepositoryImpl(MemberLevelMapper mapper) { this.mapper = mapper; }

    @Override @Transactional
    public MemberLevel save(MemberLevel l) {
        MemberLevelDO d = toDO(l);
        if (l.id() == null) { mapper.insert(d); } else { mapper.updateById(d); }
        return fromDO(d);
    }

    @Override @Transactional
    public void delete(Long id) { mapper.deleteById(id); }

    @Override
    public MemberLevel findById(Long id) { return fromDO(mapper.selectById(id)); }

    @Override
    public List<MemberLevel> findByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) return Collections.emptyList();
        return mapper.selectByIds(ids).stream().map(this::fromDO).collect(Collectors.toList());
    }

    @Override
    public List<MemberLevel> findByNameLike(String name) {
        return List.of();
    }

    @Override
    public List<MemberLevel> findByStatus(Integer status) {
        return mapper.selectListByStatus(status).stream().map(this::fromDO).collect(Collectors.toList());
    }

    @Override
    public List<MemberLevel> findAll() {
        return mapper.selectList().stream()
                .map(this::fromDO)
                .sorted(Comparator.comparing(MemberLevel::level))
                .collect(Collectors.toList());
    }

    // ── DO ↔ Domain 映射 ──
    private MemberLevelDO toDO(MemberLevel l) {
        return MemberLevelDO.builder()
                .id(l.id()).name(l.name()).level(l.level()).experience(l.experience())
                .discountPercent(l.discountPercent()).icon(l.icon()).backgroundUrl(l.backgroundUrl())
                .status(l.status()).build();
    }

    private MemberLevel fromDO(MemberLevelDO d) {
        if (d == null) return null;
        return MemberLevel.reconstitute(d.getId(), d.getName(), d.getLevel(), d.getExperience(),
                d.getDiscountPercent(), d.getIcon(), d.getBackgroundUrl(), d.getStatus());
    }
}
