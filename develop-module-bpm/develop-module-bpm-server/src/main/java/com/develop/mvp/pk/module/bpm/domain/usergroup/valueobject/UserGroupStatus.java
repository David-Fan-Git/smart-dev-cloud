package com.develop.mvp.pk.module.bpm.domain.usergroup.valueobject;
// DDD 角色：BPM用户组状态值对象 - AggregateRoot_Bpm_Skill
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import java.util.Objects;

public final class UserGroupStatus {
    public static final UserGroupStatus ENABLED = new UserGroupStatus(CommonStatusEnum.ENABLE.getStatus());
    public static final UserGroupStatus DISABLED = new UserGroupStatus(CommonStatusEnum.DISABLE.getStatus());
    private final Integer code;
    private UserGroupStatus(Integer code) { this.code = Objects.requireNonNull(code); }
    public static UserGroupStatus of(Integer code) {
        return CommonStatusEnum.ENABLE.getStatus().equals(code) ? ENABLED : DISABLED;
    }
    public boolean isEnabled() { return code.equals(CommonStatusEnum.ENABLE.getStatus()); }
    public Integer code() { return code; }
    @Override public boolean equals(Object o) { return o instanceof UserGroupStatus s && code.equals(s.code); }
    @Override public int hashCode() { return Objects.hash(code); }
}
