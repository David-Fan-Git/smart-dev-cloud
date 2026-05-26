package com.develop.mvp.pk.module.iot.domain.device.repository;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.iot.domain.device.IotDevice;
import com.develop.mvp.pk.module.iot.domain.device.valueobject.IotDeviceId;
import com.develop.mvp.pk.module.iot.domain.device.valueobject.IotDeviceState;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IotDeviceRepository {
    IotDevice save(IotDevice device);
    void delete(IotDeviceId id);
    IotDevice findById(IotDeviceId id);
    Optional<IotDevice> findByDeviceName(String deviceName);
    Optional<IotDevice> findByProductKeyAndDeviceName(String productKey, String deviceName);
    Optional<IotDevice> findBySerialNumber(String serialNumber);
    List<IotDevice> findByState(IotDeviceState state);
    List<IotDevice> findByProductId(Long productId);
    List<IotDevice> findByCondition(@Nullable Integer deviceType, @Nullable Long productId);
    List<IotDevice> findByGatewayId(Long gatewayId);
    List<IotDevice> findByIds(Collection<Long> ids);
    PageResult<IotDevice> findPage(IotDevicePageQuery query);
    PageResult<IotDevice> findUnboundSubDevicePage(IotDevicePageQuery query);
    long countByProductId(Long productId);
    long countByGatewayId(Long gatewayId);
    long countByGroupId(Long groupId);
    long countByCreateTime(LocalDateTime createTime);
    Map<Long, Integer> countDeviceMapByProductId();
    Map<Integer, Long> countDeviceGroupByState();
    List<IotDevice> findByHasLocation();
    List<IotDevice> findByProductKeyAndDeviceNames(String productKey, Collection<String> deviceNames);
    boolean existsBySerialNumber(String serialNumber, Long excludeId);
    void updateGatewayIdBatch(Collection<Long> ids, Long gatewayId);
}
