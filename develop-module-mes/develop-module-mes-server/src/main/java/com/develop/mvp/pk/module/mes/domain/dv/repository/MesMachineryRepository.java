package com.develop.mvp.pk.module.mes.domain.dv.repository;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.mes.domain.dv.MesMachinery;
import com.develop.mvp.pk.module.mes.domain.dv.valueobject.MesMachineryId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MesMachineryRepository {
    MesMachinery save(MesMachinery machinery);
    void delete(MesMachineryId id);
    MesMachinery findById(MesMachineryId id);
    Optional<MesMachinery> findByCode(String code);
    List<MesMachinery> findAll();
    List<MesMachinery> findByIds(Collection<Long> ids);
    PageResult<MesMachinery> findPage(MesMachineryPageQuery query);
    long countByMachineryTypeId(Long machineryTypeId);
}
