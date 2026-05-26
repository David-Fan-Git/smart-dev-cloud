package com.develop.mvp.pk.module.mp.infrastructure.account;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.mp.dal.dataobject.account.MpAccountDO;
import com.develop.mvp.pk.module.mp.dal.mysql.account.MpAccountMapper;
import com.develop.mvp.pk.module.mp.domain.account.MpAccount;
import com.develop.mvp.pk.module.mp.domain.account.MpAccountFactory;
import com.develop.mvp.pk.module.mp.domain.account.repository.MpAccountPageQuery;
import com.develop.mvp.pk.module.mp.domain.account.repository.MpAccountRepository;
import com.develop.mvp.pk.module.mp.domain.account.valueobject.MpAccountId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class MpAccountRepositoryImpl implements MpAccountRepository {

    private final MpAccountMapper mpAccountMapper;

    public MpAccountRepositoryImpl(MpAccountMapper mpAccountMapper) {
        this.mpAccountMapper = mpAccountMapper;
    }

    @Override
    public MpAccount save(MpAccount a) {
        MpAccountDO accountDO = toDataObject(a);
        if (a.id() != null && mpAccountMapper.selectById(a.id().value()) != null) {
            mpAccountMapper.updateById(accountDO);
        } else {
            mpAccountMapper.insert(accountDO);
        }
        return a;
    }

    @Override
    public void delete(MpAccountId id) {
        mpAccountMapper.deleteById(id.value());
    }

    @Override
    public MpAccount findById(MpAccountId id) {
        MpAccountDO accountDO = mpAccountMapper.selectById(id.value());
        return accountDO != null ? toDomain(accountDO) : null;
    }

    @Override
    public Optional<MpAccount> findByAppId(String appId) {
        MpAccountDO accountDO = mpAccountMapper.selectByAppId(appId);
        return Optional.ofNullable(accountDO).map(this::toDomain);
    }

    @Override
    public List<MpAccount> findAll() {
        return mpAccountMapper.selectList().stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public PageResult<MpAccount> findPage(MpAccountPageQuery query) {
        var reqVO = new com.develop.mvp.pk.module.mp.controller.admin.account.vo.MpAccountPageReqVO();
        reqVO.setName(query.name());
        reqVO.setAccount(query.account());
        reqVO.setAppId(query.appId());
        reqVO.setPageNo(query.pageNo());
        reqVO.setPageSize(query.pageSize());

        PageResult<MpAccountDO> doPage = mpAccountMapper.selectPage(reqVO);
        List<MpAccount> accounts = doPage.getList().stream()
                .map(this::toDomain).collect(Collectors.toList());
        return new PageResult<>(accounts, doPage.getTotal());
    }

    private MpAccountDO toDataObject(MpAccount a) {
        MpAccountDO accountDO = new MpAccountDO();
        if (a.id() != null) accountDO.setId(a.id().value());
        accountDO.setName(a.name());
        accountDO.setAccount(a.account());
        accountDO.setAppId(a.appId());
        accountDO.setAppSecret(a.appSecret());
        accountDO.setToken(a.token());
        accountDO.setAesKey(a.aesKey());
        accountDO.setQrCodeUrl(a.qrCodeUrl());
        accountDO.setRemark(a.remark());
        return accountDO;
    }

    private MpAccount toDomain(MpAccountDO accountDO) {
        return MpAccountFactory.reconstitute(
                accountDO.getId(),
                accountDO.getName(),
                accountDO.getAccount(),
                accountDO.getAppId(),
                accountDO.getAppSecret(),
                accountDO.getToken(),
                accountDO.getAesKey(),
                accountDO.getQrCodeUrl(),
                accountDO.getRemark()
        );
    }
}
