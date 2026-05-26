package com.develop.mvp.pk.module.report.domain.goview;

import com.develop.mvp.pk.module.report.domain.goview.valueobject.GoViewProjectId;
import com.develop.mvp.pk.module.report.domain.goview.valueobject.GoViewProjectStatus;

public final class ReportProjectFactory {

    private ReportProjectFactory() {}

    public static ReportProject create(String name, String picUrl, String content,
                                        Integer status, String remark) {
        return new ReportProject(
                (Long) null,
                name,
                picUrl,
                content,
                status != null ? GoViewProjectStatus.of(status) : GoViewProjectStatus.UNPUBLISHED,
                remark
        );
    }

    public static ReportProject reconstitute(Long id, String name, String picUrl,
                                              String content, Integer status, String remark) {
        return new ReportProject(
                GoViewProjectId.of(id),
                name,
                picUrl,
                content,
                GoViewProjectStatus.of(status),
                remark
        );
    }
}
