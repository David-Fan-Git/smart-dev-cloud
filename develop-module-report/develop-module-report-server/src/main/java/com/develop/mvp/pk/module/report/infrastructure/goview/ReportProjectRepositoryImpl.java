package com.develop.mvp.pk.module.report.infrastructure.goview;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.report.dal.dataobject.goview.GoViewProjectDO;
import com.develop.mvp.pk.module.report.dal.mysql.goview.GoViewProjectMapper;
import com.develop.mvp.pk.module.report.domain.goview.ReportProject;
import com.develop.mvp.pk.module.report.domain.goview.ReportProjectFactory;
import com.develop.mvp.pk.module.report.domain.goview.repository.ReportProjectPageQuery;
import com.develop.mvp.pk.module.report.domain.goview.repository.ReportProjectRepository;
import com.develop.mvp.pk.module.report.domain.goview.valueobject.GoViewProjectId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ReportProjectRepositoryImpl implements ReportProjectRepository {

    private final GoViewProjectMapper goViewProjectMapper;

    public ReportProjectRepositoryImpl(GoViewProjectMapper goViewProjectMapper) {
        this.goViewProjectMapper = goViewProjectMapper;
    }

    @Override
    public ReportProject save(ReportProject p) {
        GoViewProjectDO projectDO = toDataObject(p);
        if (p.id() != null && goViewProjectMapper.selectById(p.id().value()) != null) {
            goViewProjectMapper.updateById(projectDO);
        } else {
            goViewProjectMapper.insert(projectDO);
            // Update domain model with generated ID if new
        }
        return p;
    }

    @Override
    public void delete(GoViewProjectId id) {
        goViewProjectMapper.deleteById(id.value());
    }

    @Override
    public ReportProject findById(GoViewProjectId id) {
        GoViewProjectDO projectDO = goViewProjectMapper.selectById(id.value());
        return projectDO != null ? toDomain(projectDO) : null;
    }

    @Override
    public List<ReportProject> findAll() {
        return goViewProjectMapper.selectList().stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public PageResult<ReportProject> findPage(ReportProjectPageQuery query) {
        var pageParam = new com.develop.mvp.pk.framework.common.pojo.PageParam()
                .setPageNo(query.pageNo()).setPageSize(query.pageSize());
        PageResult<GoViewProjectDO> doPage = goViewProjectMapper.selectPage(pageParam, (Long) null);
        List<ReportProject> projects = doPage.getList().stream()
                .map(this::toDomain).collect(Collectors.toList());
        return new PageResult<>(projects, doPage.getTotal());
    }

    private GoViewProjectDO toDataObject(ReportProject p) {
        GoViewProjectDO projectDO = new GoViewProjectDO();
        if (p.id() != null) projectDO.setId(p.id().value());
        projectDO.setName(p.name());
        projectDO.setPicUrl(p.picUrl());
        projectDO.setContent(p.content());
        projectDO.setStatus(p.status().code());
        projectDO.setRemark(p.remark());
        return projectDO;
    }

    private ReportProject toDomain(GoViewProjectDO projectDO) {
        return ReportProjectFactory.reconstitute(
                projectDO.getId(),
                projectDO.getName(),
                projectDO.getPicUrl(),
                projectDO.getContent(),
                projectDO.getStatus(),
                projectDO.getRemark()
        );
    }
}
