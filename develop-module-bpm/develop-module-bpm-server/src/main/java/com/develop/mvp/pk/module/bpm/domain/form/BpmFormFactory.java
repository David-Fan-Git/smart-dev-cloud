package com.develop.mvp.pk.module.bpm.domain.form;
// DDD 角色：BPM表单工厂 - AggregateRoot_Bpm_Skill
import com.develop.mvp.pk.module.bpm.domain.form.valueobject.*;
import java.util.List;

public final class BpmFormFactory {
    private BpmFormFactory() {}
    public static BpmForm create(Long id, String name, String conf, List<String> fields, String remark) {
        return new BpmForm(FormId.of(id), FormName.of(name), FormStatus.ENABLED, conf, fields, remark);
    }
    public static BpmForm reconstitute(Long id, String name, Integer status, String conf, List<String> fields, String remark) {
        return new BpmForm(FormId.of(id), FormName.of(name), FormStatus.of(status), conf, fields, remark);
    }
}
