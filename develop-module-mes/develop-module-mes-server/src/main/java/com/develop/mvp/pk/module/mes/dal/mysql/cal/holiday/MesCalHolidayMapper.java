package com.develop.mvp.pk.module.mes.dal.mysql.cal.holiday;

import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.mes.dal.dataobject.cal.holiday.MesCalHolidayDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MES 假期设置 Mapper
 *
 * @author David
 */
@Mapper
public interface MesCalHolidayMapper extends BaseMapperX<MesCalHolidayDO> {

    default MesCalHolidayDO selectByDay(LocalDateTime day) {
        return selectOne(MesCalHolidayDO::getDay, day);
    }

    default List<MesCalHolidayDO> selectList(LocalDateTime startDay, LocalDateTime endDay) {
        return selectList(new LambdaQueryWrapperX<MesCalHolidayDO>()
                .betweenIfPresent(MesCalHolidayDO::getDay, startDay, endDay)
                .orderByAsc(MesCalHolidayDO::getDay));
    }

}
