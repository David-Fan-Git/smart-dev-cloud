package com.develop.mvp.pk.module.mes.infrastructure.dv;

import cn.hutool.core.collection.CollUtil;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.mes.dal.dataobject.dv.machinery.MesDvMachineryDO;
import com.develop.mvp.pk.module.mes.dal.mysql.dv.machinery.MesDvMachineryMapper;
import com.develop.mvp.pk.module.mes.domain.dv.MesMachinery;
import com.develop.mvp.pk.module.mes.domain.dv.MesMachineryFactory;
import com.develop.mvp.pk.module.mes.domain.dv.repository.MesMachineryPageQuery;
import com.develop.mvp.pk.module.mes.domain.dv.repository.MesMachineryRepository;
import com.develop.mvp.pk.module.mes.domain.dv.valueobject.MesMachineryId;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class MesMachineryRepositoryImpl implements MesMachineryRepository {

    private final MesDvMachineryMapper mesDvMachineryMapper;

    public MesMachineryRepositoryImpl(MesDvMachineryMapper mesDvMachineryMapper) {
        this.mesDvMachineryMapper = mesDvMachineryMapper;
    }

    @Override
    public MesMachinery save(MesMachinery machinery) {
        MesDvMachineryDO machineryDO = toDataObject(machinery);
        if (machinery.id() != null && mesDvMachineryMapper.selectById(machinery.id()) != null) {
            mesDvMachineryMapper.updateById(machineryDO);
        } else {
            mesDvMachineryMapper.insert(machineryDO);
        }
        return machinery;
    }

    @Override
    public void delete(MesMachineryId id) {
        mesDvMachineryMapper.deleteById(id.value());
    }

    @Override
    public MesMachinery findById(MesMachineryId id) {
        MesDvMachineryDO machineryDO = mesDvMachineryMapper.selectById(id.value());
        return machineryDO != null ? toDomain(machineryDO) : null;
    }

    @Override
    public Optional<MesMachinery> findByCode(String code) {
        if (code == null) return Optional.empty();
        MesDvMachineryDO machineryDO = mesDvMachineryMapper.selectByCode(code);
        return Optional.ofNullable(machineryDO != null ? toDomain(machineryDO) : null);
    }

    @Override
    public List<MesMachinery> findAll() {
        return mesDvMachineryMapper.selectList().stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<MesMachinery> findByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) return Collections.emptyList();
        return mesDvMachineryMapper.selectByIds(ids).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public PageResult<MesMachinery> findPage(MesMachineryPageQuery query) {
        var reqVO = new com.develop.mvp.pk.module.mes.controller.admin.dv.machinery.vo.MesDvMachineryPageReqVO();
        reqVO.setCode(query.code());
        reqVO.setName(query.name());
        reqVO.setBrand(query.brand());
        reqVO.setMachineryTypeId(query.machineryTypeId());
        reqVO.setWorkshopId(query.workshopId());
        reqVO.setStatus(query.status());
        if (query.pageNo() != null) reqVO.setPageNo(query.pageNo());
        if (query.pageSize() != null) reqVO.setPageSize(query.pageSize());

        PageResult<MesDvMachineryDO> doPage = mesDvMachineryMapper.selectPage(reqVO);
        List<MesMachinery> machineries = doPage.getList().stream()
                .map(this::toDomain).collect(Collectors.toList());
        return new PageResult<>(machineries, doPage.getTotal());
    }

    @Override
    public long countByMachineryTypeId(Long machineryTypeId) {
        return mesDvMachineryMapper.selectCountByMachineryTypeId(machineryTypeId);
    }

    private MesDvMachineryDO toDataObject(MesMachinery m) {
        MesDvMachineryDO machineryDO = new MesDvMachineryDO();
        if (m.id() != null) machineryDO.setId(m.id());
        machineryDO.setCode(m.code());
        machineryDO.setName(m.name());
        machineryDO.setBrand(m.brand());
        machineryDO.setSpecification(m.specification());
        machineryDO.setMachineryTypeId(m.machineryTypeId());
        machineryDO.setWorkshopId(m.workshopId());
        machineryDO.setStatus(m.status() != null ? m.status().code() : null);
        machineryDO.setLastMaintenTime(m.lastMaintenTime());
        machineryDO.setLastCheckTime(m.lastCheckTime());
        machineryDO.setRemark(m.remark());
        return machineryDO;
    }

    private MesMachinery toDomain(MesDvMachineryDO machineryDO) {
        return MesMachineryFactory.reconstitute(
                machineryDO.getId(), machineryDO.getCode(), machineryDO.getName(),
                machineryDO.getBrand(), machineryDO.getSpecification(),
                machineryDO.getMachineryTypeId(), machineryDO.getWorkshopId(),
                machineryDO.getStatus(), machineryDO.getLastMaintenTime(),
                machineryDO.getLastCheckTime(), machineryDO.getRemark()
        );
    }
}
