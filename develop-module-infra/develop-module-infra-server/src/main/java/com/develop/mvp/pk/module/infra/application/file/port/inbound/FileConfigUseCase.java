package com.develop.mvp.pk.module.infra.application.file.port.inbound;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.infra.domain.file.FileConfig;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface FileConfigUseCase {

    Long createFileConfig(String name, Integer storage, Boolean master,
                          Map<String, Object> clientConfig, String remark);

    void updateFileConfig(Long id, String name, Integer storage,
                          Map<String, Object> clientConfig, String remark);

    void updateFileConfigMaster(Long id);

    void deleteFileConfig(Long id);

    void deleteFileConfigList(List<Long> ids);

    FileConfig getFileConfig(Long id);

    PageResult<FileConfig> getFileConfigPage(String name, Integer storage,
                                             LocalDateTime[] createTime, Integer pageNo, Integer pageSize);

    String testFileConfig(Long id) throws Exception;
}
