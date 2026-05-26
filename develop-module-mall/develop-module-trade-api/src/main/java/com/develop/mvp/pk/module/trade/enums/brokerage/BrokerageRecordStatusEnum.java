package com.develop.mvp.pk.module.trade.enums.brokerage;

import com.develop.mvp.pk.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 佣金记录状态枚举
 *
 * @author David
 */
@AllArgsConstructor
@Getter
public enum BrokerageRecordStatusEnum implements ArrayValuable<Integer> {

    WAIT_SETTLEMENT(0, "待结算"),
    SETTLEMENT(1, "已结算"),
    CANCEL(2, "已取消"),
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BrokerageRecordStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
