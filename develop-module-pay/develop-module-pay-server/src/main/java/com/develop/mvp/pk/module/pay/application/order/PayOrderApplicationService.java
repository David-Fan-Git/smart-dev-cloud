package com.develop.mvp.pk.module.pay.application.order;
// DDD 角色：支付订单应用服务 - AggregateRoot_Pay_Skill
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.domain.order.PayOrder;
import com.develop.mvp.pk.module.pay.domain.order.PayOrderFactory;
import com.develop.mvp.pk.module.pay.domain.order.repository.PayOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
public class PayOrderApplicationService {
    private final PayOrderRepository repo;
    @Transactional public PayOrder create(String no, Long appId, Long channelId, String merchantOrderId,
                                           String subject, Integer price, String channelCode) {
        PayOrder o = PayOrderFactory.create(null, no, appId, merchantOrderId, subject, price);
        o.channelId(channelId).channelCode(channelCode);
        return repo.save(o);
    }
    @Transactional public void update(Long id, Integer status, String channelOrderNo) {
        PayOrder o = repo.findById(id);
        if (o == null) throw new IllegalArgumentException("Order not found: " + id);
        o.status(status).channelOrderNo(channelOrderNo);
        repo.save(o);
    }
    @Transactional public void markPaid(Long id, String channelOrderNo, Long extensionId) {
        PayOrder o = repo.findById(id);
        if (o == null) throw new IllegalArgumentException("Order not found: " + id);
        o.markPaid();
        o.channelOrderNo(channelOrderNo).extensionId(extensionId).successTime(LocalDateTime.now());
        repo.save(o);
    }
    @Transactional public void markClosed(Long id) {
        PayOrder o = repo.findById(id);
        if (o == null) throw new IllegalArgumentException("Order not found: " + id);
        o.markClosed();
        repo.save(o);
    }
    @Transactional public void updateRefundPrice(Long id, Integer incrRefundPrice) {
        PayOrder o = repo.findById(id);
        if (o == null) throw new IllegalArgumentException("Order not found: " + id);
        o.markRefund(incrRefundPrice);
        repo.save(o);
    }
    public PayOrder get(Long id) { return repo.findById(id); }
    public PayOrder getByNo(String no) { return repo.findByNo(no).orElse(null); }
    public PayOrder getByMerchantOrderId(Long appId, String merchantOrderId) {
        return repo.findByMerchantOrderId(appId, merchantOrderId).orElse(null);
    }
    public Long countByAppId(Long appId) { return repo.countByAppId(appId); }
    public PageResult<PayOrder> getPage(Long appId, Long channelId, Integer status, Integer pageNo, Integer pageSize) {
        return repo.findPage(appId, channelId, status, pageNo, pageSize);
    }
    public List<PayOrder> getExpiredOrders(LocalDateTime expireTime) {
        return repo.findByStatusAndExpireTimeBefore(0, expireTime);
    }
}
