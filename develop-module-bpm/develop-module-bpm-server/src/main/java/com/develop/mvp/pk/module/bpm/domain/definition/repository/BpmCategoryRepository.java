package com.develop.mvp.pk.module.bpm.domain.definition.repository;
// DDD 角色：BPM流程分类仓储接口 - AggregateRoot_Bpm_Skill
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.bpm.domain.definition.BpmCategory;
import com.develop.mvp.pk.module.bpm.domain.definition.valueobject.*;
import java.util.*;

public interface BpmCategoryRepository {
    void save(BpmCategory c);
    void delete(CategoryId id);
    BpmCategory findById(CategoryId id);
    Optional<BpmCategory> findByCode(CategoryCode code);
    Optional<BpmCategory> findByName(CategoryName name);
    List<BpmCategory> findAll();
    List<BpmCategory> findByStatus(CategoryStatus status);
    PageResult<BpmCategory> findPage(String name, String code, Integer status, Integer pageNo, Integer pageSize);
    long getModelCountByCategory(String code);
}
