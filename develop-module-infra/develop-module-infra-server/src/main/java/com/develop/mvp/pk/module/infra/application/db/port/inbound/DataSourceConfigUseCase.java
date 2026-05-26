package com.develop.mvp.pk.module.infra.application.db.port.inbound;

import com.develop.mvp.pk.module.infra.domain.db.DataSourceConfig;

import java.util.List;

public interface DataSourceConfigUseCase {

    Long createDataSourceConfig(String name, String url, String username, String password);

    void updateDataSourceConfig(Long id, String name, String url, String username, String password);

    void deleteDataSourceConfig(Long id);

    void deleteDataSourceConfigList(List<Long> ids);

    DataSourceConfig getDataSourceConfig(Long id);

    List<DataSourceConfig> getDataSourceConfigList();
}
