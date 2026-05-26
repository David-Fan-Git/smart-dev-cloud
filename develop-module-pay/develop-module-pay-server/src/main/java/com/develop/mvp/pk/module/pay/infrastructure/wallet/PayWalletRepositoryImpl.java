package com.develop.mvp.pk.module.pay.infrastructure.wallet;
// DDD 角色：钱包仓储实现 - AggregateRoot_Pay_Skill
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.dal.dataobject.wallet.PayWalletDO;
import com.develop.mvp.pk.module.pay.dal.mysql.wallet.PayWalletMapper;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWallet;
import com.develop.mvp.pk.module.pay.domain.wallet.repository.PayWalletRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.stream.Collectors;
@Repository
public class PayWalletRepositoryImpl implements PayWalletRepository {
    @Resource private PayWalletMapper mapper;
    static PayWallet toDomain(PayWalletDO doObj) {
        if (doObj == null) return null;
        return new PayWallet(doObj.getId(), doObj.getUserId())
                .userType(doObj.getUserType()).balance(doObj.getBalance())
                .freezePrice(doObj.getFreezePrice()).totalExpense(doObj.getTotalExpense())
                .totalRecharge(doObj.getTotalRecharge());
    }
    @Override public PayWallet save(PayWallet wallet) {
        PayWalletDO doObj = new PayWalletDO();
        doObj.setId(wallet.id());
        doObj.setUserId(wallet.userId()); doObj.setUserType(wallet.userType());
        doObj.setBalance(wallet.balance()); doObj.setFreezePrice(wallet.freezePrice());
        doObj.setTotalExpense(wallet.totalExpense()); doObj.setTotalRecharge(wallet.totalRecharge());
        if (wallet.id() == null) {
            mapper.insert(doObj);
            return toDomain(doObj);
        }
        mapper.updateById(doObj);
        return wallet;
    }
    @Override public PayWallet findById(Long id) { return toDomain(mapper.selectById(id)); }
    @Override public Optional<PayWallet> findByUserIdAndType(Long userId, Integer userType) {
        return Optional.ofNullable(toDomain(mapper.selectByUserIdAndType(userId, userType)));
    }
    @Override public PageResult<PayWallet> findPage(Long userId, Integer userType, Integer pageNo, Integer pageSize) {
        var req = new com.develop.mvp.pk.module.pay.controller.admin.wallet.vo.wallet.PayWalletPageReqVO();
        if (pageNo != null) req.setPageNo(pageNo); if (pageSize != null) req.setPageSize(pageSize);
        PageResult<PayWalletDO> page = mapper.selectPage(req);
        return new PageResult<>(page.getList().stream().map(PayWalletRepositoryImpl::toDomain).collect(Collectors.toList()), page.getTotal());
    }
    @Override public int updateBalance(Long id, int balanceDelta) { return mapper.updateWhenAdd(id, balanceDelta); }
    @Override public int updateWhenConsumption(Long id, Integer price) { return mapper.updateWhenConsumption(id, price); }
    @Override public int updateWhenConsumptionRefund(Long id, Integer price) { return mapper.updateWhenConsumptionRefund(id, price); }
    @Override public int updateWhenRecharge(Long id, Integer price) { return mapper.updateWhenRecharge(id, price); }
    @Override public int updateWhenAdd(Long id, Integer price) { return mapper.updateWhenAdd(id, price); }
    @Override public int freezePrice(Long id, Integer price) { return mapper.freezePrice(id, price); }
    @Override public int unFreezePrice(Long id, Integer price) { return mapper.unFreezePrice(id, price); }
    @Override public int updateWhenRechargeRefund(Long id, Integer price) { return mapper.updateWhenRechargeRefund(id, price); }
}
