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
