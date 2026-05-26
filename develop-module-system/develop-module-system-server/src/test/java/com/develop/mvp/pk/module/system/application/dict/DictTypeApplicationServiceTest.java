package com.develop.mvp.pk.module.system.application.dict;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.collection.ArrayUtils;
import com.develop.mvp.pk.framework.test.core.ut.BaseDbUnitTest;
import com.develop.mvp.pk.module.system.application.dict.service.DictApplicationService;
import com.develop.mvp.pk.module.system.controller.admin.dict.vo.type.DictTypePageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.dict.vo.type.DictTypeSaveReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.dict.DictDataDO;
import com.develop.mvp.pk.module.system.dal.dataobject.dict.DictTypeDO;
import com.develop.mvp.pk.module.system.dal.mysql.dict.DictDataMapper;
import com.develop.mvp.pk.module.system.dal.mysql.dict.DictTypeMapper;
import com.develop.mvp.pk.module.system.infrastructure.dict.persistence.DictDataRepositoryImpl;
import com.develop.mvp.pk.module.system.infrastructure.dict.persistence.DictTypeRepositoryImpl;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.function.Consumer;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static com.develop.mvp.pk.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static com.develop.mvp.pk.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static com.develop.mvp.pk.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static com.develop.mvp.pk.framework.test.core.util.AssertUtils.assertPojoEquals;
import static com.develop.mvp.pk.framework.test.core.util.AssertUtils.assertServiceException;
import static com.develop.mvp.pk.framework.test.core.util.RandomUtils.*;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;

@Import({DictApplicationService.class, DictTypeRepositoryImpl.class, DictDataRepositoryImpl.class})
public class DictTypeApplicationServiceTest extends BaseDbUnitTest {

    @Resource
    private DictApplicationService dictApplicationService;

    @Resource
    private DictTypeMapper dictTypeMapper;
    @Resource
    private DictDataMapper dictDataMapper;

    @Test
    public void testGetDictTypePage() {
        DictTypeDO dbDictType = randomPojo(DictTypeDO.class, o -> {
            o.setName("yunai");
            o.setType("David");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setCreateTime(buildTime(2021, 1, 15));
        });
        dictTypeMapper.insert(dbDictType);
        dictTypeMapper.insert(cloneIgnoreId(dbDictType, o -> o.setName("tudou")));
        dictTypeMapper.insert(cloneIgnoreId(dbDictType, o -> o.setType("土豆")));
        dictTypeMapper.insert(cloneIgnoreId(dbDictType, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        dictTypeMapper.insert(cloneIgnoreId(dbDictType, o -> o.setCreateTime(buildTime(2021, 1, 1))));
        DictTypePageReqVO reqVO = new DictTypePageReqVO();
        reqVO.setName("nai");
        reqVO.setType("D");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setCreateTime(buildBetweenTime(2021, 1, 10, 2021, 1, 20));

        PageResult<DictTypeDO> pageResult = dictApplicationService.getDictTypePage(reqVO);

        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbDictType, pageResult.getList().get(0));
    }

    @Test
    public void testCreateDictType_success() {
        DictTypeSaveReqVO reqVO = randomPojo(DictTypeSaveReqVO.class,
                o -> o.setStatus(randomEle(CommonStatusEnum.values()).getStatus()))
                .setId(null);

        Long dictTypeId = dictApplicationService.createDictType(reqVO);

        assertNotNull(dictTypeId);
        DictTypeDO dictType = dictTypeMapper.selectById(dictTypeId);
        assertPojoEquals(reqVO, dictType, "id");
    }

    @Test
    public void testUpdateDictType_success() {
        DictTypeDO dbDictType = randomDictTypeDO();
        dictTypeMapper.insert(dbDictType);
        DictTypeSaveReqVO reqVO = randomPojo(DictTypeSaveReqVO.class, o -> {
            o.setId(dbDictType.getId());
            o.setStatus(randomEle(CommonStatusEnum.values()).getStatus());
        });

        dictApplicationService.updateDictType(reqVO);

        DictTypeDO dictType = dictTypeMapper.selectById(reqVO.getId());
        assertPojoEquals(reqVO, dictType);
    }

    @Test
    public void testDeleteDictType_success() {
        DictTypeDO dbDictType = randomDictTypeDO();
        dictTypeMapper.insert(dbDictType);

        dictApplicationService.deleteDictType(dbDictType.getId());

        assertNull(dictTypeMapper.selectById(dbDictType.getId()));
    }

    @Test
    public void testDeleteDictType_hasChildren() {
        DictTypeDO dbDictType = randomDictTypeDO();
        dictTypeMapper.insert(dbDictType);
        dictDataMapper.insert(randomPojo(DictDataDO.class, o -> o.setDictType(dbDictType.getType())));

        assertServiceException(() -> dictApplicationService.deleteDictType(dbDictType.getId()), DICT_TYPE_HAS_CHILDREN);
    }

    @Test
    public void testGetDictTypeList() {
        DictTypeDO dictTypeDO01 = randomDictTypeDO();
        dictTypeMapper.insert(dictTypeDO01);
        DictTypeDO dictTypeDO02 = randomDictTypeDO();
        dictTypeMapper.insert(dictTypeDO02);

        List<DictTypeDO> dictTypeDOList = dictApplicationService.getDictTypeList();

        assertEquals(2, dictTypeDOList.size());
        assertPojoEquals(dictTypeDO01, dictTypeDOList.get(0));
        assertPojoEquals(dictTypeDO02, dictTypeDOList.get(1));
    }

    @Test
    public void testValidateDictTypeExists_notExists() {
        assertServiceException(() -> dictApplicationService.validateDictTypeExists(randomLongId()), DICT_TYPE_NOT_EXISTS);
    }

    @Test
    public void testValidateDictTypeUnique_valueDuplicateForCreate() {
        String type = randomString();
        dictTypeMapper.insert(randomDictTypeDO(o -> o.setType(type)));

        assertServiceException(() -> dictApplicationService.validateDictTypeUnique(null, type),
                DICT_TYPE_TYPE_DUPLICATE);
    }

    @Test
    public void testValidateDictTypeNameUnique_nameDuplicateForUpdate() {
        Long id = randomLongId();
        String name = randomString();
        dictTypeMapper.insert(randomDictTypeDO(o -> o.setName(name)));

        assertServiceException(() -> dictApplicationService.validateDictTypeNameUnique(id, name),
                DICT_TYPE_NAME_DUPLICATE);
    }

    @SafeVarargs
    private static DictTypeDO randomDictTypeDO(Consumer<DictTypeDO>... consumers) {
        Consumer<DictTypeDO> consumer = o -> o.setStatus(randomEle(CommonStatusEnum.values()).getStatus());
        return randomPojo(DictTypeDO.class, ArrayUtils.append(consumer, consumers));
    }

}
