package com.develop.mvp.pk.module.system.api.social.remote;

import com.develop.mvp.pk.module.system.api.social.SocialUserApi;
import com.develop.mvp.pk.module.system.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = ApiConstants.NAME, contextId = "systemSocialUserRemoteClient")
public interface SocialUserRemoteClient extends SocialUserApi {
}
