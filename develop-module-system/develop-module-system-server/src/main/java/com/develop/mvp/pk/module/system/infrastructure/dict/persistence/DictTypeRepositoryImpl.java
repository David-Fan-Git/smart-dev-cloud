package com.develop.mvp.pk.module.system.infrastructure.dict.persistence;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.date.LocalDateTimeUtils;
import com.develop.mvp.pk.module.system.controller.admin.dict.vo.type.DictTypePageReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.dict.DictTypeDO;
import com.develop.mvp.pk.module.system.dal.mysql.dict.DictTypeMapper;
import com.develop.mvp.pk.module.system.domain.dict.DictType;
import com.develop.mvp.pk.module.system.domain.dict.repository.DictTypeRepository;
import com.develop.mvp.pk.module.system.domain.dict.valueobject.DictTypeId;
import com.develop.mvp.pk.module.system.domain.dict.valueobject.DictTypeKey;
import com.develop.mvp.pk.module.system.domain.dict.valueobject.DictTypeName;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Dict Type Repository Impl 领域仓储实现。
 */
@Repository
public class DictTypeRepositoryImpl implements DictTypeRepository {

    private final DictTypeMapper mapper;

    /**
     * 创建 DictTypeRepositoryImpl 实例。
     *
     * @param mapper mapper 参数
     */
    public DictTypeRepositoryImpl(DictTypeMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 创建 insert 对应的数据。
     *
     * @param dictType dictType 参数
     * @return 处理结果
     */
    @Override
    public DictTypeDO insert(DictType dictType) {
        DictTypeDO dictTypeDO = toDO(dictType);
        dictTypeDO.setDeletedTime(LocalDateTimeUtils.EMPTY);
        mapper.insert(dictTypeDO);
        return dictTypeDO;
    }

    /**
     * 更新 update 对应的数据。
     *
     * @param dictType dictType 参数
     */
    @Override
    public void update(DictType dictType) {
        mapper.updateById(toDO(dictType));
    }

    /**
     * 删除 delete 对应的数据。
     *
     * @param id id 参数
     * @param deletedTime deletedTime 参数
     */
    @Override
    public void delete(Long id, LocalDateTime deletedTime) {
        mapper.updateToDelete(id, deletedTime);
    }

    /**
     * 查询 find By Id 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @Override
    public DictType findById(DictTypeId id) {
        if (id == null) {
            return null;
        }
        DictTypeDO dictType = mapper.selectById(id.value());
        return dictType != null ? toDomain(dictType) : null;
    }

    /**
     * 查询 find By Type 对应的数据。
     *
     * @param type type 参数
     * @return 处理结果
     */
    @Override
    public Optional<DictType> findByType(DictTypeKey type) {
        return Optional.ofNullable(mapper.selectByType(type.value())).map(this::toDomain);
    }

    /**
     * 查询 find By Name 对应的数据。
     *
     * @param name name 参数
     * @return 处理结果
     */
    @Override
    public Optional<DictType> findByName(String name) {
        return Optional.ofNullable(mapper.selectByName(name)).map(this::toDomain);
    }

    /**
     * 查询 find All 对应的数据。
     *
     * @return 处理结果
     */
    @Override
    public List<DictType> findAll() {
        return mapper.selectList().stream().map(this::toDomain).toList();
    }

    /**
     * 查询 find Page 对应的数据。
     *
     * @param name name 参数
     * @param type type 参数
     * @param status status 参数
     * @param createTime createTime 参数
     * @param pageNo pageNo 参数
     * @param pageSize pageSize 参数
     * @return 处理结果
     */
    @Override
    public PageResult<DictType> findPage(String name, String type, Integer status, LocalDateTime[] createTime, Integer pageNo, Integer pageSize) {
        DictTypePageReqVO reqVO = new DictTypePageReqVO();
        reqVO.setName(name);
        reqVO.setType(type);
        reqVO.setStatus(status);
        reqVO.setCreateTime(createTime);
        reqVO.setPageNo(pageNo);
        reqVO.setPageSize(pageSize);
        PageResult<DictTypeDO> doPage = mapper.selectPage(reqVO);
        return new PageResult<>(doPage.getList().stream().map(this::toDomain).toList(), doPage.getTotal());
    }

    /**
     * 查询 find Do By Id 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @Override
    public DictTypeDO findDoById(Long id) {
        return mapper.selectById(id);
    }

    /**
     * 查询 find Do By Type 对应的数据。
     *
     * @param type type 参数
     * @return 处理结果
     */
    @Override
    public DictTypeDO findDoByType(String type) {
        return mapper.selectByType(type);
    }

    /**
     * 查询 find Do By Name 对应的数据。
     *
     * @param name name 参数
     * @return 处理结果
     */
    @Override
    public DictTypeDO findDoByName(String name) {
        return mapper.selectByName(name);
    }

    /**
     * 查询 find Do By Ids 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    @Override
    public List<DictTypeDO> findDoByIds(List<Long> ids) {
        return mapper.selectByIds(ids);
    }

    /**
     * 查询 find All Do 对应的数据。
     *
     * @return 处理结果
     */
    @Override
    public List<DictTypeDO> findAllDo() {
        return mapper.selectList();
    }

    /**
     * 查询 find Do Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    @Override
    public PageResult<DictTypeDO> findDoPage(DictTypePageReqVO pageReqVO) {
        return mapper.selectPage(pageReqVO);
    }

    /**
     * 执行 to DO 对应的业务操作。
     *
     * @param dictType dictType 参数
     * @return 处理结果
     */
    private DictTypeDO toDO(DictType dictType) {
        DictTypeDO dictTypeDO = new DictTypeDO();
        dictTypeDO.setId(dictType.id() != null ? dictType.id().value() : null);
        dictTypeDO.setName(dictType.name().value());
        dictTypeDO.setType(dictType.type().value());
        dictTypeDO.setStatus(dictType.status());
        dictTypeDO.setRemark(dictType.remark());
        return dictTypeDO;
    }

    /**
     * 执行 to Domain 对应的业务操作。
     *
     * @param dictType dictType 参数
     * @return 处理结果
     */
    private DictType toDomain(DictTypeDO dictType) {
        return new DictType(DictTypeId.of(dictType.getId()), DictTypeName.of(dictType.getName()), DictTypeKey.of(dictType.getType()),
                dictType.getStatus(), dictType.getRemark());
    }

}
