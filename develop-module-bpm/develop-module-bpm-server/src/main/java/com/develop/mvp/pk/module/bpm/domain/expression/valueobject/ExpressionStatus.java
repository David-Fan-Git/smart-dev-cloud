package com.develop.mvp.pk.module.bpm.domain.expression.valueobject;
// DDD 角色：BPM表达式状态值对象 - AggregateRoot_Bpm_Skill
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import java.util.Objects;

public final class ExpressionStatus {
    public static final ExpressionStatus ENABLED = new ExpressionStatus(CommonStatusEnum.ENABLE.getStatus());
    public static final ExpressionStatus DISABLED = new ExpressionStatus(CommonStatusEnum.DISABLE.getStatus());
    private final Integer code;
    private ExpressionStatus(Integer code) { this.code = Objects.requireNonNull(code); }
    public static ExpressionStatus of(Integer code) {
        return CommonStatusEnum.ENABLE.getStatus().equals(code) ? ENABLED : DISABLED;
    }
    public boolean isEnabled() { return code.equals(CommonStatusEnum.ENABLE.getStatus()); }
    public Integer code() { return code; }
    @Override public boolean equals(Object o) { return o instanceof ExpressionStatus s && code.equals(s.code); }
    @Override public int hashCode() { return Objects.hash(code); }
}
