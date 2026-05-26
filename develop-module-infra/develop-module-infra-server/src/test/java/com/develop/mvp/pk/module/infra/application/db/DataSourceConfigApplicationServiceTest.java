package com.develop.mvp.pk.module.infra.application.db;

import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.develop.mvp.pk.module.infra.application.db.service.DataSourceConfigApplicationService;
import com.develop.mvp.pk.module.infra.domain.db.DataSourceConfig;
import com.develop.mvp.pk.module.infra.domain.db.repository.DataSourceConfigRepository;
import com.develop.mvp.pk.module.infra.domain.db.valueobject.DataSourceConfigId;
import com.develop.mvp.pk.module.infra.domain.event.DomainEvent;
import com.develop.mvp.pk.module.infra.domain.event.DomainEventPublisher;
import com.develop.mvp.pk.module.infra.infrastructure.db.DataSourceConfigFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataSourceConfigApplicationServiceTest {

    private InMemoryDataSourceConfigRepository repository;
    private DataSourceConfigApplicationService applicationService;

    @BeforeEach
    void setUp() throws Exception {
        repository = new InMemoryDataSourceConfigRepository();
        DynamicDataSourceProperties properties = new DynamicDataSourceProperties();
        properties.setPrimary("master");
        DataSourceProperty master = new DataSourceProperty();
        master.setUrl("jdbc:mysql://127.0.0.1:3306/develop");
        master.setUsername("root");
        master.setPassword("123456");
        properties.setDatasource(Map.of("master", master));
        Constructor<DataSourceConfigApplicationService> constructor = DataSourceConfigApplicationService.class.getConstructor(
                DataSourceConfigRepository.class, DomainEventPublisher.class, DynamicDataSourceProperties.class);
        DomainEventPublisher eventPublisher = event -> {};
        applicationService = constructor.newInstance(repository, eventPublisher, properties);
    }

    @Test
    void getDataSourceConfig_returnsMasterWhenIdIsZero() {
        DataSourceConfig master = applicationService.getDataSourceConfig(DataSourceConfig.ID_MASTER);

        assertEquals(DataSourceConfig.ID_MASTER, master.id().value());
        assertEquals("master", master.name().value());
        assertEquals("jdbc:mysql://127.0.0.1:3306/develop", master.url().value());
        assertEquals("root", master.username());
        assertEquals("123456", master.password());
    }

    @Test
    void getDataSourceConfigList_prependsMaster() {
        repository.save(DataSourceConfigFactory.create("slave", "jdbc:mysql://127.0.0.1:3306/slave", "root", "123456"));

        List<DataSourceConfig> configs = applicationService.getDataSourceConfigList();

        assertEquals(2, configs.size());
        assertEquals(DataSourceConfig.ID_MASTER, configs.get(0).id().value());
        assertEquals("slave", configs.get(1).name().value());
    }

    private static final class InMemoryDataSourceConfigRepository implements DataSourceConfigRepository {
        private final Map<Long, DataSourceConfig> configs = new LinkedHashMap<>();
        private long nextId = 1L;

        @Override
        public DataSourceConfig save(DataSourceConfig config) {
            Long id = config.id() != null ? config.id().value() : nextId++;
            DataSourceConfig saved = DataSourceConfigFactory.reconstitute(id, config.name().value(), config.url().value(),
                    config.username(), config.password());
            configs.put(id, saved);
            return saved;
        }

        @Override
        public void delete(DataSourceConfigId id) {
            configs.remove(id.value());
        }

        @Override
        public void deleteByIds(java.util.Collection<DataSourceConfigId> ids) {
            ids.forEach(this::delete);
        }

        @Override
        public DataSourceConfig findById(DataSourceConfigId id) {
            return configs.get(id.value());
        }

        @Override
        public List<DataSourceConfig> findAll() {
            return new ArrayList<>(configs.values());
        }
    }
}
