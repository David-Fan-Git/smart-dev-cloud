package com.develop.mvp.pk.module.report.domain.goview.repository;

public record ReportProjectPageQuery(
        String name,
        Integer status,
        Integer pageNo,
        Integer pageSize
) {}
