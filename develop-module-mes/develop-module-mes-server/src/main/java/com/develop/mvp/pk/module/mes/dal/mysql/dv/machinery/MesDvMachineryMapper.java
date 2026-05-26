package com.develop.mvp.pk.module.mes.dal.mysql.dv.machinery;

import cn.hutool.core.collection.CollUtil;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.mes.controller.admin.dv.machinery.vo.MesDvMachineryPageReqVO;
import com.develop.mvp.pk.module.mes.dal.dataobject.dv.machinery.MesDvMachineryDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MES 设备台账 Mapper
 *
 * @author David
 */
@Mapper
public interface MesDvMachineryMapper extends BaseMapperX<MesDvMachineryDO> {

    default PageResult<MesDvMachineryDO> selectPage(MesDvMachineryPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesDvMachineryDO>()
                .likeIfPresent(MesDvMachineryDO::getCode, reqVO.getCode())
                .likeIfPresent(MesDvMachineryDO::getName, reqVO.getName())
                .likeIfPresent(MesDvMachineryDO::getBrand, reqVO.getBrand())
                .eqIfPresent(MesDvMachineryDO::getMachineryTypeId, reqVO.getMachineryTypeId())
                .inIfPresent(MesDvMachineryDO::getMachineryTypeId, reqVO.getMachineryTypeIds())
                .eqIfPresent(MesDvMachineryDO::getWorkshopId, reqVO.getWorkshopId())
                .eqIfPresent(MesDvMachineryDO::getStatus, reqVO.getStatus())
                .orderByDesc(MesDvMachineryDO::getId));
    }

    default MesDvMachineryDO selectByCode(String code) {
        return selectOne(MesDvMachineryDO::getCode, code);
    }

    default Long selectCountByMachineryTypeId(Long machineryTypeId) {
        return selectCount(MesDvMachineryDO::getMachineryTypeId, machineryTypeId);
    }

}
