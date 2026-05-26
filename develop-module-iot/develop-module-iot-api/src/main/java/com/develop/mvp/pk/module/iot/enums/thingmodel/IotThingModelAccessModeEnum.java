package com.develop.mvp.pk.module.iot.enums.thingmodel;

import com.develop.mvp.pk.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IoT 产品物模型属性读取类型枚举
 *
 * @author David
 */
@AllArgsConstructor
@Getter
public enum IotThingModelAccessModeEnum implements ArrayValuable<String> {

    READ_ONLY("r"),
    READ_WRITE("rw");

    public static final String[] ARRAYS = Arrays.stream(values()).map(IotThingModelAccessModeEnum::getMode).toArray(String[]::new);

    private final String mode;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
