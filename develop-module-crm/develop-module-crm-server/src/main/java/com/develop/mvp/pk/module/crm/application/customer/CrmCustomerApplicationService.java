package com.develop.mvp.pk.module.crm.application.customer;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.crm.domain.customer.CrmCustomer;
import com.develop.mvp.pk.module.crm.domain.customer.CrmCustomerFactory;
import com.develop.mvp.pk.module.crm.domain.customer.event.DomainEvent;
import com.develop.mvp.pk.module.crm.domain.customer.event.DomainEventPublisher;
import com.develop.mvp.pk.module.crm.domain.customer.repository.CrmCustomerPageQuery;
import com.develop.mvp.pk.module.crm.domain.customer.repository.CrmCustomerRepository;
import com.develop.mvp.pk.module.crm.domain.customer.valueobject.CrmCustomerId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
public class CrmCustomerApplicationService {

    private final CrmCustomerRepository crmCustomerRepository;
    private final DomainEventPublisher eventPublisher;

    public CrmCustomerApplicationService(CrmCustomerRepository crmCustomerRepository,
                                          DomainEventPublisher eventPublisher) {
        this.crmCustomerRepository = crmCustomerRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Long createCustomer(String name, Long ownerUserId, Integer level,
                                Integer industryId, Integer source, String mobile,
                                String telephone, String qq, String wechat, String email,
                                Integer areaId, String detailAddress, String remark) {
        CrmCustomer customer = CrmCustomerFactory.create(name, ownerUserId, level, industryId,
                source, mobile, telephone, qq, wechat, email, areaId, detailAddress, remark);
        crmCustomerRepository.save(customer);
        publishEvents(customer);
        return customer.id() != null ? customer.id().value() : null;
    }

    @Transactional
    public void updateCustomer(Long id, String name, String mobile, String telephone,
                                String qq, String wechat, String email, Integer areaId,
                                String detailAddress, Integer industryId, Integer level,
                                Integer source, String remark) {
        CrmCustomer customer = findExistingCustomer(CrmCustomerId.of(id));
        customer.updateProfile(name, mobile, telephone, qq, wechat, email, areaId,
                detailAddress, industryId, level, source, remark);
        crmCustomerRepository.save(customer);
        publishEvents(customer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        CrmCustomer customer = findExistingCustomer(CrmCustomerId.of(id));
        customer.markDeleted();
        crmCustomerRepository.delete(customer.id());
        publishEvents(customer);
    }

    @Transactional
    public void transferCustomer(Long id, Long fromUserId, Long toUserId) {
        CrmCustomer customer = findExistingCustomer(CrmCustomerId.of(id));
        customer.transferOwner(fromUserId, toUserId);
        crmCustomerRepository.save(customer);
        publishEvents(customer);
    }

    @Transactional
    public void updateFollowUp(Long id, LocalDateTime contactNextTime, String contactLastContent) {
        CrmCustomer customer = findExistingCustomer(CrmCustomerId.of(id));
        customer.updateFollowUp(contactNextTime, contactLastContent);
        crmCustomerRepository.save(customer);
        publishEvents(customer);
    }

    @Transactional
    public void updateDealStatus(Long id, Boolean dealStatus) {
        CrmCustomer customer = findExistingCustomer(CrmCustomerId.of(id));
        customer.updateDealStatus(dealStatus);
        crmCustomerRepository.save(customer);
        publishEvents(customer);
    }

    @Transactional
    public void lockCustomer(Long id) {
        CrmCustomer customer = findExistingCustomer(CrmCustomerId.of(id));
        customer.lock();
        crmCustomerRepository.save(customer);
        publishEvents(customer);
    }

    @Transactional
    public void unlockCustomer(Long id) {
        CrmCustomer customer = findExistingCustomer(CrmCustomerId.of(id));
        customer.unlock();
        crmCustomerRepository.save(customer);
        publishEvents(customer);
    }

    @Transactional
    public void putCustomerToPool(Long id) {
        CrmCustomer customer = findExistingCustomer(CrmCustomerId.of(id));
        customer.putToPool();
        crmCustomerRepository.save(customer);
        publishEvents(customer);
    }

    @Transactional
    public void receiveCustomerFromPool(Long id, Long userId) {
        CrmCustomer customer = findExistingCustomer(CrmCustomerId.of(id));
        customer.receiveFromPool(userId);
        crmCustomerRepository.save(customer);
        publishEvents(customer);
    }

    public CrmCustomer getCustomer(Long id) {
        return crmCustomerRepository.findById(CrmCustomerId.of(id));
    }

    public List<CrmCustomer> getCustomerList(Collection<Long> ids) {
        List<CrmCustomerId> customerIds = ids.stream()
                .map(CrmCustomerId::of).collect(Collectors.toList());
        return crmCustomerRepository.findByIds(customerIds);
    }

    public PageResult<CrmCustomer> getCustomerPage(String name, Long ownerUserId,
                                                     Integer status, Integer pageNo,
                                                     Integer pageSize) {
        return crmCustomerRepository.findPage(
                new CrmCustomerPageQuery(name, ownerUserId, status, pageNo, pageSize));
    }

    private CrmCustomer findExistingCustomer(CrmCustomerId id) {
        CrmCustomer customer = crmCustomerRepository.findById(id);
        if (customer == null) {
            throw exception(com.develop.mvp.pk.module.crm.enums.ErrorCodeConstants.CUSTOMER_NOT_EXISTS);
        }
        return customer;
    }

    private void publishEvents(CrmCustomer customer) {
        for (DomainEvent event : customer.pullEvents()) {
            eventPublisher.publish(event);
        }
    }
}
