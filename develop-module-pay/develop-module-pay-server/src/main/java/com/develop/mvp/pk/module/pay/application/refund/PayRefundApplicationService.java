package com.develop.mvp.pk.module.pay.application.refund;
// DDD 角色：退款单应用服务 - AggregateRoot_Pay_Skill
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.domain.refund.PayRefund;
import com.develop.mvp.pk.module.pay.domain.refund.repository.PayRefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
@RequiredArgsConstructor
public class PayRefundApplicationService {
    private final PayRefundRepository repo;
    @Transactional public PayRefund create(PayRefund refund) { return repo.save(refund); }
    @Transactional public void updateStatus(Long id, Integer status, String channelRefundNo) {
        PayRefund refund = repo.findById(id);
        if (refund == null) throw new IllegalArgumentException("Refund not found: " + id);
        refund.status(status).channelRefundNo(channelRefundNo);
        repo.save(refund);
    }
    public PayRefund get(Long id) { return repo.findById(id); }
    public PayRefund getByNo(String no) { return repo.findByNo(no).orElse(null); }
    public PageResult<PayRefund> getPage(Long appId, String channelCode, String merchantOrderId,
                                          String merchantRefundId, Integer status, Integer pageNo, Integer pageSize) {
        return repo.findPage(appId, channelCode, merchantOrderId, merchantRefundId, status, pageNo, pageSize);
    }
    public Long countByAppId(Long appId) { return repo.countByAppId(appId); }
    public List<PayRefund> getListByStatus(Integer status) { return repo.findByStatus(status); }
}
