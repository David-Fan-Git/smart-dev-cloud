package com.develop.mvp.pk.module.pay.domain.wallet.service;

import java.util.concurrent.Callable;

public interface PayWalletLock {

    <V> V lock(Long walletId, Callable<V> callable);
}
