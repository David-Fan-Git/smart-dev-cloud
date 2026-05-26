package com.develop.mvp.pk.module.bpm.domain.form.valueobject;
// DDD 角色：BPM表单状态值对象 - AggregateRoot_Bpm_Skill
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import java.util.Objects;

public final class FormStatus {
    public static final FormStatus ENABLED = new FormStatus(CommonStatusEnum.ENABLE.getStatus());
    public static final FormStatus DISABLED = new FormStatus(CommonStatusEnum.DISABLE.getStatus());
    private final Integer code;
    private FormStatus(Integer code) { this.code = Objects.requireNonNull(code); }
    public static FormStatus of(Integer code) {
        return CommonStatusEnum.ENABLE.getStatus().equals(code) ? ENABLED : DISABLED;
    }
    public boolean isEnabled() { return code.equals(CommonStatusEnum.ENABLE.getStatus()); }
    public Integer code() { return code; }
    @Override public boolean equals(Object o) { return o instanceof FormStatus s && code.equals(s.code); }
    @Override public int hashCode() { return Objects.hash(code); }
}
