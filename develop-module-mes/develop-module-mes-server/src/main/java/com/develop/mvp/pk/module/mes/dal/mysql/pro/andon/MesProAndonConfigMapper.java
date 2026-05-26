package com.develop.mvp.pk.module.mes.dal.mysql.pro.andon;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.mes.controller.admin.pro.andon.vo.config.MesProAndonConfigPageReqVO;
import com.develop.mvp.pk.module.mes.dal.dataobject.pro.andon.MesProAndonConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MES 安灯呼叫配置 Mapper
 *
 * @author David
 */
@Mapper
public interface MesProAndonConfigMapper extends BaseMapperX<MesProAndonConfigDO> {

    default PageResult<MesProAndonConfigDO> selectPage(MesProAndonConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesProAndonConfigDO>()
                .likeIfPresent(MesProAndonConfigDO::getReason, reqVO.getReason())
                .eqIfPresent(MesProAndonConfigDO::getLevel, reqVO.getLevel())
                .orderByDesc(MesProAndonConfigDO::getId));
    }

}
