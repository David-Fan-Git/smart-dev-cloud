package com.develop.mvp.pk.module.iot.domain.command.valueobject;

public record IotCommandPayload(Object value) {

    public static IotCommandPayload of(Object value) {
        return new IotCommandPayload(value);
    }

}
