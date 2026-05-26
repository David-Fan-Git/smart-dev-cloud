package com.develop.mvp.pk.module.system.api.dict;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.develop.mvp.pk.module.system.application.dict.port.inbound.DictUseCase;
import com.develop.mvp.pk.module.system.dal.dataobject.dict.DictDataDO;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

/**
 * Dict Data Api Impl 模块 API 实现。
 */
@RestController // 提供 RESTful API 接口，给 Feign 调用
@Validated
@Primary // 由于 DictDataCommonApi 的存在，必须声明为 @Primary Bean
public class DictDataApiImpl implements DictDataApi {

    @Resource
    private DictUseCase dictUseCase;

    /**
     * 校验 validate Dict Data List 对应的业务规则。
     *
     * @param dictType dictType 参数
     * @param values values 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<Boolean> validateDictDataList(String dictType, Collection<String> values) {
        dictUseCase.validateDictDataList(dictType, values);
        return success(true);
    }

    /**
     * 查询 get Dict Data List 对应的数据。
     *
     * @param dictType dictType 参数
     * @return 处理结果
     */
    @Override
    public CommonResult<List<DictDataRespDTO>> getDictDataList(String dictType) {
        List<DictDataDO> list = dictUseCase.getDictDataListByDictType(dictType);
        return success(BeanUtils.toBean(list, DictDataRespDTO.class));
    }

}
