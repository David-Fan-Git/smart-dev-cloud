package com.develop.mvp.pk.module.system.infrastructure.sms.persistence;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.controller.admin.sms.vo.channel.SmsChannelPageReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.sms.SmsChannelDO;
import com.develop.mvp.pk.module.system.dal.mysql.sms.SmsChannelMapper;
import com.develop.mvp.pk.module.system.domain.sms.SmsChannel;
import com.develop.mvp.pk.module.system.domain.sms.repository.SmsChannelRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Sms Channel Repository Impl 领域仓储实现。
 */
@Repository
public class SmsChannelRepositoryImpl implements SmsChannelRepository {

	private final SmsChannelMapper mapper;

	/**
	 * 创建 SmsChannelRepositoryImpl 实例。
	 *
	 * @param mapper mapper 参数
	 */
	public SmsChannelRepositoryImpl(SmsChannelMapper mapper) {

		this.mapper = mapper;
	}

	/**
	 * 创建 save 对应的数据。
	 *
	 * @param c c 参数
	 * @return 处理结果
	 */
	@Override
	public SmsChannel save(SmsChannel c) {

		SmsChannelDO d = new SmsChannelDO();
		d.setId(c.id());
		d.setCode(c.code());
		d.setSignature(c.signature());
		d.setStatus(c.status());
		d.setApiKey(c.apiKey());
		d.setApiSecret(c.apiSecret());
		d.setCallbackUrl(c.callbackUrl());
		d.setRemark(c.remark());
		if (mapper.selectById(c.id()) == null)
			mapper.insert(d);
		else
			mapper.updateById(d);
		return c;
	}

	/**
	 * 删除 delete 对应的数据。
	 *
	 * @param id id 参数
	 */
	@Override
	public void delete(Long id) {

		mapper.deleteById(id);
	}

	/**
	 * 查询 find By Id 对应的数据。
	 *
	 * @param id id 参数
	 * @return 处理结果
	 */
	@Override
	public SmsChannel findById(Long id) {

		SmsChannelDO d = mapper.selectById(id);
		return d != null ? toDomain(d) : null;
	}

	/**
	 * 查询 find By Code 对应的数据。
	 *
	 * @param code code 参数
	 * @return 处理结果
	 */
	@Override
	public SmsChannel findByCode(String code) {

		SmsChannelDO d = mapper.selectByCode(code);
		return d != null ? toDomain(d) : null;
	}

	/**
	 * 查询 find All 对应的数据。
	 *
	 * @return 处理结果
	 */
	@Override
	public List<SmsChannel> findAll() {

		return mapper.selectList().stream().map(this::toDomain).toList();
	}

	/**
	 * 查询 find Page 对应的数据。
	 *
	 * @param signature signature 参数
	 * @param status status 参数
	 * @param pageNo pageNo 参数
	 * @param pageSize pageSize 参数
	 * @return 处理结果
	 */
	@Override
	public PageResult<SmsChannel> findPage(String signature, Integer status, Integer pageNo, Integer pageSize) {

		var reqVO = new SmsChannelPageReqVO();
		reqVO.setSignature(signature);
		reqVO.setStatus(status);
		reqVO.setPageNo(pageNo);
		reqVO.setPageSize(pageSize);
		var dp = mapper.selectPage(reqVO);
		return new PageResult<>(dp.getList().stream().map(this::toDomain).toList(), dp.getTotal());
	}

	/**
	 * 执行 to Domain 对应的业务操作。
	 *
	 * @param d d 参数
	 * @return 处理结果
	 */
	private SmsChannel toDomain(SmsChannelDO d) {

		return SmsChannel.of(d.getId(), d.getCode(), d.getSignature()).status(d.getStatus()).apiKey(d.getApiKey()).apiSecret(d.getApiSecret()).callbackUrl(d.getCallbackUrl()).remark(d.getRemark());
	}

}
