package com.develop.mvp.pk.module.system.framework.operatelog.core;

import com.develop.mvp.pk.module.system.application.dept.port.inbound.DeptUseCase;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.develop.mvp.pk.module.system.dal.dataobject.dept.PostDO;
import com.mzt.logapi.service.IParseFunction;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 岗位名字的 {@link IParseFunction} 实现类
 *
 * @author David
 */
@Slf4j
@Component
public class PostParseFunction implements IParseFunction {

    public static final String NAME = "getPostById";

    @Resource
    private DeptUseCase postUseCase;

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

        // 获取岗位信息
        PostDO post = postUseCase.getPost(Convert.toLong(value));
        if (post == null) {
            log.warn("[apply][获取岗位{{}}为空", value);
            return "";
        }
        return post.getName();
    }

}
