package com.develop.mvp.pk.module.trade.infrastructure.tradeorder;

// Skill: AggregateRoot_TradeOrder_Validation_Skill — 仓储实现

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.trade.dal.dataobject.order.TradeOrderDO;
import com.develop.mvp.pk.module.trade.dal.dataobject.order.TradeOrderItemDO;
import com.develop.mvp.pk.module.trade.dal.mysql.order.TradeOrderMapper;
import com.develop.mvp.pk.module.trade.dal.mysql.order.TradeOrderItemMapper;
import com.develop.mvp.pk.module.trade.domain.tradeorder.TradeOrder;
import com.develop.mvp.pk.module.trade.domain.tradeorder.TradeOrderFactory;
import com.develop.mvp.pk.module.trade.domain.tradeorder.repository.TradeOrderRepository;
import com.develop.mvp.pk.module.trade.domain.tradeorder.valueobject.OrderItem;
import com.develop.mvp.pk.module.trade.domain.tradeorder.valueobject.OrderItemProperty;
import com.develop.mvp.pk.module.trade.domain.tradeorder.valueobject.TradeOrderId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class TradeOrderRepositoryImpl implements TradeOrderRepository {

    private final TradeOrderMapper tradeOrderMapper;
    private final TradeOrderItemMapper tradeOrderItemMapper;

    public TradeOrderRepositoryImpl(TradeOrderMapper tradeOrderMapper,
                                     TradeOrderItemMapper tradeOrderItemMapper) {
        this.tradeOrderMapper = tradeOrderMapper;
        this.tradeOrderItemMapper = tradeOrderItemMapper;
    }

    @Override
    @Transactional
    public TradeOrder save(TradeOrder order) {
        TradeOrderDO orderDO = toDataObject(order);
        TradeOrder persistedOrder;
        if (order.id() == null || tradeOrderMapper.selectById(order.id().value()) == null) {
            tradeOrderMapper.insert(orderDO);
            persistedOrder = toDomain(orderDO);
        } else {
            tradeOrderMapper.updateById(orderDO);
            persistedOrder = order;
        }
        for (OrderItem item : order.items()) {
            TradeOrderItemDO itemDO = toItemDataObject(item, persistedOrder.id().value());
            if (item.id() != null && tradeOrderItemMapper.selectById(item.id()) != null) {
                tradeOrderItemMapper.updateById(itemDO);
            } else {
                tradeOrderItemMapper.insert(itemDO);
            }
        }
        return persistedOrder;
    }

    @Override
    public TradeOrder findById(TradeOrderId id) {
        TradeOrderDO orderDO = tradeOrderMapper.selectById(id.value());
        return orderDO != null ? toDomain(orderDO) : null;
    }

    @Override
    public TradeOrder findByNo(String no) {
        TradeOrderDO orderDO = tradeOrderMapper.selectOne(TradeOrderDO::getNo, no);
        return orderDO != null ? toDomain(orderDO) : null;
    }

    @Override
    public List<TradeOrder> findByUserId(Long userId) {
        return tradeOrderMapper.selectList(TradeOrderDO::getUserId, userId).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<TradeOrder> findByStatus(Integer status) {
        return tradeOrderMapper.selectList(TradeOrderDO::getStatus, status).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public PageResult<TradeOrder> findPage(Long userId, Integer status, String no,
                                            LocalDateTime[] createTime, Integer pageNo, Integer pageSize) {
        var reqVO = new com.develop.mvp.pk.module.trade.controller.admin.order.vo.TradeOrderPageReqVO();
        reqVO.setUserId(userId);
        reqVO.setStatus(status);
        reqVO.setNo(no);
        reqVO.setCreateTime(createTime);
        reqVO.setPageNo(pageNo);
        reqVO.setPageSize(pageSize);
        PageResult<TradeOrderDO> doPage = tradeOrderMapper.selectPage(reqVO, (java.util.Set<Long>) null);
        return new PageResult<>(
                doPage.getList().stream().map(this::toDomain).collect(Collectors.toList()),
                doPage.getTotal());
    }

    @Override
    public long countByStatus(Integer status) {
        return tradeOrderMapper.selectCount(TradeOrderDO::getStatus, status);
    }

    @Override
    public long count() {
        return tradeOrderMapper.selectCount();
    }

    private TradeOrder toDomain(TradeOrderDO orderDO) {
        List<TradeOrderItemDO> itemDOs = tradeOrderItemMapper.selectListByOrderId(orderDO.getId());
        List<OrderItem> items = itemDOs.stream().map(i -> new OrderItem(
                i.getId(), i.getUserId(), i.getCartId(), i.getSpuId(), i.getSpuName(),
                i.getSkuId(), toDomainProperties(i.getProperties()), i.getPicUrl(), i.getCount(),
                i.getCommentStatus(), i.getPrice(), i.getDiscountPrice(), i.getDeliveryPrice(),
                i.getAdjustPrice(), i.getPayPrice(), i.getCouponPrice(), i.getPointPrice(),
                i.getUsePoint(), i.getGivePoint(), i.getVipPrice(),
                i.getAfterSaleId(), i.getAfterSaleStatus())).collect(Collectors.toList());

        return TradeOrderFactory.reconstitute(
                orderDO.getId(), orderDO.getNo(), orderDO.getType(), orderDO.getTerminal(),
                orderDO.getUserId(), orderDO.getUserIp(), orderDO.getUserRemark(),
                orderDO.getStatus(), orderDO.getProductCount(), orderDO.getFinishTime(),
                orderDO.getCancelTime(), orderDO.getCancelType(), orderDO.getRemark(),
                orderDO.getCommentStatus(), orderDO.getBrokerageUserId(),
                orderDO.getPayOrderId(), orderDO.getPayStatus(), orderDO.getPayTime(),
                orderDO.getPayChannelCode(), orderDO.getTotalPrice(), orderDO.getDiscountPrice(),
                orderDO.getDeliveryPrice(), orderDO.getAdjustPrice(), orderDO.getPayPrice(),
                orderDO.getDeliveryType(), orderDO.getLogisticsId(), orderDO.getLogisticsNo(),
                orderDO.getDeliveryTime(), orderDO.getReceiveTime(), orderDO.getReceiverName(),
                orderDO.getReceiverMobile(), orderDO.getReceiverAreaId(),
                orderDO.getReceiverDetailAddress(), orderDO.getPickUpStoreId(),
                orderDO.getPickUpVerifyCode(), orderDO.getRefundStatus(), orderDO.getRefundPrice(),
                orderDO.getCouponId(), orderDO.getCouponPrice(), orderDO.getUsePoint(),
                orderDO.getPointPrice(), orderDO.getGivePoint(), orderDO.getRefundPoint(),
                orderDO.getVipPrice(), orderDO.getGiveCouponTemplateCounts(),
                orderDO.getGiveCouponIds(), orderDO.getSeckillActivityId(),
                orderDO.getBargainActivityId(), orderDO.getBargainRecordId(),
                orderDO.getCombinationActivityId(), orderDO.getCombinationHeadId(),
                orderDO.getCombinationRecordId(), orderDO.getPointActivityId(), items);
    }

    private TradeOrderDO toDataObject(TradeOrder order) {
        TradeOrderDO orderDO = new TradeOrderDO();
        orderDO.setId(order.id() != null ? order.id().value() : null); orderDO.setNo(order.no());
        orderDO.setType(order.type()); orderDO.setTerminal(order.terminal());
        orderDO.setUserId(order.userId()); orderDO.setUserIp(order.userIp());
        orderDO.setUserRemark(order.userRemark()); orderDO.setStatus(order.status());
        orderDO.setProductCount(order.productCount()); orderDO.setFinishTime(order.finishTime());
        orderDO.setCancelTime(order.cancelTime()); orderDO.setCancelType(order.cancelType());
        orderDO.setRemark(order.remark()); orderDO.setCommentStatus(order.commentStatus());
        orderDO.setBrokerageUserId(order.brokerageUserId());
        orderDO.setPayOrderId(order.payOrderId()); orderDO.setPayStatus(order.payStatus());
        orderDO.setPayTime(order.payTime()); orderDO.setPayChannelCode(order.payChannelCode());
        orderDO.setTotalPrice(order.totalPrice()); orderDO.setDiscountPrice(order.discountPrice());
        orderDO.setDeliveryPrice(order.deliveryPrice()); orderDO.setAdjustPrice(order.adjustPrice());
        orderDO.setPayPrice(order.payPrice()); orderDO.setDeliveryType(order.deliveryType());
        orderDO.setLogisticsId(order.logisticsId()); orderDO.setLogisticsNo(order.logisticsNo());
        orderDO.setDeliveryTime(order.deliveryTime()); orderDO.setReceiveTime(order.receiveTime());
        orderDO.setReceiverName(order.receiverName()); orderDO.setReceiverMobile(order.receiverMobile());
        orderDO.setReceiverAreaId(order.receiverAreaId());
        orderDO.setReceiverDetailAddress(order.receiverDetailAddress());
        orderDO.setPickUpStoreId(order.pickUpStoreId());
        orderDO.setPickUpVerifyCode(order.pickUpVerifyCode());
        orderDO.setRefundStatus(order.refundStatus()); orderDO.setRefundPrice(order.refundPrice());
        orderDO.setCouponId(order.couponId()); orderDO.setCouponPrice(order.couponPrice());
        orderDO.setUsePoint(order.usePoint()); orderDO.setPointPrice(order.pointPrice());
        orderDO.setGivePoint(order.givePoint()); orderDO.setRefundPoint(order.refundPoint());
        orderDO.setVipPrice(order.vipPrice());
        orderDO.setGiveCouponTemplateCounts(order.giveCouponTemplateCounts());
        orderDO.setGiveCouponIds(order.giveCouponIds());
        orderDO.setSeckillActivityId(order.seckillActivityId());
        orderDO.setBargainActivityId(order.bargainActivityId());
        orderDO.setBargainRecordId(order.bargainRecordId());
        orderDO.setCombinationActivityId(order.combinationActivityId());
        orderDO.setCombinationHeadId(order.combinationHeadId());
        orderDO.setCombinationRecordId(order.combinationRecordId());
        orderDO.setPointActivityId(order.pointActivityId());
        return orderDO;
    }

    private TradeOrderItemDO toItemDataObject(OrderItem item, Long orderId) {
        TradeOrderItemDO itemDO = new TradeOrderItemDO();
        itemDO.setId(item.id()); itemDO.setOrderId(orderId);
        itemDO.setUserId(item.userId()); itemDO.setCartId(item.cartId());
        itemDO.setSpuId(item.spuId()); itemDO.setSpuName(item.spuName());
        itemDO.setSkuId(item.skuId()); itemDO.setCount(item.count());
        itemDO.setProperties(toDataObjectProperties(item.properties()));
        itemDO.setPicUrl(item.picUrl()); itemDO.setCommentStatus(item.commentStatus());
        itemDO.setPrice(item.price()); itemDO.setDiscountPrice(item.discountPrice());
        itemDO.setDeliveryPrice(item.deliveryPrice()); itemDO.setAdjustPrice(item.adjustPrice());
        itemDO.setPayPrice(item.payPrice()); itemDO.setCouponPrice(item.couponPrice());
        itemDO.setPointPrice(item.pointPrice()); itemDO.setUsePoint(item.usePoint());
        itemDO.setGivePoint(item.givePoint()); itemDO.setVipPrice(item.vipPrice());
        itemDO.setAfterSaleId(item.afterSaleId()); itemDO.setAfterSaleStatus(item.afterSaleStatus());
        return itemDO;
    }

    private List<OrderItemProperty> toDomainProperties(List<TradeOrderItemDO.Property> properties) {
        return properties == null ? List.of() : properties.stream()
                .map(property -> new OrderItemProperty(property.getPropertyId(), property.getPropertyName(),
                        property.getValueId(), property.getValueName()))
                .collect(Collectors.toList());
    }

    private List<TradeOrderItemDO.Property> toDataObjectProperties(List<OrderItemProperty> properties) {
        return properties == null ? List.of() : properties.stream()
                .map(property -> {
                    TradeOrderItemDO.Property dataObjectProperty = new TradeOrderItemDO.Property();
                    dataObjectProperty.setPropertyId(property.propertyId());
                    dataObjectProperty.setPropertyName(property.propertyName());
                    dataObjectProperty.setValueId(property.valueId());
                    dataObjectProperty.setValueName(property.valueName());
                    return dataObjectProperty;
                }).collect(Collectors.toList());
    }
}
