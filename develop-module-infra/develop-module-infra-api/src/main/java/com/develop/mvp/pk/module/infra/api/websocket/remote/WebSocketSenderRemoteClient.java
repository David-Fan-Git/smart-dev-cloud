package com.develop.mvp.pk.module.infra.api.websocket.remote;

import com.develop.mvp.pk.module.infra.api.websocket.WebSocketSenderApi;
import com.develop.mvp.pk.module.infra.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = ApiConstants.NAME, contextId = "infraWebSocketSenderRemoteClient")
public interface WebSocketSenderRemoteClient extends WebSocketSenderApi {
}
