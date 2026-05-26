package com.develop.mvp.pk.module.infra.domain.config.repository;

// DDD 角色：仓储接口，定义在领域层，不依赖任何基础设施

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.infra.domain.config.Config;
import com.develop.mvp.pk.module.infra.domain.config.valueobject.ConfigId;
import com.develop.mvp.pk.module.infra.domain.config.valueobject.ConfigKey;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ConfigRepository {
    Config save(Config config);
    void delete(ConfigId id);
    void deleteByIds(Collection<ConfigId> ids);
    Config findById(ConfigId id);
    Optional<Config> findByKey(ConfigKey key);
    boolean existsByKey(ConfigKey key);
    PageResult<Config> findPage(ConfigPageQuery query);
    List<Config> findByIds(Collection<ConfigId> ids);
    List<Config> findAll();
}
