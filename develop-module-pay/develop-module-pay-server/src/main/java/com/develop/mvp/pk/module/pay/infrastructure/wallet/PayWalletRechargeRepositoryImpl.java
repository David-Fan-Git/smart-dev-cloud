package com.develop.mvp.pk.module.pay.infrastructure.wallet;
// DDD 角色：钱包充值仓储实现 - AggregateRoot_Pay_Skill
import com.develop.mvp.pk.framework.common.pojo.PageParam;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.dal.dataobject.wallet.PayWalletRechargeDO;
import com.develop.mvp.pk.module.pay.dal.mysql.wallet.PayWalletRechargeMapper;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletRecharge;
import com.develop.mvp.pk.module.pay.domain.wallet.repository.PayWalletRechargeRepository;
import com.develop.mvp.pk.module.pay.enums.refund.PayRefundStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
@Repository
public class PayWalletRechargeRepositoryImpl implements PayWalletRechargeRepository {
    @Resource private PayWalletRechargeMapper mapper;
    static PayWalletRecharge toDomain(PayWalletRechargeDO doObj) {
        if (doObj == null) return null;
        PayWalletRecharge r = new PayWalletRecharge(doObj.getId());
        r.walletId(doObj.getWalletId()).totalPrice(doObj.getTotalPrice()).payPrice(doObj.getPayPrice())
                .bonusPrice(doObj.getBonusPrice()).packageId(doObj.getPackageId())
                .payStatus(doObj.getPayStatus()).payOrderId(doObj.getPayOrderId())
                .payChannelCode(doObj.getPayChannelCode()).payTime(doObj.getPayTime())
                .payRefundId(doObj.getPayRefundId()).createTime(doObj.getCreateTime())
                .refundTotalPrice(doObj.getRefundTotalPrice()).refundPayPrice(doObj.getRefundPayPrice())
                .refundBonusPrice(doObj.getRefundBonusPrice()).refundTime(doObj.getRefundTime())
                .refundStatus(doObj.getRefundStatus());
        return r;
    }
    @Override public PayWalletRecharge save(PayWalletRecharge recharge) {
        PayWalletRechargeDO doObj = toDO(recharge);
        if (recharge.id() == null) {
            mapper.insert(doObj);
            return toDomain(doObj);
        }
        mapper.updateById(doObj);
        return recharge;
    }
    @Override public PayWalletRecharge findById(Long id) { return toDomain(mapper.selectById(id)); }
    @Override public PageResult<PayWalletRecharge> findPage(Long walletId, Boolean payStatus, Integer pageNo, Integer pageSize) {
        PageResult<PayWalletRechargeDO> page = mapper.selectPage(
                new PageParam().setPageNo(pageNo).setPageSize(pageSize), walletId, payStatus);
        return new PageResult<>(page.getList().stream().map(PayWalletRechargeRepositoryImpl::toDomain).collect(Collectors.toList()), page.getTotal());
    }
    @Override public void updatePayOrderId(Long id, Long payOrderId) {
        mapper.updateById(new PayWalletRechargeDO().setId(id).setPayOrderId(payOrderId));
    }
    @Override public int markPaid(Long id, String payChannelCode) {
        return mapper.updateByIdAndPaid(id, false,
                new PayWalletRechargeDO().setId(id).setPayStatus(true).setPayTime(LocalDateTime.now())
                        .setPayChannelCode(payChannelCode));
    }
    @Override public void markRefundWaiting(Long id, Long payRefundId) {
        mapper.updateById(new PayWalletRechargeDO().setId(id).setPayRefundId(payRefundId)
                .setRefundStatus(PayRefundStatusEnum.WAITING.getStatus()));
    }
    @Override public int markRefundSuccess(Long id, LocalDateTime refundTime, Integer refundTotalPrice,
                                           Integer refundPayPrice, Integer refundBonusPrice) {
        return mapper.updateByIdAndRefunded(id, PayRefundStatusEnum.WAITING.getStatus(),
                new PayWalletRechargeDO().setId(id).setRefundStatus(PayRefundStatusEnum.SUCCESS.getStatus())
                        .setRefundTime(refundTime).setRefundTotalPrice(refundTotalPrice).setRefundPayPrice(refundPayPrice)
                        .setRefundBonusPrice(refundBonusPrice));
    }
    @Override public int markRefundFailure(Long id) {
        return mapper.updateByIdAndRefunded(id, PayRefundStatusEnum.WAITING.getStatus(),
                new PayWalletRechargeDO().setId(id).setRefundStatus(PayRefundStatusEnum.FAILURE.getStatus()));
    }
    @Override public int updateByIdAndPaid(Long id, boolean wherePayStatus, PayWalletRecharge recharge) {
        return mapper.updateByIdAndPaid(id, wherePayStatus, toDO(recharge));
    }
    @Override public int updateByIdAndRefunded(Long id, Integer whereRefundStatus, PayWalletRecharge recharge) {
        return mapper.updateByIdAndRefunded(id, whereRefundStatus, toDO(recharge));
    }
    private static PayWalletRechargeDO toDO(PayWalletRecharge recharge) {
        PayWalletRechargeDO doObj = new PayWalletRechargeDO();
        doObj.setId(recharge.id()); doObj.setWalletId(recharge.walletId());
        doObj.setTotalPrice(recharge.totalPrice()); doObj.setPayPrice(recharge.payPrice());
        doObj.setBonusPrice(recharge.bonusPrice()); doObj.setPackageId(recharge.packageId());
        doObj.setPayStatus(recharge.payStatus()); doObj.setPayOrderId(recharge.payOrderId());
        doObj.setPayChannelCode(recharge.payChannelCode()); doObj.setPayTime(recharge.payTime());
        doObj.setPayRefundId(recharge.payRefundId()); doObj.setRefundStatus(recharge.refundStatus());
        doObj.setRefundTime(recharge.refundTime()); doObj.setRefundTotalPrice(recharge.refundTotalPrice());
        doObj.setRefundPayPrice(recharge.refundPayPrice()); doObj.setRefundBonusPrice(recharge.refundBonusPrice());
        return doObj;
    }
}
