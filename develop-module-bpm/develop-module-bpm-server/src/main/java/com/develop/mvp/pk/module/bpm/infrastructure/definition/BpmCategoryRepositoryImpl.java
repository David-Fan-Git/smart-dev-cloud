package com.develop.mvp.pk.module.bpm.infrastructure.definition;
// DDD 角色：BPM流程分类仓储实现 - AggregateRoot_Bpm_Skill

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.bpm.controller.admin.definition.vo.category.BpmCategoryPageReqVO;
import com.develop.mvp.pk.module.bpm.dal.dataobject.definition.BpmCategoryDO;
import com.develop.mvp.pk.module.bpm.dal.mysql.category.BpmCategoryMapper;
import com.develop.mvp.pk.module.bpm.domain.definition.BpmCategory;
import com.develop.mvp.pk.module.bpm.domain.definition.BpmCategoryFactory;
import com.develop.mvp.pk.module.bpm.domain.definition.repository.BpmCategoryRepository;
import com.develop.mvp.pk.module.bpm.domain.definition.valueobject.*;
import com.develop.mvp.pk.module.bpm.service.definition.BpmModelService;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class BpmCategoryRepositoryImpl implements BpmCategoryRepository {
    private final BpmCategoryMapper mapper;
    private final BpmModelService modelService;

    public BpmCategoryRepositoryImpl(BpmCategoryMapper mapper, BpmModelService modelService) {
        this.mapper = mapper;
        this.modelService = modelService;
    }

    @Override @Transactional
    public void save(BpmCategory category) {
        BpmCategoryDO d = toDO(category);
        if (mapper.selectById(category.id().value()) == null) mapper.insert(d);
        else mapper.updateById(d);
    }

    @Override @Transactional
    public void delete(CategoryId id) { mapper.deleteById(id.value()); }

    @Override
    public BpmCategory findById(CategoryId id) { return fromDO(mapper.selectById(id.value())); }

    @Override
    public Optional<BpmCategory> findByCode(CategoryCode code) {
        return Optional.ofNullable(fromDO(mapper.selectByCode(code.value())));
    }

    @Override
    public Optional<BpmCategory> findByName(CategoryName name) {
        return Optional.ofNullable(fromDO(mapper.selectByName(name.value())));
    }

    @Override
    public List<BpmCategory> findAll() {
        return mapper.selectList().stream().map(this::fromDO).collect(Collectors.toList());
    }

    @Override
    public List<BpmCategory> findByStatus(CategoryStatus status) {
        return mapper.selectListByStatus(status.code()).stream().map(this::fromDO).collect(Collectors.toList());
    }

    @Override
    public PageResult<BpmCategory> findPage(String name, String code, Integer status, Integer pageNo, Integer pageSize) {
        BpmCategoryPageReqVO req = new BpmCategoryPageReqVO().setName(name).setCode(code).setStatus(status);
        req.setPageNo(pageNo); req.setPageSize(pageSize);
        PageResult<BpmCategoryDO> page = mapper.selectPage(req);
        return new PageResult<>(page.getList().stream().map(this::fromDO).collect(Collectors.toList()), page.getTotal());
    }

    @Override
    public long getModelCountByCategory(String code) {
        return modelService.getModelCountByCategory(code);
    }

    private BpmCategoryDO toDO(BpmCategory c) {
        BpmCategoryDO d = new BpmCategoryDO();
        d.setId(c.id().value()); d.setName(c.name().value());
        d.setCode(c.code().value()); d.setSort(c.sort()); d.setStatus(c.status().code());
        return d;
    }

    private BpmCategory fromDO(BpmCategoryDO d) {
        if (d == null) return null;
        return BpmCategoryFactory.reconstitute(d.getId(), d.getName(), d.getCode(), d.getSort(), d.getStatus());
    }
}
