package com.develop.mvp.pk.module.system.application.dict.port.inbound;

// DDD 角色：入站端口 — 定义 Dict 聚合的用例边界，供 Controller/API/跨服务调用
// Hexagonal-Lite：入站端口接口，应用服务实现此接口

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.controller.admin.dict.vo.data.DictDataPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.dict.vo.data.DictDataSaveReqVO;
import com.develop.mvp.pk.module.system.controller.admin.dict.vo.type.DictTypePageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.dict.vo.type.DictTypeSaveReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.dict.DictDataDO;
import com.develop.mvp.pk.module.system.dal.dataobject.dict.DictTypeDO;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
/**
 * Dict 聚合的入站用例端口。
 */
public interface DictUseCase {

    /**
     * 创建 create Dict Type 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @return 处理结果
     */
    Long createDictType(DictTypeSaveReqVO createReqVO);

    /**
     * 创建 create Dict Type 对应的数据。
     *
     * @param name name 参数
     * @param type type 参数
     * @param status status 参数
     * @param remark remark 参数
     * @return 处理结果
     */
    Long createDictType(String name, String type, Integer status, String remark);

    /**
     * 更新 update Dict Type 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     */
    void updateDictType(DictTypeSaveReqVO updateReqVO);

    /**
     * 更新 update Dict Type 对应的数据。
     *
     * @param id id 参数
     * @param name name 参数
     * @param type type 参数
     * @param status status 参数
     * @param remark remark 参数
     */
    void updateDictType(Long id, String name, String type, Integer status, String remark);

    /**
     * 删除 delete Dict Type 对应的数据。
     *
     * @param id id 参数
     */
    void deleteDictType(Long id);

    /**
     * 删除 delete Dict Type List 对应的数据。
     *
     * @param ids ids 参数
     */
    void deleteDictTypeList(List<Long> ids);

    /**
     * 查询 get Dict Type Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    PageResult<DictTypeDO> getDictTypePage(DictTypePageReqVO pageReqVO);

    /**
     * 查询 get Dict Type Page 对应的数据。
     *
     * @param name name 参数
     * @param type type 参数
     * @param status status 参数
     * @param createTime createTime 参数
     * @param pageNo pageNo 参数
     * @param pageSize pageSize 参数
     * @return 处理结果
     */
    PageResult<DictTypeDO> getDictTypePage(String name, String type, Integer status,
                                           LocalDateTime[] createTime, Integer pageNo, Integer pageSize);

    /**
     * 查询 get Dict Type 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    DictTypeDO getDictType(Long id);

    /**
     * 查询 get Dict Type 对应的数据。
     *
     * @param type type 参数
     * @return 处理结果
     */
    DictTypeDO getDictType(String type);

    /**
     * 查询 get Dict Type By Type 对应的数据。
     *
     * @param type type 参数
     * @return 处理结果
     */
    DictTypeDO getDictTypeByType(String type);

    /**
     * 查询 get Dict Type List 对应的数据。
     *
     * @return 处理结果
     */
    List<DictTypeDO> getDictTypeList();

    /**
     * 创建 create Dict Data 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @return 处理结果
     */
    Long createDictData(DictDataSaveReqVO createReqVO);

    /**
     * 创建 create Dict Data 对应的数据。
     *
     * @param sort sort 参数
     * @param label label 参数
     * @param value value 参数
     * @param dictType dictType 参数
     * @param status status 参数
     * @param colorType colorType 参数
     * @param cssClass cssClass 参数
     * @param remark remark 参数
     * @return 处理结果
     */
    Long createDictData(Integer sort, String label, String value, String dictType,
                        Integer status, String colorType, String cssClass, String remark);

    /**
     * 更新 update Dict Data 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     */
    void updateDictData(DictDataSaveReqVO updateReqVO);

    /**
     * 更新 update Dict Data 对应的数据。
     *
     * @param id id 参数
     * @param sort sort 参数
     * @param label label 参数
     * @param value value 参数
     * @param dictType dictType 参数
     * @param status status 参数
     * @param colorType colorType 参数
     * @param cssClass cssClass 参数
     * @param remark remark 参数
     */
    void updateDictData(Long id, Integer sort, String label, String value, String dictType,
                        Integer status, String colorType, String cssClass, String remark);

    /**
     * 删除 delete Dict Data 对应的数据。
     *
     * @param id id 参数
     */
    void deleteDictData(Long id);

    /**
     * 删除 delete Dict Data List 对应的数据。
     *
     * @param ids ids 参数
     */
    void deleteDictDataList(List<Long> ids);

    /**
     * 查询 get Dict Data List 对应的数据。
     *
     * @param status status 参数
     * @param dictType dictType 参数
     * @return 处理结果
     */
    List<DictDataDO> getDictDataList(Integer status, String dictType);

    /**
     * 查询 get Dict Data Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    PageResult<DictDataDO> getDictDataPage(DictDataPageReqVO pageReqVO);

    /**
     * 查询 get Dict Data Page 对应的数据。
     *
     * @param label label 参数
     * @param dictType dictType 参数
     * @param status status 参数
     * @param pageNo pageNo 参数
     * @param pageSize pageSize 参数
     * @return 处理结果
     */
    PageResult<DictDataDO> getDictDataPage(String label, String dictType, Integer status,
                                           Integer pageNo, Integer pageSize);

    /**
     * 查询 get Dict Data 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    DictDataDO getDictData(Long id);

    /**
     * 查询 get Dict Data 对应的数据。
     *
     * @param dictType dictType 参数
     * @param value value 参数
     * @return 处理结果
     */
    DictDataDO getDictData(String dictType, String value);

    /**
     * 执行 parse Dict Data 对应的业务操作。
     *
     * @param dictType dictType 参数
     * @param label label 参数
     * @return 处理结果
     */
    DictDataDO parseDictData(String dictType, String label);

    /**
     * 查询 get Dict Data List By Dict Type 对应的数据。
     *
     * @param dictType dictType 参数
     * @return 处理结果
     */
    List<DictDataDO> getDictDataListByDictType(String dictType);

    /**
     * 查询 get Dict Data Count By Dict Type 对应的数据。
     *
     * @param dictType dictType 参数
     * @return 处理结果
     */
    long getDictDataCountByDictType(String dictType);

    /**
     * 校验 validate Dict Data List 对应的业务规则。
     *
     * @param dictType dictType 参数
     * @param values values 参数
     */
    void validateDictDataList(String dictType, Collection<String> values);
}
