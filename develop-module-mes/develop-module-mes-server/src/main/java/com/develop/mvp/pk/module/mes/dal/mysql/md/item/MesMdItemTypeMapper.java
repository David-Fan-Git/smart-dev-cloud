package com.develop.mvp.pk.module.mes.dal.mysql.md.item;

import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.mes.controller.admin.md.item.vo.type.MesMdItemTypeListReqVO;
import com.develop.mvp.pk.module.mes.dal.dataobject.md.item.MesMdItemTypeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 物料产品分类 Mapper
 *
 * @author David
 */
@Mapper
public interface MesMdItemTypeMapper extends BaseMapperX<MesMdItemTypeDO> {

    default List<MesMdItemTypeDO> selectList(MesMdItemTypeListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<MesMdItemTypeDO>()
                .likeIfPresent(MesMdItemTypeDO::getName, reqVO.getName())
                .eqIfPresent(MesMdItemTypeDO::getStatus, reqVO.getStatus())
                .orderByAsc(MesMdItemTypeDO::getSort));
    }

    default MesMdItemTypeDO selectByParentIdAndName(Long parentId, String name) {
        return selectOne(MesMdItemTypeDO::getParentId, parentId, MesMdItemTypeDO::getName, name);
    }

    default MesMdItemTypeDO selectByParentIdAndCode(Long parentId, String code) {
        return selectOne(MesMdItemTypeDO::getParentId, parentId, MesMdItemTypeDO::getCode, code);
    }

    default Long selectCountByParentId(Long parentId) {
        return selectCount(MesMdItemTypeDO::getParentId, parentId);
    }

}
