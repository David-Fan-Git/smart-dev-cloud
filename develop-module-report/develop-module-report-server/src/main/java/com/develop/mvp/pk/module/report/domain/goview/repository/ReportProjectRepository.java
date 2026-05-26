package com.develop.mvp.pk.module.report.domain.goview.repository;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.report.domain.goview.ReportProject;
import com.develop.mvp.pk.module.report.domain.goview.valueobject.GoViewProjectId;

import java.util.List;

public interface ReportProjectRepository {
    ReportProject save(ReportProject p);
    void delete(GoViewProjectId id);
    ReportProject findById(GoViewProjectId id);
    List<ReportProject> findAll();
    PageResult<ReportProject> findPage(ReportProjectPageQuery query);
}
