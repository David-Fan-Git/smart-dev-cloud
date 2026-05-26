package com.develop.mvp.pk.module.infra.application.file.port.inbound;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.infra.domain.file.File;
import com.develop.mvp.pk.module.infra.framework.file.core.client.FileClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface FileUseCase {

    String uploadFile(byte[] content, String name, String directory, String type, FileClient fileClient);

    Long createFileRecord(Long configId, String name, String path, String url, String type, Long size);

    void deleteFile(Long id, FileClient fileClient) throws Exception;

    void deleteFileList(List<Long> ids, Function<Long, FileClient> clientProvider) throws Exception;

    File getFile(Long id);

    PageResult<File> getFilePage(String path, String type, LocalDateTime[] createTime, Integer pageNo, Integer pageSize);

    byte[] getFileContent(Long configId, String path, BiFunction<Long, String, byte[]> contentProvider) throws Exception;
}
