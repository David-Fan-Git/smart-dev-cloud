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
