package com.develop.mvp.pk.module.member.api.point.remote;

import com.develop.mvp.pk.module.member.api.point.MemberPointApi;
import com.develop.mvp.pk.module.member.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = ApiConstants.NAME, contextId = "memberPointRemoteClient")
public interface MemberPointRemoteClient extends MemberPointApi {
}
