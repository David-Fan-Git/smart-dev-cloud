package com.develop.mvp.pk.module.pay.application.transfer;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.domain.transfer.PayTransfer;
import com.develop.mvp.pk.module.pay.domain.transfer.repository.PayTransferRepository;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayTransferApplicationServiceTest {

    @Test
    void getPage_passesMerchantTransferId() {
        StubTransferRepository repository = new StubTransferRepository();
        PayTransferApplicationService applicationService = new PayTransferApplicationService(repository);

        applicationService.getPage("T202605230001", 1L, "wx_app", "MT202605230001", 10, 1, 10);

        assertEquals("MT202605230001", repository.merchantTransferId);
    }

    private static final class StubTransferRepository implements PayTransferRepository {
        private String merchantTransferId;

        @Override
        public PayTransfer save(PayTransfer transfer) { return transfer; }

        @Override
        public PayTransfer findById(Long id) { return null; }

        @Override
        public Optional<PayTransfer> findByNo(String no) { return Optional.empty(); }

        @Override
        public Optional<PayTransfer> findByAppIdAndMerchantTransferId(Long appId, String merchantTransferId) {
            return Optional.empty();
        }

        @Override
        public Optional<PayTransfer> findByAppIdAndNo(Long appId, String no) { return Optional.empty(); }

        @Override
        public PageResult<PayTransfer> findPage(String no, Long appId, String channelCode,
                                                String merchantTransferId, Integer status,
                                                Integer pageNo, Integer pageSize) {
            this.merchantTransferId = merchantTransferId;
            return PageResult.empty();
        }

        @Override
        public List<PayTransfer> findByStatuses(Collection<Integer> statuses) { return List.of(); }
    }
}
