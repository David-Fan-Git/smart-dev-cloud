package com.develop.mvp.pk.module.system.domain.dict.repository;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.controller.admin.dict.vo.type.DictTypePageReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.dict.DictTypeDO;
import com.develop.mvp.pk.module.system.domain.dict.DictType;
import com.develop.mvp.pk.module.system.domain.dict.valueobject.DictTypeId;
import com.develop.mvp.pk.module.system.domain.dict.valueobject.DictTypeKey;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Dict Type Repository 领域仓储接口。
 */
public interface DictTypeRepository {

    /**
     * 创建 insert 对应的数据。
     *
     * @param dictType dictType 参数
     * @return 处理结果
     */
    DictTypeDO insert(DictType dictType);

    /**
     * 更新 update 对应的数据。
     *
     * @param dictType dictType 参数
     */
    void update(DictType dictType);

    /**
     * 删除 delete 对应的数据。
     *
     * @param id id 参数
     * @param deletedTime deletedTime 参数
     */
    void delete(Long id, LocalDateTime deletedTime);

    /**
     * 查询 find By Id 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    DictType findById(DictTypeId id);

    /**
     * 查询 find By Type 对应的数据。
     *
     * @param type type 参数
     * @return 处理结果
     */
    Optional<DictType> findByType(DictTypeKey type);

    /**
     * 查询 find By Name 对应的数据。
     *
     * @param name name 参数
     * @return 处理结果
     */
    Optional<DictType> findByName(String name);

    /**
     * 查询 find All 对应的数据。
     *
     * @return 处理结果
     */
    List<DictType> findAll();

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
    PageResult<DictType> findPage(String name, String type, Integer status, LocalDateTime[] createTime, Integer pageNo, Integer pageSize);

    /**
     * 查询 find Do By Id 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    DictTypeDO findDoById(Long id);

    /**
     * 查询 find Do By Type 对应的数据。
     *
     * @param type type 参数
     * @return 处理结果
     */
    DictTypeDO findDoByType(String type);

    /**
     * 查询 find Do By Name 对应的数据。
     *
     * @param name name 参数
     * @return 处理结果
     */
    DictTypeDO findDoByName(String name);

    /**
     * 查询 find Do By Ids 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    List<DictTypeDO> findDoByIds(List<Long> ids);

    /**
     * 查询 find All Do 对应的数据。
     *
     * @return 处理结果
     */
    List<DictTypeDO> findAllDo();

    /**
     * 查询 find Do Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    PageResult<DictTypeDO> findDoPage(DictTypePageReqVO pageReqVO);

}
