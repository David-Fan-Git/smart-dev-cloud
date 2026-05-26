package com.develop.mvp.pk.module.pay.infrastructure.refund;
// DDD 角色：退款单仓储实现 - AggregateRoot_Pay_Skill
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.dal.dataobject.refund.PayRefundDO;
import com.develop.mvp.pk.module.pay.dal.mysql.refund.PayRefundMapper;
import com.develop.mvp.pk.module.pay.domain.refund.PayRefund;
import com.develop.mvp.pk.module.pay.domain.refund.repository.PayRefundRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Repository
public class PayRefundRepositoryImpl implements PayRefundRepository {
    @Resource private PayRefundMapper mapper;
    static PayRefund toDomain(PayRefundDO doObj) {
        if (doObj == null) return null;
        return new PayRefund(doObj.getId(), doObj.getNo())
                .appId(doObj.getAppId()).channelId(doObj.getChannelId()).channelCode(doObj.getChannelCode())
                .orderId(doObj.getOrderId()).orderNo(doObj.getOrderNo())
                .userId(doObj.getUserId()).userType(doObj.getUserType())
                .merchantOrderId(doObj.getMerchantOrderId()).merchantRefundId(doObj.getMerchantRefundId())
                .notifyUrl(doObj.getNotifyUrl())
                .status(doObj.getStatus()).payPrice(doObj.getPayPrice()).refundPrice(doObj.getRefundPrice())
                .reason(doObj.getReason()).userIp(doObj.getUserIp())
                .channelOrderNo(doObj.getChannelOrderNo()).channelRefundNo(doObj.getChannelRefundNo())
                .successTime(doObj.getSuccessTime())
                .channelErrorCode(doObj.getChannelErrorCode()).channelErrorMsg(doObj.getChannelErrorMsg());
    }
    static PayRefundDO toDO(PayRefund domain) {
        return PayRefundDO.builder().id(domain.id()).no(domain.no())
                .appId(domain.appId()).channelId(domain.channelId()).channelCode(domain.channelCode())
                .orderId(domain.orderId()).orderNo(domain.orderNo())
                .userId(domain.userId()).userType(domain.userType())
                .merchantOrderId(domain.merchantOrderId()).merchantRefundId(domain.merchantRefundId())
                .notifyUrl(domain.notifyUrl())
                .status(domain.status()).payPrice(domain.payPrice()).refundPrice(domain.refundPrice())
                .reason(domain.reason()).userIp(domain.userIp())
                .channelOrderNo(domain.channelOrderNo()).channelRefundNo(domain.channelRefundNo())
                .successTime(domain.successTime())
                .channelErrorCode(domain.channelErrorCode()).channelErrorMsg(domain.channelErrorMsg()).build();
    }
    @Override public PayRefund save(PayRefund refund) {
        if (refund.id() == null) { mapper.insert(toDO(refund)); return refund; }
        mapper.updateById(toDO(refund)); return refund;
    }
    @Override public PayRefund findById(Long id) { return toDomain(mapper.selectById(id)); }
    @Override public Optional<PayRefund> findByNo(String no) { return Optional.ofNullable(toDomain(mapper.selectByNo(no))); }
    @Override public Optional<PayRefund> findByAppIdAndNo(Long appId, String no) {
        return Optional.ofNullable(toDomain(mapper.selectByAppIdAndNo(appId, no)));
    }
    @Override public Optional<PayRefund> findByAppIdAndMerchantRefundId(Long appId, String merchantRefundId) {
        return Optional.ofNullable(toDomain(mapper.selectByAppIdAndMerchantRefundId(appId, merchantRefundId)));
    }
    @Override public Long countByAppId(Long appId) { return mapper.selectCountByAppId(appId); }
    @Override public Long countByAppIdAndOrderIdAndStatus(Long appId, Long orderId, Integer status) {
        return mapper.selectCountByAppIdAndOrderId(appId, orderId, status);
    }
    @Override public PageResult<PayRefund> findPage(Long appId, String channelCode, String merchantOrderId,
                                                     String merchantRefundId, Integer status, Integer pageNo, Integer pageSize) {
        var req = new com.develop.mvp.pk.module.pay.controller.admin.refund.vo.PayRefundPageReqVO();
        req.setAppId(appId); req.setChannelCode(channelCode); req.setMerchantOrderId(merchantOrderId);
        req.setMerchantRefundId(merchantRefundId); req.setStatus(status);
        if (pageNo != null) req.setPageNo(pageNo); if (pageSize != null) req.setPageSize(pageSize);
        PageResult<PayRefundDO> page = mapper.selectPage(req);
        return new PageResult<>(page.getList().stream().map(PayRefundRepositoryImpl::toDomain).collect(Collectors.toList()), page.getTotal());
    }
    @Override public List<PayRefund> findByStatus(Integer status) {
        return mapper.selectListByStatus(status).stream().map(PayRefundRepositoryImpl::toDomain).collect(Collectors.toList());
    }
}
