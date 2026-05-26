package com.develop.mvp.pk.module.infra.infrastructure.file.persistence;

// DDD 角色：FileConfigRepository 的 MyBatis 实现

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.infra.dal.dataobject.file.FileConfigDO;
import com.develop.mvp.pk.module.infra.dal.mysql.file.FileConfigMapper;
import com.develop.mvp.pk.module.infra.domain.file.FileConfig;
import com.develop.mvp.pk.module.infra.domain.file.repository.FileConfigPageQuery;
import com.develop.mvp.pk.module.infra.domain.file.repository.FileConfigRepository;
import com.develop.mvp.pk.module.infra.domain.file.valueobject.FileConfigId;
import com.develop.mvp.pk.module.infra.infrastructure.file.FileConfigFactory;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class FileConfigRepositoryImpl implements FileConfigRepository {

    private final FileConfigMapper fileConfigMapper;

    public FileConfigRepositoryImpl(FileConfigMapper fileConfigMapper) {
        this.fileConfigMapper = fileConfigMapper;
    }

    @Override
    public FileConfig save(FileConfig config) {
        FileConfigDO configDO = toDataObject(config);
        if (config.id() == null || fileConfigMapper.selectById(config.id().value()) == null) {
            fileConfigMapper.insert(configDO);
            return toDomain(configDO);
        }
        fileConfigMapper.updateById(configDO);
        return config;
    }

    @Override
    public void delete(FileConfigId id) {
        fileConfigMapper.deleteById(id.value());
    }

    @Override
    public void deleteByIds(Collection<FileConfigId> ids) {
        fileConfigMapper.deleteByIds(ids.stream().map(FileConfigId::value).collect(Collectors.toList()));
    }

    @Override
    public FileConfig findById(FileConfigId id) {
        FileConfigDO configDO = fileConfigMapper.selectById(id.value());
        return configDO != null ? toDomain(configDO) : null;
    }

    @Override
    public FileConfig findByMaster() {
        FileConfigDO configDO = fileConfigMapper.selectByMaster();
        return configDO != null ? toDomain(configDO) : null;
    }

    @Override
    public PageResult<FileConfig> findPage(FileConfigPageQuery query) {
        var reqVO = new com.develop.mvp.pk.module.infra.controller.admin.file.vo.config.FileConfigPageReqVO();
        reqVO.setName(query.name());
        reqVO.setStorage(query.storage());
        reqVO.setCreateTime(query.createTime());
        reqVO.setPageNo(query.pageNo());
        reqVO.setPageSize(query.pageSize());

        PageResult<FileConfigDO> doPage = fileConfigMapper.selectPage(reqVO);
        List<FileConfig> configs = doPage.getList().stream()
                .map(this::toDomain).collect(Collectors.toList());
        return new PageResult<>(configs, doPage.getTotal());
    }

    @Override
    public List<FileConfig> findAll() {
        return fileConfigMapper.selectList().stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<FileConfig> findByIds(Collection<FileConfigId> ids) {
        if (ids.isEmpty()) return Collections.emptyList();
        List<Long> rawIds = ids.stream().map(FileConfigId::value).collect(Collectors.toList());
        return fileConfigMapper.selectByIds(rawIds).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    private FileConfigDO toDataObject(FileConfig config) {
        FileConfigDO configDO = new FileConfigDO();
        configDO.setId(config.id() != null ? config.id().value() : null);
        configDO.setName(config.name().value());
        configDO.setStorage(config.storage());
        configDO.setMaster(config.master());
        configDO.setConfig(config.config());
        configDO.setRemark(config.remark());
        return configDO;
    }

    private FileConfig toDomain(FileConfigDO configDO) {
        return FileConfigFactory.reconstitute(
                configDO.getId(),
                configDO.getName(),
                configDO.getStorage(),
                configDO.getMaster(),
                configDO.getConfig(),
                configDO.getRemark()
        );
    }
}
