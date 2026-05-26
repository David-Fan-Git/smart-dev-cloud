package com.develop.mvp.pk.module.report.domain.goview.valueobject;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import java.util.Objects;

public final class GoViewProjectStatus {

    public static final GoViewProjectStatus PUBLISHED = new GoViewProjectStatus(CommonStatusEnum.ENABLE.getStatus());
    public static final GoViewProjectStatus UNPUBLISHED = new GoViewProjectStatus(CommonStatusEnum.DISABLE.getStatus());

    private final Integer code;

    private GoViewProjectStatus(Integer code) {
        this.code = Objects.requireNonNull(code, "状态不能为空");
    }

    public static GoViewProjectStatus of(Integer code) {
        if (CommonStatusEnum.ENABLE.getStatus().equals(code)) return PUBLISHED;
        if (CommonStatusEnum.DISABLE.getStatus().equals(code)) return UNPUBLISHED;
        throw new IllegalArgumentException("无效的项目状态: " + code);
    }

    public GoViewProjectStatus publish() { return PUBLISHED; }
    public GoViewProjectStatus unpublish() { return UNPUBLISHED; }

    public boolean isPublished() { return code.equals(CommonStatusEnum.ENABLE.getStatus()); }
    public boolean isUnpublished() { return code.equals(CommonStatusEnum.DISABLE.getStatus()); }

    public Integer code() { return code; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GoViewProjectStatus that)) return false;
        return code.equals(that.code);
    }

    @Override
    public int hashCode() { return Objects.hash(code); }

    @Override
    public String toString() { return isPublished() ? "PUBLISHED" : "UNPUBLISHED"; }
}
