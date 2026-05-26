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
