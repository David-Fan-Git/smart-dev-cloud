package com.develop.mvp.pk.module.mp.domain.account;

import com.develop.mvp.pk.module.mp.domain.account.valueobject.MpAccountId;

public final class MpAccountFactory {

    private MpAccountFactory() {}

    public static MpAccount create(String name, String account, String appId, String appSecret,
                                    String token, String aesKey, String qrCodeUrl, String remark) {
        return new MpAccount(
                (MpAccountId) null,
                name,
                account,
                appId,
                appSecret,
                token,
                aesKey,
                qrCodeUrl,
                remark
        );
    }

    public static MpAccount reconstitute(Long id, String name, String account, String appId,
                                          String appSecret, String token, String aesKey,
                                          String qrCodeUrl, String remark) {
        return new MpAccount(
                MpAccountId.of(id),
                name,
                account,
                appId,
                appSecret,
                token,
                aesKey,
                qrCodeUrl,
                remark
        );
    }
}
