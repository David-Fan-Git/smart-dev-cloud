package com.develop.mvp.pk.module.infra.application.file;

import cn.hutool.core.io.resource.ResourceUtil;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.http.HttpUtils;
import com.develop.mvp.pk.module.infra.application.file.port.outbound.FileStoragePort;
import com.develop.mvp.pk.module.infra.application.file.result.FilePresignedUrlResult;
import com.develop.mvp.pk.module.infra.application.file.service.FileApplicationService;
import com.develop.mvp.pk.module.infra.domain.event.DomainEventPublisher;
import com.develop.mvp.pk.module.infra.domain.file.File;
import com.develop.mvp.pk.module.infra.domain.file.repository.FilePageQuery;
import com.develop.mvp.pk.module.infra.domain.file.repository.FileRepository;
import com.develop.mvp.pk.module.infra.domain.file.valueobject.FileId;
import com.develop.mvp.pk.module.infra.infrastructure.file.FileFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileApplicationServiceTest {

    private InMemoryFileRepository repository;
    private RecordingFileStoragePort storagePort;
    private FileApplicationService applicationService;

    @BeforeEach
    void setUp() {
        repository = new InMemoryFileRepository();
        storagePort = new RecordingFileStoragePort(repository);
        DomainEventPublisher eventPublisher = event -> {};
        applicationService = new FileApplicationService(repository, eventPublisher, storagePort);
    }

    @Test
    void createFile_emptyNameAndType_normalizesNameTypePathAndStoresUploadedMetadata() {
        byte[] content = ResourceUtil.readBytes("file/erweima.jpg");
        storagePort.uploadUrl = "https://static.example.com/a.jpg";

        String url = applicationService.createFile(content, null, null, null);

        assertEquals("https://static.example.com/a.jpg", url);
        File saved = repository.files.values().iterator().next();
        assertEquals(10L, saved.configId().value());
        assertEquals(url, saved.url());
        assertEquals((long) content.length, saved.size());
        assertEquals("6318848e882d8a7e7e82789d87608f684ee52d41966bfc8cad3ce15aad2b970e.jpg", saved.name());
        assertTrue(saved.path().matches("\\d{8}/6318848e882d8a7e7e82789d87608f684ee52d41966bfc8cad3ce15aad2b970e\\.jpg"));
        assertEquals(saved.path(), storagePort.uploadPath);
        assertEquals("image/jpeg", storagePort.uploadType);
    }

    @Test
    void createFileRecord_stripsUrlQueryBeforeSaving() {
        Long id = applicationService.createFileRecord(10L, "a.jpg", "a.jpg",
                "https://static.example.com/a.jpg?X-Amz-Signature=abc", "image/jpeg", 3L);

        assertEquals("https://static.example.com/a.jpg", repository.findById(FileId.of(id)).url());
        assertEquals(HttpUtils.removeUrlQuery("https://static.example.com/a.jpg?X-Amz-Signature=abc"),
                repository.findById(FileId.of(id)).url());
    }

    @Test
    void deleteFile_deletesStorageBeforeMetadata() throws Exception {
        File file = repository.save(FileFactory.create(10L, "a.jpg", "a.jpg", "https://static/a.jpg", "image/jpeg", 3L));

        applicationService.deleteFile(file.id().value());

        assertEquals("delete:a.jpg:metadataExists=true", storagePort.operations.get(0));
        assertEquals(List.of(10L), storagePort.deleteConfigIds);
        assertFalse(repository.files.containsKey(file.id().value()));
    }

    @Test
    void deleteFileList_stopsOnFirstStorageFailureAndKeepsCurrentPartialEffectSemantics() {
        File first = repository.save(FileFactory.create(10L, "a.jpg", "a.jpg", "https://static/a.jpg", "image/jpeg", 3L));
        File second = repository.save(FileFactory.create(11L, "b.jpg", "b.jpg", "https://static/b.jpg", "image/jpeg", 3L));
        File third = repository.save(FileFactory.create(12L, "c.jpg", "c.jpg", "https://static/c.jpg", "image/jpeg", 3L));
        storagePort.failDeletePath = "b.jpg";

        assertThrows(IllegalStateException.class,
                () -> applicationService.deleteFileList(List.of(first.id().value(), second.id().value(), third.id().value())));

        assertFalse(repository.files.containsKey(first.id().value()));
        assertTrue(repository.files.containsKey(second.id().value()));
        assertTrue(repository.files.containsKey(third.id().value()));
        assertEquals(List.of("delete:a.jpg:metadataExists=true", "delete:b.jpg:metadataExists=true"), storagePort.operations);
        assertEquals(List.of(10L, 11L), storagePort.deleteConfigIds);
    }

    @Test
    void getFileContent_readsThroughStoragePort() throws Exception {
        storagePort.content = new byte[]{9};

        assertArrayEquals(new byte[]{9}, applicationService.getFileContent(10L, "a.jpg"));
        assertEquals(10L, storagePort.lastGetContentConfigId);
        assertEquals("a.jpg", storagePort.lastGetContentPath);
    }

    @Test
    void presignPutUrl_generatesCurrentPathAndReturnsApplicationResult() {
        FilePresignedUrlResult result = applicationService.presignPutUrl("a.jpg", "avatar");

        assertEquals(10L, result.configId());
        assertTrue(result.path().matches("avatar/\\d{8}/a\\.jpg"));
        assertEquals("https://upload.example.com/" + result.path(), result.uploadUrl());
        assertEquals("https://visit.example.com/" + result.path(), result.url());
    }

    @Test
    void presignGetUrl_delegatesFullResourceUrlAndExpiration() {
        assertEquals("https://signed.example.com/a.jpg",
                applicationService.presignGetUrl("https://static.example.com/a.jpg?x=1", 60));
        assertEquals("https://static.example.com/a.jpg?x=1", storagePort.presignGetResourceUrl);
        assertEquals(60, storagePort.presignGetExpirationSeconds);
    }

    private static final class RecordingFileStoragePort implements FileStoragePort {
        private final InMemoryFileRepository repository;
        private String uploadUrl = "https://static.example.com/uploaded.jpg";
        private String uploadPath;
        private String uploadType;
        private byte[] content;
        private String failDeletePath;
        private String presignGetResourceUrl;
        private Integer presignGetExpirationSeconds;
        private Long lastGetContentConfigId;
        private String lastGetContentPath;
        private final List<Long> deleteConfigIds = new ArrayList<>();
        private final List<String> operations = new ArrayList<>();

        private RecordingFileStoragePort(InMemoryFileRepository repository) {
            this.repository = repository;
        }

        private boolean repositoryContainsPath(String path) {
            return repository.files.values().stream().anyMatch(file -> path.equals(file.path()));
        }

        @Override
        public UploadResult uploadToMaster(byte[] content, String path, String type) {
            this.uploadPath = path;
            this.uploadType = type;
            return new UploadResult(10L, uploadUrl);
        }

        @Override
        public void delete(Long configId, String path) {
            deleteConfigIds.add(configId);
            operations.add("delete:" + path + ":metadataExists=" + repositoryContainsPath(path));
            if (path.equals(failDeletePath)) {
                throw new IllegalStateException("delete failed");
            }
        }

        @Override
        public byte[] getContent(Long configId, String path) {
            this.lastGetContentConfigId = configId;
            this.lastGetContentPath = path;
            return content;
        }

        @Override
        public PresignedPutResult presignPutFromMaster(String path) {
            return new PresignedPutResult(10L,
                    "https://upload.example.com/" + path,
                    "https://visit.example.com/" + path);
        }

        @Override
        public String presignGetFromMaster(String resourceUrl, Integer expirationSeconds) {
            this.presignGetResourceUrl = resourceUrl;
            this.presignGetExpirationSeconds = expirationSeconds;
            return "https://signed.example.com/a.jpg";
        }
    }

    private static final class InMemoryFileRepository implements FileRepository {
        private final Map<Long, File> files = new LinkedHashMap<>();
        private long nextId = 1L;

        @Override
        public File save(File file) {
            Long id = file.id() != null ? file.id().value() : nextId++;
            File saved = FileFactory.reconstitute(id,
                    file.configId() != null ? file.configId().value() : null,
                    file.name(), file.path(), file.url(), file.type(), file.size());
            files.put(id, saved);
            return saved;
        }

        @Override
        public void delete(FileId id) {
            files.remove(id.value());
        }

        @Override
        public void deleteByIds(Collection<FileId> ids) {
            ids.forEach(this::delete);
        }

        @Override
        public File findById(FileId id) {
            return files.get(id.value());
        }

        @Override
        public PageResult<File> findPage(FilePageQuery query) {
            return new PageResult<>(new ArrayList<>(files.values()), (long) files.size());
        }

        @Override
        public List<File> findByIds(Collection<FileId> ids) {
            return ids.stream().map(this::findById).toList();
        }
    }
}
