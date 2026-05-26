package com.develop.mvp.pk.module.pay.domain.channel.repository;
// DDD 角色：支付渠道仓储接口 - AggregateRoot_Pay_Skill
import com.develop.mvp.pk.module.pay.domain.channel.PayChannel;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
public interface PayChannelRepository {
    PayChannel save(PayChannel channel);
    PayChannel findById(Long id);
    Optional<PayChannel> findByAppIdAndCode(Long appId, String code);
    List<PayChannel> findByAppIds(Collection<Long> appIds);
    List<PayChannel> findEnabledByAppId(Long appId);
    void deleteById(Long id);
}
