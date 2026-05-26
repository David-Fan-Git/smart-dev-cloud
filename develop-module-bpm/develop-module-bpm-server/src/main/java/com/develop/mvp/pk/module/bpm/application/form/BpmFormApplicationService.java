package com.develop.mvp.pk.module.bpm.application.form;
// DDD 角色：BPM表单应用服务 - AggregateRoot_Bpm_Skill

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.bpm.domain.form.BpmForm;
import com.develop.mvp.pk.module.bpm.domain.form.BpmFormFactory;
import com.develop.mvp.pk.module.bpm.domain.form.event.FormDomainEvent;
import com.develop.mvp.pk.module.bpm.domain.form.repository.BpmFormRepository;
import com.develop.mvp.pk.module.bpm.domain.form.valueobject.FormId;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.bpm.enums.ErrorCodeConstants.FORM_NOT_EXISTS;

@Service
@RequiredArgsConstructor
public class BpmFormApplicationService {
    private final BpmFormRepository repo;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long create(String name, Integer status, String conf, List<String> fields, String remark) {
        BpmForm form = BpmFormFactory.create(null, name, conf, fields, remark);
        repo.save(form);
        publishEvents(form);
        return form.id().value();
    }

    @Transactional
    public void update(Long id, String name, Integer status, String conf, List<String> fields, String remark) {
        findExisting(id);
        BpmForm form = BpmFormFactory.reconstitute(id, name, status, conf, fields, remark);
        repo.save(form);
        publishEvents(form);
    }

    @Transactional
    public void delete(Long id) {
        findExisting(id);
        BpmForm form = BpmFormFactory.reconstitute(id, "", 0, null, null, null);
        form.markDeleted();
        repo.delete(form.id());
        publishEvents(form);
    }

    public BpmForm get(Long id) { return repo.findById(FormId.of(id)); }
    public List<BpmForm> getAll() { return repo.findAll(); }
    public PageResult<BpmForm> getPage(String name, Integer pageNo, Integer pageSize) {
        return repo.findPage(name, pageNo, pageSize);
    }

    private BpmForm findExisting(Long id) {
        BpmForm f = repo.findById(FormId.of(id));
        if (f == null) throw exception(FORM_NOT_EXISTS);
        return f;
    }

    private void publishEvents(BpmForm form) {
        for (FormDomainEvent event : form.pullEvents()) eventPublisher.publishEvent(event);
    }
}
