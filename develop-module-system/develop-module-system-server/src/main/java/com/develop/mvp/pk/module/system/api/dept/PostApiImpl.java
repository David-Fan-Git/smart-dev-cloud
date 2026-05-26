package com.develop.mvp.pk.module.system.api.dept;

import com.develop.mvp.pk.module.system.application.dept.port.inbound.DeptUseCase;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.system.api.dept.dto.PostRespDTO;
import com.develop.mvp.pk.module.system.dal.dataobject.dept.PostDO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

/**
 * Post Api Impl 模块 API 实现。
 */
@RestController // 提供 RESTful API 接口，给 Feign 调用
@Validated
public class PostApiImpl implements PostApi {

    @Resource
    private DeptUseCase postUseCase;

    /**
     * 执行 valid Post List 对应的业务操作。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<Boolean> validPostList(Collection<Long> ids) {
        postUseCase.validatePostList(ids);
        return success(true);
    }

    /**
     * 查询 get Post List 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<List<PostRespDTO>> getPostList(Collection<Long> ids) {
        List<PostDO> list = postUseCase.getPostList(ids);
        return success(BeanUtils.toBean(list, PostRespDTO.class));
    }

}
