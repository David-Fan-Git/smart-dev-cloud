package com.develop.mvp.pk.module.infra.infrastructure.db.persistence;

// DDD 角色：DataSourceConfigRepository 的 MyBatis 实现

import com.develop.mvp.pk.module.infra.dal.dataobject.db.DataSourceConfigDO;
import com.develop.mvp.pk.module.infra.dal.mysql.db.DataSourceConfigMapper;
import com.develop.mvp.pk.module.infra.domain.db.DataSourceConfig;
import com.develop.mvp.pk.module.infra.domain.db.repository.DataSourceConfigRepository;
import com.develop.mvp.pk.module.infra.domain.db.valueobject.DataSourceConfigId;
import com.develop.mvp.pk.module.infra.infrastructure.db.DataSourceConfigFactory;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class DataSourceConfigRepositoryImpl implements DataSourceConfigRepository {

    private final DataSourceConfigMapper dataSourceConfigMapper;

    public DataSourceConfigRepositoryImpl(DataSourceConfigMapper dataSourceConfigMapper) {
        this.dataSourceConfigMapper = dataSourceConfigMapper;
    }

    @Override
    public DataSourceConfig save(DataSourceConfig config) {
        DataSourceConfigDO configDO = toDataObject(config);
        if (config.id() == null || dataSourceConfigMapper.selectById(config.id().value()) == null) {
            dataSourceConfigMapper.insert(configDO);
            return toDomain(configDO);
        }
        dataSourceConfigMapper.updateById(configDO);
        return config;
    }

    @Override
    public void delete(DataSourceConfigId id) {
        dataSourceConfigMapper.deleteById(id.value());
    }

    @Override
    public void deleteByIds(Collection<DataSourceConfigId> ids) {
        dataSourceConfigMapper.deleteByIds(ids.stream().map(DataSourceConfigId::value).collect(Collectors.toList()));
    }

    @Override
    public DataSourceConfig findById(DataSourceConfigId id) {
        DataSourceConfigDO configDO = dataSourceConfigMapper.selectById(id.value());
        return configDO != null ? toDomain(configDO) : null;
    }

    @Override
    public List<DataSourceConfig> findAll() {
        return dataSourceConfigMapper.selectList().stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    private DataSourceConfigDO toDataObject(DataSourceConfig config) {
        DataSourceConfigDO configDO = new DataSourceConfigDO();
        configDO.setId(config.id() != null ? config.id().value() : null);
        configDO.setName(config.name().value());
        configDO.setUrl(config.url().value());
        configDO.setUsername(config.username());
        configDO.setPassword(config.password());
        return configDO;
    }

    private DataSourceConfig toDomain(DataSourceConfigDO configDO) {
        return DataSourceConfigFactory.reconstitute(
                configDO.getId(),
                configDO.getName(),
                configDO.getUrl(),
                configDO.getUsername(),
                configDO.getPassword()
        );
    }
}
