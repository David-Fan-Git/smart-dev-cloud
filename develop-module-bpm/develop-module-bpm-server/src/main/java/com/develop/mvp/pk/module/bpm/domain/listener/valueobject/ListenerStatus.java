package com.develop.mvp.pk.module.bpm.domain.listener.valueobject;
// DDD 角色：BPM监听器状态值对象 - AggregateRoot_Bpm_Skill
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import java.util.Objects;

public final class ListenerStatus {
    public static final ListenerStatus ENABLED = new ListenerStatus(CommonStatusEnum.ENABLE.getStatus());
    public static final ListenerStatus DISABLED = new ListenerStatus(CommonStatusEnum.DISABLE.getStatus());
    private final Integer code;
    private ListenerStatus(Integer code) { this.code = Objects.requireNonNull(code); }
    public static ListenerStatus of(Integer code) {
        return CommonStatusEnum.ENABLE.getStatus().equals(code) ? ENABLED : DISABLED;
    }
    public boolean isEnabled() { return code.equals(CommonStatusEnum.ENABLE.getStatus()); }
    public Integer code() { return code; }
    @Override public boolean equals(Object o) { return o instanceof ListenerStatus s && code.equals(s.code); }
    @Override public int hashCode() { return Objects.hash(code); }
}
