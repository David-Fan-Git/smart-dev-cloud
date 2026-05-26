package com.develop.mvp.pk.module.mes.dal.mysql.dv.checkplan;

import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.mes.dal.dataobject.dv.checkplan.MesDvCheckPlanMachineryDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 点检保养方案设备 Mapper
 *
 * @author David
 */
@Mapper
public interface MesDvCheckPlanMachineryMapper extends BaseMapperX<MesDvCheckPlanMachineryDO> {

    default List<MesDvCheckPlanMachineryDO> selectListByPlanId(Long planId) {
        return selectList(MesDvCheckPlanMachineryDO::getPlanId, planId);
    }

    default Long selectCountByPlanId(Long planId) {
        return selectCount(MesDvCheckPlanMachineryDO::getPlanId, planId);
    }

    default void deleteByPlanId(Long planId) {
        delete(new LambdaQueryWrapperX<MesDvCheckPlanMachineryDO>()
                .eq(MesDvCheckPlanMachineryDO::getPlanId, planId));
    }

    default Long selectCountByMachineryId(Long machineryId) {
        return selectCount(MesDvCheckPlanMachineryDO::getMachineryId, machineryId);
    }

    default List<MesDvCheckPlanMachineryDO> selectListByMachineryId(Long machineryId) {
        return selectList(MesDvCheckPlanMachineryDO::getMachineryId, machineryId);
    }

    default MesDvCheckPlanMachineryDO selectByPlanIdAndMachineryId(Long planId, Long machineryId) {
        return selectOne(new LambdaQueryWrapperX<MesDvCheckPlanMachineryDO>()
                .eq(MesDvCheckPlanMachineryDO::getPlanId, planId)
                .eq(MesDvCheckPlanMachineryDO::getMachineryId, machineryId));
    }

}
