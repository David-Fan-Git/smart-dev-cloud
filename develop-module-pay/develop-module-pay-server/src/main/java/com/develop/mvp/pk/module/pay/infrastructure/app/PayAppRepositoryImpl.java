package com.develop.mvp.pk.module.pay.infrastructure.app;
// DDD 角色：支付应用仓储实现 - AggregateRoot_Pay_Skill

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.dal.dataobject.app.PayAppDO;
import com.develop.mvp.pk.module.pay.dal.mysql.app.PayAppMapper;
import com.develop.mvp.pk.module.pay.domain.app.PayApp;
import com.develop.mvp.pk.module.pay.domain.app.repository.PayAppRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class PayAppRepositoryImpl implements PayAppRepository {

	@Resource
	private PayAppMapper mapper;

	static PayApp toDomain(PayAppDO doObj) {

		if (doObj == null)
			return null;
		return new PayApp(doObj.getId(), doObj.getName()).appKey(doObj.getAppKey()).status(doObj.getStatus()).remark(doObj.getRemark()).orderNotifyUrl(doObj.getOrderNotifyUrl()).refundNotifyUrl(doObj.getRefundNotifyUrl()).transferNotifyUrl(doObj.getTransferNotifyUrl());
	}

	static PayAppDO toDO(PayApp domain) {

		return PayAppDO.builder().id(domain.id()).appKey(domain.appKey()).name(domain.name()).status(domain.status()).remark(domain.remark()).orderNotifyUrl(domain.orderNotifyUrl()).refundNotifyUrl(domain.refundNotifyUrl()).transferNotifyUrl(domain.transferNotifyUrl()).build();
	}

	@Override
	public PayApp save(PayApp app) {

		PayAppDO doObj = toDO(app);
		if (app.id() == null) {
			mapper.insert(doObj);
			return toDomain(doObj);
		}
		mapper.updateById(doObj);
		return app;
	}

	@Override
	public PayApp findById(Long id) {

		return toDomain(mapper.selectById(id));
	}

	@Override
	public Optional<PayApp> findByAppKey(String appKey) {

		return Optional.ofNullable(toDomain(mapper.selectByAppKey(appKey)));
	}

	@Override
	public List<PayApp> findByIds(Collection<Long> ids) {

		return mapper.selectByIds(ids).stream().map(PayAppRepositoryImpl::toDomain).collect(Collectors.toList());
	}

	@Override
	public List<PayApp> findAll() {

		return mapper.selectList().stream().map(PayAppRepositoryImpl::toDomain).collect(Collectors.toList());
	}

	@Override
	public PageResult<PayApp> findPage(String name, String appKey, Integer status, Integer pageNo, Integer pageSize) {

		var req = new com.develop.mvp.pk.module.pay.controller.admin.app.vo.PayAppPageReqVO();
		req.setName(name);
		req.setAppKey(appKey);
		req.setStatus(status);
		if (pageNo != null)
			req.setPageNo(pageNo);
		if (pageSize != null)
			req.setPageSize(pageSize);
		PageResult<PayAppDO> page = mapper.selectPage(req);
		return new PageResult<>(page.getList().stream().map(PayAppRepositoryImpl::toDomain).collect(Collectors.toList()), page.getTotal());
	}

	@Override
	public void deleteById(Long id) {

		mapper.deleteById(id);
	}

	@Override
	public boolean existsById(Long id) {

		return mapper.selectById(id) != null;
	}

}
