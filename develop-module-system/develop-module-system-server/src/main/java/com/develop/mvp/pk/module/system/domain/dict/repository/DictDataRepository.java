package com.develop.mvp.pk.module.system.domain.dict.repository;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.controller.admin.dict.vo.data.DictDataPageReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.dict.DictDataDO;
import com.develop.mvp.pk.module.system.domain.dict.DictData;
import com.develop.mvp.pk.module.system.domain.dict.valueobject.DictDataId;
import com.develop.mvp.pk.module.system.domain.dict.valueobject.DictTypeKey;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Dict Data Repository 领域仓储接口。
 */
public interface DictDataRepository {

    /**
     * 创建 insert 对应的数据。
     *
     * @param dictData dictData 参数
     * @return 处理结果
     */
    DictDataDO insert(DictData dictData);

    /**
     * 更新 update 对应的数据。
     *
     * @param dictData dictData 参数
     */
    void update(DictData dictData);

    /**
     * 删除 delete 对应的数据。
     *
     * @param id id 参数
     */
    void delete(Long id);

    /**
     * 删除 delete By Ids 对应的数据。
     *
     * @param ids ids 参数
     */
    void deleteByIds(List<Long> ids);

    /**
     * 查询 find By Id 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    DictData findById(DictDataId id);

    /**
     * 查询 find By Type And Value 对应的数据。
     *
     * @param dictType dictType 参数
     * @param value value 参数
     * @return 处理结果
     */
    Optional<DictData> findByTypeAndValue(DictTypeKey dictType, String value);

    /**
     * 查询 find By Type And Label 对应的数据。
     *
     * @param dictType dictType 参数
     * @param label label 参数
     * @return 处理结果
     */
    Optional<DictData> findByTypeAndLabel(DictTypeKey dictType, String label);

    /**
     * 查询 find By Type And Values 对应的数据。
     *
     * @param dictType dictType 参数
     * @param values values 参数
     * @return 处理结果
     */
    List<DictData> findByTypeAndValues(DictTypeKey dictType, Collection<String> values);

    /**
     * 查询 find By Dict Type 对应的数据。
     *
     * @param dictType dictType 参数
     * @return 处理结果
     */
    List<DictData> findByDictType(DictTypeKey dictType);

    /**
     * 查询 find By Status And Dict Type 对应的数据。
     *
     * @param status status 参数
     * @param dictType dictType 参数
     * @return 处理结果
     */
    List<DictData> findByStatusAndDictType(Integer status, DictTypeKey dictType);

    /**
     * 查询 find Page 对应的数据。
     *
     * @param label label 参数
     * @param dictType dictType 参数
     * @param status status 参数
     * @param pageNo pageNo 参数
     * @param pageSize pageSize 参数
     * @return 处理结果
     */
    PageResult<DictData> findPage(String label, DictTypeKey dictType, Integer status, Integer pageNo, Integer pageSize);

    /**
     * 查询 count By Dict Type 对应的数据。
     *
     * @param dictType dictType 参数
     * @return 处理结果
     */
    long countByDictType(DictTypeKey dictType);

    /**
     * 查询 count By Dict Type 对应的数据。
     *
     * @param dictType dictType 参数
     * @return 处理结果
     */
    long countByDictType(String dictType);

    /**
     * 查询 find Do By Id 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    DictDataDO findDoById(Long id);

    /**
     * 查询 find Do By Type And Value 对应的数据。
     *
     * @param dictType dictType 参数
     * @param value value 参数
     * @return 处理结果
     */
    DictDataDO findDoByTypeAndValue(String dictType, String value);

    /**
     * 查询 find Do By Type And Label 对应的数据。
     *
     * @param dictType dictType 参数
     * @param label label 参数
     * @return 处理结果
     */
    DictDataDO findDoByTypeAndLabel(String dictType, String label);

    /**
     * 查询 find Do By Type And Values 对应的数据。
     *
     * @param dictType dictType 参数
     * @param values values 参数
     * @return 处理结果
     */
    List<DictDataDO> findDoByTypeAndValues(String dictType, Collection<String> values);

    /**
     * 查询 find Do By Dict Type 对应的数据。
     *
     * @param dictType dictType 参数
     * @return 处理结果
     */
    List<DictDataDO> findDoByDictType(String dictType);

    /**
     * 查询 find Do By Status And Dict Type 对应的数据。
     *
     * @param status status 参数
     * @param dictType dictType 参数
     * @return 处理结果
     */
    List<DictDataDO> findDoByStatusAndDictType(Integer status, String dictType);

    /**
     * 查询 find Do Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    PageResult<DictDataDO> findDoPage(DictDataPageReqVO pageReqVO);

}
