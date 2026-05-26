package com.develop.mvp.pk.module.ai.application.model;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.ai.domain.model.AiModel;
import com.develop.mvp.pk.module.ai.domain.model.AiModelFactory;
import com.develop.mvp.pk.module.ai.domain.model.event.AiModelCreatedEvent;
import com.develop.mvp.pk.module.ai.domain.model.event.DomainEvent;
import com.develop.mvp.pk.module.ai.domain.model.event.DomainEventPublisher;
import com.develop.mvp.pk.module.ai.domain.model.repository.AiModelPageQuery;
import com.develop.mvp.pk.module.ai.domain.model.repository.AiModelRepository;
import com.develop.mvp.pk.module.ai.domain.model.valueobject.AiModelId;
import com.develop.mvp.pk.module.ai.enums.model.AiModelTypeEnum;
import com.develop.mvp.pk.module.ai.enums.model.AiPlatformEnum;
import com.develop.mvp.pk.module.ai.service.model.AiApiKeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import static com.develop.mvp.pk.framework.common.enums.CommonStatusEnum.DISABLE;
import static com.develop.mvp.pk.framework.test.core.util.AssertUtils.assertServiceException;
import static com.develop.mvp.pk.module.ai.enums.ErrorCodeConstants.MODEL_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AiModelApplicationServiceTest {

    private static final String NAME = "DeepSeek V3";
    private static final String MODEL = "deepseek-chat";
    private static final String PLATFORM = AiPlatformEnum.DEEP_SEEK.getPlatform();
    private static final Integer TYPE = AiModelTypeEnum.CHAT.getType();
    private static final Long KEY_ID = 10L;

    @Mock
    private AiApiKeyService apiKeyService;
    @Mock
    private DomainEventPublisher eventPublisher;

    private InMemoryAiModelRepository aiModelRepository;
    private AiModelApplicationService applicationService;

    @BeforeEach
    void setUp() throws Exception {
        aiModelRepository = new InMemoryAiModelRepository();
        Constructor<AiModelApplicationService> constructor = AiModelApplicationService.class.getConstructor(
                AiModelRepository.class, DomainEventPublisher.class, AiApiKeyService.class);
        applicationService = constructor.newInstance(aiModelRepository, eventPublisher, apiKeyService);
    }

    @Test
    void createModel_validatesApiKey() {
        applicationService.createModel(NAME, MODEL, PLATFORM, TYPE, KEY_ID, 1, 0.8, 4096, 10);

        verify(apiKeyService).validateApiKey(eq(KEY_ID));
    }

    @Test
    void createModel_rejectsDuplicatePlatformTypeAndModel() {
        aiModelRepository.save(AiModelFactory.create(NAME, MODEL, PLATFORM, TYPE, KEY_ID, 1, 0.8, 4096, 10));

        assertServiceException(() -> applicationService.createModel(
                "重复模型", MODEL, PLATFORM, TYPE, 20L, 2, 0.7, 2048, 5), MODEL_EXISTS);
    }

    @Test
    void updateModel_validatesApiKey() {
        AiModel saved = aiModelRepository.save(AiModelFactory.create(NAME, MODEL, PLATFORM, TYPE, KEY_ID, 1, 0.8, 4096, 10));

        applicationService.updateModel(saved.id(), 20L, NAME, MODEL, PLATFORM, TYPE, 1, 0.8, 4096, 10);

        verify(apiKeyService).validateApiKey(eq(20L));
    }

    @Test
    void updateModel_rejectsDuplicatePlatformTypeAndModel() {
        aiModelRepository.save(AiModelFactory.create(NAME, MODEL, PLATFORM, TYPE, KEY_ID, 1, 0.8, 4096, 10));
        AiModel target = aiModelRepository.save(AiModelFactory.create("Moonshot", "moonshot-v1", AiPlatformEnum.MOONSHOT.getPlatform(),
                TYPE, 20L, 2, 0.7, 2048, 5));

        assertServiceException(() -> applicationService.updateModel(
                target.id(), 20L, "重复模型", MODEL, PLATFORM, TYPE, 2, 0.7, 2048, 5), MODEL_EXISTS);
    }

    @Test
    void createModel_publishesCreatedEventWithPersistedId() {
        Long id = applicationService.createModel(NAME, MODEL, PLATFORM, TYPE, KEY_ID, 1, 0.8, 4096, 10);

        verify(eventPublisher).publish(argThat(event -> event instanceof AiModelCreatedEvent createdEvent
                && id.equals(createdEvent.modelId())
                && NAME.equals(createdEvent.name())
                && MODEL.equals(createdEvent.model())
                && PLATFORM.equals(createdEvent.platform())));
    }

    @Test
    void createModel_rejectsCreatedEventWithoutPersistedId() {
        AiModel model = AiModelFactory.create(NAME, MODEL, PLATFORM, TYPE, KEY_ID, 1, 0.8, 4096, 10);

        assertThrows(IllegalStateException.class, model::markCreated);
    }

    @Test
    void createModel_persistsStatus() {
        Long id = applicationService.createModel(NAME, MODEL, PLATFORM, TYPE, KEY_ID, 1, DISABLE.getStatus(), 0.8, 4096, 10);

        assertEquals(DISABLE.getStatus(), aiModelRepository.findById(AiModelId.of(id)).status().code());
    }

    @Test
    void updateModel_persistsStatus() {
        AiModel saved = aiModelRepository.save(AiModelFactory.create(NAME, MODEL, PLATFORM, TYPE, KEY_ID, 1, 0.8, 4096, 10));

        applicationService.updateModel(saved.id(), KEY_ID, NAME, MODEL, PLATFORM, TYPE, 1, DISABLE.getStatus(), 0.8, 4096, 10);

        assertEquals(DISABLE.getStatus(), aiModelRepository.findById(AiModelId.of(saved.id())).status().code());
    }

    @Test
    void getModelPage_passesAllFiltersToRepository() {
        applicationService.getModelPage(NAME, MODEL, PLATFORM, TYPE, DISABLE.getStatus(), 3, 20);

        assertEquals(new AiModelPageQuery(NAME, MODEL, PLATFORM, TYPE, DISABLE.getStatus(), 3, 20),
                aiModelRepository.lastPageQuery);
    }

    private static final class InMemoryAiModelRepository implements AiModelRepository {

        private final List<AiModel> models = new ArrayList<>();
        private AiModelPageQuery lastPageQuery;
        private long nextId = 1L;

        @Override
        public AiModel save(AiModel model) {
            AiModel saved = AiModelFactory.reconstitute(model.id() != null ? model.id() : nextId++,
                    model.keyId(), model.name(), model.model(), model.platform(), model.type(), model.sort(),
                    model.status().code(), model.temperature(), model.maxTokens(), model.maxContexts());
            models.removeIf(existing -> existing.id().equals(saved.id()));
            models.add(saved);
            return saved;
        }

        @Override
        public void delete(AiModelId id) {
            models.removeIf(model -> model.modelId().equals(id));
        }

        @Override
        public AiModel findById(AiModelId id) {
            return models.stream()
                    .filter(model -> model.modelId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<AiModel> findAll() {
            return List.copyOf(models);
        }

        @Override
        public List<AiModel> findByStatusAndType(Integer status, Integer type, String platform) {
            return List.of();
        }

        @Override
        public AiModel findFirstByStatus(Integer type, Integer status) {
            return null;
        }

        @Override
        public PageResult<AiModel> findPage(AiModelPageQuery query) {
            lastPageQuery = query;
            return PageResult.empty();
        }
    }
}
