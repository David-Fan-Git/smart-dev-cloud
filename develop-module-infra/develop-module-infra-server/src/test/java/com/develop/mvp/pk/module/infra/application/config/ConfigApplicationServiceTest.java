package com.develop.mvp.pk.module.infra.application.config;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.infra.application.config.service.ConfigApplicationService;
import com.develop.mvp.pk.module.infra.domain.config.Config;
import com.develop.mvp.pk.module.infra.domain.config.repository.ConfigPageQuery;
import com.develop.mvp.pk.module.infra.domain.config.repository.ConfigRepository;
import com.develop.mvp.pk.module.infra.domain.config.valueobject.ConfigId;
import com.develop.mvp.pk.module.infra.domain.config.valueobject.ConfigKey;
import com.develop.mvp.pk.module.infra.domain.event.DomainEventPublisher;
import com.develop.mvp.pk.module.infra.enums.config.ConfigTypeEnum;
import com.develop.mvp.pk.module.infra.domain.config.factory.ConfigFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConfigApplicationServiceTest {

    private InMemoryConfigRepository repository;
    private ConfigApplicationService applicationService;

    @BeforeEach
    void setUp() {
        repository = new InMemoryConfigRepository();
        DomainEventPublisher eventPublisher = event -> {};
        applicationService = new ConfigApplicationService(repository, eventPublisher);
    }

    @Test
    void createConfig_generatesNonNullIdAndPreservesDefaults() {
        Long id = applicationService.createConfig("infra.test.key", "enabled", "测试配置", "infra", null, null, "remark");

        Config saved = repository.findById(ConfigId.of(id));
        assertNotNull(id);
        assertEquals(id, saved.id().value());
        assertEquals("infra.test.key", saved.key().value());
        assertEquals(ConfigTypeEnum.CUSTOM.getType(), saved.type().code());
        assertEquals(true, saved.visible().value());
    }

    private static final class InMemoryConfigRepository implements ConfigRepository {
        private final Map<Long, Config> configs = new LinkedHashMap<>();
        private long nextId = 1L;

        @Override
        public Config save(Config config) {
            Long id = config.id() != null ? config.id().value() : nextId++;
            Config saved = ConfigFactory.reconstitute(id, config.key().value(), config.value(), config.name(),
                    config.category(), config.type().code(), config.visible().value(), config.remark());
            configs.put(id, saved);
            return saved;
        }

        @Override
        public void delete(ConfigId id) {
            configs.remove(id.value());
        }

        @Override
        public void deleteByIds(Collection<ConfigId> ids) {
            ids.forEach(this::delete);
        }

        @Override
        public Config findById(ConfigId id) {
            return configs.get(id.value());
        }

        @Override
        public Optional<Config> findByKey(ConfigKey key) {
            return configs.values().stream().filter(config -> config.key().equals(key)).findFirst();
        }

        @Override
        public boolean existsByKey(ConfigKey key) {
            return findByKey(key).isPresent();
        }

        @Override
        public PageResult<Config> findPage(ConfigPageQuery query) {
            return new PageResult<>(findAll(), (long) configs.size());
        }

        @Override
        public List<Config> findByIds(Collection<ConfigId> ids) {
            return ids.stream().map(this::findById).toList();
        }

        @Override
        public List<Config> findAll() {
            return new ArrayList<>(configs.values());
        }
    }
}
