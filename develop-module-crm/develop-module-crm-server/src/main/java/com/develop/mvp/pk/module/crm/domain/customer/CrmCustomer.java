package com.develop.mvp.pk.module.crm.domain.customer;

import com.develop.mvp.pk.module.crm.domain.customer.event.CrmCustomerCreatedEvent;
import com.develop.mvp.pk.module.crm.domain.customer.event.CrmCustomerDeletedEvent;
import com.develop.mvp.pk.module.crm.domain.customer.event.CrmCustomerTransferedEvent;
import com.develop.mvp.pk.module.crm.domain.customer.event.DomainEvent;
import com.develop.mvp.pk.module.crm.domain.customer.valueobject.CrmCustomerId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CrmCustomer {

    private final CrmCustomerId id;
    private String name;
    private Boolean followUpStatus;
    private LocalDateTime contactLastTime;
    private String contactLastContent;
    private LocalDateTime contactNextTime;
    private Long ownerUserId;
    private LocalDateTime ownerTime;
    private Boolean lockStatus;
    private Boolean dealStatus;
    private String mobile;
    private String telephone;
    private String qq;
    private String wechat;
    private String email;
    private Integer areaId;
    private String detailAddress;
    private Integer industryId;
    private Integer level;
    private Integer source;
    private String remark;

    private final List<DomainEvent> events = new ArrayList<>();

    CrmCustomer(CrmCustomerId id, String name, Boolean followUpStatus,
                LocalDateTime contactLastTime, String contactLastContent,
                LocalDateTime contactNextTime, Long ownerUserId, LocalDateTime ownerTime,
                Boolean lockStatus, Boolean dealStatus, String mobile, String telephone,
                String qq, String wechat, String email, Integer areaId, String detailAddress,
                Integer industryId, Integer level, Integer source, String remark) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "客户名称不能为空");
        this.followUpStatus = followUpStatus;
        this.contactLastTime = contactLastTime;
        this.contactLastContent = contactLastContent;
        this.contactNextTime = contactNextTime;
        this.ownerUserId = ownerUserId;
        this.ownerTime = ownerTime;
        this.lockStatus = lockStatus;
        this.dealStatus = dealStatus;
        this.mobile = mobile;
        this.telephone = telephone;
        this.qq = qq;
        this.wechat = wechat;
        this.email = email;
        this.areaId = areaId;
        this.detailAddress = detailAddress;
        this.industryId = industryId;
        this.level = level;
        this.source = source;
        this.remark = remark;
    }

    CrmCustomer(Long id, String name, Long ownerUserId, Integer level,
                Integer industryId, Integer source, String mobile,
                String telephone, String qq, String wechat, String email,
                Integer areaId, String detailAddress, String remark) {
        this.id = id != null ? CrmCustomerId.of(id) : null;
        this.name = Objects.requireNonNull(name, "客户名称不能为空");
        this.ownerUserId = ownerUserId;
        this.level = level;
        this.industryId = industryId;
        this.source = source;
        this.mobile = mobile;
        this.telephone = telephone;
        this.qq = qq;
        this.wechat = wechat;
        this.email = email;
        this.areaId = areaId;
        this.detailAddress = detailAddress;
        this.remark = remark;
        this.followUpStatus = false;
        this.lockStatus = false;
        this.dealStatus = false;
    }

    public static CrmCustomer of(Long id, String name) {
        return new CrmCustomer(id != null ? CrmCustomerId.of(id) : null, name, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null);
    }

    public void updateProfile(String name, String mobile, String telephone, String qq,
                               String wechat, String email, Integer areaId, String detailAddress,
                               Integer industryId, Integer level, Integer source, String remark) {
        this.name = Objects.requireNonNull(name, "客户名称不能为空");
        this.mobile = mobile;
        this.telephone = telephone;
        this.qq = qq;
        this.wechat = wechat;
        this.email = email;
        this.areaId = areaId;
        this.detailAddress = detailAddress;
        this.industryId = industryId;
        this.level = level;
        this.source = source;
        this.remark = remark;
    }

    public void assignOwner(Long userId) {
        this.ownerUserId = userId;
        this.ownerTime = LocalDateTime.now();
        events.add(new CrmCustomerTransferedEvent(this.id.value(), this.ownerUserId, userId));
    }

    public void transferOwner(Long fromUserId, Long toUserId) {
        this.ownerUserId = toUserId;
        this.ownerTime = LocalDateTime.now();
        events.add(new CrmCustomerTransferedEvent(this.id.value(), fromUserId, toUserId));
    }

    public void updateFollowUp(LocalDateTime contactNextTime, String contactLastContent) {
        this.contactLastTime = LocalDateTime.now();
        this.contactLastContent = contactLastContent;
        this.contactNextTime = contactNextTime;
        this.followUpStatus = true;
    }

    public void updateDealStatus(Boolean dealStatus) {
        this.dealStatus = dealStatus;
    }

    public void lock() {
        this.lockStatus = true;
    }

    public void unlock() {
        this.lockStatus = false;
    }

    public void markDeleted() {
        events.add(new CrmCustomerDeletedEvent(this.id.value(), this.name));
    }

    public void putToPool() {
        this.ownerUserId = null;
        this.ownerTime = null;
    }

    public void receiveFromPool(Long userId) {
        this.ownerUserId = userId;
        this.ownerTime = LocalDateTime.now();
    }

    // Query methods

    public CrmCustomerId id() { return id; }
    public String name() { return name; }
    public Boolean followUpStatus() { return followUpStatus; }
    public LocalDateTime contactLastTime() { return contactLastTime; }
    public String contactLastContent() { return contactLastContent; }
    public LocalDateTime contactNextTime() { return contactNextTime; }
    public Long ownerUserId() { return ownerUserId; }
    public LocalDateTime ownerTime() { return ownerTime; }
    public Boolean lockStatus() { return lockStatus; }
    public Boolean dealStatus() { return dealStatus; }
    public String mobile() { return mobile; }
    public String telephone() { return telephone; }
    public String qq() { return qq; }
    public String wechat() { return wechat; }
    public String email() { return email; }
    public Integer areaId() { return areaId; }
    public String detailAddress() { return detailAddress; }
    public Integer industryId() { return industryId; }
    public Integer level() { return level; }
    public Integer source() { return source; }
    public String remark() { return remark; }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> result = new ArrayList<>(events);
        events.clear();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CrmCustomer that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "CrmCustomer{id=" + id + ", name=" + name + '}';
    }
}
