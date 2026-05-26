package com.develop.mvp.pk.module.mp.domain.account;

import com.develop.mvp.pk.module.mp.domain.account.event.DomainEvent;
import com.develop.mvp.pk.module.mp.domain.account.event.MpAccountCreatedEvent;
import com.develop.mvp.pk.module.mp.domain.account.event.MpAccountDeletedEvent;
import com.develop.mvp.pk.module.mp.domain.account.valueobject.MpAccountId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MpAccount {

    private final MpAccountId id;
    private String name;
    private String account;
    private String appId;
    private String appSecret;
    private String token;
    private String aesKey;
    private String qrCodeUrl;
    private String remark;

    private final List<DomainEvent> events = new ArrayList<>();

    MpAccount(MpAccountId id, String name, String account, String appId, String appSecret,
              String token, String aesKey, String qrCodeUrl, String remark) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "公众号名称不能为空");
        this.account = account;
        this.appId = Objects.requireNonNull(appId, "appId不能为空");
        this.appSecret = appSecret;
        this.token = token;
        this.aesKey = aesKey;
        this.qrCodeUrl = qrCodeUrl;
        this.remark = remark;
    }

    MpAccount(Long id, String name, String account, String appId, String appSecret,
              String token, String aesKey, String qrCodeUrl, String remark) {
        this.id = id != null ? MpAccountId.of(id) : null;
        this.name = Objects.requireNonNull(name, "公众号名称不能为空");
        this.account = account;
        this.appId = Objects.requireNonNull(appId, "appId不能为空");
        this.appSecret = appSecret;
        this.token = token;
        this.aesKey = aesKey;
        this.qrCodeUrl = qrCodeUrl;
        this.remark = remark;
    }

    public static MpAccount of(Long id, String name) {
        return new MpAccount(id, name, null, "placeholder", null, null, null, null, null);
    }

    public void updateProfile(String name, String account, String appId, String appSecret,
                               String token, String aesKey, String remark) {
        this.name = Objects.requireNonNull(name, "公众号名称不能为空");
        this.account = account;
        this.appId = Objects.requireNonNull(appId, "appId不能为空");
        this.appSecret = appSecret;
        this.token = token;
        this.aesKey = aesKey;
        this.remark = remark;
    }

    public void updateQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public void markDeleted() {
        events.add(new MpAccountDeletedEvent(this.id.value(), this.name));
    }

    // Query methods

    public MpAccountId id() { return id; }
    public String name() { return name; }
    public String account() { return account; }
    public String appId() { return appId; }
    public String appSecret() { return appSecret; }
    public String token() { return token; }
    public String aesKey() { return aesKey; }
    public String qrCodeUrl() { return qrCodeUrl; }
    public String remark() { return remark; }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> result = new ArrayList<>(events);
        events.clear();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MpAccount that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "MpAccount{id=" + id + ", name=" + name + '}';
    }
}
