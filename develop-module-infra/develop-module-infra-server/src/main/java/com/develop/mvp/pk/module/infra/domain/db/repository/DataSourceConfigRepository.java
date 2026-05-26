package com.develop.mvp.pk.module.infra.domain.db.repository;

// DDD 角色：仓储接口，定义在领域层，不依赖任何基础设施

import com.develop.mvp.pk.module.infra.domain.db.DataSourceConfig;
import com.develop.mvp.pk.module.infra.domain.db.valueobject.DataSourceConfigId;

import java.util.Collection;
import java.util.List;

public interface DataSourceConfigRepository {
    DataSourceConfig save(DataSourceConfig config);
    void delete(DataSourceConfigId id);
    void deleteByIds(Collection<DataSourceConfigId> ids);
    DataSourceConfig findById(DataSourceConfigId id);
    List<DataSourceConfig> findAll();
}
