package com.develop.mvp.pk.module.pay.infrastructure.wallet;

import com.develop.mvp.pk.framework.common.util.date.DateUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RedisPayWalletLockTest {

    @Test
    void walletLockTimeoutIsOwnedByRedisWalletLock() {
        assertEquals(120 * DateUtils.SECOND_MILLIS, RedisPayWalletLock.UPDATE_TIMEOUT_MILLIS);
    }
}
