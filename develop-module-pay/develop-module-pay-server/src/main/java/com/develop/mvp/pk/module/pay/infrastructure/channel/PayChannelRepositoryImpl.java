package com.develop.mvp.pk.module.pay.infrastructure.channel;
// DDD 角色：支付渠道仓储实现 - AggregateRoot_Pay_Skill

import com.develop.mvp.pk.module.pay.dal.dataobject.channel.PayChannelDO;
import com.develop.mvp.pk.module.pay.dal.mysql.channel.PayChannelMapper;
import com.develop.mvp.pk.module.pay.domain.channel.PayChannel;
import com.develop.mvp.pk.module.pay.domain.channel.repository.PayChannelRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class PayChannelRepositoryImpl implements PayChannelRepository {

	@Resource
	private PayChannelMapper mapper;

	static PayChannel toDomain(PayChannelDO doObj) {

		if (doObj == null)
			return null;
		return new PayChannel(doObj.getId(), doObj.getCode(), doObj.getAppId()).status(doObj.getStatus()).feeRate(doObj.getFeeRate()).remark(doObj.getRemark()).config(doObj.getConfig()).tenantId(doObj.getTenantId());
	}

	static PayChannelDO toDO(PayChannel domain) {

		PayChannelDO doObj = new PayChannelDO();
		doObj.setId(domain.id());
		doObj.setCode(domain.code());
		doObj.setAppId(domain.appId());
		doObj.setStatus(domain.status());
		doObj.setFeeRate(domain.feeRate());
		doObj.setRemark(domain.remark());
		doObj.setConfig((com.develop.mvp.pk.module.pay.framework.pay.core.client.PayClientConfig) domain.config());
		doObj.setTenantId(domain.tenantId());
		return doObj;
	}

	@Override
	public PayChannel save(PayChannel channel) {

		PayChannelDO doObj = toDO(channel);
		if (channel.id() == null) {
			mapper.insert(doObj);
			return toDomain(doObj);
		}
		mapper.updateById(doObj);
		return channel;
	}

	@Override
	public PayChannel findById(Long id) {

		return toDomain(mapper.selectById(id));
	}

	@Override
	public Optional<PayChannel> findByAppIdAndCode(Long appId, String code) {

		return Optional.ofNullable(toDomain(mapper.selectByAppIdAndCode(appId, code)));
	}

	@Override
	public List<PayChannel> findByAppIds(Collection<Long> appIds) {

		return mapper.selectListByAppIds(appIds).stream().map(PayChannelRepositoryImpl::toDomain).collect(Collectors.toList());
	}

	@Override
	public List<PayChannel> findEnabledByAppId(Long appId) {

		return mapper.selectListByAppId(appId, 0).stream().map(PayChannelRepositoryImpl::toDomain).collect(Collectors.toList());
	}

	@Override
	public void deleteById(Long id) {

		mapper.deleteById(id);
	}

}
