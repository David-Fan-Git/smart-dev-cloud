package com.develop.mvp.pk.module.mes.application.dv;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.mes.domain.dv.MesMachinery;
import com.develop.mvp.pk.module.mes.domain.dv.MesMachineryFactory;
import com.develop.mvp.pk.module.mes.domain.dv.event.DomainEvent;
import com.develop.mvp.pk.module.mes.domain.dv.event.DomainEventPublisher;
import com.develop.mvp.pk.module.mes.domain.dv.repository.MesMachineryPageQuery;
import com.develop.mvp.pk.module.mes.domain.dv.repository.MesMachineryRepository;
import com.develop.mvp.pk.module.mes.domain.dv.valueobject.MesMachineryId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.mes.enums.ErrorCodeConstants.*;

@Service
public class MesMachineryApplicationService {

    private final MesMachineryRepository mesMachineryRepository;
    private final DomainEventPublisher eventPublisher;

    public MesMachineryApplicationService(MesMachineryRepository mesMachineryRepository,
                                           DomainEventPublisher eventPublisher) {
        this.mesMachineryRepository = mesMachineryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Long createMachinery(String code, String name, String brand,
                                 String specification, Long machineryTypeId,
                                 Long workshopId, String remark) {
        MesMachinery machinery = MesMachineryFactory.create(code, name, brand,
                specification, machineryTypeId, workshopId, remark);
        mesMachineryRepository.save(machinery);
        publishEvents(machinery);
        return machinery.id();
    }

    @Transactional
    public void updateMachinery(Long id, String code, String name, String brand,
                                 String specification, Long machineryTypeId,
                                 Long workshopId, String remark) {
        MesMachinery machinery = findExistingMachinery(MesMachineryId.of(id));
        machinery.updateProfile(code, name, brand, specification, machineryTypeId,
                workshopId, null, null, remark);
        mesMachineryRepository.save(machinery);
        publishEvents(machinery);
    }

    @Transactional
    public void deleteMachinery(Long id) {
        MesMachinery machinery = findExistingMachinery(MesMachineryId.of(id));
        machinery.markDeleted();
        mesMachineryRepository.delete(MesMachineryId.of(id));
        publishEvents(machinery);
    }

    @Transactional
    public void updateLastCheckTime(Long machineryId, LocalDateTime lastCheckTime) {
        MesMachinery machinery = findExistingMachinery(MesMachineryId.of(machineryId));
        machinery.updateLastCheckTime(lastCheckTime);
        mesMachineryRepository.save(machinery);
        publishEvents(machinery);
    }

    @Transactional
    public void updateLastMaintenTime(Long machineryId, LocalDateTime lastMaintenTime) {
        MesMachinery machinery = findExistingMachinery(MesMachineryId.of(machineryId));
        machinery.updateLastMaintenTime(lastMaintenTime);
        mesMachineryRepository.save(machinery);
        publishEvents(machinery);
    }

    public MesMachinery getMachinery(Long id) {
        return mesMachineryRepository.findById(MesMachineryId.of(id));
    }

    public List<MesMachinery> getMachineryList() {
        return mesMachineryRepository.findAll();
    }

    public List<MesMachinery> getMachineryList(Collection<Long> ids) {
        return mesMachineryRepository.findByIds(ids);
    }

    public PageResult<MesMachinery> getMachineryPage(String code, String name, String brand,
                                                      Long machineryTypeId, Long workshopId,
                                                      Integer status, Integer pageNo,
                                                      Integer pageSize) {
        return mesMachineryRepository.findPage(new MesMachineryPageQuery(
                code, name, brand, machineryTypeId, workshopId, status, pageNo, pageSize));
    }

    public long getMachineryCountByMachineryTypeId(Long machineryTypeId) {
        return mesMachineryRepository.countByMachineryTypeId(machineryTypeId);
    }

    private MesMachinery findExistingMachinery(MesMachineryId id) {
        MesMachinery machinery = mesMachineryRepository.findById(id);
        if (machinery == null) {
            throw exception(DV_MACHINERY_NOT_EXISTS);
        }
        return machinery;
    }

    private void publishEvents(MesMachinery machinery) {
        for (DomainEvent event : machinery.pullEvents()) {
            eventPublisher.publish(event);
        }
    }
}
