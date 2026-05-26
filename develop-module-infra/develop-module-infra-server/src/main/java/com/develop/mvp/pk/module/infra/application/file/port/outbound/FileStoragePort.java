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
