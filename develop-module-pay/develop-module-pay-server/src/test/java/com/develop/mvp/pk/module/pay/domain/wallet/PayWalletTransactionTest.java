package com.develop.mvp.pk.module.pay.domain.wallet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class PayWalletTransactionTest {

    @Test
    void create_allowsTransientId() {
        PayWalletTransaction transaction = new PayWalletTransaction(null);

        assertNull(transaction.id());
    }
}
