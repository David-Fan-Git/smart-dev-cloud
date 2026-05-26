package com.develop.mvp.pk.module.infra.infrastructure.file;

// DDD 角色：工厂，负责创建和重建 FileConfig 聚合

import com.develop.mvp.pk.module.infra.domain.file.FileConfig;
import com.develop.mvp.pk.module.infra.domain.file.valueobject.FileConfigId;
import com.develop.mvp.pk.module.infra.domain.file.valueobject.FileConfigName;
import com.develop.mvp.pk.module.infra.framework.file.core.client.FileClientConfig;

public final class FileConfigFactory {

    private FileConfigFactory() {}

    /** 创建新文件配置 */
    public static FileConfig create(String name, Integer storage, Boolean master,
                                    FileClientConfig config, String remark) {
        return new FileConfig(null, FileConfigName.of(name), storage, master, config, remark);
    }

    /** 创建文件配置 */
    public static FileConfig create(Long id, String name, Integer storage,
                                    Boolean master, FileClientConfig config, String remark) {
        return new FileConfig(
                id != null ? FileConfigId.of(id) : null,
                FileConfigName.of(name),
                storage, master, config, remark
        );
    }

    /** 从持久化数据重建 */
    public static FileConfig reconstitute(Long id, String name, Integer storage,
                                          Boolean master, FileClientConfig config, String remark) {
        return new FileConfig(
                FileConfigId.of(id),
                FileConfigName.of(name),
                storage, master, config, remark
        );
    }
}
