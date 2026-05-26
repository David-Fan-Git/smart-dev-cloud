package com.develop.mvp.pk.module.infra.api.config;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.module.infra.application.config.port.inbound.ConfigUseCase;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

@RestController
@Validated
public class ConfigApiImpl implements ConfigApi {

    @Resource
    private ConfigUseCase configApplicationService;

    @Override
    public CommonResult<String> getConfigValueByKey(String key) {
        String value = configApplicationService.getConfigValueByKey(key);
        return success(value);
    }
}
