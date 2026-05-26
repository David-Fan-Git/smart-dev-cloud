package com.develop.mvp.pk.module.infra.infrastructure.file;

// DDD 角色：工厂，负责创建和重建 File 聚合

import com.develop.mvp.pk.module.infra.domain.file.File;
import com.develop.mvp.pk.module.infra.domain.file.valueobject.FileConfigId;
import com.develop.mvp.pk.module.infra.domain.file.valueobject.FileId;

public final class FileFactory {

    private FileFactory() {}

    /** 创建新文件记录 */
    public static File create(Long configId, String name, String path, String url, String type, Long size) {
        return new File(null, configId != null ? FileConfigId.of(configId) : null, name, path, url, type, size);
    }

    /** 创建新文件记录 */
    public static File create(Long id, Long configId, String name, String path,
                              String url, String type, Long size) {
        return new File(
                id != null ? FileId.of(id) : null,
                configId != null ? FileConfigId.of(configId) : null,
                name, path, url, type, size
        );
    }

    /** 从持久化数据重建 */
    public static File reconstitute(Long id, Long configId, String name, String path,
                                    String url, String type, Long size) {
        return new File(
                FileId.of(id),
                configId != null ? FileConfigId.of(configId) : null,
                name, path, url, type, size
        );
    }
}
