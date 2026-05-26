package com.develop.mvp.pk.module.system.controller.admin.captcha;

import cn.hutool.core.util.StrUtil;
import com.develop.mvp.pk.framework.common.util.servlet.ServletUtils;
import com.develop.mvp.pk.framework.tenant.core.aop.TenantIgnore;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Captcha Controller 控制器。
 */
@Tag(name = "管理后台 - 验证码")
@RestController("adminCaptchaController")
@RequestMapping("/system/captcha")
public class CaptchaController {

    @Resource
    private CaptchaService captchaService;

    /**
     * 查询 get 对应的数据。
     *
     * @param data data 参数
     * @param request request 参数
     * @return 处理结果
     */
    @PostMapping({"/get"})
    @Operation(summary = "获得验证码")
    @PermitAll
    @TenantIgnore
    public ResponseModel get(@RequestBody CaptchaVO data, HttpServletRequest request) {
        assert request.getRemoteHost() != null;
        data.setBrowserInfo(getRemoteId(request));
        return captchaService.get(data);
    }

    /**
     * 校验 check 对应的业务规则。
     *
     * @param data data 参数
     * @param request request 参数
     * @return 处理结果
     */
    @PostMapping("/check")
    @Operation(summary = "校验验证码")
    @PermitAll
    @TenantIgnore
    public ResponseModel check(@RequestBody CaptchaVO data, HttpServletRequest request) {
        data.setBrowserInfo(getRemoteId(request));
        return captchaService.check(data);
    }

    /**
     * 查询 get Remote Id 对应的数据。
     *
     * @param request request 参数
     * @return 处理结果
     */
    public static String getRemoteId(HttpServletRequest request) {
        String ip = ServletUtils.getClientIP(request);
        String ua = request.getHeader("user-agent");
        if (StrUtil.isNotBlank(ip)) {
            return ip + ua;
        }
        return request.getRemoteAddr() + ua;
    }

}
