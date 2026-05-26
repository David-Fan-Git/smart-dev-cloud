package com.develop.mvp.pk.module.mes.domain.dv;

import com.develop.mvp.pk.module.mes.domain.dv.event.DomainEvent;
import com.develop.mvp.pk.module.mes.domain.dv.event.MesMachineryCreatedEvent;
import com.develop.mvp.pk.module.mes.domain.dv.event.MesMachineryDeletedEvent;
import com.develop.mvp.pk.module.mes.domain.dv.valueobject.MesMachineryId;
import com.develop.mvp.pk.module.mes.domain.dv.valueobject.MesMachineryStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MesMachinery {

    private final MesMachineryId id;
    private String code;
    private String name;
    private String brand;
    private String specification;
    private Long machineryTypeId;
    private Long workshopId;
    private MesMachineryStatus status;
    private LocalDateTime lastMaintenTime;
    private LocalDateTime lastCheckTime;
    private String remark;

    private final List<DomainEvent> events = new ArrayList<>();

    // Full constructor for factory use (package-private)
    MesMachinery(Long id, String code, String name, String brand, String specification,
                 Long machineryTypeId, Long workshopId, MesMachineryStatus status,
                 LocalDateTime lastMaintenTime, LocalDateTime lastCheckTime, String remark) {
        this.id = id != null ? MesMachineryId.of(id) : null;
        this.code = code;
        this.name = Objects.requireNonNull(name, "设备名称不能为空");
        this.brand = brand;
        this.specification = specification;
        this.machineryTypeId = machineryTypeId;
        this.workshopId = workshopId;
        this.status = status != null ? status : MesMachineryStatus.STOP;
        this.lastMaintenTime = lastMaintenTime;
        this.lastCheckTime = lastCheckTime;
        this.remark = remark;
    }

    // Minimal constructor for skeleton compatibility
    public MesMachinery(Long id, String name) {
        this.id = id != null ? MesMachineryId.of(id) : null;
        this.name = Objects.requireNonNull(name, "设备名称不能为空");
        this.status = MesMachineryStatus.STOP;
        this.events.clear();
    }

    // Factory for backward compatibility
    public static MesMachinery of(Long id, String name) {
        return new MesMachinery(id, name);
    }

    // Business methods

    public void updateProfile(String code, String name, String brand, String specification,
                               Long machineryTypeId, Long workshopId,
                               LocalDateTime lastMaintenTime, LocalDateTime lastCheckTime,
                               String remark) {
        this.code = code;
        this.name = Objects.requireNonNull(name, "设备名称不能为空");
        this.brand = brand;
        this.specification = specification;
        this.machineryTypeId = machineryTypeId;
        this.workshopId = workshopId;
        this.lastMaintenTime = lastMaintenTime;
        this.lastCheckTime = lastCheckTime;
        this.remark = remark;
    }

    public void updateLastCheckTime(LocalDateTime lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }

    public void updateLastMaintenTime(LocalDateTime lastMaintenTime) {
        this.lastMaintenTime = lastMaintenTime;
    }

    public void startProducing() {
        this.status = MesMachineryStatus.PRODUCING;
    }

    public void stop() {
        this.status = MesMachineryStatus.STOP;
    }

    public void startMaintenance() {
        this.status = MesMachineryStatus.MAINTENANCE;
    }

    public void markDeleted() {
        events.add(new MesMachineryDeletedEvent(
                this.id != null ? this.id.value() : null, this.code, this.name));
    }

    // Query methods

    public MesMachineryId machineryId() { return id; }
    public Long id() { return id != null ? id.value() : null; }
    public String code() { return code; }
    public String name() { return name; }
    public String brand() { return brand; }
    public String specification() { return specification; }
    public Long machineryTypeId() { return machineryTypeId; }
    public Long workshopId() { return workshopId; }
    public MesMachineryStatus status() { return status; }
    public LocalDateTime lastMaintenTime() { return lastMaintenTime; }
    public LocalDateTime lastCheckTime() { return lastCheckTime; }
    public String remark() { return remark; }

    public boolean isStop() { return status.isStop(); }
    public boolean isProducing() { return status.isProducing(); }
    public boolean isMaintenance() { return status.isMaintenance(); }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> result = new ArrayList<>(events);
        events.clear();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MesMachinery that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MesMachinery{id=" + id + ", name=" + name + '}';
    }
}
