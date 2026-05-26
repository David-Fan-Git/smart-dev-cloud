package com.develop.mvp.pk.module.bpm.infrastructure.form;
// DDD 角色：BPM表单仓储实现 - AggregateRoot_Bpm_Skill

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.bpm.controller.admin.definition.vo.form.BpmFormPageReqVO;
import com.develop.mvp.pk.module.bpm.dal.dataobject.definition.BpmFormDO;
import com.develop.mvp.pk.module.bpm.dal.mysql.definition.BpmFormMapper;
import com.develop.mvp.pk.module.bpm.domain.form.BpmForm;
import com.develop.mvp.pk.module.bpm.domain.form.BpmFormFactory;
import com.develop.mvp.pk.module.bpm.domain.form.repository.BpmFormRepository;
import com.develop.mvp.pk.module.bpm.domain.form.valueobject.FormId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class BpmFormRepositoryImpl implements BpmFormRepository {
    private final BpmFormMapper mapper;
    public BpmFormRepositoryImpl(BpmFormMapper mapper) { this.mapper = mapper; }

    @Override @Transactional
    public void save(BpmForm form) {
        BpmFormDO d = toDO(form);
        if (mapper.selectById(form.id().value()) == null) mapper.insert(d);
        else mapper.updateById(d);
    }

    @Override @Transactional
    public void delete(FormId id) { mapper.deleteById(id.value()); }

    @Override
    public BpmForm findById(FormId id) { return fromDO(mapper.selectById(id.value())); }

    @Override
    public List<BpmForm> findAll() {
        return mapper.selectList().stream().map(this::fromDO).collect(Collectors.toList());
    }

    @Override
    public List<BpmForm> findByIds(Collection<FormId> ids) {
        if (ids.isEmpty()) return Collections.emptyList();
        return mapper.selectBatchIds(ids.stream().map(FormId::value).toList())
                .stream().map(this::fromDO).collect(Collectors.toList());
    }

    @Override
    public PageResult<BpmForm> findPage(String name, Integer pageNo, Integer pageSize) {
        BpmFormPageReqVO req = new BpmFormPageReqVO().setName(name);
        req.setPageNo(pageNo); req.setPageSize(pageSize);
        PageResult<BpmFormDO> page = mapper.selectPage(req);
        return new PageResult<>(page.getList().stream().map(this::fromDO).collect(Collectors.toList()), page.getTotal());
    }

    private BpmFormDO toDO(BpmForm f) {
        BpmFormDO d = new BpmFormDO();
        d.setId(f.id().value()); d.setName(f.name().value());
        d.setStatus(f.status().code()); d.setConf(f.conf());
        d.setFields(f.fields()); d.setRemark(f.remark());
        return d;
    }

    private BpmForm fromDO(BpmFormDO d) {
        if (d == null) return null;
        return BpmFormFactory.reconstitute(d.getId(), d.getName(), d.getStatus(), d.getConf(), d.getFields(), d.getRemark());
    }
}
