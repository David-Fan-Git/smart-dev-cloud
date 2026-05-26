package com.develop.mvp.pk.module.infra.application.file;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.infra.application.file.service.FileConfigApplicationService;
import com.develop.mvp.pk.module.infra.domain.event.DomainEventPublisher;
import com.develop.mvp.pk.module.infra.domain.file.FileConfig;
import com.develop.mvp.pk.module.infra.domain.file.repository.FileConfigPageQuery;
import com.develop.mvp.pk.module.infra.domain.file.repository.FileConfigRepository;
import com.develop.mvp.pk.module.infra.domain.file.valueobject.FileConfigId;
import com.develop.mvp.pk.module.infra.framework.file.core.client.FileClient;
import com.develop.mvp.pk.module.infra.framework.file.core.client.FileClientFactory;
import com.develop.mvp.pk.module.infra.framework.file.core.client.FileClientConfig;
import com.develop.mvp.pk.module.infra.framework.file.core.client.local.LocalFileClientConfig;
import com.develop.mvp.pk.module.infra.framework.file.core.enums.FileStorageEnum;
import com.develop.mvp.pk.module.infra.infrastructure.file.FileConfigFactory;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileConfigApplicationServiceTest {

    private InMemoryFileConfigRepository repository;
    private RecordingFileClientFactory fileClientFactory;
    private FileConfigApplicationService applicationService;

    @BeforeEach
    void setUp() {
        repository = new InMemoryFileConfigRepository();
        fileClientFactory = new RecordingFileClientFactory();
        DomainEventPublisher eventPublisher = event -> {};
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        applicationService = new FileConfigApplicationService(repository, eventPublisher, validator, fileClientFactory);
    }

    @Test
    void createFileConfig_preservesClientConfig() {
        Long id = applicationService.createFileConfig("本地存储", FileStorageEnum.LOCAL.getStorage(), false, localConfigMap(), "remark");

        LocalFileClientConfig config = assertInstanceOf(LocalFileClientConfig.class,
                repository.findById(FileConfigId.of(id)).config());
        assertEquals("/tmp/uploads", config.getBasePath());
        assertEquals("https://static.example.com", config.getDomain());
    }

    @Test
    void updateFileConfig_preservesClientConfig() {
        Long id = applicationService.createFileConfig("本地存储", FileStorageEnum.LOCAL.getStorage(), false, localConfigMap(), "remark");
        Map<String, Object> updatedConfig = localConfigMap();
        updatedConfig.put("basePath", "/data/uploads");

        applicationService.updateFileConfig(id, "本地存储", FileStorageEnum.LOCAL.getStorage(), updatedConfig, "updated");

        LocalFileClientConfig config = assertInstanceOf(LocalFileClientConfig.class,
                repository.findById(FileConfigId.of(id)).config());
        assertEquals("/data/uploads", config.getBasePath());
        assertEquals("https://static.example.com", config.getDomain());
    }

    @Test
    void testFileConfig_uploadsSampleImageAndReturnsUrl() throws Exception {
        Long id = applicationService.createFileConfig("本地存储", FileStorageEnum.LOCAL.getStorage(), false, localConfigMap(), "remark");

        String url = applicationService.testFileConfig(id);

        assertEquals("https://static.example.com/public/test.jpg", url);
        assertEquals(id, fileClientFactory.createdConfigId);
        assertEquals(FileStorageEnum.LOCAL.getStorage(), fileClientFactory.createdStorage);
        assertEquals(id, fileClientFactory.requestedConfigId);
        assertTrue(fileClientFactory.client.uploadedPath.startsWith("public/"));
        assertTrue(fileClientFactory.client.uploadedPath.endsWith(".jpg"));
        assertEquals("image/jpeg", fileClientFactory.client.uploadedType);
    }

    private static Map<String, Object> localConfigMap() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("basePath", "/tmp/uploads");
        config.put("domain", "https://static.example.com");
        return config;
    }

    private static final class RecordingFileClientFactory implements FileClientFactory {
        private final RecordingFileClient client = new RecordingFileClient();
        private Long createdConfigId;
        private Integer createdStorage;
        private Long requestedConfigId;

        @Override
        public FileClient getFileClient(Long configId) {
            requestedConfigId = configId;
            return client;
        }

        @Override
        public <Config extends FileClientConfig> void createOrUpdateFileClient(Long configId, Integer storage, Config config) {
            createdConfigId = configId;
            createdStorage = storage;
        }
    }

    private static final class RecordingFileClient implements FileClient {
        private String uploadedPath;
        private String uploadedType;

        @Override
        public Long getId() {
            return 1L;
        }

        @Override
        public String upload(byte[] content, String path, String type) {
            uploadedPath = path;
            uploadedType = type;
            return "https://static.example.com/public/test.jpg";
        }

        @Override
        public void delete(String path) {
        }

        @Override
        public byte[] getContent(String path) {
            return new byte[0];
        }
    }

    private static final class InMemoryFileConfigRepository implements FileConfigRepository {
        private final Map<Long, FileConfig> configs = new LinkedHashMap<>();
        private long nextId = 1L;

        @Override
        public FileConfig save(FileConfig config) {
            Long id = config.id() != null ? config.id().value() : nextId++;
            FileConfig saved = FileConfigFactory.reconstitute(id, config.name().value(), config.storage(),
                    config.master(), config.config(), config.remark());
            configs.put(id, saved);
            return saved;
        }

        @Override
        public void delete(FileConfigId id) {
            configs.remove(id.value());
        }

        @Override
        public void deleteByIds(Collection<FileConfigId> ids) {
            ids.forEach(this::delete);
        }

        @Override
        public FileConfig findById(FileConfigId id) {
            return configs.get(id.value());
        }

        @Override
        public FileConfig findByMaster() {
            return configs.values().stream().filter(FileConfig::isMaster).findFirst().orElse(null);
        }

        @Override
        public PageResult<FileConfig> findPage(FileConfigPageQuery query) {
            return new PageResult<>(findAll(), (long) configs.size());
        }

        @Override
        public List<FileConfig> findAll() {
            return new ArrayList<>(configs.values());
        }

        @Override
        public List<FileConfig> findByIds(Collection<FileConfigId> ids) {
            return ids.stream().map(this::findById).toList();
        }
    }
}
