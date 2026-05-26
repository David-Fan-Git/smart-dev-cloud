package com.develop.mvp.pk.module.infra.api.file;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.module.infra.api.file.dto.FileCreateReqDTO;
import com.develop.mvp.pk.module.infra.application.file.port.inbound.FileUseCase;
import com.develop.mvp.pk.module.infra.framework.file.core.client.FileClient;
import com.develop.mvp.pk.module.infra.service.file.FileConfigService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

@RestController
@Validated
public class FileApiImpl implements FileApi {

    @Resource
    private FileUseCase fileApplicationService;
    @Resource
    private FileConfigService fileConfigService;

    @Override
    public CommonResult<String> createFile(FileCreateReqDTO createReqDTO) {
        FileClient masterClient = fileConfigService.getMasterFileClient();
        String url = fileApplicationService.uploadFile(
                createReqDTO.getContent(), createReqDTO.getName(),
                createReqDTO.getDirectory(), createReqDTO.getType(), masterClient);
        return success(url);
    }

    @Override
    public CommonResult<String> presignGetUrl(String url, Integer expirationSeconds) {
        FileClient masterClient = fileConfigService.getMasterFileClient();
        return success(masterClient.presignGetUrl(url, expirationSeconds));
    }
}
