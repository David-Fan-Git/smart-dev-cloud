package com.develop.mvp.pk.module.iot.domain.command.valueobject;

public record IotCommandId(Long value) {

    public static IotCommandId of(Long value) {
        return new IotCommandId(value);
    }

}
