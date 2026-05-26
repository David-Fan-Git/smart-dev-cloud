# Infra File Boundary Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 收口 `develop-module-infra` File 子域调用边界，让 Controller/App/API 入口只调用 application 用例，不再传递或直接操作 `FileClient`。

**Architecture:** 本阶段只做 File 子域最小闭环：新增 application outbound port 隔离文件存储技术能力，由 infrastructure adapter 继续复用旧 `FileConfigServiceImpl` 的 master client、指定 client、缓存和失效语义。`FileApplicationService` 保留现有上传 name/type/path/url 规范化、删除顺序、分页和下载行为；旧 `FileServiceImpl` 与 `FileConfigServiceImpl` 不删除。

**Tech Stack:** Java 17, Spring Boot 3.5.x, JUnit 5, Mockito, Maven, MyBatis Plus, existing DDD/hexagonal packages.

---

## Scope guard

本计划只实施 `docs/superpowers/specs/2026-05-26-infra-ddd-boundary-hardening-design.md` 的阶段 1：文件链路调用边界。

- 不改 API local/remote 装配策略。
- 不改 logger、db、codegen、websocket。
- 不删除 `service/file/FileServiceImpl.java` 或 `service/file/FileConfigServiceImpl.java`。
- 不改变 Controller 路径、HTTP 方法、VO 字段、`CommonResult` 包装、权限注解、`@PermitAll`、`@TenantIgnore`。
- 不改变文件上传命名、path 生成、URL 去 query、文件删除顺序、批量删除遇错中断、下载 404/attachment 行为。
- 本阶段允许 `FileStorageAdapter` 依赖 legacy `FileConfigService`，因为它是当前缓存和 master 行为事实源。

## Files

- Create: `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/application/file/port/outbound/FileStoragePort.java`
  - Application outbound port，封装 upload/delete/getContent/presign/master config id。
- Create: `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/application/file/result/FilePresignedUrlResult.java`
  - Application 返回预签名上传结果，不让 application 依赖 Controller VO。
- Create: `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/infrastructure/file/external/FileStorageAdapter.java`
  - Infrastructure adapter，内部继续调用 `FileConfigService#getMasterFileClient()` 与 `getFileClient(id)`。
- Modify: `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/application/file/port/inbound/FileUseCase.java`
  - 删除调用方可见的 `FileClient`、`Function<Long, FileClient>`、`BiFunction<Long, String, byte[]>` 参数。
- Modify: `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/application/file/service/FileApplicationService.java`
  - 注入 `FileStoragePort`；迁移 upload/delete/list/content/presign 编排。
- Modify: `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/controller/admin/file/FileController.java`
  - 移除 `FileConfigService` 与 `FileClient` 依赖；将预签名 result 转为 `FilePresignedUrlRespVO`。
- Modify: `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/controller/app/file/AppFileController.java`
  - 文件入口改为调用 `FileUseCase`，不再调用旧 `FileService`。
- Modify: `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/api/file/FileApiImpl.java`
  - 移除 `FileConfigService` 与 `FileClient`，改为调用 `FileUseCase`。
- Create: `develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/infrastructure/file/external/FileStorageAdapterTest.java`
  - 锁定 adapter 复用 legacy client/cache 的调用边界。
- Create: `develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/application/file/FileApplicationServiceTest.java`
  - 锁定 application 行为：上传规范化、删除顺序、批量失败、下载、预签名。
- Create: `develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/controller/admin/file/FileControllerBoundaryTest.java`
  - 静态边界测试：Admin Controller 不再导入 `FileClient`/`FileConfigService`，仍保留关键映射和下载处理。
- Create: `develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/api/file/FileApiImplBoundaryTest.java`
  - 静态边界测试：API 实现不再导入 `FileClient`/`FileConfigService`，仍实现 `FileApi`。

---

### Task 1: Add File storage outbound contract

**Files:**
- Create: `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/application/file/port/outbound/FileStoragePort.java`
- Create: `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/application/file/result/FilePresignedUrlResult.java`

- [ ] **Step 1: Create application result type**

Create `FilePresignedUrlResult.java`:

```java
package com.develop.mvp.pk.module.infra.application.file.result;

public record FilePresignedUrlResult(
        Long configId,
        String path,
        String uploadUrl,
        String url
) {
}
```

- [ ] **Step 2: Create outbound port**

Create `FileStoragePort.java`:

```java
package com.develop.mvp.pk.module.infra.application.file.port.outbound;

public interface FileStoragePort {

    UploadResult uploadToMaster(byte[] content, String path, String type) throws Exception;

    void delete(Long configId, String path) throws Exception;

    byte[] getContent(Long configId, String path) throws Exception;

    PresignedPutResult presignPutFromMaster(String path);

    String presignGetFromMaster(String resourceUrl, Integer expirationSeconds);

    record UploadResult(Long configId, String url) {
    }

    record PresignedPutResult(Long configId, String uploadUrl, String url) {
    }
}
```

- [ ] **Step 3: Run compile and verify expected failure scope**

Run:

```bash
mvn compile -pl develop-module-infra/develop-module-infra-server -am -DskipTests
```

Expected: compile may still fail later only if these new files expose syntax/import errors. If it fails for unrelated pre-existing repository state, record the first failing module and continue only after confirming the failure is unrelated to these new files.

---

### Task 2: Add FileStorageAdapter with legacy client/cache reuse

**Files:**
- Create: `develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/infrastructure/file/external/FileStorageAdapterTest.java`
- Create: `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/infrastructure/file/external/FileStorageAdapter.java`

- [ ] **Step 1: Write adapter tests first**

Create `FileStorageAdapterTest.java`:

```java
package com.develop.mvp.pk.module.infra.infrastructure.file.external;

import com.develop.mvp.pk.module.infra.application.file.port.outbound.FileStoragePort;
import com.develop.mvp.pk.module.infra.framework.file.core.client.FileClient;
import com.develop.mvp.pk.module.infra.service.file.FileConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileStorageAdapterTest {

    private FileConfigService fileConfigService;
    private FileClient masterClient;
    private FileClient fileClient;
    private FileStorageAdapter adapter;

    @BeforeEach
    void setUp() {
        fileConfigService = mock(FileConfigService.class);
        masterClient = mock(FileClient.class);
        fileClient = mock(FileClient.class);
        adapter = new FileStorageAdapter(fileConfigService);
    }

    @Test
    void uploadToMaster_usesMasterClientAndReturnsClientIdAndUrl() throws Exception {
        byte[] content = new byte[]{1, 2, 3};
        when(fileConfigService.getMasterFileClient()).thenReturn(masterClient);
        when(masterClient.getId()).thenReturn(10L);
        when(masterClient.upload(content, "20260526/a.jpg", "image/jpeg")).thenReturn("https://static/a.jpg");

        FileStoragePort.UploadResult result = adapter.uploadToMaster(content, "20260526/a.jpg", "image/jpeg");

        assertEquals(10L, result.configId());
        assertEquals("https://static/a.jpg", result.url());
        verify(masterClient).upload(content, "20260526/a.jpg", "image/jpeg");
    }

    @Test
    void delete_usesConfigClientAndKeepsExplicitFailureWhenMissing() {
        when(fileConfigService.getFileClient(10L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> adapter.delete(10L, "a.jpg"));
    }

    @Test
    void getContent_usesConfigClient() throws Exception {
        byte[] content = new byte[]{1};
        when(fileConfigService.getFileClient(10L)).thenReturn(fileClient);
        when(fileClient.getContent("a.jpg")).thenReturn(content);

        assertArrayEquals(content, adapter.getContent(10L, "a.jpg"));
    }

    @Test
    void presignPutFromMaster_returnsMasterClientIdAndUrls() {
        when(fileConfigService.getMasterFileClient()).thenReturn(masterClient);
        when(masterClient.getId()).thenReturn(10L);
        when(masterClient.presignPutUrl("a.jpg")).thenReturn("https://upload/a.jpg");
        when(masterClient.presignGetUrl("a.jpg", null)).thenReturn("https://visit/a.jpg");

        FileStoragePort.PresignedPutResult result = adapter.presignPutFromMaster("a.jpg");

        assertEquals(10L, result.configId());
        assertEquals("https://upload/a.jpg", result.uploadUrl());
        assertEquals("https://visit/a.jpg", result.url());
    }

    @Test
    void presignGetFromMaster_delegatesResourceUrlAndExpiration() {
        when(fileConfigService.getMasterFileClient()).thenReturn(masterClient);
        when(masterClient.presignGetUrl("https://static/a.jpg?x=1", 60)).thenReturn("https://signed/a.jpg");

        assertEquals("https://signed/a.jpg", adapter.presignGetFromMaster("https://static/a.jpg?x=1", 60));
    }
}
```

- [ ] **Step 2: Run adapter test and verify it fails before implementation**

Run:

```bash
mvn test -pl develop-module-infra/develop-module-infra-server -Dtest=FileStorageAdapterTest
```

Expected: FAIL because `FileStorageAdapter` does not exist yet.

- [ ] **Step 3: Implement adapter**

Create `FileStorageAdapter.java`:

```java
package com.develop.mvp.pk.module.infra.infrastructure.file.external;

import cn.hutool.core.lang.Assert;
import com.develop.mvp.pk.module.infra.application.file.port.outbound.FileStoragePort;
import com.develop.mvp.pk.module.infra.framework.file.core.client.FileClient;
import com.develop.mvp.pk.module.infra.service.file.FileConfigService;
import org.springframework.stereotype.Component;

@Component
public class FileStorageAdapter implements FileStoragePort {

    private final FileConfigService fileConfigService;

    public FileStorageAdapter(FileConfigService fileConfigService) {
        this.fileConfigService = fileConfigService;
    }

    @Override
    public UploadResult uploadToMaster(byte[] content, String path, String type) throws Exception {
        FileClient client = fileConfigService.getMasterFileClient();
        Assert.notNull(client, "客户端(master) 不能为空");
        String url = client.upload(content, path, type);
        return new UploadResult(client.getId(), url);
    }

    @Override
    public void delete(Long configId, String path) throws Exception {
        FileClient client = fileConfigService.getFileClient(configId);
        Assert.notNull(client, "客户端({}) 不能为空", configId);
        client.delete(path);
    }

    @Override
    public byte[] getContent(Long configId, String path) throws Exception {
        FileClient client = fileConfigService.getFileClient(configId);
        Assert.notNull(client, "客户端({}) 不能为空", configId);
        return client.getContent(path);
    }

    @Override
    public PresignedPutResult presignPutFromMaster(String path) {
        FileClient client = fileConfigService.getMasterFileClient();
        Assert.notNull(client, "客户端(master) 不能为空");
        return new PresignedPutResult(client.getId(), client.presignPutUrl(path), client.presignGetUrl(path, null));
    }

    @Override
    public String presignGetFromMaster(String resourceUrl, Integer expirationSeconds) {
        FileClient client = fileConfigService.getMasterFileClient();
        Assert.notNull(client, "客户端(master) 不能为空");
        return client.presignGetUrl(resourceUrl, expirationSeconds);
    }
}
```

- [ ] **Step 4: Run adapter test and verify it passes**

Run:

```bash
mvn test -pl develop-module-infra/develop-module-infra-server -Dtest=FileStorageAdapterTest
```

Expected: PASS.

---

### Task 3: Add FileApplicationService behavior tests for new boundary

**Files:**
- Create: `develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/application/file/FileApplicationServiceTest.java`

- [ ] **Step 1: Write FileApplicationService tests against no-client use case signatures**

Create `FileApplicationServiceTest.java`:

```java
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

import java.time.LocalDateTime;
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
        storagePort = new RecordingFileStoragePort();
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

        assertEquals("delete:a.jpg", storagePort.operations.get(0));
        assertFalse(repository.files.containsKey(file.id().value()));
    }

    @Test
    void deleteFileList_stopsOnFirstStorageFailureAndKeepsCurrentPartialEffectSemantics() {
        File first = repository.save(FileFactory.create(10L, "a.jpg", "a.jpg", "https://static/a.jpg", "image/jpeg", 3L));
        File second = repository.save(FileFactory.create(11L, "b.jpg", "b.jpg", "https://static/b.jpg", "image/jpeg", 3L));
        storagePort.failDeletePath = "b.jpg";

        assertThrows(IllegalStateException.class,
                () -> applicationService.deleteFileList(List.of(first.id().value(), second.id().value())));

        assertFalse(repository.files.containsKey(first.id().value()));
        assertTrue(repository.files.containsKey(second.id().value()));
        assertEquals(List.of("delete:a.jpg", "delete:b.jpg"), storagePort.operations);
    }

    @Test
    void getFileContent_readsThroughStoragePort() throws Exception {
        storagePort.content = new byte[]{9};

        assertArrayEquals(new byte[]{9}, applicationService.getFileContent(10L, "a.jpg"));
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
        private String uploadUrl = "https://static.example.com/uploaded.jpg";
        private String uploadPath;
        private String uploadType;
        private byte[] content;
        private String failDeletePath;
        private String presignGetResourceUrl;
        private Integer presignGetExpirationSeconds;
        private final List<String> operations = new ArrayList<>();

        @Override
        public UploadResult uploadToMaster(byte[] content, String path, String type) {
            this.uploadPath = path;
            this.uploadType = type;
            return new UploadResult(10L, uploadUrl);
        }

        @Override
        public void delete(Long configId, String path) {
            operations.add("delete:" + path);
            if (path.equals(failDeletePath)) {
                throw new IllegalStateException("delete failed");
            }
        }

        @Override
        public byte[] getContent(Long configId, String path) {
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
```

- [ ] **Step 2: Run application test and verify it fails before service migration**

Run:

```bash
mvn test -pl develop-module-infra/develop-module-infra-server -Dtest=FileApplicationServiceTest
```

Expected: FAIL because `FileApplicationService` constructor and `FileUseCase` signatures do not yet match the new tests.

---

### Task 4: Migrate FileUseCase and FileApplicationService

**Files:**
- Modify: `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/application/file/port/inbound/FileUseCase.java`
- Modify: `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/application/file/service/FileApplicationService.java`

- [ ] **Step 1: Replace FileUseCase signatures**

Update `FileUseCase.java` to this shape:

```java
package com.develop.mvp.pk.module.infra.application.file.port.inbound;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.infra.application.file.result.FilePresignedUrlResult;
import com.develop.mvp.pk.module.infra.domain.file.File;

import java.time.LocalDateTime;
import java.util.List;

public interface FileUseCase {

    String createFile(byte[] content, String name, String directory, String type);

    Long createFileRecord(Long configId, String name, String path, String url, String type, Long size);

    void deleteFile(Long id) throws Exception;

    void deleteFileList(List<Long> ids) throws Exception;

    File getFile(Long id);

    PageResult<File> getFilePage(String path, String type, LocalDateTime[] createTime, Integer pageNo, Integer pageSize);

    byte[] getFileContent(Long configId, String path) throws Exception;

    FilePresignedUrlResult presignPutUrl(String name, String directory);

    String presignGetUrl(String resourceUrl, Integer expirationSeconds);
}
```

- [ ] **Step 2: Modify FileApplicationService constructor and fields**

In `FileApplicationService.java`, remove imports of `FileClient`, `Function`, and `BiFunction`. Add imports for `FileStoragePort` and `FilePresignedUrlResult`.

Use constructor:

```java
private final FileRepository fileRepository;
private final DomainEventPublisher eventPublisher;
private final FileStoragePort fileStoragePort;

public FileApplicationService(FileRepository fileRepository,
                              DomainEventPublisher eventPublisher,
                              FileStoragePort fileStoragePort) {
    this.fileRepository = fileRepository;
    this.eventPublisher = eventPublisher;
    this.fileStoragePort = fileStoragePort;
}
```

- [ ] **Step 3: Rename upload method to createFile and use outbound port**

Replace the current `uploadFile(byte[] content, String name, String directory, String type, FileClient fileClient)` method with:

```java
@Override
@Transactional
@SneakyThrows
public String createFile(byte[] content, String name, String directory, String type) {
    if (StrUtil.isEmpty(type)) {
        type = FileTypeUtils.getMineType(content, name);
    }
    if (StrUtil.isEmpty(name)) {
        name = DigestUtil.sha256Hex(content);
    }
    if (StrUtil.isEmpty(FileUtil.extName(name))) {
        String extension = FileTypeUtils.getExtension(type);
        if (StrUtil.isNotEmpty(extension)) {
            name = name + extension;
        }
    }

    String path = generateUploadPath(name, directory);
    FileStoragePort.UploadResult uploadResult = fileStoragePort.uploadToMaster(content, path, type);

    File file = FileFactory.create(uploadResult.configId(), name, path, uploadResult.url(), type, (long) content.length);
    file = fileRepository.save(file);
    file.markUploaded();
    publishEvents(file);
    return uploadResult.url();
}
```

- [ ] **Step 4: Replace delete methods to use outbound port**

Replace `deleteFile(Long id, FileClient fileClient)` with:

```java
@Override
@Transactional
public void deleteFile(Long id) throws Exception {
    File file = findExistingFile(FileId.of(id));
    fileStoragePort.delete(file.configId() != null ? file.configId().value() : null, file.path());
    file.markDeleted();
    fileRepository.delete(file.id());
    publishEvents(file);
}
```

Replace `deleteFileList(List<Long> ids, Function<Long, FileClient> clientProvider)` with:

```java
@Override
@Transactional
public void deleteFileList(List<Long> ids) throws Exception {
    for (Long id : ids) {
        File file = findExistingFile(FileId.of(id));
        fileStoragePort.delete(file.configId() != null ? file.configId().value() : null, file.path());
        file.markDeleted();
        fileRepository.delete(file.id());
        publishEvents(file);
    }
}
```

This preserves the current per-item sequence: find metadata, delete storage, then delete DB metadata. If storage delete throws, current item metadata remains and later ids are not processed.

- [ ] **Step 5: Replace content and presign methods**

Replace the provider-based content method with:

```java
@Override
public byte[] getFileContent(Long configId, String path) throws Exception {
    return fileStoragePort.getContent(configId, path);
}
```

Add:

```java
@Override
public FilePresignedUrlResult presignPutUrl(String name, String directory) {
    String path = generateUploadPath(name, directory);
    FileStoragePort.PresignedPutResult result = fileStoragePort.presignPutFromMaster(path);
    return new FilePresignedUrlResult(result.configId(), path, result.uploadUrl(), result.url());
}

@Override
public String presignGetUrl(String resourceUrl, Integer expirationSeconds) {
    return fileStoragePort.presignGetFromMaster(resourceUrl, expirationSeconds);
}
```

- [ ] **Step 6: Run application and adapter tests**

Run:

```bash
mvn test -pl develop-module-infra/develop-module-infra-server -Dtest=FileApplicationServiceTest,FileStorageAdapterTest
```

Expected: PASS.

---

### Task 5: Migrate Admin FileController to pure application boundary

**Files:**
- Modify: `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/controller/admin/file/FileController.java`
- Create: `develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/controller/admin/file/FileControllerBoundaryTest.java`

- [ ] **Step 1: Write boundary test**

Create `FileControllerBoundaryTest.java`:

```java
package com.develop.mvp.pk.module.infra.controller.admin.file;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileControllerBoundaryTest {

    @Test
    void fileController_doesNotDependOnFileClientOrFileConfigService() throws Exception {
        String source = Files.readString(Path.of("src/main/java/com/develop/mvp/pk/module/infra/controller/admin/file/FileController.java"));

        assertFalse(source.contains("framework.file.core.client.FileClient"));
        assertFalse(source.contains("service.file.FileConfigService"));
        assertFalse(source.contains("getMasterFileClient()"));
        assertFalse(source.contains("getFileClient("));
        assertTrue(source.contains("fileApplicationService.createFile("));
        assertTrue(source.contains("fileApplicationService.presignPutUrl("));
        assertTrue(source.contains("fileApplicationService.getFileContent("));
        assertTrue(source.contains("writeAttachment(response, path, content)"));
        assertTrue(source.contains("response.setStatus(HttpStatus.NOT_FOUND.value())"));
    }
}
```

- [ ] **Step 2: Run boundary test and verify it fails before controller migration**

Run:

```bash
mvn test -pl develop-module-infra/develop-module-infra-server -Dtest=FileControllerBoundaryTest
```

Expected: FAIL because current controller still imports `FileClient` and `FileConfigService`.

- [ ] **Step 3: Remove FileClient/FileConfigService from FileController**

In `FileController.java`:

Remove imports:

```java
import com.develop.mvp.pk.module.infra.framework.file.core.client.FileClient;
import com.develop.mvp.pk.module.infra.service.file.FileConfigService;
```

Add import:

```java
import com.develop.mvp.pk.module.infra.application.file.result.FilePresignedUrlResult;
```

Remove field:

```java
@Resource
private FileConfigService fileConfigService;
```

- [ ] **Step 4: Update upload endpoint**

Replace body with:

```java
MultipartFile file = uploadReqVO.getFile();
byte[] content = IoUtil.readBytes(file.getInputStream());
return success(fileApplicationService.createFile(content, file.getOriginalFilename(),
        uploadReqVO.getDirectory(), file.getContentType()));
```

- [ ] **Step 5: Update presigned-url endpoint**

Replace body with:

```java
FilePresignedUrlResult result = fileApplicationService.presignPutUrl(name, directory);
return success(new FilePresignedUrlRespVO().setConfigId(result.configId())
        .setPath(result.path()).setUploadUrl(result.uploadUrl()).setUrl(result.url()));
```

- [ ] **Step 6: Update delete endpoints**

Replace single delete body with:

```java
fileApplicationService.deleteFile(id);
return success(true);
```

Replace batch delete body with:

```java
fileApplicationService.deleteFileList(ids);
return success(true);
```

- [ ] **Step 7: Update download endpoint**

Replace direct client access with:

```java
byte[] content = fileApplicationService.getFileContent(configId, path);
if (content == null) {
    log.warn("[getFileContent][configId({}) path({}) 文件不存在]", configId, path);
    response.setStatus(HttpStatus.NOT_FOUND.value());
    return;
}
writeAttachment(response, path, content);
```

- [ ] **Step 8: Run boundary test and compile**

Run:

```bash
mvn test -pl develop-module-infra/develop-module-infra-server -Dtest=FileControllerBoundaryTest
mvn compile -pl develop-module-infra/develop-module-infra-server -am -DskipTests
```

Expected: boundary test PASS and compile PASS.

---

### Task 6: Migrate AppFileController and FileApiImpl callers

**Files:**
- Modify: `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/controller/app/file/AppFileController.java`
- Modify: `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/api/file/FileApiImpl.java`
- Create: `develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/api/file/FileApiImplBoundaryTest.java`

- [ ] **Step 1: Write API implementation boundary test**

Create `FileApiImplBoundaryTest.java`:

```java
package com.develop.mvp.pk.module.infra.api.file;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileApiImplBoundaryTest {

    @Test
    void fileApiImpl_doesNotDependOnFileClientOrFileConfigService() throws Exception {
        String source = Files.readString(Path.of("src/main/java/com/develop/mvp/pk/module/infra/api/file/FileApiImpl.java"));

        assertFalse(source.contains("framework.file.core.client.FileClient"));
        assertFalse(source.contains("service.file.FileConfigService"));
        assertFalse(source.contains("getMasterFileClient()"));
        assertTrue(source.contains("implements FileApi"));
        assertTrue(source.contains("fileApplicationService.createFile("));
        assertTrue(source.contains("fileApplicationService.presignGetUrl("));
    }
}
```

- [ ] **Step 2: Run API boundary test and verify it fails before migration**

Run:

```bash
mvn test -pl develop-module-infra/develop-module-infra-server -Dtest=FileApiImplBoundaryTest
```

Expected: FAIL because current API implementation still imports `FileClient` and `FileConfigService`.

- [ ] **Step 3: Migrate FileApiImpl**

In `FileApiImpl.java`, remove imports and field for `FileClient` and `FileConfigService`.

Replace `createFile(FileCreateReqDTO createReqDTO)` body with:

```java
String url = fileApplicationService.createFile(
        createReqDTO.getContent(), createReqDTO.getName(),
        createReqDTO.getDirectory(), createReqDTO.getType());
return success(url);
```

Replace `presignGetUrl(String url, Integer expirationSeconds)` body with:

```java
return success(fileApplicationService.presignGetUrl(url, expirationSeconds));
```

- [ ] **Step 4: Migrate AppFileController from legacy FileService to FileUseCase**

In `AppFileController.java`, replace:

```java
import com.develop.mvp.pk.module.infra.service.file.FileService;
```

with:

```java
import com.develop.mvp.pk.module.infra.application.file.port.inbound.FileUseCase;
import com.develop.mvp.pk.module.infra.application.file.result.FilePresignedUrlResult;
```

Replace field:

```java
@Resource
private FileService fileService;
```

with:

```java
@Resource
private FileUseCase fileApplicationService;
```

Replace upload body with:

```java
MultipartFile file = uploadReqVO.getFile();
byte[] content = IoUtil.readBytes(file.getInputStream());
return success(fileApplicationService.createFile(content, file.getOriginalFilename(),
        uploadReqVO.getDirectory(), file.getContentType()));
```

Replace presigned-url body with:

```java
FilePresignedUrlResult result = fileApplicationService.presignPutUrl(name, directory);
return success(new FilePresignedUrlRespVO().setConfigId(result.configId())
        .setPath(result.path()).setUploadUrl(result.uploadUrl()).setUrl(result.url()));
```

Replace create-file body with:

```java
return success(fileApplicationService.createFileRecord(
        createReqVO.getConfigId(), createReqVO.getName(), createReqVO.getPath(),
        createReqVO.getUrl(), createReqVO.getType(), createReqVO.getSize()));
```

- [ ] **Step 5: Run migrated caller tests and compile**

Run:

```bash
mvn test -pl develop-module-infra/develop-module-infra-server -Dtest=FileApiImplBoundaryTest,FileControllerBoundaryTest
mvn compile -pl develop-module-infra/develop-module-infra-server -am -DskipTests
```

Expected: tests PASS and compile PASS.

---

### Task 7: Run phase verification and preserve legacy baseline

**Files:**
- No production file changes.
- Optional commit if the user has authorized local commits for this execution session.

- [ ] **Step 1: Run focused File tests**

Run:

```bash
mvn test -pl develop-module-infra/develop-module-infra-server -Dtest=FileApplicationServiceTest,FileStorageAdapterTest,FileControllerBoundaryTest,FileApiImplBoundaryTest,FileServiceImplTest,FileConfigServiceImplTest
```

Expected: PASS. This verifies new boundary tests plus legacy service/cache baseline.

- [ ] **Step 2: Run existing File client tests**

Run:

```bash
mvn test -pl develop-module-infra/develop-module-infra-server -Dtest=LocalFileClientTest,FtpFileClientTest,SftpFileClientTest,S3FileClientTest
```

Expected: PASS. This checks storage clients and existing S3 presign behavior were not weakened.

- [ ] **Step 3: Run infra API/server compile**

Run:

```bash
mvn compile -pl develop-module-infra/develop-module-infra-api -am -DskipTests
mvn compile -pl develop-module-infra/develop-module-infra-server -am -DskipTests
```

Expected: PASS.

- [ ] **Step 4: Run develop-server compile contract check**

Run:

```bash
mvn compile -pl develop-server -am -DskipTests
```

Expected: PASS. If this fails because of modules outside Infra File, record the first non-Infra failure and run the narrower infra server compile again to prove this phase did not break File boundary code.

- [ ] **Step 5: Run dependency boundary grep checks**

Run:

```bash
grep -R "framework.file.core.client.FileClient\|service.file.FileConfigService" \
  develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/controller/admin/file/FileController.java \
  develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/controller/app/file/AppFileController.java \
  develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/api/file/FileApiImpl.java
```

Expected: command exits with no matches. If using shell `grep`, no matches may return exit code 1; treat that as the expected boundary result after visually confirming no matching lines were printed.

- [ ] **Step 6: Inspect git diff for scope creep**

Run:

```bash
git diff -- develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/application/file \
  develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/infrastructure/file \
  develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/controller/admin/file/FileController.java \
  develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/controller/app/file/AppFileController.java \
  develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/api/file/FileApiImpl.java \
  develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/application/file \
  develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/infrastructure/file \
  develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/controller/admin/file \
  develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/api/file
```

Expected: diff only contains File boundary changes from this plan. No logger/db/codegen/API-local-remote changes.

- [ ] **Step 7: Commit if authorized**

If the user has explicitly authorized committing in the execution session, run:

```bash
git status --short
git add develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/application/file/port/outbound/FileStoragePort.java \
  develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/application/file/result/FilePresignedUrlResult.java \
  develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/infrastructure/file/external/FileStorageAdapter.java \
  develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/application/file/port/inbound/FileUseCase.java \
  develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/application/file/service/FileApplicationService.java \
  develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/controller/admin/file/FileController.java \
  develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/controller/app/file/AppFileController.java \
  develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/api/file/FileApiImpl.java \
  develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/infrastructure/file/external/FileStorageAdapterTest.java \
  develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/application/file/FileApplicationServiceTest.java \
  develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/controller/admin/file/FileControllerBoundaryTest.java \
  develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/api/file/FileApiImplBoundaryTest.java
git commit -m "$(cat <<'EOF'
DDD重构：收口 Infra File 调用边界

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>
EOF
)"
```

Expected: commit succeeds. If hooks fail, fix the underlying issue and create a new commit; do not bypass hooks.

---

## Acceptance checklist

- [ ] `FileUseCase` no longer imports or exposes `FileClient`、`Function<Long, FileClient>`、`BiFunction<Long, String, byte[]>`。
- [ ] `FileController` 上传、预签名、下载、删除、批量删除入口均不直接依赖 `FileClient` 或 `FileConfigService`。
- [ ] `AppFileController` 文件上传、预签名、前端直传记录创建均调用 `FileUseCase`。
- [ ] `FileApiImpl` `createFile` 与 `presignGetUrl` 均调用 `FileUseCase`，`FileApi` 默认 helper 和 `CommonResult#getCheckedData` 语义未变。
- [ ] `FileStorageAdapter` 是唯一新增的 legacy `FileConfigService` 调用归口，并继续复用 `getMasterFileClient()`、`getFileClient(id)`。
- [ ] `/infra/file/presigned-url` 返回字段仍为 `configId`、`path`、`uploadUrl`、`url`，且 `configId` 来自实际 master client。
- [ ] `/infra/file/{configId}/get/**` 空内容仍返回 HTTP 404，非空内容仍 `writeAttachment`。
- [ ] 上传 name/type/path/url 规范化行为由 `FileApplicationService` 保留。
- [ ] 删除仍保持“先删存储、再删 DB 元数据”。
- [ ] 批量删除仍保持逐条执行，遇错中断，不改为吞错继续或全部回滚。
- [ ] `FileConfigServiceImpl` cache、master pseudo-key `0L`、缓存失效、test upload 行为未删除。
- [ ] 未修改 logger/db/codegen/API local-remote 装配。
- [ ] 本计划列出的 focused tests、legacy File tests、infra compile 至少完成；无法执行的命令必须记录阻塞原因和未验证风险。
