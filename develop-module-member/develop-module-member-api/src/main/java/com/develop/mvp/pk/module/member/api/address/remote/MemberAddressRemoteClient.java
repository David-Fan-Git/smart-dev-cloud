package com.develop.mvp.pk.module.member.api.address.remote;

import com.develop.mvp.pk.module.member.api.address.MemberAddressApi;
import com.develop.mvp.pk.module.member.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = ApiConstants.NAME, contextId = "memberAddressRemoteClient")
public interface MemberAddressRemoteClient extends MemberAddressApi {
}
