package com.develop.mvp.pk.module.pay.infrastructure.order;
// DDD 角色：支付订单仓储实现 - AggregateRoot_Pay_Skill
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.dal.dataobject.order.PayOrderDO;
import com.develop.mvp.pk.module.pay.dal.mysql.order.PayOrderMapper;
import com.develop.mvp.pk.module.pay.domain.order.PayOrder;
import com.develop.mvp.pk.module.pay.domain.order.repository.PayOrderRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Repository
public class PayOrderRepositoryImpl implements PayOrderRepository {
    @Resource private PayOrderMapper mapper;
    static PayOrder toDomain(PayOrderDO doObj) {
        if (doObj == null) return null;
        return new PayOrder(doObj.getId(), doObj.getNo())
                .appId(doObj.getAppId()).channelId(doObj.getChannelId()).channelCode(doObj.getChannelCode())
                .userId(doObj.getUserId()).userType(doObj.getUserType())
                .merchantOrderId(doObj.getMerchantOrderId()).subject(doObj.getSubject())
                .body(doObj.getBody()).notifyUrl(doObj.getNotifyUrl()).userIp(doObj.getUserIp())
                .price(doObj.getPrice()).channelFeeRate(doObj.getChannelFeeRate())
                .channelFeePrice(doObj.getChannelFeePrice()).status(doObj.getStatus())
                .expireTime(doObj.getExpireTime()).successTime(doObj.getSuccessTime())
                .extensionId(doObj.getExtensionId()).refundPrice(doObj.getRefundPrice())
                .channelUserId(doObj.getChannelUserId()).channelOrderNo(doObj.getChannelOrderNo());
    }
    static PayOrderDO toDO(PayOrder domain) {
        return PayOrderDO.builder().id(domain.id()).no(domain.no())
                .appId(domain.appId()).channelId(domain.channelId()).channelCode(domain.channelCode())
                .userId(domain.userId()).userType(domain.userType())
                .merchantOrderId(domain.merchantOrderId()).subject(domain.subject())
                .body(domain.body()).notifyUrl(domain.notifyUrl()).userIp(domain.userIp())
                .price(domain.price()).channelFeeRate(domain.channelFeeRate())
                .channelFeePrice(domain.channelFeePrice()).status(domain.status())
                .expireTime(domain.expireTime()).successTime(domain.successTime())
                .extensionId(domain.extensionId()).refundPrice(domain.refundPrice())
                .channelUserId(domain.channelUserId()).channelOrderNo(domain.channelOrderNo()).build();
    }
    @Override public PayOrder save(PayOrder order) {
        if (order.id() == null) { mapper.insert(toDO(order)); return order; }
        mapper.updateById(toDO(order)); return order;
    }
    @Override public PayOrder findById(Long id) { return toDomain(mapper.selectById(id)); }
    @Override public Optional<PayOrder> findByNo(String no) { return Optional.ofNullable(toDomain(mapper.selectByNo(no))); }
    @Override public Optional<PayOrder> findByMerchantOrderId(Long appId, String merchantOrderId) {
        return Optional.ofNullable(toDomain(mapper.selectByAppIdAndMerchantOrderId(appId, merchantOrderId)));
    }
    @Override public PageResult<PayOrder> findPage(Long appId, Long channelId, Integer status, Integer pageNo, Integer pageSize) {
        var req = new com.develop.mvp.pk.module.pay.controller.admin.order.vo.PayOrderPageReqVO();
        req.setAppId(appId); req.setChannelId(channelId); req.setStatus(status);
        if (pageNo != null) req.setPageNo(pageNo); if (pageSize != null) req.setPageSize(pageSize);
        PageResult<PayOrderDO> page = mapper.selectPage(req);
        return new PageResult<>(page.getList().stream().map(PayOrderRepositoryImpl::toDomain).collect(Collectors.toList()), page.getTotal());
    }
    @Override public Long countByAppId(Long appId) { return mapper.selectCountByAppId(appId); }
    @Override public List<PayOrder> findByStatusAndExpireTimeBefore(Integer status, LocalDateTime expireTime) {
        return mapper.selectListByStatusAndExpireTimeLt(status, expireTime).stream().map(PayOrderRepositoryImpl::toDomain).collect(Collectors.toList());
    }
    @Override public int updateByIdAndStatus(Long id, Integer status, PayOrder update) {
        PayOrderDO doObj = toDO(update);
        doObj.setId(id);
        return mapper.updateByIdAndStatus(id, status, doObj);
    }
}
