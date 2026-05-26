package com.develop.mvp.pk.module.system.framework.operatelog.core;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.develop.mvp.pk.module.system.dal.dataobject.user.AdminUserDO;
import com.develop.mvp.pk.module.system.application.user.port.inbound.AdminUserUseCase;
import com.mzt.logapi.service.IParseFunction;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 管理员名字的 {@link IParseFunction} 实现类
 *
 * @author David
 */
@Slf4j
@Component
public class AdminUserParseFunction implements IParseFunction {

    public static final String NAME = "getAdminUserById";

    @Resource
    private AdminUserUseCase adminUserService;

    /**
     * 执行 function Name 对应的业务操作。
     *
     * @return 处理结果
     */
    @Override
    public String functionName() {
        return NAME;
    }

    /**
     * 执行 apply 对应的业务操作。
     *
     * @param value value 参数
     * @return 处理结果
     */
    @Override
    public String apply(Object value) {
        if (StrUtil.isEmptyIfStr(value)) {
            return "";
        }

        // 获取用户信息
        AdminUserDO user = adminUserService.getUser(Convert.toLong(value));
        if (user == null) {
            log.warn("[apply][获取用户{{}}为空", value);
            return "";
        }
        // 返回格式 David(13888888888)
        String nickname = user.getNickname();
        if (StrUtil.isEmpty(user.getMobile())) {
            return nickname;
        }
        return StrUtil.format("{}({})", nickname, user.getMobile());
    }

}
