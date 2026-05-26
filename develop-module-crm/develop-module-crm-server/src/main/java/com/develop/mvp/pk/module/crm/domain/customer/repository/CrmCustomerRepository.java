package com.develop.mvp.pk.module.crm.domain.customer.repository;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.crm.domain.customer.CrmCustomer;
import com.develop.mvp.pk.module.crm.domain.customer.valueobject.CrmCustomerId;

import java.util.Collection;
import java.util.List;

public interface CrmCustomerRepository {
    CrmCustomer save(CrmCustomer c);
    void delete(CrmCustomerId id);
    CrmCustomer findById(CrmCustomerId id);
    List<CrmCustomer> findByIds(Collection<CrmCustomerId> ids);
    PageResult<CrmCustomer> findPage(CrmCustomerPageQuery query);
}
