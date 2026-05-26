package com.develop.mvp.pk.module.member.api.user.remote;

import com.develop.mvp.pk.module.member.api.user.MemberUserApi;
import com.develop.mvp.pk.module.member.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = ApiConstants.NAME, contextId = "memberUserRemoteClient")
public interface MemberUserRemoteClient extends MemberUserApi {
}
