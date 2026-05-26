package com.develop.mvp.pk.module.infra.infrastructure.db;

// DDD 角色：工厂，负责创建和重建 DataSourceConfig 聚合

import com.develop.mvp.pk.module.infra.domain.db.DataSourceConfig;
import com.develop.mvp.pk.module.infra.domain.db.valueobject.DataSourceConfigId;
import com.develop.mvp.pk.module.infra.domain.db.valueobject.DataSourceConfigName;
import com.develop.mvp.pk.module.infra.domain.db.valueobject.DataSourceConfigUrl;

public final class DataSourceConfigFactory {

    private DataSourceConfigFactory() {}

    /** 创建新数据源配置 */
    public static DataSourceConfig create(String name, String url, String username, String password) {
        return new DataSourceConfig(null, DataSourceConfigName.of(name), DataSourceConfigUrl.of(url),
                username, password);
    }

    /** 创建新数据源配置 */
    public static DataSourceConfig create(Long id, String name, String url,
                                          String username, String password) {
        return new DataSourceConfig(
                id != null ? DataSourceConfigId.of(id) : null,
                DataSourceConfigName.of(name),
                DataSourceConfigUrl.of(url),
                username, password
        );
    }

    /** 从持久化数据重建 */
    public static DataSourceConfig reconstitute(Long id, String name, String url,
                                                String username, String password) {
        return new DataSourceConfig(
                DataSourceConfigId.of(id),
                DataSourceConfigName.of(name),
                DataSourceConfigUrl.of(url),
                username, password
        );
    }
}
