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
