package com.develop.mvp.pk.module.infra.application.db.service;

// DDD 角色：应用编排服务

import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.develop.mvp.pk.module.infra.application.db.port.inbound.DataSourceConfigUseCase;
import com.develop.mvp.pk.module.infra.domain.db.DataSourceConfig;
import com.develop.mvp.pk.module.infra.domain.db.repository.DataSourceConfigRepository;
import com.develop.mvp.pk.module.infra.domain.db.valueobject.DataSourceConfigId;
import com.develop.mvp.pk.module.infra.domain.event.DomainEventPublisher;
import com.develop.mvp.pk.framework.mybatis.core.util.JdbcUtils;
import com.develop.mvp.pk.module.infra.infrastructure.db.DataSourceConfigFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.infra.enums.ErrorCodeConstants.DATA_SOURCE_CONFIG_NOT_EXISTS;
import static com.develop.mvp.pk.module.infra.enums.ErrorCodeConstants.DATA_SOURCE_CONFIG_NOT_OK;

@Service
public class DataSourceConfigApplicationService implements DataSourceConfigUseCase {

    private final DataSourceConfigRepository dataSourceConfigRepository;
    private final DomainEventPublisher eventPublisher;
    private final DynamicDataSourceProperties dynamicDataSourceProperties;

    public DataSourceConfigApplicationService(DataSourceConfigRepository dataSourceConfigRepository,
                                               DomainEventPublisher eventPublisher,
                                               DynamicDataSourceProperties dynamicDataSourceProperties) {
        this.dataSourceConfigRepository = dataSourceConfigRepository;
        this.eventPublisher = eventPublisher;
        this.dynamicDataSourceProperties = dynamicDataSourceProperties;
    }

    // ── 命令 ──

    @Transactional
    public Long createDataSourceConfig(String name, String url, String username, String password) {
        validateConnectionOK(url, username, password);
        DataSourceConfig config = DataSourceConfigFactory.create(name, url, username, password);
        config = dataSourceConfigRepository.save(config);
        config.markCreated();
        publishEvents(config);
        return config.id().value();
    }

    @Transactional
    public void updateDataSourceConfig(Long id, String name, String url, String username, String password) {
        DataSourceConfig config = findExistingConfig(DataSourceConfigId.of(id));
        validateConnectionOK(url, username, password);
        // 持久化时通过基础设施层处理
        configRepositorySaveWithNewIdentity(config, name, url, username, password);
        publishEvents(config);
    }

    @Transactional
    public void deleteDataSourceConfig(Long id) {
        DataSourceConfig config = findExistingConfig(DataSourceConfigId.of(id));
        config.markDeleted();
        dataSourceConfigRepository.delete(config.id());
        publishEvents(config);
    }

    @Transactional
    public void deleteDataSourceConfigList(List<Long> ids) {
        for (Long id : ids) {
            deleteDataSourceConfig(id);
        }
    }

    // ── 查询 ──

    public DataSourceConfig getDataSourceConfig(Long id) {
        if (Objects.equals(id, DataSourceConfig.ID_MASTER)) {
            return buildMasterDataSourceConfig();
        }
        return dataSourceConfigRepository.findById(DataSourceConfigId.of(id));
    }

    public List<DataSourceConfig> getDataSourceConfigList() {
        List<DataSourceConfig> configs = new ArrayList<>(dataSourceConfigRepository.findAll());
        configs.add(0, buildMasterDataSourceConfig());
        return configs;
    }

    // ── 私有方法 ──

    private void configRepositorySaveWithNewIdentity(DataSourceConfig config, String name,
                                                      String url, String username, String password) {
        DataSourceConfig newConfig = DataSourceConfigFactory.create(
                config.id().value(), name, url, username, password);
        dataSourceConfigRepository.save(newConfig);
    }

    private DataSourceConfig findExistingConfig(DataSourceConfigId id) {
        DataSourceConfig config = dataSourceConfigRepository.findById(id);
        if (config == null) throw exception(DATA_SOURCE_CONFIG_NOT_EXISTS);
        return config;
    }

    private DataSourceConfig buildMasterDataSourceConfig() {
        String primary = dynamicDataSourceProperties.getPrimary();
        DataSourceProperty dataSourceProperty = dynamicDataSourceProperties.getDatasource().get(primary);
        return DataSourceConfigFactory.reconstitute(DataSourceConfig.ID_MASTER, primary,
                dataSourceProperty.getUrl(), dataSourceProperty.getUsername(), dataSourceProperty.getPassword());
    }

    private void validateConnectionOK(String url, String username, String password) {
        boolean success = JdbcUtils.isConnectionOK(url, username, password);
        if (!success) {
            throw exception(DATA_SOURCE_CONFIG_NOT_OK);
        }
    }

    private void publishEvents(DataSourceConfig config) {
        for (var event : config.pullEvents()) {
            eventPublisher.publish(event);
        }
    }
}
