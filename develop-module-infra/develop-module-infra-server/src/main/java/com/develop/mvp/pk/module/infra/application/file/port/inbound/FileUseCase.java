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
