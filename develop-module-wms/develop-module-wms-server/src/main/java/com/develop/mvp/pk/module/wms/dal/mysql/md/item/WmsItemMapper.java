package com.develop.mvp.pk.module.wms.dal.mysql.md.item;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.wms.controller.admin.md.item.vo.item.WmsItemListReqVO;
import com.develop.mvp.pk.module.wms.controller.admin.md.item.vo.item.WmsItemPageReqVO;
import com.develop.mvp.pk.module.wms.dal.dataobject.md.item.WmsItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * WMS 商品 Mapper
 *
 * @author David
 */
@Mapper
public interface WmsItemMapper extends BaseMapperX<WmsItemDO> {

    default PageResult<WmsItemDO> selectPage(WmsItemPageReqVO reqVO, Collection<Long> categoryIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsItemDO>()
                .likeIfPresent(WmsItemDO::getCode, reqVO.getCode())
                .likeIfPresent(WmsItemDO::getName, reqVO.getName())
                .inIfPresent(WmsItemDO::getCategoryId, categoryIds)
                .eqIfPresent(WmsItemDO::getBrandId, reqVO.getBrandId())
                .betweenIfPresent(WmsItemDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WmsItemDO::getId));
    }

    default List<WmsItemDO> selectList(WmsItemListReqVO reqVO, Collection<Long> categoryIds) {
        return selectList(new LambdaQueryWrapperX<WmsItemDO>()
                .likeIfPresent(WmsItemDO::getCode, reqVO.getCode())
                .likeIfPresent(WmsItemDO::getName, reqVO.getName())
                .inIfPresent(WmsItemDO::getCategoryId, categoryIds)
                .eqIfPresent(WmsItemDO::getBrandId, reqVO.getBrandId())
                .betweenIfPresent(WmsItemDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WmsItemDO::getId));
    }

    default WmsItemDO selectByName(String name) {
        return selectOne(WmsItemDO::getName, name);
    }

    default WmsItemDO selectByCode(String code) {
        return selectOne(WmsItemDO::getCode, code);
    }

    default Long selectCountByCategoryId(Long categoryId) {
        return selectCount(WmsItemDO::getCategoryId, categoryId);
    }

    default Long selectCountByBrandId(Long brandId) {
        return selectCount(WmsItemDO::getBrandId, brandId);
    }

}
