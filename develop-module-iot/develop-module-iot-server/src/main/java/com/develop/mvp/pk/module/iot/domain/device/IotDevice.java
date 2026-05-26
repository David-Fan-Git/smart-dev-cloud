package com.develop.mvp.pk.module.iot.domain.device;

import com.develop.mvp.pk.module.iot.domain.device.event.DomainEvent;
import com.develop.mvp.pk.module.iot.domain.device.event.IotDeviceCreatedEvent;
import com.develop.mvp.pk.module.iot.domain.device.event.IotDeviceDeletedEvent;
import com.develop.mvp.pk.module.iot.domain.device.event.IotDeviceStateChangedEvent;
import com.develop.mvp.pk.module.iot.domain.device.valueobject.IotDeviceId;
import com.develop.mvp.pk.module.iot.domain.device.valueobject.IotDeviceState;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class IotDevice {

    private final IotDeviceId id;
    private String deviceName;
    private String nickname;
    private String serialNumber;
    private String picUrl;
    private Set<Long> groupIds;
    private Long productId;
    private String productKey;
    private Integer deviceType;
    private Long gatewayId;
    private IotDeviceState state;
    private LocalDateTime onlineTime;
    private LocalDateTime offlineTime;
    private LocalDateTime activeTime;
    private Long firmwareId;
    private String deviceSecret;
    private String config;
    private BigDecimal latitude;
    private BigDecimal longitude;

    private final List<DomainEvent> events = new ArrayList<>();

    // Full constructor for factory use (package-private)
    IotDevice(Long id, String deviceName, String nickname, String serialNumber,
              String picUrl, Set<Long> groupIds, Long productId, String productKey,
              Integer deviceType, Long gatewayId, IotDeviceState state,
              LocalDateTime onlineTime, LocalDateTime offlineTime, LocalDateTime activeTime,
              Long firmwareId, String deviceSecret, String config,
              BigDecimal latitude, BigDecimal longitude) {
        this.id = id != null ? IotDeviceId.of(id) : null;
        this.deviceName = Objects.requireNonNull(deviceName, "设备名称不能为空");
        this.nickname = nickname;
        this.serialNumber = serialNumber;
        this.picUrl = picUrl;
        this.groupIds = groupIds;
        this.productId = Objects.requireNonNull(productId, "产品编号不能为空");
        this.productKey = productKey;
        this.deviceType = deviceType;
        this.gatewayId = gatewayId;
        this.state = state != null ? state : IotDeviceState.INACTIVE;
        this.onlineTime = onlineTime;
        this.offlineTime = offlineTime;
        this.activeTime = activeTime;
        this.firmwareId = firmwareId;
        this.deviceSecret = deviceSecret;
        this.config = config;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Minimal constructor for skeleton compatibility
    public IotDevice(Long id, String name) {
        this.id = id != null ? IotDeviceId.of(id) : null;
        this.deviceName = Objects.requireNonNull(name, "设备名称不能为空");
        this.state = IotDeviceState.INACTIVE;
        this.events.clear();
    }

    // Factory for backward compatibility
    public static IotDevice of(Long id, String name) {
        IotDevice device = new IotDevice(id, name);
        return device;
    }

    // Business methods

    public void updateProfile(String nickname, String serialNumber, String picUrl,
                               Set<Long> groupIds, Long gatewayId, String config,
                               BigDecimal latitude, BigDecimal longitude) {
        this.nickname = nickname;
        this.serialNumber = serialNumber;
        this.picUrl = picUrl;
        this.groupIds = groupIds;
        this.gatewayId = gatewayId;
        this.config = config;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void updateGroups(Set<Long> groupIds) {
        this.groupIds = groupIds;
    }

    public void goOnline() {
        IotDeviceState oldState = this.state;
        this.state = IotDeviceState.ONLINE;
        if (this.activeTime == null) {
            this.activeTime = LocalDateTime.now();
        }
        this.onlineTime = LocalDateTime.now();
        if (oldState != IotDeviceState.ONLINE) {
            events.add(new IotDeviceStateChangedEvent(
                    this.id != null ? this.id.value() : null, this.deviceName, oldState, this.state));
        }
    }

    public void goOffline() {
        IotDeviceState oldState = this.state;
        this.state = IotDeviceState.OFFLINE;
        this.offlineTime = LocalDateTime.now();
        if (oldState != IotDeviceState.OFFLINE) {
            events.add(new IotDeviceStateChangedEvent(
                    this.id != null ? this.id.value() : null, this.deviceName, oldState, this.state));
        }
    }

    public void markActive() {
        if (this.state.isInactive()) {
            this.state = IotDeviceState.ONLINE;
            this.activeTime = LocalDateTime.now();
            this.onlineTime = LocalDateTime.now();
        }
    }

    public void updateFirmware(Long firmwareId) {
        this.firmwareId = firmwareId;
    }

    public void updateLocation(BigDecimal longitude, BigDecimal latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public void bindGateway(Long gatewayId) {
        this.gatewayId = gatewayId;
    }

    public void unbindGateway() {
        this.gatewayId = null;
    }

    public void markDeleted() {
        events.add(new IotDeviceDeletedEvent(
                this.id != null ? this.id.value() : null, this.deviceName, this.productKey));
    }

    // Query methods

    public IotDeviceId deviceId() { return id; }
    public Long id() { return id != null ? id.value() : null; }
    public String deviceName() { return deviceName; }
    public String nickname() { return nickname; }
    public String serialNumber() { return serialNumber; }
    public String picUrl() { return picUrl; }
    public Set<Long> groupIds() { return groupIds; }
    public Long productId() { return productId; }
    public String productKey() { return productKey; }
    public Integer deviceType() { return deviceType; }
    public Long gatewayId() { return gatewayId; }
    public IotDeviceState state() { return state; }
    public LocalDateTime onlineTime() { return onlineTime; }
    public LocalDateTime offlineTime() { return offlineTime; }
    public LocalDateTime activeTime() { return activeTime; }
    public Long firmwareId() { return firmwareId; }
    public String deviceSecret() { return deviceSecret; }
    public String config() { return config; }
    public BigDecimal latitude() { return latitude; }
    public BigDecimal longitude() { return longitude; }

    public boolean isOnline() { return state.isOnline(); }
    public boolean isOffline() { return state.isOffline(); }
    public boolean isInactive() { return state.isInactive(); }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> result = new ArrayList<>(events);
        events.clear();
        return result;
    }

    void addEvent(DomainEvent event) {
        this.events.add(event);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IotDevice that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "IotDevice{id=" + id + ", deviceName=" + deviceName + '}';
    }
}
