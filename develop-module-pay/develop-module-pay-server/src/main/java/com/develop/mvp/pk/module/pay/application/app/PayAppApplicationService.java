package com.develop.mvp.pk.module.pay.application.app;
// DDD 角色：支付应用应用服务 - AggregateRoot_Pay_Skill
import cn.hutool.core.collection.CollUtil;
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.application.order.PayOrderApplicationService;
import com.develop.mvp.pk.module.pay.application.refund.PayRefundApplicationService;
import com.develop.mvp.pk.module.pay.domain.app.PayApp;
import com.develop.mvp.pk.module.pay.domain.app.PayAppFactory;
import com.develop.mvp.pk.module.pay.domain.app.repository.PayAppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.pay.enums.ErrorCodeConstants.*;

@Service
@RequiredArgsConstructor
public class PayAppApplicationService {
    private final PayAppRepository repo;
    @Lazy
    private final PayOrderApplicationService orderApplicationService;
    @Lazy
    private final PayRefundApplicationService refundApplicationService;

    @Transactional public PayApp create(String name, String appKey) {
        return create(name, appKey, CommonStatusEnum.ENABLE.getStatus(), null, null, null, null);
    }

    @Transactional public PayApp create(String name, String appKey, Integer status, String remark,
                                        String orderNotifyUrl, String refundNotifyUrl, String transferNotifyUrl) {
        validateAppKeyUnique(null, appKey);
        PayApp app = PayAppFactory.create(null, name, appKey);
        app.status(status).remark(remark).orderNotifyUrl(orderNotifyUrl)
                .refundNotifyUrl(refundNotifyUrl).transferNotifyUrl(transferNotifyUrl);
        return repo.save(app);
    }

    @Transactional public void update(Long id, String name, String appKey, String remark,
                                       String orderNotifyUrl, String refundNotifyUrl, String transferNotifyUrl) {
        update(id, name, appKey, null, remark, orderNotifyUrl, refundNotifyUrl, transferNotifyUrl);
    }

    @Transactional public void update(Long id, String name, String appKey, Integer status, String remark,
                                       String orderNotifyUrl, String refundNotifyUrl, String transferNotifyUrl) {
        PayApp app = requireExisting(id);
        validateAppKeyUnique(id, appKey);
        app.name(name).appKey(appKey).remark(remark).orderNotifyUrl(orderNotifyUrl)
                .refundNotifyUrl(refundNotifyUrl).transferNotifyUrl(transferNotifyUrl);
        if (status != null) {
            app.status(status);
        }
        repo.save(app);
    }

    @Transactional public void updateStatus(Long id, Integer status) {
        PayApp app = requireExisting(id);
        app.status(status);
        repo.save(app);
    }

    @Transactional public void delete(Long id) {
        requireExisting(id);
        if (orderApplicationService.countByAppId(id) > 0) {
            throw exception(APP_EXIST_ORDER_CANT_DELETE);
        }
        if (refundApplicationService.countByAppId(id) > 0) {
            throw exception(APP_EXIST_REFUND_CANT_DELETE);
        }
        repo.deleteById(id);
    }

    public PayApp get(Long id) { return repo.findById(id); }
    public PayApp getByAppKey(String appKey) { return repo.findByAppKey(appKey).orElse(null); }
    public List<PayApp> getList() { return repo.findAll(); }
    public List<PayApp> getList(Collection<Long> ids) { return CollUtil.isEmpty(ids) ? Collections.emptyList() : repo.findByIds(ids); }
    public PageResult<PayApp> getPage(String name, String appKey, Integer status, Integer pageNo, Integer pageSize) {
        return repo.findPage(name, appKey, status, pageNo, pageSize);
    }

    public PayApp valid(Long appId) { return validatePayApp(repo.findById(appId)); }
    public PayApp valid(String appKey) { return validatePayApp(repo.findByAppKey(appKey).orElse(null)); }

    private PayApp requireExisting(Long id) {
        PayApp app = repo.findById(id);
        if (app == null) {
            throw exception(APP_NOT_FOUND);
        }
        return app;
    }

    private void validateAppKeyUnique(Long id, String appKey) {
        PayApp app = repo.findByAppKey(appKey).orElse(null);
        if (app == null) {
            return;
        }
        if (id == null || !app.id().equals(id)) {
            throw exception(APP_KEY_EXISTS);
        }
    }

    private PayApp validatePayApp(PayApp app) {
        if (app == null) {
            throw exception(APP_NOT_FOUND);
        }
        if (CommonStatusEnum.isDisable(app.status())) {
            throw exception(APP_IS_DISABLE);
        }
        return app;
    }
}
