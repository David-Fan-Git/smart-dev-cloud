package com.develop.mvp.pk.module.member.infrastructure.address;

// Skill: AggregateRoot_MemberAddress_Skill — 仓储实现 MemberAddressRepositoryImpl

import com.develop.mvp.pk.module.member.dal.dataobject.address.MemberAddressDO;
import com.develop.mvp.pk.module.member.dal.mysql.address.MemberAddressMapper;
import com.develop.mvp.pk.module.member.domain.address.MemberAddress;
import com.develop.mvp.pk.module.member.domain.address.repository.MemberAddressRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MemberAddressRepositoryImpl implements MemberAddressRepository {

    private final MemberAddressMapper mapper;
    public MemberAddressRepositoryImpl(MemberAddressMapper mapper) { this.mapper = mapper; }

    @Override @Transactional
    public MemberAddress save(MemberAddress a) {
        MemberAddressDO d = toDO(a);
        if (a.id() == null) { mapper.insert(d); } else { mapper.updateById(d); }
        return fromDO(d);
    }

    @Override @Transactional
    public void delete(Long id) { mapper.deleteById(id); }

    @Override
    public MemberAddress findById(Long id) { return fromDO(mapper.selectById(id)); }

    @Override
    public MemberAddress findByIdAndUserId(Long id, Long userId) {
        return fromDO(mapper.selectByIdAndUserId(id, userId));
    }

    @Override
    public List<MemberAddress> findByUserId(Long userId) {
        return mapper.selectListByUserIdAndDefaulted(userId, null)
                .stream().map(this::fromDO).collect(Collectors.toList());
    }

    @Override
    public List<MemberAddress> findByUserIdAndDefaulted(Long userId, Boolean defaulted) {
        return mapper.selectListByUserIdAndDefaulted(userId, defaulted)
                .stream().map(this::fromDO).collect(Collectors.toList());
    }

    // ── DO ↔ Domain 映射 ──
    private MemberAddressDO toDO(MemberAddress a) {
        return MemberAddressDO.builder()
                .id(a.id()).userId(a.userId()).name(a.name()).mobile(a.mobile())
                .areaId(a.areaId()).detailAddress(a.detailAddress()).defaultStatus(a.defaultStatus())
                .build();
    }

    private MemberAddress fromDO(MemberAddressDO d) {
        if (d == null) return null;
        return MemberAddress.reconstitute(d.getId(), d.getUserId(), d.getName(), d.getMobile(),
                d.getAreaId(), d.getDetailAddress(), Boolean.TRUE.equals(d.getDefaultStatus()));
    }
}
