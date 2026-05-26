package com.develop.mvp.pk.module.system.application.dict.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.collection.CollectionUtils;
import com.develop.mvp.pk.module.system.application.dict.port.inbound.DictUseCase;
import com.develop.mvp.pk.module.system.controller.admin.dict.vo.data.DictDataPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.dict.vo.data.DictDataSaveReqVO;
import com.develop.mvp.pk.module.system.controller.admin.dict.vo.type.DictTypePageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.dict.vo.type.DictTypeSaveReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.dict.DictDataDO;
import com.develop.mvp.pk.module.system.dal.dataobject.dict.DictTypeDO;
import com.develop.mvp.pk.module.system.domain.dict.DictData;
import com.develop.mvp.pk.module.system.domain.dict.DictType;
import com.develop.mvp.pk.module.system.domain.dict.repository.DictDataRepository;
import com.develop.mvp.pk.module.system.domain.dict.repository.DictTypeRepository;
import com.develop.mvp.pk.module.system.domain.dict.valueobject.DictDataId;
import com.develop.mvp.pk.module.system.domain.dict.valueobject.DictDataValue;
import com.develop.mvp.pk.module.system.domain.dict.valueobject.DictTypeId;
import com.develop.mvp.pk.module.system.domain.dict.valueobject.DictTypeKey;
import com.develop.mvp.pk.module.system.domain.dict.valueobject.DictTypeName;
import com.google.common.annotations.VisibleForTesting;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.*;

/**
 * Dict Application Service 应用服务。
 */
@RequiredArgsConstructor
public class DictApplicationService implements DictUseCase {

	private static final Comparator<DictDataDO> COMPARATOR_TYPE_AND_SORT = Comparator.comparing(DictDataDO::getDictType).thenComparingInt(DictDataDO::getSort);

	private final DictTypeRepository dictTypeRepository;

	private final DictDataRepository dictDataRepository;

	/**
	 * 创建 create Dict Type 对应的数据。
	 *
	 * @param createReqVO createReqVO 参数
	 * @return 处理结果
	 */
	@Transactional
	public Long createDictType(DictTypeSaveReqVO createReqVO) {

		validateDictTypeNameUnique(null, createReqVO.getName());
		validateDictTypeUnique(null, createReqVO.getType());
		DictTypeDO dictType = dictTypeRepository.insert(toDomain(createReqVO));
		return dictType.getId();
	}

	/**
	 * 创建 create Dict Type 对应的数据。
	 *
	 * @param name   name 参数
	 * @param type   type 参数
	 * @param status status 参数
	 * @param remark remark 参数
	 * @return 处理结果
	 */
	@Transactional
	public Long createDictType(String name, String type, Integer status, String remark) {

		DictTypeSaveReqVO reqVO = new DictTypeSaveReqVO();
		reqVO.setName(name);
		reqVO.setType(type);
		reqVO.setStatus(status);
		reqVO.setRemark(remark);
		return createDictType(reqVO);
	}

	/**
	 * 更新 update Dict Type 对应的数据。
	 *
	 * @param updateReqVO updateReqVO 参数
	 */
	@Transactional
	public void updateDictType(DictTypeSaveReqVO updateReqVO) {

		validateDictTypeExists(updateReqVO.getId());
		validateDictTypeNameUnique(updateReqVO.getId(), updateReqVO.getName());
		validateDictTypeUnique(updateReqVO.getId(), updateReqVO.getType());
		dictTypeRepository.update(toDomain(updateReqVO));
	}

	/**
	 * 更新 update Dict Type 对应的数据。
	 *
	 * @param id     id 参数
	 * @param name   name 参数
	 * @param type   type 参数
	 * @param status status 参数
	 * @param remark remark 参数
	 */
	@Transactional
	public void updateDictType(Long id, String name, String type, Integer status, String remark) {

		DictTypeSaveReqVO reqVO = new DictTypeSaveReqVO();
		reqVO.setId(id);
		reqVO.setName(name);
		reqVO.setType(type);
		reqVO.setStatus(status);
		reqVO.setRemark(remark);
		updateDictType(reqVO);
	}

	/**
	 * 删除 delete Dict Type 对应的数据。
	 *
	 * @param id id 参数
	 */
	@Transactional
	public void deleteDictType(Long id) {

		DictTypeDO dictType = validateDictTypeExists(id);
		if (dictDataRepository.countByDictType(dictType.getType()) > 0) {
			throw exception(DICT_TYPE_HAS_CHILDREN);
		}
		dictTypeRepository.delete(id, LocalDateTime.now());
	}

	/**
	 * 删除 delete Dict Type List 对应的数据。
	 *
	 * @param ids ids 参数
	 */
	@Transactional
	public void deleteDictTypeList(List<Long> ids) {

		List<DictTypeDO> dictTypes = dictTypeRepository.findDoByIds(ids);
		dictTypes.forEach(dictType -> {
			if (dictDataRepository.countByDictType(dictType.getType()) > 0) {
				throw exception(DICT_TYPE_HAS_CHILDREN);
			}
		});
		LocalDateTime now = LocalDateTime.now();
		ids.forEach(id -> dictTypeRepository.delete(id, now));
	}

	/**
	 * 查询 get Dict Type Page 对应的数据。
	 *
	 * @param pageReqVO pageReqVO 参数
	 * @return 处理结果
	 */
	public PageResult<DictTypeDO> getDictTypePage(DictTypePageReqVO pageReqVO) {

		return dictTypeRepository.findDoPage(pageReqVO);
	}

	/**
	 * 查询 get Dict Type Page 对应的数据。
	 *
	 * @param name       name 参数
	 * @param type       type 参数
	 * @param status     status 参数
	 * @param createTime createTime 参数
	 * @param pageNo     pageNo 参数
	 * @param pageSize   pageSize 参数
	 * @return 处理结果
	 */
	public PageResult<DictTypeDO> getDictTypePage(String name, String type, Integer status, LocalDateTime[] createTime, Integer pageNo, Integer pageSize) {

		DictTypePageReqVO reqVO = new DictTypePageReqVO();
		reqVO.setName(name);
		reqVO.setType(type);
		reqVO.setStatus(status);
		reqVO.setCreateTime(createTime);
		reqVO.setPageNo(pageNo);
		reqVO.setPageSize(pageSize);
		return getDictTypePage(reqVO);
	}

	/**
	 * 查询 get Dict Type 对应的数据。
	 *
	 * @param id id 参数
	 * @return 处理结果
	 */
	public DictTypeDO getDictType(Long id) {

		return dictTypeRepository.findDoById(id);
	}

	/**
	 * 查询 get Dict Type 对应的数据。
	 *
	 * @param type type 参数
	 * @return 处理结果
	 */
	public DictTypeDO getDictType(String type) {

		return dictTypeRepository.findDoByType(type);
	}

	/**
	 * 查询 get Dict Type By Type 对应的数据。
	 *
	 * @param type type 参数
	 * @return 处理结果
	 */
	public DictTypeDO getDictTypeByType(String type) {

		return getDictType(type);
	}

	/**
	 * 查询 get Dict Type List 对应的数据。
	 *
	 * @return 处理结果
	 */
	public List<DictTypeDO> getDictTypeList() {

		return dictTypeRepository.findAllDo();
	}

	/**
	 * 创建 create Dict Data 对应的数据。
	 *
	 * @param createReqVO createReqVO 参数
	 * @return 处理结果
	 */
	@Transactional
	public Long createDictData(DictDataSaveReqVO createReqVO) {

		validateDictTypeExists(createReqVO.getDictType());
		validateDictDataValueUnique(null, createReqVO.getDictType(), createReqVO.getValue());
		DictDataDO dictData = dictDataRepository.insert(toDomain(createReqVO));
		return dictData.getId();
	}

	/**
	 * 创建 create Dict Data 对应的数据。
	 *
	 * @param sort      sort 参数
	 * @param label     label 参数
	 * @param value     value 参数
	 * @param dictType  dictType 参数
	 * @param status    status 参数
	 * @param colorType colorType 参数
	 * @param cssClass  cssClass 参数
	 * @param remark    remark 参数
	 * @return 处理结果
	 */
	@Transactional
	public Long createDictData(Integer sort, String label, String value, String dictType, Integer status, String colorType, String cssClass, String remark) {

		DictDataSaveReqVO reqVO = new DictDataSaveReqVO();
		reqVO.setSort(sort);
		reqVO.setLabel(label);
		reqVO.setValue(value);
		reqVO.setDictType(dictType);
		reqVO.setStatus(status);
		reqVO.setColorType(colorType);
		reqVO.setCssClass(cssClass);
		reqVO.setRemark(remark);
		return createDictData(reqVO);
	}

	/**
	 * 更新 update Dict Data 对应的数据。
	 *
	 * @param updateReqVO updateReqVO 参数
	 */
	@Transactional
	public void updateDictData(DictDataSaveReqVO updateReqVO) {

		validateDictDataExists(updateReqVO.getId());
		validateDictTypeExists(updateReqVO.getDictType());
		validateDictDataValueUnique(updateReqVO.getId(), updateReqVO.getDictType(), updateReqVO.getValue());
		dictDataRepository.update(toDomain(updateReqVO));
	}

	/**
	 * 更新 update Dict Data 对应的数据。
	 *
	 * @param id        id 参数
	 * @param sort      sort 参数
	 * @param label     label 参数
	 * @param value     value 参数
	 * @param dictType  dictType 参数
	 * @param status    status 参数
	 * @param colorType colorType 参数
	 * @param cssClass  cssClass 参数
	 * @param remark    remark 参数
	 */
	@Transactional
	public void updateDictData(Long id, Integer sort, String label, String value, String dictType, Integer status, String colorType, String cssClass, String remark) {

		DictDataSaveReqVO reqVO = new DictDataSaveReqVO();
		reqVO.setId(id);
		reqVO.setSort(sort);
		reqVO.setLabel(label);
		reqVO.setValue(value);
		reqVO.setDictType(dictType);
		reqVO.setStatus(status);
		reqVO.setColorType(colorType);
		reqVO.setCssClass(cssClass);
		reqVO.setRemark(remark);
		updateDictData(reqVO);
	}

	/**
	 * 删除 delete Dict Data 对应的数据。
	 *
	 * @param id id 参数
	 */
	@Transactional
	public void deleteDictData(Long id) {

		validateDictDataExists(id);
		dictDataRepository.delete(id);
	}

	/**
	 * 删除 delete Dict Data List 对应的数据。
	 *
	 * @param ids ids 参数
	 */
	@Transactional
	public void deleteDictDataList(List<Long> ids) {

		dictDataRepository.deleteByIds(ids);
	}

	/**
	 * 查询 get Dict Data List 对应的数据。
	 *
	 * @param status   status 参数
	 * @param dictType dictType 参数
	 * @return 处理结果
	 */
	public List<DictDataDO> getDictDataList(Integer status, String dictType) {

		List<DictDataDO> list = dictDataRepository.findDoByStatusAndDictType(status, dictType);
		list.sort(COMPARATOR_TYPE_AND_SORT);
		return list;
	}

	/**
	 * 查询 get Dict Data Page 对应的数据。
	 *
	 * @param pageReqVO pageReqVO 参数
	 * @return 处理结果
	 */
	public PageResult<DictDataDO> getDictDataPage(DictDataPageReqVO pageReqVO) {

		return dictDataRepository.findDoPage(pageReqVO);
	}

	/**
	 * 查询 get Dict Data Page 对应的数据。
	 *
	 * @param label    label 参数
	 * @param dictType dictType 参数
	 * @param status   status 参数
	 * @param pageNo   pageNo 参数
	 * @param pageSize pageSize 参数
	 * @return 处理结果
	 */
	public PageResult<DictDataDO> getDictDataPage(String label, String dictType, Integer status, Integer pageNo, Integer pageSize) {

		DictDataPageReqVO reqVO = new DictDataPageReqVO();
		reqVO.setLabel(label);
		reqVO.setDictType(dictType);
		reqVO.setStatus(status);
		reqVO.setPageNo(pageNo);
		reqVO.setPageSize(pageSize);
		return getDictDataPage(reqVO);
	}

	/**
	 * 查询 get Dict Data 对应的数据。
	 *
	 * @param id id 参数
	 * @return 处理结果
	 */
	public DictDataDO getDictData(Long id) {

		return dictDataRepository.findDoById(id);
	}

	/**
	 * 查询 get Dict Data 对应的数据。
	 *
	 * @param dictType dictType 参数
	 * @param value    value 参数
	 * @return 处理结果
	 */
	public DictDataDO getDictData(String dictType, String value) {

		return dictDataRepository.findDoByTypeAndValue(dictType, value);
	}

	/**
	 * 执行 parse Dict Data 对应的业务操作。
	 *
	 * @param dictType dictType 参数
	 * @param label    label 参数
	 * @return 处理结果
	 */
	public DictDataDO parseDictData(String dictType, String label) {

		return dictDataRepository.findDoByTypeAndLabel(dictType, label);
	}

	/**
	 * 查询 get Dict Data List By Dict Type 对应的数据。
	 *
	 * @param dictType dictType 参数
	 * @return 处理结果
	 */
	public List<DictDataDO> getDictDataListByDictType(String dictType) {

		List<DictDataDO> list = dictDataRepository.findDoByDictType(dictType);
		list.sort(Comparator.comparing(DictDataDO::getSort));
		return list;
	}

	/**
	 * 查询 get Dict Data Count By Dict Type 对应的数据。
	 *
	 * @param dictType dictType 参数
	 * @return 处理结果
	 */
	public long getDictDataCountByDictType(String dictType) {

		return dictDataRepository.countByDictType(dictType);
	}

	/**
	 * 校验 validate Dict Data List 对应的业务规则。
	 *
	 * @param dictType dictType 参数
	 * @param values   values 参数
	 */
	public void validateDictDataList(String dictType, Collection<String> values) {

		if (CollUtil.isEmpty(values)) {
			return;
		}
		Map<String, DictDataDO> dictDataMap = CollectionUtils.convertMap(dictDataRepository.findDoByTypeAndValues(dictType, values), DictDataDO::getValue);
		values.forEach(value -> {
			DictDataDO dictData = dictDataMap.get(value);
			if (dictData == null) {
				throw exception(DICT_DATA_NOT_EXISTS);
			}
			if (!CommonStatusEnum.ENABLE.getStatus().equals(dictData.getStatus())) {
				throw exception(DICT_DATA_NOT_ENABLE, dictData.getLabel());
			}
		});
	}

	/**
	 * 校验 validate Dict Type Exists 对应的业务规则。
	 *
	 * @param id id 参数
	 * @return 处理结果
	 */
	@VisibleForTesting
	public DictTypeDO validateDictTypeExists(Long id) {

		if (id == null) {
			return null;
		}
		DictTypeDO dictType = dictTypeRepository.findDoById(id);
		if (dictType == null) {
			throw exception(DICT_TYPE_NOT_EXISTS);
		}
		return dictType;
	}

	/**
	 * 校验 validate Dict Type Exists 对应的业务规则。
	 *
	 * @param type type 参数
	 */
	@VisibleForTesting
	public void validateDictTypeExists(String type) {

		DictTypeDO dictType = dictTypeRepository.findDoByType(type);
		if (dictType == null) {
			throw exception(DICT_TYPE_NOT_EXISTS);
		}
		if (!CommonStatusEnum.ENABLE.getStatus().equals(dictType.getStatus())) {
			throw exception(DICT_TYPE_NOT_ENABLE);
		}
	}

	/**
	 * 校验 validate Dict Type Name Unique 对应的业务规则。
	 *
	 * @param id   id 参数
	 * @param name name 参数
	 */
	@VisibleForTesting
	public void validateDictTypeNameUnique(Long id, String name) {

		DictTypeDO dictType = dictTypeRepository.findDoByName(name);
		if (dictType == null) {
			return;
		}
		if (id == null) {
			throw exception(DICT_TYPE_NAME_DUPLICATE);
		}
		if (!dictType.getId().equals(id)) {
			throw exception(DICT_TYPE_NAME_DUPLICATE);
		}
	}

	/**
	 * 校验 validate Dict Type Unique 对应的业务规则。
	 *
	 * @param id   id 参数
	 * @param type type 参数
	 */
	@VisibleForTesting
	public void validateDictTypeUnique(Long id, String type) {

		if (StrUtil.isEmpty(type)) {
			return;
		}
		DictTypeDO dictType = dictTypeRepository.findDoByType(type);
		if (dictType == null) {
			return;
		}
		if (id == null) {
			throw exception(DICT_TYPE_TYPE_DUPLICATE);
		}
		if (!dictType.getId().equals(id)) {
			throw exception(DICT_TYPE_TYPE_DUPLICATE);
		}
	}

	/**
	 * 校验 validate Dict Data Value Unique 对应的业务规则。
	 *
	 * @param id       id 参数
	 * @param dictType dictType 参数
	 * @param value    value 参数
	 */
	@VisibleForTesting
	public void validateDictDataValueUnique(Long id, String dictType, String value) {

		DictDataDO dictData = dictDataRepository.findDoByTypeAndValue(dictType, value);
		if (dictData == null) {
			return;
		}
		if (id == null) {
			throw exception(DICT_DATA_VALUE_DUPLICATE);
		}
		if (!dictData.getId().equals(id)) {
			throw exception(DICT_DATA_VALUE_DUPLICATE);
		}
	}

	/**
	 * 校验 validate Dict Data Exists 对应的业务规则。
	 *
	 * @param id id 参数
	 */
	@VisibleForTesting
	public void validateDictDataExists(Long id) {

		if (id == null) {
			return;
		}
		DictDataDO dictData = dictDataRepository.findDoById(id);
		if (dictData == null) {
			throw exception(DICT_DATA_NOT_EXISTS);
		}
	}

	/**
	 * 执行 to Domain 对应的业务操作。
	 *
	 * @param reqVO reqVO 参数
	 * @return 处理结果
	 */
	private DictType toDomain(DictTypeSaveReqVO reqVO) {

		return new DictType(DictTypeId.of(reqVO.getId()), DictTypeName.of(reqVO.getName()), DictTypeKey.of(reqVO.getType()), reqVO.getStatus(), reqVO.getRemark());
	}

	/**
	 * 执行 to Domain 对应的业务操作。
	 *
	 * @param reqVO reqVO 参数
	 * @return 处理结果
	 */
	private DictData toDomain(DictDataSaveReqVO reqVO) {

		return new DictData(DictDataId.of(reqVO.getId()), DictTypeKey.of(reqVO.getDictType()), DictDataValue.of(reqVO.getValue()), reqVO.getLabel(), reqVO.getSort(), reqVO.getStatus(), reqVO.getColorType(), reqVO.getCssClass(), reqVO.getRemark());
	}

}
