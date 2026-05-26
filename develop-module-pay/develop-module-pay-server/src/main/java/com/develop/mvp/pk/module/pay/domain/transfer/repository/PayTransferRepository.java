package com.develop.mvp.pk.module.pay.domain.transfer.repository;
// DDD 角色：转账单仓储接口 - AggregateRoot_Pay_Skill
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.domain.transfer.PayTransfer;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
public interface PayTransferRepository {
    PayTransfer save(PayTransfer transfer);
    PayTransfer findById(Long id);
    Optional<PayTransfer> findByNo(String no);
    Optional<PayTransfer> findByAppIdAndMerchantTransferId(Long appId, String merchantTransferId);
    Optional<PayTransfer> findByAppIdAndNo(Long appId, String no);
    PageResult<PayTransfer> findPage(String no, Long appId, String channelCode,
                                      String merchantTransferId, Integer status, Integer pageNo, Integer pageSize);
    List<PayTransfer> findByStatuses(Collection<Integer> statuses);
}
