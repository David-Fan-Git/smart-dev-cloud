package com.develop.mvp.pk.module.iot.application.device.port.inbound;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.iot.domain.device.IotDevice;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IotDeviceUseCase {

    Long createDevice(String deviceName, String nickname, String serialNumber,
                      String picUrl, Set<Long> groupIds, Long productId,
                      String productKey, Integer deviceType, Long gatewayId,
                      String deviceSecret, String config,
                      BigDecimal latitude, BigDecimal longitude);

    void updateDevice(Long id, String nickname, String serialNumber,
                      String picUrl, Set<Long> groupIds, Long gatewayId,
                      String config, BigDecimal latitude, BigDecimal longitude);

    void deleteDevice(Long id);

    void updateDeviceState(Long id, Integer state);

    void updateDeviceGroup(Collection<Long> ids, Set<Long> groupIds);

    void updateDeviceFirmware(Long deviceId, Long firmwareId);

    void updateDeviceLocation(Long deviceId, BigDecimal longitude, BigDecimal latitude);

    void bindDeviceGateway(Collection<Long> subIds, Long gatewayId);

    void unbindDeviceGateway(Collection<Long> subIds, Long gatewayId);

    IotDevice getDevice(Long id);

    List<IotDevice> getDeviceListByState(Integer state);

    List<IotDevice> getDeviceListByProductId(Long productId);

    List<IotDevice> getDeviceListByCondition(@Nullable Integer deviceType, @Nullable Long productId);

    List<IotDevice> getDeviceListByGatewayId(Long gatewayId);

    List<IotDevice> getDeviceList(Collection<Long> ids);

    PageResult<IotDevice> getDevicePage(String deviceName, String nickname,
                                        Long productId, Integer deviceType,
                                        Integer status, Long groupId,
                                        Long gatewayId, Integer pageNo,
                                        Integer pageSize);

    long getDeviceCountByProductId(Long productId);

    long getDeviceCountByGroupId(Long groupId);

    long getDeviceCount(LocalDateTime createTime);

    Map<Long, Integer> getDeviceCountMapByProductId();

    Map<Integer, Long> getDeviceCountMapByState();

}
