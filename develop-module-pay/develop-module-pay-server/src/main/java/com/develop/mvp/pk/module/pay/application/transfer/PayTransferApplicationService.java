package com.develop.mvp.pk.module.pay.application.transfer;
// DDD 角色：转账单应用服务 - AggregateRoot_Pay_Skill
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.domain.transfer.PayTransfer;
import com.develop.mvp.pk.module.pay.domain.transfer.repository.PayTransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collection;
import java.util.List;
@Service
@RequiredArgsConstructor
public class PayTransferApplicationService {
    private final PayTransferRepository repo;
    @Transactional public PayTransfer create(PayTransfer transfer) { return repo.save(transfer); }
    @Transactional public void updateStatus(Long id, Integer status) {
        PayTransfer transfer = repo.findById(id);
        if (transfer == null) throw new IllegalArgumentException("Transfer not found: " + id);
        transfer.status(status);
        repo.save(transfer);
    }
    public PayTransfer get(Long id) { return repo.findById(id); }
    public PayTransfer getByNo(String no) { return repo.findByNo(no).orElse(null); }
    public PageResult<PayTransfer> getPage(String no, Long appId, String channelCode,
                                            String merchantTransferId, Integer status, Integer pageNo, Integer pageSize) {
        return repo.findPage(no, appId, channelCode, merchantTransferId, status, pageNo, pageSize);
    }
    public List<PayTransfer> getListByStatuses(Collection<Integer> statuses) { return repo.findByStatuses(statuses); }
}
