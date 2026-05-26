package com.develop.mvp.pk.module.erp.domain.product.valueobject;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import java.util.Objects;

public final class ErpProductStatus {

    public static final ErpProductStatus ENABLED = new ErpProductStatus(CommonStatusEnum.ENABLE.getStatus());
    public static final ErpProductStatus DISABLED = new ErpProductStatus(CommonStatusEnum.DISABLE.getStatus());

    private final Integer code;

    private ErpProductStatus(Integer code) {
        this.code = Objects.requireNonNull(code, "状态不能为空");
    }

    public static ErpProductStatus of(Integer code) {
        if (CommonStatusEnum.ENABLE.getStatus().equals(code)) return ENABLED;
        if (CommonStatusEnum.DISABLE.getStatus().equals(code)) return DISABLED;
        throw new IllegalArgumentException("无效的产品状态: " + code);
    }

    public ErpProductStatus enable() { return ENABLED; }
    public ErpProductStatus disable() { return DISABLED; }

    public boolean isEnabled() { return code.equals(CommonStatusEnum.ENABLE.getStatus()); }
    public boolean isDisabled() { return code.equals(CommonStatusEnum.DISABLE.getStatus()); }

    public Integer code() { return code; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ErpProductStatus that)) return false;
        return code.equals(that.code);
    }

    @Override
    public int hashCode() { return Objects.hash(code); }

    @Override
    public String toString() { return isEnabled() ? "ENABLED" : "DISABLED"; }
}
