package com.develop.mvp.pk.module.iot.domain.command.valueobject;

import java.util.Objects;

public record IotCommandStatus(Integer code) {

    public static final int CREATED_CODE = 0;
    public static final int PENDING_CODE = 10;
    public static final int SENT_CODE = 20;
    public static final int ACKED_CODE = 30;
    public static final int FAILED_CODE = 40;
    public static final int TIMEOUT_CODE = 50;
    public static final int CANCELED_CODE = 60;

    public static IotCommandStatus of(Integer code) {
        return new IotCommandStatus(code);
    }

    public static IotCommandStatus created() {
        return of(CREATED_CODE);
    }

    public static IotCommandStatus pending() {
        return of(PENDING_CODE);
    }

    public static IotCommandStatus sent() {
        return of(SENT_CODE);
    }

    public static IotCommandStatus acked() {
        return of(ACKED_CODE);
    }

    public static IotCommandStatus failed() {
        return of(FAILED_CODE);
    }

    public static IotCommandStatus timeout() {
        return of(TIMEOUT_CODE);
    }

    public static IotCommandStatus canceled() {
        return of(CANCELED_CODE);
    }

    public boolean isFinalState() {
        return Objects.equals(code, ACKED_CODE) || Objects.equals(code, FAILED_CODE)
                || Objects.equals(code, TIMEOUT_CODE) || Objects.equals(code, CANCELED_CODE);
    }

}
