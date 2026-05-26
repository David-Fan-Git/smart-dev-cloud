package com.develop.mvp.pk.module.system.framework.operatelog.core;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.develop.mvp.pk.framework.ip.core.utils.AreaUtils;
import com.mzt.logapi.service.IParseFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 地名的 {@link IParseFunction} 实现类
 *
 * @author David
 */
@Slf4j
@Component
public class AreaParseFunction implements IParseFunction {

    public static final String NAME = "getArea";

    /**
     * 执行 execute Before 对应的业务操作。
     *
     * @return 处理结果
     */
    @Override
    public boolean executeBefore() {
        return true; // 先转换值后对比
    }

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
        return AreaUtils.format(Convert.toInt(value));
    }

}
