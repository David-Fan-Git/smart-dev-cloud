package com.develop.mvp.pk.module.system.controller.app.tenant;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.tenant.core.aop.TenantIgnore;
import com.develop.mvp.pk.module.system.application.tenant.port.inbound.TenantUseCase;
import com.develop.mvp.pk.module.system.controller.app.tenant.vo.AppTenantRespVO;
import com.develop.mvp.pk.module.system.domain.tenant.Tenant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

/**
 * App Tenant Controller 控制器。
 */
@Tag(name = "用户 App - 租户")
@RestController
@RequestMapping("/system/tenant")
@Validated
public class AppTenantController {

    @Resource
    private TenantUseCase tenantUseCase;

    /**
     * 查询 get Tenant By Website 对应的数据。
     *
     * @param website website 参数
     * @return 处理结果
     */
    @GetMapping("/get-by-website")
    @PermitAll
    @TenantIgnore
    @Operation(summary = "使用域名，获得租户信息", description = "根据用户的域名，获得租户信息")
    @Parameter(name = "website", description = "域名", required = true, example = "www.iocoder.cn")
    public CommonResult<AppTenantRespVO> getTenantByWebsite(
            @RequestParam("website") @Pattern(regexp = "^[a-zA-Z0-9.-]+(:\\d{1,5})?$", message = "网站域名格式不正确") String website) {
        Tenant tenant = tenantUseCase.getTenantByWebsite(website);
        if (tenant == null || tenant.isDisabled()) {
            return success(null);
        }
        return success(new AppTenantRespVO().setId(tenant.id().value()).setName(tenant.name().value()));
    }

}
