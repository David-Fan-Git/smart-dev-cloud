package com.develop.mvp.pk.module.bpm.domain.definition.valueobject;
// DDD 角色：BPM流程分类状态值对象 - AggregateRoot_Bpm_Skill
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import java.util.Objects;

public final class CategoryStatus {
    public static final CategoryStatus ENABLED = new CategoryStatus(CommonStatusEnum.ENABLE.getStatus());
    public static final CategoryStatus DISABLED = new CategoryStatus(CommonStatusEnum.DISABLE.getStatus());
    private final Integer code;
    private CategoryStatus(Integer code) { this.code = Objects.requireNonNull(code); }
    public static CategoryStatus of(Integer code) {
        return CommonStatusEnum.ENABLE.getStatus().equals(code) ? ENABLED : DISABLED;
    }
    public boolean isEnabled() { return code.equals(CommonStatusEnum.ENABLE.getStatus()); }
    public Integer code() { return code; }
    public CategoryStatus disable() { return DISABLED; }
    @Override public boolean equals(Object o) { return o instanceof CategoryStatus s && code.equals(s.code); }
    @Override public int hashCode() { return Objects.hash(code); }
}
