package com.develop.mvp.pk.module.infra.api.file;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.module.infra.api.file.dto.FileCreateReqDTO;
import com.develop.mvp.pk.module.infra.application.file.port.inbound.FileUseCase;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

@RestController
@Validated
public class FileApiImpl implements FileApi {

    @Resource
    private FileUseCase fileApplicationService;

    @Override
    public CommonResult<String> createFile(FileCreateReqDTO createReqDTO) {
        String url = fileApplicationService.createFile(
                createReqDTO.getContent(), createReqDTO.getName(),
                createReqDTO.getDirectory(), createReqDTO.getType());
        return success(url);
    }

    @Override
    public CommonResult<String> presignGetUrl(String url, Integer expirationSeconds) {
        return success(fileApplicationService.presignGetUrl(url, expirationSeconds));
    }
}
