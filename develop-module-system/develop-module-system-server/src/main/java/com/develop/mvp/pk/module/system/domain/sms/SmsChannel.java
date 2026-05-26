package com.develop.mvp.pk.module.system.domain.sms;
// DDD 角色：短信渠道聚合根

import java.util.Objects;

/**
 * Sms Channel 领域模型。
 */
public final class SmsChannel {

	private final Long id;

	private final String code, signature;

	private Integer status;

	private String apiKey, apiSecret, callbackUrl, remark;

	/**
	 * 创建 SmsChannel 实例。
	 *
	 * @param id id 参数
	 * @param code code 参数
	 * @param signature signature 参数
	 */
	private SmsChannel(Long id, String code, String signature) {

		this.id = Objects.requireNonNull(id);
		this.code = Objects.requireNonNull(code);
		this.signature = Objects.requireNonNull(signature);
	}

	/**
	 * 执行 of 对应的业务操作。
	 *
	 * @param id id 参数
	 * @param code code 参数
	 * @param sig sig 参数
	 * @return 处理结果
	 */
	public static SmsChannel of(Long id, String code, String sig) {

		return new SmsChannel(id, code, sig);
	}

	/**
	 * 执行 id 对应的业务操作。
	 *
	 * @return 处理结果
	 */
	public Long id() {

		return id;
	}

	/**
	 * 执行 code 对应的业务操作。
	 *
	 * @return 处理结果
	 */
	public String code() {

		return code;
	}

	/**
	 * 执行 signature 对应的业务操作。
	 *
	 * @return 处理结果
	 */
	public String signature() {

		return signature;
	}

	/**
	 * 执行 status 对应的业务操作。
	 *
	 * @return 处理结果
	 */
	public Integer status() {

		return status;
	}

	/**
	 * 执行 api Key 对应的业务操作。
	 *
	 * @return 处理结果
	 */
	public String apiKey() {

		return apiKey;
	}

	/**
	 * 执行 api Secret 对应的业务操作。
	 *
	 * @return 处理结果
	 */
	public String apiSecret() {

		return apiSecret;
	}

	/**
	 * 执行 callback Url 对应的业务操作。
	 *
	 * @return 处理结果
	 */
	public String callbackUrl() {

		return callbackUrl;
	}

	/**
	 * 执行 remark 对应的业务操作。
	 *
	 * @return 处理结果
	 */
	public String remark() {

		return remark;
	}

	/**
	 * 执行 status 对应的业务操作。
	 *
	 * @param v v 参数
	 * @return 处理结果
	 */
	public SmsChannel status(Integer v) {

		status = v;
		return this;
	}

	/**
	 * 执行 api Key 对应的业务操作。
	 *
	 * @param v v 参数
	 * @return 处理结果
	 */
	public SmsChannel apiKey(String v) {

		apiKey = v;
		return this;
	}

	/**
	 * 执行 api Secret 对应的业务操作。
	 *
	 * @param v v 参数
	 * @return 处理结果
	 */
	public SmsChannel apiSecret(String v) {

		apiSecret = v;
		return this;
	}

	/**
	 * 执行 callback Url 对应的业务操作。
	 *
	 * @param v v 参数
	 * @return 处理结果
	 */
	public SmsChannel callbackUrl(String v) {

		callbackUrl = v;
		return this;
	}

	/**
	 * 执行 remark 对应的业务操作。
	 *
	 * @param v v 参数
	 * @return 处理结果
	 */
	public SmsChannel remark(String v) {

		remark = v;
		return this;
	}

	/**
	 * 执行 equals 对应的业务操作。
	 *
	 * @param o o 参数
	 * @return 处理结果
	 */
	@Override
	public boolean equals(Object o) {

		return o instanceof SmsChannel c && id.equals(c.id);
	}

	/**
	 * 判断 hash Code 对应的条件是否成立。
	 *
	 * @return 处理结果
	 */
	@Override
	public int hashCode() {

		return Objects.hash(id);
	}

}
