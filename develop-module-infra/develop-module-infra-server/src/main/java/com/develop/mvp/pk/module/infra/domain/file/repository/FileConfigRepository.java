package com.develop.mvp.pk.module.infra.domain.file.repository;

// DDD 角色：仓储接口，定义在领域层，不依赖任何基础设施

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.infra.domain.file.FileConfig;
import com.develop.mvp.pk.module.infra.domain.file.valueobject.FileConfigId;

import java.util.Collection;
import java.util.List;

public interface FileConfigRepository {
    FileConfig save(FileConfig config);
    void delete(FileConfigId id);
    void deleteByIds(Collection<FileConfigId> ids);
    FileConfig findById(FileConfigId id);
    FileConfig findByMaster();
    PageResult<FileConfig> findPage(FileConfigPageQuery query);
    List<FileConfig> findAll();
    List<FileConfig> findByIds(Collection<FileConfigId> ids);
}
