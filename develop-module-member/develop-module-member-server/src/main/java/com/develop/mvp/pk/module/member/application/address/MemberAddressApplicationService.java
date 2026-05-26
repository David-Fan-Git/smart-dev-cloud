package com.develop.mvp.pk.module.member.application.address;

// Skill: AggregateRoot_MemberAddress_Skill — 应用服务 MemberAddressApplicationService

import com.develop.mvp.pk.module.member.domain.address.MemberAddress;
import com.develop.mvp.pk.module.member.domain.address.repository.MemberAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.member.enums.ErrorCodeConstants.ADDRESS_NOT_EXISTS;

@Service
@RequiredArgsConstructor
public class MemberAddressApplicationService {

    private final MemberAddressRepository repo;

    // ── 创建 ──
    @Transactional
    public Long createAddress(Long userId, String name, String mobile,
                              Long areaId, String detailAddress, Boolean defaultStatus) {
        // 如果添加的是默认收件地址，则将原默认地址修改为非默认
        if (Boolean.TRUE.equals(defaultStatus)) {
            List<MemberAddress> defaultAddresses = repo.findByUserIdAndDefaulted(userId, true);
            defaultAddresses.forEach(a -> { a.unmarkDefault(); repo.save(a); });
        }
        MemberAddress address = MemberAddress.create(userId, name, mobile, areaId, detailAddress,
                Boolean.TRUE.equals(defaultStatus));
        address = repo.save(address);
        return address.id();
    }

    // ── 更新 ──
    @Transactional
    public void updateAddress(Long userId, Long id, String name, String mobile,
                              Long areaId, String detailAddress, Boolean defaultStatus) {
        MemberAddress address = getByUser(userId, id);
        address.updateInfo(name, mobile, areaId, detailAddress);
        // 如果修改的是默认收件地址，则将原默认地址修改为非默认
        if (Boolean.TRUE.equals(defaultStatus)) {
            List<MemberAddress> defaultAddresses = repo.findByUserIdAndDefaulted(userId, true);
            defaultAddresses.stream().filter(a -> !a.id().equals(id))
                    .forEach(a -> { a.unmarkDefault(); repo.save(a); });
            address.markDefault();
        }
        repo.save(address);
    }

    // ── 删除 ──
    @Transactional
    public void deleteAddress(Long userId, Long id) {
        getByUser(userId, id);
        repo.delete(id);
    }

    // ── 查询 ──
    public MemberAddress getAddress(Long userId, Long id) {
        return getByUser(userId, id);
    }

    public List<MemberAddress> getAddressList(Long userId) {
        return repo.findByUserId(userId);
    }

    public MemberAddress getDefaultUserAddress(Long userId) {
        List<MemberAddress> addresses = repo.findByUserIdAndDefaulted(userId, true);
        return addresses.isEmpty() ? null : addresses.get(0);
    }

    // ── 校验 ──
    private MemberAddress getByUser(Long userId, Long id) {
        MemberAddress a = repo.findByIdAndUserId(id, userId);
        if (a == null) throw exception(ADDRESS_NOT_EXISTS);
        return a;
    }
}
