package com.develop.mvp.pk.module.pay.infrastructure.transfer;
// DDD 角色：转账单仓储实现 - AggregateRoot_Pay_Skill
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.dal.dataobject.transfer.PayTransferDO;
import com.develop.mvp.pk.module.pay.dal.mysql.transfer.PayTransferMapper;
import com.develop.mvp.pk.module.pay.domain.transfer.PayTransfer;
import com.develop.mvp.pk.module.pay.domain.transfer.repository.PayTransferRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Repository
public class PayTransferRepositoryImpl implements PayTransferRepository {
    @Resource private PayTransferMapper mapper;
    static PayTransfer toDomain(PayTransferDO doObj) {
        if (doObj == null) return null;
        return new PayTransfer(doObj.getId(), doObj.getNo())
                .appId(doObj.getAppId()).channelId(doObj.getChannelId()).channelCode(doObj.getChannelCode())
                .userId(doObj.getUserId()).userType(doObj.getUserType())
                .merchantTransferId(doObj.getMerchantTransferId()).subject(doObj.getSubject())
                .price(doObj.getPrice()).userAccount(doObj.getUserAccount()).userName(doObj.getUserName())
                .status(doObj.getStatus()).successTime(doObj.getSuccessTime())
                .notifyUrl(doObj.getNotifyUrl()).userIp(doObj.getUserIp())
                .channelExtras(doObj.getChannelExtras()).channelTransferNo(doObj.getChannelTransferNo())
                .channelErrorCode(doObj.getChannelErrorCode()).channelErrorMsg(doObj.getChannelErrorMsg())
                .channelPackageInfo(doObj.getChannelPackageInfo());
    }
    static PayTransferDO toDO(PayTransfer domain) {
        PayTransferDO doObj = new PayTransferDO();
        doObj.setId(domain.id()); doObj.setNo(domain.no());
        doObj.setAppId(domain.appId()); doObj.setChannelId(domain.channelId()); doObj.setChannelCode(domain.channelCode());
        doObj.setUserId(domain.userId()); doObj.setUserType(domain.userType());
        doObj.setMerchantTransferId(domain.merchantTransferId()); doObj.setSubject(domain.subject());
        doObj.setPrice(domain.price()); doObj.setUserAccount(domain.userAccount()); doObj.setUserName(domain.userName());
        doObj.setStatus(domain.status()); doObj.setSuccessTime(domain.successTime());
        doObj.setNotifyUrl(domain.notifyUrl()); doObj.setUserIp(domain.userIp());
        doObj.setChannelExtras(domain.channelExtras()); doObj.setChannelTransferNo(domain.channelTransferNo());
        doObj.setChannelErrorCode(domain.channelErrorCode()); doObj.setChannelErrorMsg(domain.channelErrorMsg());
        doObj.setChannelPackageInfo(domain.channelPackageInfo());
        return doObj;
    }
    @Override public PayTransfer save(PayTransfer transfer) {
        if (transfer.id() == null) { mapper.insert(toDO(transfer)); return transfer; }
        mapper.updateById(toDO(transfer)); return transfer;
    }
    @Override public PayTransfer findById(Long id) { return toDomain(mapper.selectById(id)); }
    @Override public Optional<PayTransfer> findByNo(String no) { return Optional.ofNullable(toDomain(mapper.selectByNo(no))); }
    @Override public Optional<PayTransfer> findByAppIdAndMerchantTransferId(Long appId, String merchantTransferId) {
        return Optional.ofNullable(toDomain(mapper.selectByAppIdAndMerchantOrderId(appId, merchantTransferId)));
    }
    @Override public Optional<PayTransfer> findByAppIdAndNo(Long appId, String no) {
        return Optional.ofNullable(toDomain(mapper.selectByAppIdAndNo(appId, no)));
    }
    @Override public PageResult<PayTransfer> findPage(String no, Long appId, String channelCode,
                                                       String merchantTransferId, Integer status, Integer pageNo, Integer pageSize) {
        var req = new com.develop.mvp.pk.module.pay.controller.admin.transfer.vo.PayTransferPageReqVO();
        req.setNo(no); req.setAppId(appId); req.setChannelCode(channelCode);
        req.setMerchantTransferId(merchantTransferId); req.setStatus(status);
        if (pageNo != null) req.setPageNo(pageNo); if (pageSize != null) req.setPageSize(pageSize);
        PageResult<PayTransferDO> page = mapper.selectPage(req);
        return new PageResult<>(page.getList().stream().map(PayTransferRepositoryImpl::toDomain).collect(Collectors.toList()), page.getTotal());
    }
    @Override public List<PayTransfer> findByStatuses(Collection<Integer> statuses) {
        return mapper.selectListByStatus(statuses).stream().map(PayTransferRepositoryImpl::toDomain).collect(Collectors.toList());
    }
}
