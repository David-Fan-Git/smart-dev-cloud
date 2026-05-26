package com.develop.mvp.pk.module.pay.domain.channel.valueobject;
// DDD 角色：渠道编码值对象 - AggregateRoot_Pay_Skill

import java.util.Objects;

public final class ChannelCode {

	private final String value;

	public ChannelCode(String value) {

		if (value == null || value.isBlank())
			throw new IllegalArgumentException("ChannelCode must not be blank");
		this.value = value;
	}

	public String value() {

		return value;
	}

	public boolean isWechat() {

		return value.startsWith("wx_");
	}

	public boolean isAlipay() {

		return value.startsWith("alipay_");
	}

	public boolean isWallet() {

		return "wallet".equals(value);
	}

	@Override
	public boolean equals(Object o) {

		return o instanceof ChannelCode c && value.equals(c.value);
	}

	@Override
	public int hashCode() {

		return Objects.hash(value);
	}

	@Override
	public String toString() {

		return value;
	}

}
