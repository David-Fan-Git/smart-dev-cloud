package com.develop.mvp.pk.module.system.dal.mysql.dict;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.system.controller.admin.dict.vo.data.DictDataPageReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.dict.DictDataDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Dict Data Mapper 持久化 Mapper。
 */
@Mapper
public interface DictDataMapper extends BaseMapperX<DictDataDO> {

    /**
     * 查询 select By Dict Type And Value 对应的数据。
     *
     * @param dictType dictType 参数
     * @param value value 参数
     * @return 处理结果
     */
    default DictDataDO selectByDictTypeAndValue(String dictType, String value) {
        return selectOne(DictDataDO::getDictType, dictType, DictDataDO::getValue, value);
    }

    /**
     * 查询 select By Dict Type And Label 对应的数据。
     *
     * @param dictType dictType 参数
     * @param label label 参数
     * @return 处理结果
     */
    default DictDataDO selectByDictTypeAndLabel(String dictType, String label) {
        return selectOne(DictDataDO::getDictType, dictType, DictDataDO::getLabel, label);
    }

    /**
     * 查询 select By Dict Type And Values 对应的数据。
     *
     * @param dictType dictType 参数
     * @param values values 参数
     * @return 处理结果
     */
    default List<DictDataDO> selectByDictTypeAndValues(String dictType, Collection<String> values) {
        return selectList(new LambdaQueryWrapper<DictDataDO>().eq(DictDataDO::getDictType, dictType)
                .in(DictDataDO::getValue, values));
    }

    /**
     * 查询 select Count By Dict Type 对应的数据。
     *
     * @param dictType dictType 参数
     * @return 处理结果
     */
    default long selectCountByDictType(String dictType) {
        return selectCount(DictDataDO::getDictType, dictType);
    }

    /**
     * 查询 select Page 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    default PageResult<DictDataDO> selectPage(DictDataPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DictDataDO>()
                .likeIfPresent(DictDataDO::getLabel, reqVO.getLabel())
                .eqIfPresent(DictDataDO::getDictType, reqVO.getDictType())
                .eqIfPresent(DictDataDO::getStatus, reqVO.getStatus())
                .orderByDesc(Arrays.asList(DictDataDO::getDictType, DictDataDO::getSort)));
    }

    /**
     * 查询 select List By Status And Dict Type 对应的数据。
     *
     * @param status status 参数
     * @param dictType dictType 参数
     * @return 处理结果
     */
    default List<DictDataDO> selectListByStatusAndDictType(Integer status, String dictType) {
        return selectList(new LambdaQueryWrapperX<DictDataDO>()
                .eqIfPresent(DictDataDO::getStatus, status)
                .eqIfPresent(DictDataDO::getDictType, dictType));
    }

}
