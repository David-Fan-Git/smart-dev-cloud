package com.develop.mvp.pk.module.system.api.dept;

import com.develop.mvp.pk.module.system.application.dept.port.inbound.DeptUseCase;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.system.api.dept.dto.DeptRespDTO;
import com.develop.mvp.pk.module.system.dal.dataobject.dept.DeptDO;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

/**
 * Dept Api Impl 模块 API 实现。
 */
@RestController // 提供 RESTful API 接口，给 Feign 调用
@Validated
public class DeptApiImpl implements DeptApi {

    @Resource
    private DeptUseCase deptUseCase;

    /**
     * 查询 get Dept 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<DeptRespDTO> getDept(Long id) {
        DeptDO dept = deptUseCase.getDept(id);
        return success(BeanUtils.toBean(dept, DeptRespDTO.class));
    }

    /**
     * 查询 get Dept List 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<List<DeptRespDTO>> getDeptList(Collection<Long> ids) {
        List<DeptDO> depts = deptUseCase.getDeptList(ids);
        return success(BeanUtils.toBean(depts, DeptRespDTO.class));
    }

    /**
     * 校验 validate Dept List 对应的业务规则。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<Boolean> validateDeptList(Collection<Long> ids) {
        deptUseCase.validateDeptList(ids);
        return success(true);
    }

    /**
     * 查询 get Child Dept List 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<List<DeptRespDTO>> getChildDeptList(Long id) {
        List<DeptDO> depts = deptUseCase.getChildDeptList(id);
        return success(BeanUtils.toBean(depts, DeptRespDTO.class));
    }

}
