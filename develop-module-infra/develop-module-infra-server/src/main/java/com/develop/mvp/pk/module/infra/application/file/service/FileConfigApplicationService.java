package com.develop.mvp.pk.module.infra.application.file.service;

// DDD 角色：应用编排服务
// 规则 R01：只能有一个 Master 文件配置
// 规则 R02：Master 配置不可删除

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.infra.application.file.port.inbound.FileConfigUseCase;
import com.develop.mvp.pk.framework.common.util.json.JsonUtils;
import com.develop.mvp.pk.framework.common.util.validation.ValidationUtils;
import com.develop.mvp.pk.module.infra.domain.event.DomainEventPublisher;
import com.develop.mvp.pk.module.infra.domain.file.FileConfig;
import com.develop.mvp.pk.module.infra.domain.file.repository.FileConfigPageQuery;
import com.develop.mvp.pk.module.infra.domain.file.repository.FileConfigRepository;
import com.develop.mvp.pk.module.infra.domain.file.valueobject.FileConfigId;
import com.develop.mvp.pk.module.infra.framework.file.core.client.FileClient;
import com.develop.mvp.pk.module.infra.framework.file.core.client.FileClientConfig;
import com.develop.mvp.pk.module.infra.framework.file.core.client.FileClientFactory;
import com.develop.mvp.pk.module.infra.framework.file.core.enums.FileStorageEnum;
import com.develop.mvp.pk.module.infra.infrastructure.file.FileConfigFactory;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.infra.enums.ErrorCodeConstants.FILE_CONFIG_DELETE_FAIL_MASTER;
import static com.develop.mvp.pk.module.infra.enums.ErrorCodeConstants.FILE_CONFIG_NOT_EXISTS;

@Service
public class FileConfigApplicationService implements FileConfigUseCase {

    private final FileConfigRepository fileConfigRepository;
    private final DomainEventPublisher eventPublisher;
    private final Validator validator;
    /**
     * 迁移期只在“测试指定文件配置”这个用例中直接使用 FileClientFactory。
     *
     * <p>完整的主配置缓存、异步刷新和 CACHE_MASTER_ID 行为仍由 legacy FileConfigServiceImpl 保护，
     * 不能在没有完整回归测试前整体替换，否则文件上传、预签名和删除链路都可能受影响。</p>
     */
    private final FileClientFactory fileClientFactory;

    public FileConfigApplicationService(FileConfigRepository fileConfigRepository,
                                         DomainEventPublisher eventPublisher,
                                         Validator validator,
                                         FileClientFactory fileClientFactory) {
        this.fileConfigRepository = fileConfigRepository;
        this.eventPublisher = eventPublisher;
        this.validator = validator;
        this.fileClientFactory = fileClientFactory;
    }

    // ── 命令 ──

    @Transactional
    public Long createFileConfig(String name, Integer storage, Boolean master,
                                  Map<String, Object> clientConfig, String remark) {
        FileConfig config = FileConfigFactory.create(name, storage, master,
                parseClientConfig(storage, clientConfig), remark);
        config = fileConfigRepository.save(config);
        config.markCreated();
        publishEvents(config);
        return config.id().value();
    }

    @Transactional
    public void updateFileConfig(Long id, String name, Integer storage,
                                  Map<String, Object> clientConfig, String remark) {
        FileConfig config = findExistingConfig(FileConfigId.of(id));
        config.updateProfile(storage, name, parseClientConfig(storage, clientConfig), remark);
        fileConfigRepository.save(config);
        publishEvents(config);
    }

    /** 规则 R01：设置为 Master */
    @Transactional
    public void updateFileConfigMaster(Long id) {
        findExistingConfig(FileConfigId.of(id));
        // 清除所有配置的 master 标记
        List<FileConfig> allConfigs = fileConfigRepository.findAll();
        for (FileConfig config : allConfigs) {
            if (config.isMaster()) {
                config.clearMaster();
                fileConfigRepository.save(config);
            }
        }
        // 设置新的 master
        FileConfig target = findExistingConfig(FileConfigId.of(id));
        target.setAsMaster();
        fileConfigRepository.save(target);
        publishEvents(target);
    }

    /** 规则 R02：Master 不可删除 */
    @Transactional
    public void deleteFileConfig(Long id) {
        FileConfig config = findExistingConfig(FileConfigId.of(id));
        if (config.isMaster()) {
            throw exception(FILE_CONFIG_DELETE_FAIL_MASTER);
        }
        config.markDeleted();
        fileConfigRepository.delete(config.id());
        publishEvents(config);
    }

    @Transactional
    public void deleteFileConfigList(List<Long> ids) {
        for (Long id : ids) {
            FileConfig config = findExistingConfig(FileConfigId.of(id));
            if (config.isMaster()) {
                throw exception(FILE_CONFIG_DELETE_FAIL_MASTER);
            }
        }
        for (Long id : ids) {
            fileConfigRepository.delete(FileConfigId.of(id));
        }
    }

    // ── 查询 ──

    public FileConfig getFileConfig(Long id) {
        return fileConfigRepository.findById(FileConfigId.of(id));
    }

    public PageResult<FileConfig> getFileConfigPage(String name, Integer storage,
                                                     java.time.LocalDateTime[] createTime,
                                                     Integer pageNo, Integer pageSize) {
        return fileConfigRepository.findPage(new FileConfigPageQuery(
                name, storage, createTime, pageNo, pageSize));
    }

    public String testFileConfig(Long id) throws Exception {
        FileConfig config = findExistingConfig(FileConfigId.of(id));
        fileClientFactory.createOrUpdateFileClient(config.id().value(), config.storage(), config.config());
        FileClient fileClient = fileClientFactory.getFileClient(config.id().value());
        byte[] content = ResourceUtil.readBytes("file/erweima.jpg");
        // 测试文件配置必须真的走目标存储客户端；只返回固定文案会掩盖存储参数错误。
        return fileClient.upload(content, "public" + StrUtil.SLASH + IdUtil.fastSimpleUUID() + ".jpg", "image/jpeg");
    }

    // ── 私有方法 ──

    private FileClientConfig parseClientConfig(Integer storage, Map<String, Object> config) {
        Class<? extends FileClientConfig> configClass = FileStorageEnum.getByStorage(storage).getConfigClass();
        FileClientConfig clientConfig = JsonUtils.parseObject2(JsonUtils.toJsonString(config), configClass);
        ValidationUtils.validate(validator, clientConfig);
        return clientConfig;
    }

    private FileConfig findExistingConfig(FileConfigId id) {
        FileConfig config = fileConfigRepository.findById(id);
        if (config == null) throw exception(FILE_CONFIG_NOT_EXISTS);
        return config;
    }

    private void publishEvents(FileConfig config) {
        for (var event : config.pullEvents()) {
            eventPublisher.publish(event);
        }
    }
}
