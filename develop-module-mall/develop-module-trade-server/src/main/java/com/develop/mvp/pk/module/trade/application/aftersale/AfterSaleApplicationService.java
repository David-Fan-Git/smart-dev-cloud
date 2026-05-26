package com.develop.mvp.pk.module.trade.application.aftersale;

// Skill: AggregateRoot_AfterSale_Validation_Skill

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.trade.domain.aftersale.AfterSale;
import com.develop.mvp.pk.module.trade.domain.aftersale.AfterSaleFactory;
import com.develop.mvp.pk.module.trade.domain.aftersale.repository.AfterSaleRepository;
import com.develop.mvp.pk.module.trade.domain.aftersale.valueobject.AfterSaleId;
import com.develop.mvp.pk.module.trade.domain.event.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.trade.enums.ErrorCodeConstants.*;

@Service
public class AfterSaleApplicationService {

    private final AfterSaleRepository afterSaleRepository;
    private final DomainEventPublisher eventPublisher;

    public AfterSaleApplicationService(AfterSaleRepository afterSaleRepository,
                                        DomainEventPublisher eventPublisher) {
        this.afterSaleRepository = afterSaleRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Long createAfterSale(Long id, String no, Long userId, Long orderId, Long orderItemId,
                                 Long spuId, Long skuId, Integer count, Integer type,
                                 String reason, String description, String[] proofPictures,
                                 Integer status, Integer refundPrice) {
        AfterSale afterSale = AfterSaleFactory.create(id, no, userId, orderId, orderItemId,
                spuId, skuId, count, type, reason, description, proofPictures, status, refundPrice);
        afterSale = afterSaleRepository.save(afterSale);
        return afterSale.id().value();
    }

    @Transactional
    public void approveAfterSale(Long id, String payChannelCode, LocalDateTime auditTime) {
        AfterSale afterSale = findExistingAfterSale(AfterSaleId.of(id));
        afterSale.approve(payChannelCode, auditTime);
        afterSaleRepository.save(afterSale);
    }

    @Transactional
    public void rejectAfterSale(Long id, String rejectReason, LocalDateTime refuseTime) {
        AfterSale afterSale = findExistingAfterSale(AfterSaleId.of(id));
        afterSale.reject(rejectReason, refuseTime);
        afterSaleRepository.save(afterSale);
    }

    @Transactional
    public void refundComplete(Long id, Long payRefundId, LocalDateTime refundTime) {
        AfterSale afterSale = findExistingAfterSale(AfterSaleId.of(id));
        afterSale.refundComplete(payRefundId, refundTime);
        afterSaleRepository.save(afterSale);
    }

    // ── 查询 ──

    public AfterSale getAfterSale(Long id) {
        return afterSaleRepository.findById(AfterSaleId.of(id));
    }

    public List<AfterSale> getByOrderId(Long orderId) {
        return afterSaleRepository.findByOrderId(orderId);
    }

    public PageResult<AfterSale> getAfterSalePage(Long userId, Integer status, Integer type, String no,
                                                   Integer pageNo, Integer pageSize) {
        return afterSaleRepository.findPage(userId, status, type, no, pageNo, pageSize);
    }

    private AfterSale findExistingAfterSale(AfterSaleId id) {
        AfterSale afterSale = afterSaleRepository.findById(id);
        if (afterSale == null) throw exception(AFTER_SALE_NOT_FOUND);
        return afterSale;
    }
}
