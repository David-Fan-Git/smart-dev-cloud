package com.develop.mvp.pk.module.system.infrastructure.dict.persistence;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.controller.admin.dict.vo.data.DictDataPageReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.dict.DictDataDO;
import com.develop.mvp.pk.module.system.dal.mysql.dict.DictDataMapper;
import com.develop.mvp.pk.module.system.domain.dict.DictData;
import com.develop.mvp.pk.module.system.domain.dict.repository.DictDataRepository;
import com.develop.mvp.pk.module.system.domain.dict.valueobject.DictDataId;
import com.develop.mvp.pk.module.system.domain.dict.valueobject.DictDataValue;
import com.develop.mvp.pk.module.system.domain.dict.valueobject.DictTypeKey;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Dict Data Repository Impl 领域仓储实现。
 */
@Repository
public class DictDataRepositoryImpl implements DictDataRepository {

    private final DictDataMapper mapper;

    /**
     * 创建 DictDataRepositoryImpl 实例。
     *
     * @param mapper mapper 参数
     */
    public DictDataRepositoryImpl(DictDataMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 创建 insert 对应的数据。
     *
     * @param dictData dictData 参数
     * @return 处理结果
     */
    @Override
    public DictDataDO insert(DictData dictData) {
        DictDataDO dictDataDO = toDO(dictData);
        mapper.insert(dictDataDO);
        return dictDataDO;
    }

    /**
     * 更新 update 对应的数据。
     *
     * @param dictData dictData 参数
     */
    @Override
    public void update(DictData dictData) {
        mapper.updateById(toDO(dictData));
    }

    /**
     * 删除 delete 对应的数据。
     *
     * @param id id 参数
     */
    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    /**
     * 删除 delete By Ids 对应的数据。
     *
     * @param ids ids 参数
     */
    @Override
    public void deleteByIds(List<Long> ids) {
        mapper.deleteByIds(ids);
    }

    /**
     * 查询 find By Id 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @Override
    public DictData findById(DictDataId id) {
        if (id == null) {
            return null;
        }
        DictDataDO dictData = mapper.selectById(id.value());
        return dictData != null ? toDomain(dictData) : null;
    }

    /**
     * 查询 find By Type And Value 对应的数据。
     *
     * @param type type 参数
     * @param value value 参数
     * @return 处理结果
     */
    @Override
    public Optional<DictData> findByTypeAndValue(DictTypeKey type, String value) {
        return Optional.ofNullable(mapper.selectByDictTypeAndValue(type.value(), value)).map(this::toDomain);
    }

    /**
     * 查询 find By Type And Label 对应的数据。
     *
     * @param type type 参数
     * @param label label 参数
     * @return 处理结果
     */
    @Override
    public Optional<DictData> findByTypeAndLabel(DictTypeKey type, String label) {
        return Optional.ofNullable(mapper.selectByDictTypeAndLabel(type.value(), label)).map(this::toDomain);
    }

    /**
     * 查询 find By Type And Values 对应的数据。
     *
     * @param type type 参数
     * @param values values 参数
     * @return 处理结果
     */
    @Override
    public List<DictData> findByTypeAndValues(DictTypeKey type, Collection<String> values) {
        return mapper.selectByDictTypeAndValues(type.value(), values).stream().map(this::toDomain).toList();
    }

    /**
     * 查询 find By Dict Type 对应的数据。
     *
     * @param type type 参数
     * @return 处理结果
     */
    @Override
    public List<DictData> findByDictType(DictTypeKey type) {
        return mapper.selectList(DictDataDO::getDictType, type.value()).stream().map(this::toDomain).toList();
    }

    /**
     * 查询 find By Status And Dict Type 对应的数据。
     *
     * @param status status 参数
     * @param type type 参数
     * @return 处理结果
     */
    @Override
    public List<DictData> findByStatusAndDictType(Integer status, DictTypeKey type) {
        return mapper.selectListByStatusAndDictType(status, type != null ? type.value() : null).stream().map(this::toDomain).toList();
    }

    /**
     * 查询 find Page 对应的数据。
     *
     * @param label label 参数
     * @param type type 参数
     * @param status status 参数
     * @param pageNo pageNo 参数
     * @param pageSize pageSize 参数
     * @return 处理结果
     */
    @Override
    public PageResult<DictData> findPage(String label, DictTypeKey type, Integer status, Integer pageNo, Integer pageSize) {
        DictDataPageReqVO reqVO = new DictDataPageReqVO();
        reqVO.setLabel(label);
        reqVO.setDictType(type != null ? type.value() : null);
        reqVO.setStatus(status);
        reqVO.setPageNo(pageNo);
        reqVO.setPageSize(pageSize);
        PageResult<DictDataDO> doPage = mapper.selectPage(reqVO);
        return new PageResult<>(doPage.getList().stream().map(this::toDomain).toList(), doPage.getTotal());
    }

    /**
     * 查询 count By Dict Type 对应的数据。
     *
     * @param type type 参数
     * @return 处理结果
     */
    @Override
    public long countByDictType(DictTypeKey type) {
        return countByDictType(type.value());
    }

    /**
     * 查询 count By Dict Type 对应的数据。
     *
     * @param dictType dictType 参数
     * @return 处理结果
     */
    @Override
    public long countByDictType(String dictType) {
        return mapper.selectCountByDictType(dictType);
    }

    /**
     * 查询 find Do By Id 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @Override
    public DictDataDO findDoById(Long id) {
        return mapper.selectById(id);
    }

    /**
     * 查询 find Do By Type And Value 对应的数据。
     *
     * @param dictType dictType 参数
     * @param value value 参数
     * @return 处理结果
     */
    @Override
    public DictDataDO findDoByTypeAndValue(String dictType, String value) {
        return mapper.selectByDictTypeAndValue(dictType, value);
    }

    /**
     * 查询 find Do By Type And Label 对应的数据。
     *
     * @param dictType dictType 参数
     * @param label label 参数
     * @return 处理结果
     */
    @Override
    public DictDataDO findDoByTypeAndLabel(String dictType, String label) {
        return mapper.selectByDictTypeAndLabel(dictType, label);
    }

    /**
     * 查询 find Do By Type And Values 对应的数据。
     *
     * @param dictType dictType 参数
     * @param values values 参数
     * @return 处理结果
     */
    @Override
    public List<DictDataDO> findDoByTypeAndValues(String dictType, Collection<String> values) {
        return mapper.selectByDictTypeAndValues(dictType, values);
    }

    /**
     * 查询 find Do By Dict Type 对应的数据。
     *
     * @param dictType dictType 参数
     * @return 处理结果
     */
    @Override
    public List<DictDataDO> findDoByDictType(String dictType) {
        return mapper.selectList(DictDataDO::getDictType, dictType);
    }

    /**
     * 查询 find Do By Status And Dict Type 对应的数据。
     *
     * @param status status 参数
     * @param dictType dictType 参数
     * @return 处理结果
     */
    @Override
    public List<DictDataDO> findDoByStatusAndDictType(Integer status, String dictType) {
        return mapper.selectListByStatusAndDictType(status, dictType);
    }

    /**
     * 查询 find Do Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    @Override
    public PageResult<DictDataDO> findDoPage(DictDataPageReqVO pageReqVO) {
        return mapper.selectPage(pageReqVO);
    }

    /**
     * 执行 to DO 对应的业务操作。
     *
     * @param dictData dictData 参数
     * @return 处理结果
     */
    private DictDataDO toDO(DictData dictData) {
        DictDataDO dictDataDO = new DictDataDO();
        dictDataDO.setId(dictData.id() != null ? dictData.id().value() : null);
        dictDataDO.setSort(dictData.sort());
        dictDataDO.setLabel(dictData.label());
        dictDataDO.setValue(dictData.value().value());
        dictDataDO.setDictType(dictData.dictType().value());
        dictDataDO.setStatus(dictData.status());
        dictDataDO.setColorType(dictData.colorType());
        dictDataDO.setCssClass(dictData.cssClass());
        dictDataDO.setRemark(dictData.remark());
        return dictDataDO;
    }

    /**
     * 执行 to Domain 对应的业务操作。
     *
     * @param dictData dictData 参数
     * @return 处理结果
     */
    private DictData toDomain(DictDataDO dictData) {
        return new DictData(DictDataId.of(dictData.getId()), DictTypeKey.of(dictData.getDictType()), DictDataValue.of(dictData.getValue()),
                dictData.getLabel(), dictData.getSort(), dictData.getStatus(), dictData.getColorType(), dictData.getCssClass(), dictData.getRemark());
    }

}
