package com.develop.mvp.pk.module.bpm.domain.definition;
// DDD 角色：BPM流程分类工厂 - AggregateRoot_Bpm_Skill
import com.develop.mvp.pk.module.bpm.domain.definition.valueobject.*;

public final class BpmCategoryFactory {
    private BpmCategoryFactory() {}
    public static BpmCategory create(Long id, String name, String code, Integer sort) {
        return new BpmCategory(CategoryId.of(id), CategoryName.of(name), CategoryCode.of(code), sort, CategoryStatus.ENABLED);
    }
    public static BpmCategory reconstitute(Long id, String name, String code, Integer sort, Integer status) {
        return new BpmCategory(CategoryId.of(id), CategoryName.of(name), CategoryCode.of(code), sort, CategoryStatus.of(status));
    }
}
