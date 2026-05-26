package com.develop.mvp.pk.module.pay.infrastructure.wallet;
// DDD 角色：钱包交易流水仓储实现 - AggregateRoot_Pay_Skill
import com.develop.mvp.pk.framework.common.pojo.PageParam;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.dal.dataobject.wallet.PayWalletTransactionDO;
import com.develop.mvp.pk.module.pay.dal.mysql.wallet.PayWalletTransactionMapper;
import com.develop.mvp.pk.module.pay.dal.redis.no.PayNoRedisDAO;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletTransaction;
import com.develop.mvp.pk.module.pay.domain.wallet.repository.PayWalletTransactionRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;
@Repository
public class PayWalletTransactionRepositoryImpl implements PayWalletTransactionRepository {
    private static final String WALLET_NO_PREFIX = "W";

    @Resource private PayWalletTransactionMapper mapper;
    @Resource private PayNoRedisDAO noRedisDAO;
    static PayWalletTransaction toDomain(PayWalletTransactionDO doObj) {
        if (doObj == null) return null;
        return new PayWalletTransaction(doObj.getId())
                .no(doObj.getNo()).walletId(doObj.getWalletId())
                .bizType(doObj.getBizType()).bizId(doObj.getBizId())
                .title(doObj.getTitle()).price(doObj.getPrice()).balance(doObj.getBalance())
                .creator(doObj.getCreator()).createTime(doObj.getCreateTime());
    }
    @Override public PayWalletTransaction save(PayWalletTransaction transaction) {
        PayWalletTransactionDO doObj = new PayWalletTransactionDO();
        doObj.setId(transaction.id());
        doObj.setNo(transaction.no() == null ? noRedisDAO.generate(WALLET_NO_PREFIX) : transaction.no()); doObj.setWalletId(transaction.walletId());
        doObj.setBizType(transaction.bizType()); doObj.setBizId(transaction.bizId());
        doObj.setTitle(transaction.title()); doObj.setPrice(transaction.price());
        doObj.setBalance(transaction.balance());
        if (transaction.id() == null) {
            mapper.insert(doObj);
            return toDomain(doObj);
        }
        mapper.updateById(doObj);
        return transaction;
    }
    @Override public Optional<PayWalletTransaction> findByNo(String no) {
        return Optional.ofNullable(toDomain(mapper.selectByNo(no)));
    }
    @Override public Optional<PayWalletTransaction> findByBiz(String bizId, Integer bizType) {
        return Optional.ofNullable(toDomain(mapper.selectByBiz(bizId, bizType)));
    }
    @Override public PageResult<PayWalletTransaction> findPage(Long walletId, Integer type, Integer pageNo, Integer pageSize, LocalDateTime[] createTime) {
        PageResult<PayWalletTransactionDO> page = mapper.selectPage(walletId, type,
                new PageParam().setPageNo(pageNo).setPageSize(pageSize), createTime);
        return new PageResult<>(page.getList().stream().map(PayWalletTransactionRepositoryImpl::toDomain).collect(Collectors.toList()), page.getTotal());
    }
    @Override public Integer sumPriceByType(Long walletId, Integer type, LocalDateTime[] createTime) {
        return mapper.selectPriceSum(walletId, type, createTime);
    }
}
