package com.develop.mvp.pk.module.pay.domain.refund.repository;
// DDD 角色：退款单仓储接口 - AggregateRoot_Pay_Skill
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.domain.refund.PayRefund;
import java.util.List;
import java.util.Optional;
public interface PayRefundRepository {
    PayRefund save(PayRefund refund);
    PayRefund findById(Long id);
    Optional<PayRefund> findByNo(String no);
    Optional<PayRefund> findByAppIdAndNo(Long appId, String no);
    Optional<PayRefund> findByAppIdAndMerchantRefundId(Long appId, String merchantRefundId);
    Long countByAppId(Long appId);
    Long countByAppIdAndOrderIdAndStatus(Long appId, Long orderId, Integer status);
    PageResult<PayRefund> findPage(Long appId, String channelCode, String merchantOrderId,
                                    String merchantRefundId, Integer status, Integer pageNo, Integer pageSize);
    List<PayRefund> findByStatus(Integer status);
}
