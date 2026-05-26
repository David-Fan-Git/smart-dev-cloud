package com.develop.mvp.pk.module.member.api.config.remote;

import com.develop.mvp.pk.module.member.api.config.MemberConfigApi;
import com.develop.mvp.pk.module.member.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = ApiConstants.NAME, contextId = "memberConfigRemoteClient")
public interface MemberConfigRemoteClient extends MemberConfigApi {
}
