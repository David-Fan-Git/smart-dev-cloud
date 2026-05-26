package com.develop.mvp.pk.module.trade.application.aftersale;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.trade.domain.aftersale.AfterSale;
import com.develop.mvp.pk.module.trade.domain.aftersale.AfterSaleFactory;
import com.develop.mvp.pk.module.trade.domain.aftersale.repository.AfterSaleRepository;
import com.develop.mvp.pk.module.trade.domain.aftersale.valueobject.AfterSaleId;
import com.develop.mvp.pk.module.trade.domain.event.DomainEventPublisher;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AfterSaleApplicationServiceTest {

    @Test
    void createAfterSale_returnsPersistedId() {
        AfterSaleApplicationService applicationService = new AfterSaleApplicationService(new StubAfterSaleRepository(), event -> {});

        Long id = applicationService.createAfterSale(null, "AS202605230001", 1L, 2L, 3L,
                4L, 5L, 1, 10, "不想要了", "description", new String[0], 10, 100);

        assertEquals(100L, id);
    }

    private static final class StubAfterSaleRepository implements AfterSaleRepository {

        @Override
        public AfterSale save(AfterSale afterSale) {
            return AfterSaleFactory.reconstitute(100L, afterSale.no(), afterSale.userId(), afterSale.orderId(),
                    afterSale.orderItemId(), afterSale.spuId(), afterSale.skuId(), afterSale.count(),
                    afterSale.type(), afterSale.reason(), afterSale.description(), afterSale.proofPictures(),
                    afterSale.status(), afterSale.refundPrice(), afterSale.rejectReason(), afterSale.payChannelCode(),
                    afterSale.payRefundId(), afterSale.auditTime(), afterSale.refuseTime(), afterSale.refundTime());
        }

        @Override
        public AfterSale findById(AfterSaleId id) { return null; }

        @Override
        public AfterSale findByNo(String no) { return null; }

        @Override
        public List<AfterSale> findByUserId(Long userId) { return List.of(); }

        @Override
        public List<AfterSale> findByOrderId(Long orderId) { return List.of(); }

        @Override
        public PageResult<AfterSale> findPage(Long userId, Integer status, Integer type, String no,
                                              Integer pageNo, Integer pageSize) {
            return PageResult.empty();
        }

        @Override
        public long count() { return 0; }
    }
}
