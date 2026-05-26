package com.develop.mvp.pk.module.report.application.goview;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.report.domain.goview.ReportProject;
import com.develop.mvp.pk.module.report.domain.goview.ReportProjectFactory;
import com.develop.mvp.pk.module.report.domain.goview.event.DomainEvent;
import com.develop.mvp.pk.module.report.domain.goview.event.DomainEventPublisher;
import com.develop.mvp.pk.module.report.domain.goview.repository.ReportProjectPageQuery;
import com.develop.mvp.pk.module.report.domain.goview.repository.ReportProjectRepository;
import com.develop.mvp.pk.module.report.domain.goview.valueobject.GoViewProjectId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportProjectApplicationService {

    private final ReportProjectRepository reportProjectRepository;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public Long createProject(String name, String picUrl, String content,
                              Integer status, String remark) {
        ReportProject project = ReportProjectFactory.create(name, picUrl, content, status, remark);
        reportProjectRepository.save(project);
        publishEvents(project);
        return project.id() != null ? project.id().value() : null;
    }

    @Transactional
    public void updateProject(Long id, String name, String picUrl, String content, String remark) {
        ReportProject project = findExistingProject(GoViewProjectId.of(id));
        project.updateProfile(name, picUrl, content, remark);
        reportProjectRepository.save(project);
        publishEvents(project);
    }

    @Transactional
    public void deleteProject(Long id) {
        ReportProject project = findExistingProject(GoViewProjectId.of(id));
        project.markDeleted();
        reportProjectRepository.delete(project.id());
        publishEvents(project);
    }

    public ReportProject getProject(Long id) {
        return reportProjectRepository.findById(GoViewProjectId.of(id));
    }

    public List<ReportProject> getProjectList() {
        return reportProjectRepository.findAll();
    }

    public PageResult<ReportProject> getProjectPage(String name, Integer status,
                                                     Integer pageNo, Integer pageSize) {
        return reportProjectRepository.findPage(
                new ReportProjectPageQuery(name, status, pageNo, pageSize));
    }

    private ReportProject findExistingProject(GoViewProjectId id) {
        ReportProject project = reportProjectRepository.findById(id);
        if (project == null) {
            throw new RuntimeException("项目不存在: " + id.value());
        }
        return project;
    }

    private void publishEvents(ReportProject project) {
        for (DomainEvent event : project.pullEvents()) {
            eventPublisher.publish(event);
        }
    }
}
