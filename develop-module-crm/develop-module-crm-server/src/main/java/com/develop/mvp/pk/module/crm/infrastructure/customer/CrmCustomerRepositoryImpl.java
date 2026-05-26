package com.develop.mvp.pk.module.crm.infrastructure.customer;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.crm.dal.dataobject.customer.CrmCustomerDO;
import com.develop.mvp.pk.module.crm.dal.mysql.customer.CrmCustomerMapper;
import com.develop.mvp.pk.module.crm.domain.customer.CrmCustomer;
import com.develop.mvp.pk.module.crm.domain.customer.CrmCustomerFactory;
import com.develop.mvp.pk.module.crm.domain.customer.repository.CrmCustomerPageQuery;
import com.develop.mvp.pk.module.crm.domain.customer.repository.CrmCustomerRepository;
import com.develop.mvp.pk.module.crm.domain.customer.valueobject.CrmCustomerId;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CrmCustomerRepositoryImpl implements CrmCustomerRepository {

    private final CrmCustomerMapper crmCustomerMapper;

    public CrmCustomerRepositoryImpl(CrmCustomerMapper crmCustomerMapper) {
        this.crmCustomerMapper = crmCustomerMapper;
    }

    @Override
    public CrmCustomer save(CrmCustomer c) {
        CrmCustomerDO customerDO = toDataObject(c);
        if (c.id() != null && crmCustomerMapper.selectById(c.id().value()) != null) {
            crmCustomerMapper.updateById(customerDO);
        } else {
            crmCustomerMapper.insert(customerDO);
        }
        return c;
    }

    @Override
    public void delete(CrmCustomerId id) {
        crmCustomerMapper.deleteById(id.value());
    }

    @Override
    public CrmCustomer findById(CrmCustomerId id) {
        CrmCustomerDO customerDO = crmCustomerMapper.selectById(id.value());
        return customerDO != null ? toDomain(customerDO) : null;
    }

    @Override
    public List<CrmCustomer> findByIds(Collection<CrmCustomerId> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        List<Long> rawIds = ids.stream().map(CrmCustomerId::value).collect(Collectors.toList());
        return crmCustomerMapper.selectBatchIds(rawIds).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public PageResult<CrmCustomer> findPage(CrmCustomerPageQuery query) {
        var reqVO = new com.develop.mvp.pk.module.crm.controller.admin.customer.vo.customer.CrmCustomerPageReqVO();
        reqVO.setName(query.name());
        reqVO.setPageNo(query.pageNo());
        reqVO.setPageSize(query.pageSize());

        PageResult<CrmCustomerDO> doPage = crmCustomerMapper.selectPage(reqVO, query.ownerUserId());
        List<CrmCustomer> customers = doPage.getList().stream()
                .map(this::toDomain).collect(Collectors.toList());
        return new PageResult<>(customers, doPage.getTotal());
    }

    private CrmCustomerDO toDataObject(CrmCustomer c) {
        CrmCustomerDO customerDO = new CrmCustomerDO();
        if (c.id() != null) customerDO.setId(c.id().value());
        customerDO.setName(c.name());
        customerDO.setFollowUpStatus(c.followUpStatus());
        customerDO.setContactLastTime(c.contactLastTime());
        customerDO.setContactLastContent(c.contactLastContent());
        customerDO.setContactNextTime(c.contactNextTime());
        customerDO.setOwnerUserId(c.ownerUserId());
        customerDO.setOwnerTime(c.ownerTime());
        customerDO.setLockStatus(c.lockStatus());
        customerDO.setDealStatus(c.dealStatus());
        customerDO.setMobile(c.mobile());
        customerDO.setTelephone(c.telephone());
        customerDO.setQq(c.qq());
        customerDO.setWechat(c.wechat());
        customerDO.setEmail(c.email());
        customerDO.setAreaId(c.areaId());
        customerDO.setDetailAddress(c.detailAddress());
        customerDO.setIndustryId(c.industryId());
        customerDO.setLevel(c.level());
        customerDO.setSource(c.source());
        customerDO.setRemark(c.remark());
        return customerDO;
    }

    private CrmCustomer toDomain(CrmCustomerDO customerDO) {
        return CrmCustomerFactory.reconstitute(
                customerDO.getId(),
                customerDO.getName(),
                customerDO.getFollowUpStatus(),
                customerDO.getContactLastTime(),
                customerDO.getContactLastContent(),
                customerDO.getContactNextTime(),
                customerDO.getOwnerUserId(),
                customerDO.getOwnerTime(),
                customerDO.getLockStatus(),
                customerDO.getDealStatus(),
                customerDO.getMobile(),
                customerDO.getTelephone(),
                customerDO.getQq(),
                customerDO.getWechat(),
                customerDO.getEmail(),
                customerDO.getAreaId(),
                customerDO.getDetailAddress(),
                customerDO.getIndustryId(),
                customerDO.getLevel(),
                customerDO.getSource(),
                customerDO.getRemark()
        );
    }
}
