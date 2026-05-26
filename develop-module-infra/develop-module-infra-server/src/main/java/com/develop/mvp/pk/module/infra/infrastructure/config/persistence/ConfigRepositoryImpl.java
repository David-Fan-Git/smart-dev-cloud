package com.develop.mvp.pk.module.infra.infrastructure.config.persistence;

// DDD 角色：ConfigRepository 的 MyBatis 实现，负责 DO 领域模型映射

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.infra.dal.dataobject.config.ConfigDO;
import com.develop.mvp.pk.module.infra.dal.mysql.config.ConfigMapper;
import com.develop.mvp.pk.module.infra.domain.config.Config;
import com.develop.mvp.pk.module.infra.domain.config.repository.ConfigPageQuery;
import com.develop.mvp.pk.module.infra.domain.config.repository.ConfigRepository;
import com.develop.mvp.pk.module.infra.domain.config.valueobject.ConfigId;
import com.develop.mvp.pk.module.infra.domain.config.valueobject.ConfigKey;
import com.develop.mvp.pk.module.infra.domain.config.factory.ConfigFactory;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ConfigRepositoryImpl implements ConfigRepository {

    private final ConfigMapper configMapper;

    public ConfigRepositoryImpl(ConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

    @Override
    public Config save(Config config) {
        ConfigDO configDO = toDataObject(config);
        if (config.id() == null) {
            configMapper.insert(configDO);
            return toDomain(configDO);
        }
        if (configMapper.selectById(config.id().value()) == null) {
            configMapper.insert(configDO);
        } else {
            configMapper.updateById(configDO);
        }
        return toDomain(configDO);
    }

    @Override
    public void delete(ConfigId id) {
        configMapper.deleteById(id.value());
    }

    @Override
    public void deleteByIds(Collection<ConfigId> ids) {
        configMapper.deleteByIds(ids.stream().map(ConfigId::value).collect(Collectors.toList()));
    }

    @Override
    public Config findById(ConfigId id) {
        ConfigDO configDO = configMapper.selectById(id.value());
        return configDO != null ? toDomain(configDO) : null;
    }

    @Override
    public Optional<Config> findByKey(ConfigKey key) {
        ConfigDO configDO = configMapper.selectByKey(key.value());
        return Optional.ofNullable(configDO).map(this::toDomain);
    }

    @Override
    public boolean existsByKey(ConfigKey key) {
        return configMapper.selectByKey(key.value()) != null;
    }

    @Override
    public PageResult<Config> findPage(ConfigPageQuery query) {
        var reqVO = new com.develop.mvp.pk.module.infra.controller.admin.config.vo.ConfigPageReqVO();
        reqVO.setName(query.name());
        reqVO.setKey(query.key());
        reqVO.setType(query.type());
        reqVO.setCreateTime(query.createTime());
        reqVO.setPageNo(query.pageNo());
        reqVO.setPageSize(query.pageSize());

        PageResult<ConfigDO> doPage = configMapper.selectPage(reqVO);
        List<Config> configs = doPage.getList().stream()
                .map(this::toDomain).collect(Collectors.toList());
        return new PageResult<>(configs, doPage.getTotal());
    }

    @Override
    public List<Config> findByIds(Collection<ConfigId> ids) {
        if (ids.isEmpty()) return Collections.emptyList();
        List<Long> rawIds = ids.stream().map(ConfigId::value).collect(Collectors.toList());
        return configMapper.selectByIds(rawIds).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Config> findAll() {
        return configMapper.selectList().stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    private ConfigDO toDataObject(Config config) {
        ConfigDO configDO = new ConfigDO();
        configDO.setId(config.id() != null ? config.id().value() : null);
        configDO.setConfigKey(config.key().value());
        configDO.setValue(config.value());
        configDO.setName(config.name());
        configDO.setCategory(config.category());
        configDO.setType(config.type().code());
        configDO.setVisible(config.visible().value());
        configDO.setRemark(config.remark());
        return configDO;
    }

    private Config toDomain(ConfigDO configDO) {
        return ConfigFactory.reconstitute(
                configDO.getId(),
                configDO.getConfigKey(),
                configDO.getValue(),
                configDO.getName(),
                configDO.getCategory(),
                configDO.getType(),
                configDO.getVisible(),
                configDO.getRemark()
        );
    }
}
