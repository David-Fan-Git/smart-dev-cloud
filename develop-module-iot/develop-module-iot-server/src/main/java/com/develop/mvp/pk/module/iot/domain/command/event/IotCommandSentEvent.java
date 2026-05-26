package com.develop.mvp.pk.module.iot.domain.command.event;

public record IotCommandSentEvent(Long deviceId, String requestId, String messageId, String serverId) {
}
