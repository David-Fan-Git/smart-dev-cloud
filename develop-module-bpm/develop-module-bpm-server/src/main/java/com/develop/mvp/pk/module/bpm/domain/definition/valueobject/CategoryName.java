package com.develop.mvp.pk.module.bpm.domain.definition.valueobject;
// DDD 角色：BPM流程分类名称值对象 - AggregateRoot_Bpm_Skill
import com.develop.mvp.pk.framework.common.exception.ServiceException;
import static com.develop.mvp.pk.module.bpm.enums.ErrorCodeConstants.CATEGORY_NAME_DUPLICATE;
import java.util.Objects;

public final class CategoryName {
    private final String value;
    private CategoryName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        this.value = value.trim();
    }
    public static CategoryName of(String value) { return new CategoryName(value); }
    public String value() { return value; }
    @Override public boolean equals(Object o) { return o instanceof CategoryName c && value.equals(c.value); }
    @Override public int hashCode() { return Objects.hash(value); }
}
