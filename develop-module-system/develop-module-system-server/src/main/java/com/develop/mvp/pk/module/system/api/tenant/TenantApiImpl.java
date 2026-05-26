package com.develop.mvp.pk.module.system.api.tenant;

// Skill: AggregateRoot_Tenant_Validation_Skill — 适配 TenantApiImpl 使用 TenantUseCase 入站端口

import com.develop.mvp.pk.framework.common.biz.system.tenant.TenantCommonApi;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.tenant.core.aop.TenantIgnore;
import com.develop.mvp.pk.module.system.application.tenant.port.inbound.TenantUseCase;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

/**
 * Tenant Api Impl 模块 API 实现。
 */
@RestController
@Validated
public class TenantApiImpl implements TenantCommonApi {

    @Resource
    private TenantUseCase tenantUseCase;

    /**
     * 查询 get Tenant Id List 对应的数据。
     *
     * @return 处理结果
     */
    @Override
    @TenantIgnore
    public CommonResult<List<Long>> getTenantIdList() {
        return success(tenantUseCase.getTenantIdList());
    }

    /**
     * 执行 valid Tenant 对应的业务操作。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @Override
    @TenantIgnore
    public CommonResult<Boolean> validTenant(Long id) {
        tenantUseCase.getAndValidateTenant(id);
        return success(true);
    }

}
