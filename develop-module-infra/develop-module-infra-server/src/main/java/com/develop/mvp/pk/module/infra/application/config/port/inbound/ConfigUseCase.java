package com.develop.mvp.pk.module.infra.application.config.port.inbound;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.infra.domain.config.Config;

import java.time.LocalDateTime;
import java.util.List;

public interface ConfigUseCase {

    Long createConfig(String key, String value, String name, String category,
                      Integer type, Boolean visible, String remark);

    void updateConfig(Long id, String key, String value, String name, String category,
                      Integer type, Boolean visible, String remark);

    void deleteConfig(Long id);

    void deleteConfigList(List<Long> ids);

    Config getConfig(Long id);

    Config getConfigByKey(String key);

    String getConfigValueByKey(String key);

    PageResult<Config> getConfigPage(String name, String configKey, Integer type,
                                     LocalDateTime[] createTime, Integer pageNo, Integer pageSize);
}
