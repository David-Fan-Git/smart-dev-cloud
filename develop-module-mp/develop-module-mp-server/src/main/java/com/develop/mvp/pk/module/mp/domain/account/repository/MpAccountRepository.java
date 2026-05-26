package com.develop.mvp.pk.module.mp.domain.account.repository;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.mp.domain.account.MpAccount;
import com.develop.mvp.pk.module.mp.domain.account.valueobject.MpAccountId;

import java.util.List;
import java.util.Optional;

public interface MpAccountRepository {
    MpAccount save(MpAccount a);
    void delete(MpAccountId id);
    MpAccount findById(MpAccountId id);
    Optional<MpAccount> findByAppId(String appId);
    List<MpAccount> findAll();
    PageResult<MpAccount> findPage(MpAccountPageQuery query);
}
