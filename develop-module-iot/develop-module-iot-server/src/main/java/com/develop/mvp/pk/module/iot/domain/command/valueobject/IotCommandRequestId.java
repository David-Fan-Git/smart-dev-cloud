package com.develop.mvp.pk.module.iot.domain.command.valueobject;

public record IotCommandRequestId(String value) {

    public static IotCommandRequestId of(String value) {
        return new IotCommandRequestId(value);
    }

}
