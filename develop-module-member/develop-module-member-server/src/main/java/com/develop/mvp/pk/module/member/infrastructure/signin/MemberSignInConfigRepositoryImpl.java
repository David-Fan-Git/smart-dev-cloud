package com.develop.mvp.pk.module.member.infrastructure.signin;

// Skill: AggregateRoot_MemberSignInConfig_Skill — 仓储实现 MemberSignInConfigRepositoryImpl

import com.develop.mvp.pk.module.member.dal.dataobject.signin.MemberSignInConfigDO;
import com.develop.mvp.pk.module.member.dal.mysql.signin.MemberSignInConfigMapper;
import com.develop.mvp.pk.module.member.domain.signin.MemberSignInConfig;
import com.develop.mvp.pk.module.member.domain.signin.repository.MemberSignInConfigRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MemberSignInConfigRepositoryImpl implements MemberSignInConfigRepository {

    private final MemberSignInConfigMapper mapper;
    public MemberSignInConfigRepositoryImpl(MemberSignInConfigMapper mapper) { this.mapper = mapper; }

    @Override @Transactional
    public MemberSignInConfig save(MemberSignInConfig c) {
        MemberSignInConfigDO d = toDO(c);
        if (c.id() == null) { mapper.insert(d); } else { mapper.updateById(d); }
        return fromDO(d);
    }

    @Override @Transactional
    public void delete(Long id) { mapper.deleteById(id); }

    @Override
    public MemberSignInConfig findById(Long id) { return fromDO(mapper.selectById(id)); }

    @Override
    public MemberSignInConfig findByDay(Integer day) { return fromDO(mapper.selectByDay(day)); }

    @Override
    public List<MemberSignInConfig> findAll() {
        List<MemberSignInConfigDO> list = mapper.selectList();
        list.sort(Comparator.comparing(MemberSignInConfigDO::getDay));
        return list.stream().map(this::fromDO).collect(Collectors.toList());
    }

    @Override
    public List<MemberSignInConfig> findByStatus(Integer status) {
        List<MemberSignInConfigDO> list = mapper.selectListByStatus(status);
        list.sort(Comparator.comparing(MemberSignInConfigDO::getDay));
        return list.stream().map(this::fromDO).collect(Collectors.toList());
    }

    // ── DO ↔ Domain 映射 ──
    private MemberSignInConfigDO toDO(MemberSignInConfig c) {
        return MemberSignInConfigDO.builder()
                .id(c.id()).day(c.day()).point(c.point()).experience(c.experience()).status(c.status())
                .build();
    }

    private MemberSignInConfig fromDO(MemberSignInConfigDO d) {
        if (d == null) return null;
        return MemberSignInConfig.reconstitute(d.getId(), d.getDay(), d.getPoint(), d.getExperience(), d.getStatus());
    }
}
