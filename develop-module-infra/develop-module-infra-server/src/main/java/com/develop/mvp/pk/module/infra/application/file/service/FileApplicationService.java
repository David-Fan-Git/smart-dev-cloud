package com.develop.mvp.pk.module.infra.application.file.service;

// DDD 角色：应用编排服务 - 文件上传/下载/删除

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.infra.application.file.port.inbound.FileUseCase;
import com.develop.mvp.pk.framework.common.util.http.HttpUtils;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.infra.domain.event.DomainEventPublisher;
import com.develop.mvp.pk.module.infra.domain.file.File;
import com.develop.mvp.pk.module.infra.domain.file.repository.FilePageQuery;
import com.develop.mvp.pk.module.infra.domain.file.repository.FileRepository;
import com.develop.mvp.pk.module.infra.domain.file.valueobject.FileId;
import com.develop.mvp.pk.module.infra.framework.file.core.client.FileClient;
import com.develop.mvp.pk.module.infra.framework.file.core.utils.FileTypeUtils;
import com.develop.mvp.pk.module.infra.infrastructure.file.FileFactory;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static cn.hutool.core.date.DatePattern.PURE_DATE_PATTERN;
import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.infra.enums.ErrorCodeConstants.FILE_NOT_EXISTS;

@Service
public class FileApplicationService implements FileUseCase {

    // 上传文件的前缀是否包含日期
    private static final boolean PATH_PREFIX_DATE_ENABLE = true;
    // 上传文件的后缀是否启用
    private static final boolean PATH_SUFFIX_TIMESTAMP_ENABLE = false;
    // 后缀是否作为上级目录
    private static final boolean PATH_SUFFIX_AS_DIRECTORY = true;

    private final FileRepository fileRepository;
    private final DomainEventPublisher eventPublisher;

    public FileApplicationService(FileRepository fileRepository,
                                   DomainEventPublisher eventPublisher) {
        this.fileRepository = fileRepository;
        this.eventPublisher = eventPublisher;
    }

    // ── 命令 ──

    /**
     * 上传文件并保存记录
     */
    @Transactional
    @SneakyThrows
    public String uploadFile(byte[] content, String name, String directory, String type,
                              FileClient fileClient) {
        // 处理 type 和 name
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

        // 生成 path 并上传
        String path = generateUploadPath(name, directory);
        Assert.notNull(fileClient, "客户端(master) 不能为空");
        String url = fileClient.upload(content, path, type);

        // 创建领域对象并保存
        File file = FileFactory.create(fileClient.getId(), name, path, url, type, (long) content.length);
        file = fileRepository.save(file);
        file.markUploaded();
        publishEvents(file);
        return url;
    }

    @Transactional
    public Long createFileRecord(Long configId, String name, String path, String url,
                                  String type, Long size) {
        url = HttpUtils.removeUrlQuery(url);
        File file = FileFactory.create(configId, name, path, url, type, size);
        file = fileRepository.save(file);
        file.markUploaded();
        publishEvents(file);
        return file.id().value();
    }

    @Transactional
    public void deleteFile(Long id, FileClient fileClient) throws Exception {
        File file = findExistingFile(FileId.of(id));
        if (fileClient != null) {
            fileClient.delete(file.path());
        }
        file.markDeleted();
        fileRepository.delete(file.id());
        publishEvents(file);
    }

    @Transactional
    public void deleteFileList(List<Long> ids, java.util.function.Function<Long, FileClient> clientProvider) throws Exception {
        for (Long id : ids) {
            File file = findExistingFile(FileId.of(id));
            FileClient client = clientProvider.apply(file.configId() != null ? file.configId().value() : null);
            if (client != null) {
                client.delete(file.path());
            }
            fileRepository.delete(file.id());
        }
    }

    // ── 查询 ──

    public File getFile(Long id) {
        return findExistingFile(FileId.of(id));
    }

    public PageResult<File> getFilePage(String path, String type,
                                         java.time.LocalDateTime[] createTime,
                                         Integer pageNo, Integer pageSize) {
        return fileRepository.findPage(new FilePageQuery(path, type, createTime, pageNo, pageSize));
    }

    public byte[] getFileContent(Long configId, String path,
                                  java.util.function.BiFunction<Long, String, byte[]> contentProvider) throws Exception {
        return contentProvider.apply(configId, path);
    }

    // ── 私有方法 ──

    private File findExistingFile(FileId id) {
        File file = fileRepository.findById(id);
        if (file == null) throw exception(FILE_NOT_EXISTS);
        return file;
    }

    String generateUploadPath(String name, String directory) {
        String prefix = null;
        if (PATH_PREFIX_DATE_ENABLE) {
            prefix = LocalDateTimeUtil.format(LocalDateTimeUtil.now(), PURE_DATE_PATTERN);
        }
        String suffix = null;
        if (PATH_SUFFIX_TIMESTAMP_ENABLE) {
            suffix = String.valueOf(System.currentTimeMillis()) + RandomUtil.randomInt(10000, 100000);
        }
        if (StrUtil.isNotEmpty(suffix)) {
            if (PATH_SUFFIX_AS_DIRECTORY) {
                name = suffix + StrUtil.SLASH + name;
            } else {
                String ext = FileUtil.extName(name);
                if (StrUtil.isNotEmpty(ext)) {
                    name = FileUtil.mainName(name) + StrUtil.C_UNDERLINE + suffix + StrUtil.DOT + ext;
                } else {
                    name = name + StrUtil.C_UNDERLINE + suffix;
                }
            }
        }
        if (StrUtil.isNotEmpty(prefix)) {
            name = prefix + StrUtil.SLASH + name;
        }
        if (StrUtil.isNotEmpty(directory)) {
            name = directory + StrUtil.SLASH + name;
        }
        return name;
    }

    private void publishEvents(File file) {
        for (var event : file.pullEvents()) {
            eventPublisher.publish(event);
        }
    }
}
