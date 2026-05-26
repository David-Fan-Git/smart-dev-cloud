package com.develop.mvp.pk.module.infra.application.config.service;

// DDD 角色：应用编排服务，不包含业务规则，仅编排领域对象和基础设施
// 规则 R01：SYSTEM 类型的配置不可删除
// 规则 R02：配置键在全局不可重复
// 规则 R03：不可见的配置不允许返回给前端

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.infra.application.config.port.inbound.ConfigUseCase;
import com.develop.mvp.pk.module.infra.domain.config.Config;
import com.develop.mvp.pk.module.infra.domain.config.repository.ConfigPageQuery;
import com.develop.mvp.pk.module.infra.domain.config.repository.ConfigRepository;
import com.develop.mvp.pk.module.infra.domain.config.valueobject.ConfigId;
import com.develop.mvp.pk.module.infra.domain.config.valueobject.ConfigKey;
import com.develop.mvp.pk.module.infra.domain.config.factory.ConfigFactory;
import com.develop.mvp.pk.module.infra.domain.config.valueobject.ConfigVisible;
import com.develop.mvp.pk.module.infra.domain.event.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.infra.enums.ErrorCodeConstants.*;

@Service
public class ConfigApplicationService implements ConfigUseCase {

    private final ConfigRepository configRepository;
    private final DomainEventPublisher eventPublisher;

    public ConfigApplicationService(ConfigRepository configRepository,
                                     DomainEventPublisher eventPublisher) {
        this.configRepository = configRepository;
        this.eventPublisher = eventPublisher;
    }

    // ── 命令 ──

    @Transactional
    public Long createConfig(String key, String value, String name, String category,
                              Integer type, Boolean visible, String remark) {
        // 规则 R02：校验 key 唯一性
        assertKeyUnique(ConfigKey.of(key), null);

        Config config = ConfigFactory.create(null, key, value, name, category, type, visible, remark);
        // 设置 ID 为自动生成的
        config = configRepository.save(config);
        config.markCreated();
        publishEvents(config);
        return config.id().value();
    }

    @Transactional
    public void updateConfig(Long id, String key, String value, String name, String category,
                              Integer type, Boolean visible, String remark) {
        Config config = findExistingConfig(ConfigId.of(id));
        // 规则 R02：校验 key 唯一性
        assertKeyUnique(ConfigKey.of(key), config.id());

        config.updateProfile(value, name, category, visible != null ? ConfigVisible.of(visible) : null, remark);
        configRepository.save(config);
        publishEvents(config);
    }

    @Transactional
    public void deleteConfig(Long id) {
        Config config = findExistingConfig(ConfigId.of(id));
        // 规则 R01：系统配置不可删除
        if (config.isSystemType()) {
            throw exception(CONFIG_CAN_NOT_DELETE_SYSTEM_TYPE);
        }
        config.markDeleted();
        configRepository.delete(config.id());
        publishEvents(config);
    }

    @Transactional
    public void deleteConfigList(List<Long> ids) {
        for (Long id : ids) {
            deleteConfig(id);
        }
    }

    // ── 查询 ──

    public Config getConfig(Long id) {
        return configRepository.findById(ConfigId.of(id));
    }

    /** 根据键获取配置值（规则 R03：不可见的配置不返回） */
    public Config getConfigByKey(String key) {
        return configRepository.findByKey(ConfigKey.of(key)).orElse(null);
    }

    /** 规则 R03：获取可见的配置值 */
    public String getConfigValueByKey(String key) {
        Config config = getConfigByKey(key);
        if (config == null) return null;
        if (!config.isVisible()) {
            throw exception(CONFIG_GET_VALUE_ERROR_IF_VISIBLE);
        }
        return config.value();
    }

    public PageResult<Config> getConfigPage(String name, String configKey, Integer type,
                                             java.time.LocalDateTime[] createTime,
                                             Integer pageNo, Integer pageSize) {
        return configRepository.findPage(new ConfigPageQuery(
                name, configKey, type, createTime, pageNo, pageSize));
    }

    // ── 私有方法 ──

    private Config findExistingConfig(ConfigId id) {
        Config config = configRepository.findById(id);
        if (config == null) throw exception(CONFIG_NOT_EXISTS);
        return config;
    }

    private void assertKeyUnique(ConfigKey key, ConfigId excludeId) {
        Config existing = configRepository.findByKey(key).orElse(null);
        if (existing == null) return;
        if (excludeId == null || !existing.id().equals(excludeId)) {
            throw exception(CONFIG_KEY_DUPLICATE);
        }
    }

    private void publishEvents(Config config) {
        for (var event : config.pullEvents()) {
            eventPublisher.publish(event);
        }
    }
}
