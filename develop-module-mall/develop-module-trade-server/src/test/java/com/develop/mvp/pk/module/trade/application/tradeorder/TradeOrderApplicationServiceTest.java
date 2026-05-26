package com.develop.mvp.pk.module.trade.application.tradeorder;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.trade.domain.tradeorder.TradeOrder;
import com.develop.mvp.pk.module.trade.domain.tradeorder.TradeOrderFactory;
import com.develop.mvp.pk.module.trade.domain.tradeorder.repository.TradeOrderRepository;
import com.develop.mvp.pk.module.trade.domain.tradeorder.valueobject.TradeOrderId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TradeOrderApplicationServiceTest {

    @Test
    void createOrder_returnsPersistedOrder() {
        TradeOrderApplicationService applicationService = new TradeOrderApplicationService(new StubTradeOrderRepository(), event -> {});

        TradeOrder order = applicationService.createOrder(null, "NO202605230001", 1, 20, 1L,
                "127.0.0.1", null, 100, 0, 0, 0, 100, 1,
                "张三", "13800138000", 110000, "北京市朝阳区", List.of());

        assertEquals(100L, order.id().value());
    }

    private static final class StubTradeOrderRepository implements TradeOrderRepository {

        @Override
        public TradeOrder save(TradeOrder order) {
            return TradeOrderFactory.reconstitute(100L, order.no(), order.type(), order.terminal(), order.userId(),
                    order.userIp(), order.userRemark(), order.status(), order.productCount(), order.finishTime(),
                    order.cancelTime(), order.cancelType(), order.remark(), order.commentStatus(), order.brokerageUserId(),
                    order.payOrderId(), order.payStatus(), order.payTime(), order.payChannelCode(), order.totalPrice(),
                    order.discountPrice(), order.deliveryPrice(), order.adjustPrice(), order.payPrice(), order.deliveryType(),
                    order.logisticsId(), order.logisticsNo(), order.deliveryTime(), order.receiveTime(), order.receiverName(),
                    order.receiverMobile(), order.receiverAreaId(), order.receiverDetailAddress(), order.pickUpStoreId(),
                    order.pickUpVerifyCode(), order.refundStatus(), order.refundPrice(), order.couponId(), order.couponPrice(),
                    order.usePoint(), order.pointPrice(), order.givePoint(), order.refundPoint(), order.vipPrice(),
                    order.giveCouponTemplateCounts(), order.giveCouponIds(), order.seckillActivityId(), order.bargainActivityId(),
                    order.bargainRecordId(), order.combinationActivityId(), order.combinationHeadId(),
                    order.combinationRecordId(), order.pointActivityId(), order.items());
        }

        @Override
        public TradeOrder findById(TradeOrderId id) { return null; }

        @Override
        public TradeOrder findByNo(String no) { return null; }

        @Override
        public List<TradeOrder> findByUserId(Long userId) { return List.of(); }

        @Override
        public List<TradeOrder> findByStatus(Integer status) { return List.of(); }

        @Override
        public PageResult<TradeOrder> findPage(Long userId, Integer status, String no,
                                               LocalDateTime[] createTime, Integer pageNo, Integer pageSize) {
            return PageResult.empty();
        }

        @Override
        public long countByStatus(Integer status) { return 0; }

        @Override
        public long count() { return 0; }
    }
}
