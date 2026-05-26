package com.develop.mvp.pk.module.system.api.permission;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.module.system.application.permission.port.inbound.RoleUseCase;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.Collection;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

/**
 * Role Api Impl 模块 API 实现。
 */
@RestController // 提供 RESTful API 接口，给 Feign 调用
@Validated
public class RoleApiImpl implements RoleApi {

    @Resource
    private RoleUseCase roleService;

    /**
     * 执行 valid Role List 对应的业务操作。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<Boolean> validRoleList(Collection<Long> ids) {
        roleService.validateRoleList(ids);
        return success(true);
    }
}
