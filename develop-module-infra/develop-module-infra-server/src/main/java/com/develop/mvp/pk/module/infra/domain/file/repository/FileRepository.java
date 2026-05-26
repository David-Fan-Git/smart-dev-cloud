package com.develop.mvp.pk.module.infra.domain.file.repository;

// DDD 角色：仓储接口，定义在领域层，不依赖任何基础设施

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.infra.domain.file.File;
import com.develop.mvp.pk.module.infra.domain.file.valueobject.FileId;

import java.util.Collection;
import java.util.List;

public interface FileRepository {
    File save(File file);
    void delete(FileId id);
    void deleteByIds(Collection<FileId> ids);
    File findById(FileId id);
    PageResult<File> findPage(FilePageQuery query);
    List<File> findByIds(Collection<FileId> ids);
}
