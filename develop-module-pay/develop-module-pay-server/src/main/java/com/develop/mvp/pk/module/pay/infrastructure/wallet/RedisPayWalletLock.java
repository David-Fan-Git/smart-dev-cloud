package com.develop.mvp.pk.module.pay.infrastructure.wallet;

import com.develop.mvp.pk.framework.common.util.date.DateUtils;
import com.develop.mvp.pk.module.pay.dal.redis.wallet.PayWalletLockRedisDAO;
import com.develop.mvp.pk.module.pay.domain.wallet.service.PayWalletLock;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

@Component
public class RedisPayWalletLock implements PayWalletLock {

    public static final long UPDATE_TIMEOUT_MILLIS = 120 * DateUtils.SECOND_MILLIS;

    @Resource
    private PayWalletLockRedisDAO lockRedisDAO;

    @Override
    @SneakyThrows
    public <V> V lock(Long walletId, Callable<V> callable) {
        return lockRedisDAO.lock(walletId, UPDATE_TIMEOUT_MILLIS, callable);
    }
}
