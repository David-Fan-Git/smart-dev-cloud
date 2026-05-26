package com.develop.mvp.pk.module.ai.application.model;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.ai.domain.model.AiModel;
import com.develop.mvp.pk.module.ai.domain.model.AiModelFactory;
import com.develop.mvp.pk.module.ai.domain.model.event.DomainEvent;
import com.develop.mvp.pk.module.ai.domain.model.event.DomainEventPublisher;
import com.develop.mvp.pk.module.ai.domain.model.repository.AiModelPageQuery;
import com.develop.mvp.pk.module.ai.domain.model.repository.AiModelRepository;
import com.develop.mvp.pk.module.ai.domain.model.valueobject.AiModelId;
import com.develop.mvp.pk.module.ai.service.model.AiApiKeyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.ai.enums.ErrorCodeConstants.*;

@Service
public class AiModelApplicationService {

    private final AiModelRepository aiModelRepository;
    private final DomainEventPublisher eventPublisher;
    private final AiApiKeyService apiKeyService;

    public AiModelApplicationService(AiModelRepository aiModelRepository,
                                      DomainEventPublisher eventPublisher,
                                      AiApiKeyService apiKeyService) {
        this.aiModelRepository = aiModelRepository;
        this.eventPublisher = eventPublisher;
        this.apiKeyService = apiKeyService;
    }

    @Transactional
    public Long createModel(String name, String model, String platform,
                             Integer type, Long keyId, Integer sort,
                             Double temperature, Integer maxTokens,
                             Integer maxContexts) {
        return createModel(name, model, platform, type, keyId, sort,
                null, temperature, maxTokens, maxContexts);
    }

    @Transactional
    public Long createModel(String name, String model, String platform,
                             Integer type, Long keyId, Integer sort,
                             Integer status, Double temperature,
                             Integer maxTokens, Integer maxContexts) {
        apiKeyService.validateApiKey(keyId);
        validateModelUnique(null, platform, type, model);
        AiModel aiModel = AiModelFactory.create(name, model, platform, type, keyId,
                sort, status, temperature, maxTokens, maxContexts);
        aiModel = aiModelRepository.save(aiModel);
        aiModel.markCreated();
        publishEvents(aiModel);
        return aiModel.id();
    }

    @Transactional
    public void updateModel(Long id, Long keyId, String name, String model,
                             String platform, Integer type, Integer sort,
                             Double temperature, Integer maxTokens,
                             Integer maxContexts) {
        updateModel(id, keyId, name, model, platform, type, sort,
                null, temperature, maxTokens, maxContexts);
    }

    @Transactional
    public void updateModel(Long id, Long keyId, String name, String model,
                             String platform, Integer type, Integer sort,
                             Integer status, Double temperature,
                             Integer maxTokens, Integer maxContexts) {
        AiModel aiModel = findExistingModel(AiModelId.of(id));
        apiKeyService.validateApiKey(keyId);
        validateModelUnique(id, platform, type, model);
        aiModel.updateProfile(keyId, name, model, platform, type, sort,
                status, temperature, maxTokens, maxContexts);
        aiModelRepository.save(aiModel);
        publishEvents(aiModel);
    }

    @Transactional
    public void deleteModel(Long id) {
        AiModel aiModel = findExistingModel(AiModelId.of(id));
        aiModel.markDeleted();
        aiModelRepository.delete(AiModelId.of(id));
        publishEvents(aiModel);
    }

    @Transactional
    public void enableModel(Long id) {
        AiModel aiModel = findExistingModel(AiModelId.of(id));
        aiModel.enable();
        aiModelRepository.save(aiModel);
        publishEvents(aiModel);
    }

    @Transactional
    public void disableModel(Long id) {
        AiModel aiModel = findExistingModel(AiModelId.of(id));
        aiModel.disable();
        aiModelRepository.save(aiModel);
        publishEvents(aiModel);
    }

    public AiModel getModel(Long id) {
        return aiModelRepository.findById(AiModelId.of(id));
    }

    public List<AiModel> getModelList() {
        return aiModelRepository.findAll();
    }

    public List<AiModel> getModelListByStatusAndType(Integer status, Integer type, String platform) {
        return aiModelRepository.findByStatusAndType(status, type, platform);
    }

    public AiModel getRequiredDefaultModel(Integer type) {
        AiModel model = aiModelRepository.findFirstByStatus(type,
                com.develop.mvp.pk.framework.common.enums.CommonStatusEnum.ENABLE.getStatus());
        if (model == null) {
            throw exception(MODEL_DEFAULT_NOT_EXISTS);
        }
        return model;
    }

    public PageResult<AiModel> getModelPage(String name, String model, String platform,
                                              Integer type, Integer status,
                                              Integer pageNo, Integer pageSize) {
        return aiModelRepository.findPage(new AiModelPageQuery(
                name, model, platform, type, status, pageNo, pageSize));
    }

    private AiModel findExistingModel(AiModelId id) {
        AiModel model = aiModelRepository.findById(id);
        if (model == null) {
            throw exception(MODEL_NOT_EXISTS);
        }
        return model;
    }

    private void validateModelUnique(Long id, String platform, Integer type, String model) {
        boolean exists = aiModelRepository.findAll().stream()
                .anyMatch(existing -> !java.util.Objects.equals(existing.id(), id)
                        && java.util.Objects.equals(existing.platform(), platform)
                        && java.util.Objects.equals(existing.type(), type)
                        && java.util.Objects.equals(existing.model(), model));
        if (exists) {
            throw exception(MODEL_EXISTS);
        }
    }

    private void publishEvents(AiModel model) {
        for (DomainEvent event : model.pullEvents()) {
            eventPublisher.publish(event);
        }
    }
}
