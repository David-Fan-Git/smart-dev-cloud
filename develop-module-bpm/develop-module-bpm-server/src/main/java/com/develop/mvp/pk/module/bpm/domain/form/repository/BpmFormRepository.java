package com.develop.mvp.pk.module.bpm.domain.form.repository;
// DDD 角色：BPM表单仓储接口 - AggregateRoot_Bpm_Skill
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.bpm.domain.form.BpmForm;
import com.develop.mvp.pk.module.bpm.domain.form.valueobject.FormId;
import java.util.*;

public interface BpmFormRepository {
    void save(BpmForm form);
    void delete(FormId id);
    BpmForm findById(FormId id);
    List<BpmForm> findAll();
    List<BpmForm> findByIds(Collection<FormId> ids);
    PageResult<BpmForm> findPage(String name, Integer pageNo, Integer pageSize);
}
