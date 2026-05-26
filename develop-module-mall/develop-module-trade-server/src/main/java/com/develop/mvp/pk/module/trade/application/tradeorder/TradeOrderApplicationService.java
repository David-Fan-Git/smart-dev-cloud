package com.develop.mvp.pk.module.trade.application.tradeorder;

// Skill: AggregateRoot_TradeOrder_Validation_Skill — 应用服务 TradeOrderApplicationService

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.trade.domain.tradeorder.TradeOrder;
import com.develop.mvp.pk.module.trade.domain.tradeorder.TradeOrderFactory;
import com.develop.mvp.pk.module.trade.domain.tradeorder.repository.TradeOrderRepository;
import com.develop.mvp.pk.module.trade.domain.tradeorder.valueobject.TradeOrderId;
import com.develop.mvp.pk.module.trade.domain.event.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.trade.enums.ErrorCodeConstants.*;

@Service
public class TradeOrderApplicationService {

    private final TradeOrderRepository tradeOrderRepository;
    private final DomainEventPublisher eventPublisher;

    public TradeOrderApplicationService(TradeOrderRepository tradeOrderRepository,
                                         DomainEventPublisher eventPublisher) {
        this.tradeOrderRepository = tradeOrderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TradeOrder createOrder(Long id, String no, Integer type, Integer terminal, Long userId,
                                   String userIp, String userRemark,
                                   Integer totalPrice, Integer discountPrice, Integer deliveryPrice,
                                   Integer adjustPrice, Integer payPrice,
                                   Integer deliveryType, String receiverName, String receiverMobile,
                                   Integer receiverAreaId, String receiverDetailAddress,
                                   List<com.develop.mvp.pk.module.trade.domain.tradeorder.valueobject.OrderItem> items) {
        TradeOrder order = TradeOrderFactory.create(id, no, type, terminal, userId, userIp,
                userRemark, totalPrice, discountPrice, deliveryPrice, adjustPrice, payPrice,
                deliveryType, receiverName, receiverMobile, receiverAreaId, receiverDetailAddress, items);
        order = tradeOrderRepository.save(order);
        publishEvents(order);
        return order;
    }

    @Transactional
    public void payOrder(Long id, Long payOrderId, String payChannelCode, LocalDateTime payTime) {
        TradeOrder order = findExistingOrder(TradeOrderId.of(id));
        order.paySuccess(payOrderId, payChannelCode, payTime);
        tradeOrderRepository.save(order);
        publishEvents(order);
    }

    @Transactional
    public void deliverOrder(Long id, Long logisticsId, String logisticsNo, LocalDateTime deliveryTime) {
        TradeOrder order = findExistingOrder(TradeOrderId.of(id));
        order.deliver(logisticsId, logisticsNo, deliveryTime);
        tradeOrderRepository.save(order);
        publishEvents(order);
    }

    @Transactional
    public void receiveOrder(Long id, LocalDateTime receiveTime) {
        TradeOrder order = findExistingOrder(TradeOrderId.of(id));
        order.receive(receiveTime);
        tradeOrderRepository.save(order);
        publishEvents(order);
    }

    @Transactional
    public void cancelOrder(Long id, Integer cancelType, LocalDateTime cancelTime) {
        TradeOrder order = findExistingOrder(TradeOrderId.of(id));
        order.cancel(cancelType, cancelTime);
        tradeOrderRepository.save(order);
        publishEvents(order);
    }

    @Transactional
    public void updateReceiver(Long id, String receiverName, String receiverMobile,
                                Integer receiverAreaId, String receiverDetailAddress) {
        TradeOrder order = findExistingOrder(TradeOrderId.of(id));
        order.updateReceiver(receiverName, receiverMobile, receiverAreaId, receiverDetailAddress);
        tradeOrderRepository.save(order);
        publishEvents(order);
    }

    @Transactional
    public void updateRemark(Long id, String remark) {
        TradeOrder order = findExistingOrder(TradeOrderId.of(id));
        order.updateRemark(remark);
        tradeOrderRepository.save(order);
    }

    // ── 查询 ──

    public TradeOrder getOrder(Long id) {
        return tradeOrderRepository.findById(TradeOrderId.of(id));
    }

    public TradeOrder getOrderByNo(String no) {
        return tradeOrderRepository.findByNo(no);
    }

    public List<TradeOrder> getUserOrders(Long userId) {
        return tradeOrderRepository.findByUserId(userId);
    }

    public PageResult<TradeOrder> getOrderPage(Long userId, Integer status, String no,
                                                LocalDateTime[] createTime, Integer pageNo, Integer pageSize) {
        return tradeOrderRepository.findPage(userId, status, no, createTime, pageNo, pageSize);
    }

    // ── 私有方法 ──

    private TradeOrder findExistingOrder(TradeOrderId id) {
        TradeOrder order = tradeOrderRepository.findById(id);
        if (order == null) throw exception(ORDER_NOT_FOUND);
        return order;
    }

    private void publishEvents(TradeOrder order) {
        for (var event : order.pullEvents()) {
            eventPublisher.publish(event);
        }
    }
}
