package com.develop.mvp.pk.module.pay.domain.order.repository;
// DDD 角色：支付订单仓储接口 - AggregateRoot_Pay_Skill
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.domain.order.PayOrder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
public interface PayOrderRepository {
    PayOrder save(PayOrder o);
    PayOrder findById(Long id);
    Optional<PayOrder> findByNo(String no);
    Optional<PayOrder> findByMerchantOrderId(Long appId, String merchantOrderId);
    PageResult<PayOrder> findPage(Long appId, Long channelId, Integer status, Integer pageNo, Integer pageSize);
    Long countByAppId(Long appId);
    List<PayOrder> findByStatusAndExpireTimeBefore(Integer status, LocalDateTime expireTime);
    int updateByIdAndStatus(Long id, Integer status, PayOrder update);
}
