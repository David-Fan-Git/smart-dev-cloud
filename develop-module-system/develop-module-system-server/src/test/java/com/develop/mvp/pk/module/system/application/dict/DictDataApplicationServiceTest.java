package com.develop.mvp.pk.module.system.application.dict;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.collection.ArrayUtils;
import com.develop.mvp.pk.framework.test.core.ut.BaseDbUnitTest;
import com.develop.mvp.pk.module.system.application.dict.service.DictApplicationService;
import com.develop.mvp.pk.module.system.controller.admin.dict.vo.data.DictDataPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.dict.vo.data.DictDataSaveReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.dict.DictDataDO;
import com.develop.mvp.pk.module.system.dal.dataobject.dict.DictTypeDO;
import com.develop.mvp.pk.module.system.dal.mysql.dict.DictDataMapper;
import com.develop.mvp.pk.module.system.infrastructure.dict.persistence.DictDataRepositoryImpl;
import com.develop.mvp.pk.module.system.infrastructure.dict.persistence.DictTypeRepositoryImpl;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.function.Consumer;

import static com.develop.mvp.pk.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static com.develop.mvp.pk.framework.test.core.util.AssertUtils.assertPojoEquals;
import static com.develop.mvp.pk.framework.test.core.util.AssertUtils.assertServiceException;
import static com.develop.mvp.pk.framework.test.core.util.RandomUtils.*;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.*;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

@Import({DictApplicationService.class, DictDataRepositoryImpl.class, DictTypeRepositoryImpl.class})
public class DictDataApplicationServiceTest extends BaseDbUnitTest {

    @Resource
    private DictApplicationService dictApplicationService;

    @Resource
    private DictDataMapper dictDataMapper;

    @Test
    public void testGetDictDataList() {
        DictDataDO dictDataDO01 = randomDictDataDO().setDictType("yunai").setSort(2)
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
        dictDataMapper.insert(dictDataDO01);
        DictDataDO dictDataDO02 = randomDictDataDO().setDictType("yunai").setSort(1)
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
        dictDataMapper.insert(dictDataDO02);
        DictDataDO dictDataDO03 = randomDictDataDO().setDictType("yunai").setSort(3)
                .setStatus(CommonStatusEnum.DISABLE.getStatus());
        dictDataMapper.insert(dictDataDO03);
        DictDataDO dictDataDO04 = randomDictDataDO().setDictType("yunai2").setSort(3)
                .setStatus(CommonStatusEnum.DISABLE.getStatus());
        dictDataMapper.insert(dictDataDO04);

        List<DictDataDO> dictDataDOList = dictApplicationService.getDictDataList(CommonStatusEnum.ENABLE.getStatus(), "yunai");

        assertEquals(2, dictDataDOList.size());
        assertPojoEquals(dictDataDO02, dictDataDOList.get(0));
        assertPojoEquals(dictDataDO01, dictDataDOList.get(1));
    }

    @Test
    public void testGetDictDataPage() {
        DictDataDO dbDictData = randomPojo(DictDataDO.class, o -> {
            o.setLabel("David");
            o.setDictType("yunai");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        dictDataMapper.insert(dbDictData);
        dictDataMapper.insert(cloneIgnoreId(dbDictData, o -> o.setLabel("艿")));
        dictDataMapper.insert(cloneIgnoreId(dbDictData, o -> o.setDictType("nai")));
        dictDataMapper.insert(cloneIgnoreId(dbDictData, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        DictDataPageReqVO reqVO = new DictDataPageReqVO();
        reqVO.setLabel("D");
        reqVO.setDictType("yunai");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        PageResult<DictDataDO> pageResult = dictApplicationService.getDictDataPage(reqVO);

        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbDictData, pageResult.getList().get(0));
    }

    @Test
    public void testCreateDictData_success() {
        DictDataSaveReqVO reqVO = randomPojo(DictDataSaveReqVO.class,
                o -> o.setStatus(randomCommonStatus()))
                .setId(null);
        dictApplicationService.createDictType(randomString(), reqVO.getDictType(), CommonStatusEnum.ENABLE.getStatus(), randomString());

        Long dictDataId = dictApplicationService.createDictData(reqVO);

        assertNotNull(dictDataId);
        DictDataDO dictData = dictDataMapper.selectById(dictDataId);
        assertPojoEquals(reqVO, dictData, "id");
    }

    @Test
    public void testUpdateDictData_success() {
        DictDataDO dbDictData = randomDictDataDO();
        dictDataMapper.insert(dbDictData);
        DictDataSaveReqVO reqVO = randomPojo(DictDataSaveReqVO.class, o -> {
            o.setId(dbDictData.getId());
            o.setStatus(randomCommonStatus());
        });
        dictApplicationService.createDictType(randomString(), reqVO.getDictType(), CommonStatusEnum.ENABLE.getStatus(), randomString());

        dictApplicationService.updateDictData(reqVO);

        DictDataDO dictData = dictDataMapper.selectById(reqVO.getId());
        assertPojoEquals(reqVO, dictData);
    }

    @Test
    public void testDeleteDictData_success() {
        DictDataDO dbDictData = randomDictDataDO();
        dictDataMapper.insert(dbDictData);

        dictApplicationService.deleteDictData(dbDictData.getId());

        assertNull(dictDataMapper.selectById(dbDictData.getId()));
    }

    @Test
    public void testValidateDictDataExists_notExists() {
        assertServiceException(() -> dictApplicationService.validateDictDataExists(randomLongId()), DICT_DATA_NOT_EXISTS);
    }

    @Test
    public void testValidateDictTypeExists_notEnable() {
        String dictType = randomString();
        dictApplicationService.createDictType(randomString(), dictType, CommonStatusEnum.DISABLE.getStatus(), randomString());

        assertServiceException(() -> dictApplicationService.validateDictTypeExists(dictType), DICT_TYPE_NOT_ENABLE);
    }

    @Test
    public void testValidateDictDataValueUnique_valueDuplicateForCreate() {
        String dictType = randomString();
        String value = randomString();
        dictDataMapper.insert(randomDictDataDO(o -> {
            o.setDictType(dictType);
            o.setValue(value);
        }));

        assertServiceException(() -> dictApplicationService.validateDictDataValueUnique(null, dictType, value),
                DICT_DATA_VALUE_DUPLICATE);
    }

    @Test
    public void testValidateDictDataList_notEnable() {
        DictDataDO dictDataDO = randomDictDataDO().setStatus(CommonStatusEnum.DISABLE.getStatus());
        dictDataMapper.insert(dictDataDO);

        assertServiceException(() -> dictApplicationService.validateDictDataList(dictDataDO.getDictType(), singletonList(dictDataDO.getValue())),
                DICT_DATA_NOT_ENABLE, dictDataDO.getLabel());
    }

    @Test
    public void testGetDictDataListByDictType() {
        DictDataDO dictDataDO01 = randomDictDataDO().setDictType("yunai").setSort(2);
        dictDataMapper.insert(dictDataDO01);
        DictDataDO dictDataDO02 = randomDictDataDO().setDictType("yunai").setSort(1);
        dictDataMapper.insert(dictDataDO02);

        List<DictDataDO> list = dictApplicationService.getDictDataListByDictType("yunai");

        assertEquals(2, list.size());
        assertPojoEquals(dictDataDO02, list.get(0));
        assertPojoEquals(dictDataDO01, list.get(1));
    }

    @SafeVarargs
    private static DictDataDO randomDictDataDO(Consumer<DictDataDO>... consumers) {
        Consumer<DictDataDO> consumer = o -> o.setStatus(randomCommonStatus());
        return randomPojo(DictDataDO.class, ArrayUtils.append(consumer, consumers));
    }

}
