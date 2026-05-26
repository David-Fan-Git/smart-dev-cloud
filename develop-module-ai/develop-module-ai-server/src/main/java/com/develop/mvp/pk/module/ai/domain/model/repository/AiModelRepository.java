package com.develop.mvp.pk.module.ai.domain.model.repository;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.ai.domain.model.AiModel;
import com.develop.mvp.pk.module.ai.domain.model.valueobject.AiModelId;

import javax.annotation.Nullable;
import java.util.List;

public interface AiModelRepository {
    AiModel save(AiModel model);
    void delete(AiModelId id);
    AiModel findById(AiModelId id);
    List<AiModel> findAll();
    List<AiModel> findByStatusAndType(Integer status, Integer type, @Nullable String platform);
    AiModel findFirstByStatus(Integer type, Integer status);
    PageResult<AiModel> findPage(AiModelPageQuery query);
}
