package com.develop.mvp.pk.module.member.api.level.remote;

import com.develop.mvp.pk.module.member.api.level.MemberLevelApi;
import com.develop.mvp.pk.module.member.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = ApiConstants.NAME, contextId = "memberLevelRemoteClient")
public interface MemberLevelRemoteClient extends MemberLevelApi {
}
