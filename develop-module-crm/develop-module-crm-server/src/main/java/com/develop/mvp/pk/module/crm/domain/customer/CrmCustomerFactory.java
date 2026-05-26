package com.develop.mvp.pk.module.crm.domain.customer;

import com.develop.mvp.pk.module.crm.domain.customer.valueobject.CrmCustomerId;

import java.time.LocalDateTime;

public final class CrmCustomerFactory {

    private CrmCustomerFactory() {}

    public static CrmCustomer create(String name, Long ownerUserId, Integer level,
                                      Integer industryId, Integer source, String mobile,
                                      String telephone, String qq, String wechat, String email,
                                      Integer areaId, String detailAddress, String remark) {
        return new CrmCustomer(
                null, name, ownerUserId, level, industryId, source,
                mobile, telephone, qq, wechat, email, areaId, detailAddress, remark
        );
    }

    public static CrmCustomer reconstitute(Long id, String name, Boolean followUpStatus,
                                            LocalDateTime contactLastTime, String contactLastContent,
                                            LocalDateTime contactNextTime, Long ownerUserId,
                                            LocalDateTime ownerTime, Boolean lockStatus,
                                            Boolean dealStatus, String mobile, String telephone,
                                            String qq, String wechat, String email, Integer areaId,
                                            String detailAddress, Integer industryId, Integer level,
                                            Integer source, String remark) {
        return new CrmCustomer(
                CrmCustomerId.of(id), name, followUpStatus, contactLastTime, contactLastContent,
                contactNextTime, ownerUserId, ownerTime, lockStatus, dealStatus,
                mobile, telephone, qq, wechat, email, areaId, detailAddress,
                industryId, level, source, remark
        );
    }
}
