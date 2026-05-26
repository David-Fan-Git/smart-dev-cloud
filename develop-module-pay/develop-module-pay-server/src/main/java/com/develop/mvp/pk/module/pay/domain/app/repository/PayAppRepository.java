package com.develop.mvp.pk.module.pay.domain.app.repository;
// DDD 角色：支付应用仓储接口 - AggregateRoot_Pay_Skill
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.domain.app.PayApp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
public interface PayAppRepository {
    PayApp save(PayApp app);
    PayApp findById(Long id);
    Optional<PayApp> findByAppKey(String appKey);
    List<PayApp> findByIds(Collection<Long> ids);
    List<PayApp> findAll();
    PageResult<PayApp> findPage(String name, String appKey, Integer status, Integer pageNo, Integer pageSize);
    void deleteById(Long id);
    boolean existsById(Long id);
}
