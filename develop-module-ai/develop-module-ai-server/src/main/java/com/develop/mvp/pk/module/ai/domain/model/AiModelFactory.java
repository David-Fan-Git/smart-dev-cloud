package com.develop.mvp.pk.module.ai.domain.model;

import com.develop.mvp.pk.module.ai.domain.model.valueobject.AiModelId;
import com.develop.mvp.pk.module.ai.domain.model.valueobject.AiModelStatus;

public class AiModelFactory {

    public static AiModel create(String name, String model, String platform,
                                  Integer type, Long keyId, Integer sort,
                                  Double temperature, Integer maxTokens,
                                  Integer maxContexts) {
        return create(name, model, platform, type, keyId, sort,
                AiModelStatus.ENABLED.code(), temperature, maxTokens, maxContexts);
    }

    public static AiModel create(String name, String model, String platform,
                                  Integer type, Long keyId, Integer sort,
                                  Integer status, Double temperature,
                                  Integer maxTokens, Integer maxContexts) {
        return new AiModel(null, keyId, name, model, platform, type, sort,
                status != null ? AiModelStatus.of(status) : AiModelStatus.ENABLED,
                temperature, maxTokens, maxContexts);
    }

    public static AiModel reconstitute(Long id, Long keyId, String name, String model,
                                        String platform, Integer type, Integer sort,
                                        Integer status, Double temperature,
                                        Integer maxTokens, Integer maxContexts) {
        return new AiModel(id, keyId, name, model, platform, type, sort,
                status != null ? AiModelStatus.of(status) : AiModelStatus.ENABLED,
                temperature, maxTokens, maxContexts);
    }
}
