package com.develop.mvp.pk.module.iot.application.device;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.iot.domain.device.IotDevice;
import com.develop.mvp.pk.module.iot.domain.device.IotDeviceFactory;
import com.develop.mvp.pk.module.iot.domain.device.event.DomainEvent;
import com.develop.mvp.pk.module.iot.domain.device.event.DomainEventPublisher;
import com.develop.mvp.pk.module.iot.domain.device.repository.IotDevicePageQuery;
import com.develop.mvp.pk.module.iot.domain.device.repository.IotDeviceRepository;
import com.develop.mvp.pk.module.iot.domain.device.valueobject.IotDeviceId;
import com.develop.mvp.pk.module.iot.domain.device.valueobject.IotDeviceState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.iot.enums.ErrorCodeConstants.*;

@Service
public class IotDeviceApplicationService {

    private final IotDeviceRepository iotDeviceRepository;
    private final DomainEventPublisher eventPublisher;

    public IotDeviceApplicationService(IotDeviceRepository iotDeviceRepository,
                                        DomainEventPublisher eventPublisher) {
        this.iotDeviceRepository = iotDeviceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Long createDevice(String deviceName, String nickname, String serialNumber,
                              String picUrl, Set<Long> groupIds, Long productId,
                              String productKey, Integer deviceType, Long gatewayId,
                              String deviceSecret, String config,
                              BigDecimal latitude, BigDecimal longitude) {
        IotDevice device = IotDeviceFactory.create(deviceName, nickname, serialNumber,
                picUrl, groupIds, productId, productKey, deviceType, gatewayId,
                deviceSecret, config, latitude, longitude);
        IotDevice savedDevice = iotDeviceRepository.save(device);
        publishEvents(device);
        return savedDevice.id();
    }

    @Transactional
    public void updateDevice(Long id, String nickname, String serialNumber,
                              String picUrl, Set<Long> groupIds, Long gatewayId,
                              String config, BigDecimal latitude, BigDecimal longitude) {
        IotDevice device = findExistingDevice(IotDeviceId.of(id));
        device.updateProfile(nickname, serialNumber, picUrl, groupIds, gatewayId, config,
                latitude, longitude);
        iotDeviceRepository.save(device);
        publishEvents(device);
    }

    @Transactional
    public void deleteDevice(Long id) {
        IotDevice device = findExistingDevice(IotDeviceId.of(id));
        device.markDeleted();
        iotDeviceRepository.delete(IotDeviceId.of(id));
        publishEvents(device);
    }

    @Transactional
    public void updateDeviceState(Long id, Integer state) {
        IotDevice device = findExistingDevice(IotDeviceId.of(id));
        IotDeviceState targetState = IotDeviceState.of(state);
        if (targetState.isOnline()) {
            device.goOnline();
        } else if (targetState.isOffline()) {
            device.goOffline();
        }
        iotDeviceRepository.save(device);
        publishEvents(device);
    }

    @Transactional
    public void updateDeviceGroup(Collection<Long> ids, Set<Long> groupIds) {
        for (Long id : ids) {
            IotDevice device = findExistingDevice(IotDeviceId.of(id));
            device.updateGroups(groupIds);
            iotDeviceRepository.save(device);
            publishEvents(device);
        }
    }

    @Transactional
    public void updateDeviceFirmware(Long deviceId, Long firmwareId) {
        IotDevice device = findExistingDevice(IotDeviceId.of(deviceId));
        device.updateFirmware(firmwareId);
        iotDeviceRepository.save(device);
        publishEvents(device);
    }

    @Transactional
    public void updateDeviceLocation(Long deviceId, BigDecimal longitude, BigDecimal latitude) {
        IotDevice device = findExistingDevice(IotDeviceId.of(deviceId));
        device.updateLocation(longitude, latitude);
        iotDeviceRepository.save(device);
        publishEvents(device);
    }

    @Transactional
    public void bindDeviceGateway(Collection<Long> subIds, Long gatewayId) {
        for (Long subId : subIds) {
            IotDevice device = findExistingDevice(IotDeviceId.of(subId));
            device.bindGateway(gatewayId);
            iotDeviceRepository.save(device);
            publishEvents(device);
        }
    }

    @Transactional
    public void unbindDeviceGateway(Collection<Long> subIds, Long gatewayId) {
        for (Long subId : subIds) {
            IotDevice device = findExistingDevice(IotDeviceId.of(subId));
            if (device.gatewayId() != null && device.gatewayId().equals(gatewayId)) {
                device.unbindGateway();
                iotDeviceRepository.save(device);
                publishEvents(device);
            }
        }
    }

    public IotDevice getDevice(Long id) {
        return iotDeviceRepository.findById(IotDeviceId.of(id));
    }

    public List<IotDevice> getDeviceListByState(Integer state) {
        return iotDeviceRepository.findByState(IotDeviceState.of(state));
    }

    public List<IotDevice> getDeviceListByProductId(Long productId) {
        return iotDeviceRepository.findByProductId(productId);
    }

    public List<IotDevice> getDeviceListByCondition(@Nullable Integer deviceType, @Nullable Long productId) {
        return iotDeviceRepository.findByCondition(deviceType, productId);
    }

    public List<IotDevice> getDeviceListByGatewayId(Long gatewayId) {
        return iotDeviceRepository.findByGatewayId(gatewayId);
    }

    public List<IotDevice> getDeviceList(Collection<Long> ids) {
        return iotDeviceRepository.findByIds(ids);
    }

    public PageResult<IotDevice> getDevicePage(String deviceName, String nickname,
                                                Long productId, Integer deviceType,
                                                Integer status, Long groupId,
                                                Long gatewayId, Integer pageNo,
                                                Integer pageSize) {
        return iotDeviceRepository.findPage(new IotDevicePageQuery(
                deviceName, nickname, productId, deviceType,
                status != null ? IotDeviceState.of(status) : null,
                groupId, gatewayId, pageNo, pageSize));
    }

    public long getDeviceCountByProductId(Long productId) {
        return iotDeviceRepository.countByProductId(productId);
    }

    public long getDeviceCountByGroupId(Long groupId) {
        return iotDeviceRepository.countByGroupId(groupId);
    }

    public long getDeviceCount(LocalDateTime createTime) {
        return iotDeviceRepository.countByCreateTime(createTime);
    }

    public Map<Long, Integer> getDeviceCountMapByProductId() {
        return iotDeviceRepository.countDeviceMapByProductId();
    }

    public Map<Integer, Long> getDeviceCountMapByState() {
        return iotDeviceRepository.countDeviceGroupByState();
    }

    private IotDevice findExistingDevice(IotDeviceId id) {
        IotDevice device = iotDeviceRepository.findById(id);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        return device;
    }

    private void publishEvents(IotDevice device) {
        for (DomainEvent event : device.pullEvents()) {
            eventPublisher.publish(event);
        }
    }
}
