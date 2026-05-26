package com.develop.mvp.pk.module.report.domain.goview.valueobject;

import java.util.Objects;

public final class GoViewProjectId {
    private final Long value;

    private GoViewProjectId(Long value) {
        this.value = Objects.requireNonNull(value, "projectId不能为空");
    }

    public static GoViewProjectId of(Long value) { return new GoViewProjectId(value); }

    public Long value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GoViewProjectId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return "GoViewProjectId{" + value + '}'; }
}
