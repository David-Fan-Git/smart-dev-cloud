package com.develop.mvp.pk.module.mp.application.account;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.mp.domain.account.MpAccount;
import com.develop.mvp.pk.module.mp.domain.account.MpAccountFactory;
import com.develop.mvp.pk.module.mp.domain.account.event.DomainEvent;
import com.develop.mvp.pk.module.mp.domain.account.event.DomainEventPublisher;
import com.develop.mvp.pk.module.mp.domain.account.repository.MpAccountPageQuery;
import com.develop.mvp.pk.module.mp.domain.account.repository.MpAccountRepository;
import com.develop.mvp.pk.module.mp.domain.account.valueobject.MpAccountId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.mp.enums.ErrorCodeConstants.ACCOUNT_NOT_EXISTS;

@Service
public class MpAccountApplicationService {

    private final MpAccountRepository mpAccountRepository;
    private final DomainEventPublisher eventPublisher;

    public MpAccountApplicationService(MpAccountRepository mpAccountRepository,
                                        DomainEventPublisher eventPublisher) {
        this.mpAccountRepository = mpAccountRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Long createAccount(String name, String account, String appId, String appSecret,
                               String token, String aesKey, String qrCodeUrl, String remark) {
        assertAppIdUnique(appId, null);
        MpAccount mpAccount = MpAccountFactory.create(name, account, appId, appSecret,
                token, aesKey, qrCodeUrl, remark);
        mpAccountRepository.save(mpAccount);
        publishEvents(mpAccount);
        return mpAccount.id() != null ? mpAccount.id().value() : null;
    }

    @Transactional
    public void updateAccount(Long id, String name, String account, String appId, String appSecret,
                               String token, String aesKey, String remark) {
        MpAccount mpAccount = findExistingAccount(MpAccountId.of(id));
        assertAppIdUnique(appId, mpAccount.id());
        mpAccount.updateProfile(name, account, appId, appSecret, token, aesKey, remark);
        mpAccountRepository.save(mpAccount);
        publishEvents(mpAccount);
    }

    @Transactional
    public void deleteAccount(Long id) {
        MpAccount mpAccount = findExistingAccount(MpAccountId.of(id));
        mpAccount.markDeleted();
        mpAccountRepository.delete(mpAccount.id());
        publishEvents(mpAccount);
    }

    @Transactional
    public void updateQrCodeUrl(Long id, String qrCodeUrl) {
        MpAccount mpAccount = findExistingAccount(MpAccountId.of(id));
        mpAccount.updateQrCodeUrl(qrCodeUrl);
        mpAccountRepository.save(mpAccount);
        publishEvents(mpAccount);
    }

    public MpAccount getAccount(Long id) {
        return mpAccountRepository.findById(MpAccountId.of(id));
    }

    public MpAccount getAccountByAppId(String appId) {
        return mpAccountRepository.findByAppId(appId).orElse(null);
    }

    public List<MpAccount> getAccountList() {
        return mpAccountRepository.findAll();
    }

    public PageResult<MpAccount> getAccountPage(String name, String account, String appId,
                                                  Integer pageNo, Integer pageSize) {
        return mpAccountRepository.findPage(
                new MpAccountPageQuery(name, account, appId, pageNo, pageSize));
    }

    private MpAccount findExistingAccount(MpAccountId id) {
        MpAccount account = mpAccountRepository.findById(id);
        if (account == null) {
            throw exception(ACCOUNT_NOT_EXISTS);
        }
        return account;
    }

    private void assertAppIdUnique(String appId, MpAccountId excludeId) {
        mpAccountRepository.findByAppId(appId).ifPresent(existing -> {
            if (excludeId == null || !existing.id().equals(excludeId)) {
                throw exception(com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.USER_USERNAME_EXISTS);
            }
        });
    }

    private void publishEvents(MpAccount account) {
        for (DomainEvent event : account.pullEvents()) {
            eventPublisher.publish(event);
        }
    }
}
