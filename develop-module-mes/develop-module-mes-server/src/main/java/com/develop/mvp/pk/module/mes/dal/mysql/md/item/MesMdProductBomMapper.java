package com.develop.mvp.pk.module.mes.dal.mysql.md.item;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.mes.controller.admin.md.item.vo.bom.MesMdProductBomPageReqVO;
import com.develop.mvp.pk.module.mes.dal.dataobject.md.item.MesMdProductBomDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * MES 产品BOM Mapper
 *
 * @author David
 */
@Mapper
public interface MesMdProductBomMapper extends BaseMapperX<MesMdProductBomDO> {

    default PageResult<MesMdProductBomDO> selectPage(MesMdProductBomPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesMdProductBomDO>()
                .eq(MesMdProductBomDO::getItemId, reqVO.getItemId())
                .eqIfPresent(MesMdProductBomDO::getStatus, reqVO.getStatus())
                .orderByDesc(MesMdProductBomDO::getId));
    }

    default List<MesMdProductBomDO> selectByItemId(Long itemId) {
        return selectList(MesMdProductBomDO::getItemId, itemId);
    }

    default List<MesMdProductBomDO> selectByItemIds(Collection<Long> itemIds) {
        return selectList(MesMdProductBomDO::getItemId, itemIds);
    }

    default List<MesMdProductBomDO> selectAll() {
        return selectList();
    }

    default void deleteByItemId(Long itemId) {
        delete(MesMdProductBomDO::getItemId, itemId);
    }

}
