package com.develop.mvp.pk.module.mes.domain.dv;

import com.develop.mvp.pk.module.mes.domain.dv.valueobject.MesMachineryId;
import com.develop.mvp.pk.module.mes.domain.dv.valueobject.MesMachineryStatus;

import java.time.LocalDateTime;

public class MesMachineryFactory {

    public static MesMachinery create(String code, String name, String brand,
                                       String specification, Long machineryTypeId,
                                       Long workshopId, String remark) {
        return new MesMachinery(null, code, name, brand, specification, machineryTypeId,
                workshopId, MesMachineryStatus.STOP, null, null, remark);
    }

    public static MesMachinery reconstitute(Long id, String code, String name,
                                             String brand, String specification,
                                             Long machineryTypeId, Long workshopId,
                                             Integer status, LocalDateTime lastMaintenTime,
                                             LocalDateTime lastCheckTime, String remark) {
        return new MesMachinery(id, code, name, brand, specification, machineryTypeId,
                workshopId,
                status != null ? MesMachineryStatus.of(status) : MesMachineryStatus.STOP,
                lastMaintenTime, lastCheckTime, remark);
    }
}
