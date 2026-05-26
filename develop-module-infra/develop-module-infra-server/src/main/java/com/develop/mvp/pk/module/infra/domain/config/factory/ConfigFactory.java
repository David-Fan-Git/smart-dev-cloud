package com.develop.mvp.pk.module.infra.domain.config.factory;

// DDD 角色：工厂，负责创建和重建 Config 聚合

import com.develop.mvp.pk.module.infra.domain.config.Config;
import com.develop.mvp.pk.module.infra.domain.config.valueobject.ConfigId;
import com.develop.mvp.pk.module.infra.domain.config.valueobject.ConfigKey;
import com.develop.mvp.pk.module.infra.domain.config.valueobject.ConfigType;
import com.develop.mvp.pk.module.infra.domain.config.valueobject.ConfigVisible;

public final class ConfigFactory {

    private ConfigFactory() {}

    /**
     * 创建尚未持久化的新配置聚合。
     *
     * <p>DDD 中的聚合可以先表达“准备创建的业务对象”，再交给仓储分配数据库编号。
     * 因此这里允许 id 为空；只有从数据库重建对象时，才要求 id 必须存在。</p>
     */
    public static Config create(Long id, String key, String value, String name,
                                String category, Integer type, Boolean visible, String remark) {
        return new Config(
                id != null ? ConfigId.of(id) : null,
                ConfigKey.of(key),
                value, name, category,
                type != null ? ConfigType.of(type) : null,
                visible != null ? ConfigVisible.of(visible) : null,
                remark
        );
    }

    /** 从持久化数据重建 Config 聚合 */
    public static Config reconstitute(Long id, String key, String value, String name,
                                      String category, Integer type, Boolean visible, String remark) {
        return new Config(
                ConfigId.of(id),
                ConfigKey.of(key),
                value, name, category,
                ConfigType.of(type),
                ConfigVisible.of(visible),
                remark
        );
    }
}
