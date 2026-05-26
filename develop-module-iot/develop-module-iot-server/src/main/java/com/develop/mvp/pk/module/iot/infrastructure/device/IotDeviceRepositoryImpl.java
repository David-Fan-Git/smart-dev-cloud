package com.develop.mvp.pk.module.iot.infrastructure.device;

import cn.hutool.core.collection.CollUtil;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.iot.dal.dataobject.device.IotDeviceDO;
import com.develop.mvp.pk.module.iot.dal.mysql.device.IotDeviceMapper;
import com.develop.mvp.pk.module.iot.domain.device.IotDevice;
import com.develop.mvp.pk.module.iot.domain.device.IotDeviceFactory;
import com.develop.mvp.pk.module.iot.domain.device.repository.IotDevicePageQuery;
import com.develop.mvp.pk.module.iot.domain.device.repository.IotDeviceRepository;
import com.develop.mvp.pk.module.iot.domain.device.valueobject.IotDeviceId;
import com.develop.mvp.pk.module.iot.domain.device.valueobject.IotDeviceState;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class IotDeviceRepositoryImpl implements IotDeviceRepository {

    private final IotDeviceMapper iotDeviceMapper;

    public IotDeviceRepositoryImpl(IotDeviceMapper iotDeviceMapper) {
        this.iotDeviceMapper = iotDeviceMapper;
    }

    @Override
    public IotDevice save(IotDevice device) {
        IotDeviceDO deviceDO = toDataObject(device);
        if (device.id() != null && iotDeviceMapper.selectById(device.id()) != null) {
            iotDeviceMapper.updateById(deviceDO);
        } else {
            iotDeviceMapper.insert(deviceDO);
        }
        return device;
    }

    @Override
    public void delete(IotDeviceId id) {
        iotDeviceMapper.deleteById(id.value());
    }

    @Override
    public IotDevice findById(IotDeviceId id) {
        IotDeviceDO deviceDO = iotDeviceMapper.selectById(id.value());
        return deviceDO != null ? toDomain(deviceDO) : null;
    }

    @Override
    public Optional<IotDevice> findByDeviceName(String deviceName) {
        if (deviceName == null) return Optional.empty();
        IotDeviceDO deviceDO = iotDeviceMapper.selectByDeviceName(deviceName);
        return Optional.ofNullable(deviceDO != null ? toDomain(deviceDO) : null);
    }

    @Override
    public Optional<IotDevice> findByProductKeyAndDeviceName(String productKey, String deviceName) {
        if (productKey == null || deviceName == null) return Optional.empty();
        IotDeviceDO deviceDO = iotDeviceMapper.selectByProductKeyAndDeviceName(productKey, deviceName);
        return Optional.ofNullable(deviceDO != null ? toDomain(deviceDO) : null);
    }

    @Override
    public Optional<IotDevice> findBySerialNumber(String serialNumber) {
        if (serialNumber == null) return Optional.empty();
        IotDeviceDO deviceDO = iotDeviceMapper.selectBySerialNumber(serialNumber);
        return Optional.ofNullable(deviceDO != null ? toDomain(deviceDO) : null);
    }

    @Override
    public List<IotDevice> findByState(IotDeviceState state) {
        return iotDeviceMapper.selectListByState(state.code()).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<IotDevice> findByProductId(Long productId) {
        return iotDeviceMapper.selectListByProductId(productId).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<IotDevice> findByCondition(@Nullable Integer deviceType, @Nullable Long productId) {
        return iotDeviceMapper.selectListByCondition(deviceType, productId).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<IotDevice> findByGatewayId(Long gatewayId) {
        return iotDeviceMapper.selectListByGatewayId(gatewayId).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<IotDevice> findByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) return Collections.emptyList();
        return iotDeviceMapper.selectByIds(ids).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public PageResult<IotDevice> findPage(IotDevicePageQuery query) {
        var reqVO = new com.develop.mvp.pk.module.iot.controller.admin.device.vo.device.IotDevicePageReqVO();
        reqVO.setDeviceName(query.deviceName());
        reqVO.setNickname(query.nickname());
        reqVO.setProductId(query.productId());
        reqVO.setDeviceType(query.deviceType());
        reqVO.setStatus(query.state() != null ? query.state().code() : null);
        reqVO.setGroupId(query.groupId());
        reqVO.setGatewayId(query.gatewayId());
        if (query.pageNo() != null) reqVO.setPageNo(query.pageNo());
        if (query.pageSize() != null) reqVO.setPageSize(query.pageSize());

        PageResult<IotDeviceDO> doPage = iotDeviceMapper.selectPage(reqVO);
        List<IotDevice> devices = doPage.getList().stream()
                .map(this::toDomain).collect(Collectors.toList());
        return new PageResult<>(devices, doPage.getTotal());
    }

    @Override
    public PageResult<IotDevice> findUnboundSubDevicePage(IotDevicePageQuery query) {
        var reqVO = new com.develop.mvp.pk.module.iot.controller.admin.device.vo.device.IotDevicePageReqVO();
        reqVO.setDeviceName(query.deviceName());
        reqVO.setNickname(query.nickname());
        reqVO.setProductId(query.productId());
        if (query.pageNo() != null) reqVO.setPageNo(query.pageNo());
        if (query.pageSize() != null) reqVO.setPageSize(query.pageSize());

        PageResult<IotDeviceDO> doPage = iotDeviceMapper.selectUnboundSubDevicePage(reqVO);
        List<IotDevice> devices = doPage.getList().stream()
                .map(this::toDomain).collect(Collectors.toList());
        return new PageResult<>(devices, doPage.getTotal());
    }

    @Override
    public long countByProductId(Long productId) {
        return iotDeviceMapper.selectCountByProductId(productId);
    }

    @Override
    public long countByGatewayId(Long gatewayId) {
        return iotDeviceMapper.selectCountByGatewayId(gatewayId);
    }

    @Override
    public long countByGroupId(Long groupId) {
        return iotDeviceMapper.selectCountByGroupId(groupId);
    }

    @Override
    public long countByCreateTime(LocalDateTime createTime) {
        return iotDeviceMapper.selectCountByCreateTime(createTime);
    }

    @Override
    public Map<Long, Integer> countDeviceMapByProductId() {
        return iotDeviceMapper.selectDeviceCountMapByProductId();
    }

    @Override
    public Map<Integer, Long> countDeviceGroupByState() {
        return iotDeviceMapper.selectDeviceCountGroupByState();
    }

    @Override
    public List<IotDevice> findByHasLocation() {
        return iotDeviceMapper.selectListByHasLocation().stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<IotDevice> findByProductKeyAndDeviceNames(String productKey, Collection<String> deviceNames) {
        if (productKey == null || CollUtil.isEmpty(deviceNames)) return Collections.emptyList();
        return iotDeviceMapper.selectByProductKeyAndDeviceNames(productKey, deviceNames).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsBySerialNumber(String serialNumber, Long excludeId) {
        if (serialNumber == null) return false;
        IotDeviceDO exist = iotDeviceMapper.selectBySerialNumber(serialNumber);
        return exist != null && !exist.getId().equals(excludeId);
    }

    @Override
    public void updateGatewayIdBatch(Collection<Long> ids, Long gatewayId) {
        iotDeviceMapper.updateGatewayIdBatch(ids, gatewayId);
    }

    private IotDeviceDO toDataObject(IotDevice device) {
        IotDeviceDO deviceDO = new IotDeviceDO();
        if (device.id() != null) deviceDO.setId(device.id());
        deviceDO.setDeviceName(device.deviceName());
        deviceDO.setNickname(device.nickname());
        deviceDO.setSerialNumber(device.serialNumber());
        deviceDO.setPicUrl(device.picUrl());
        deviceDO.setGroupIds(device.groupIds());
        deviceDO.setProductId(device.productId());
        deviceDO.setProductKey(device.productKey());
        deviceDO.setDeviceType(device.deviceType());
        deviceDO.setGatewayId(device.gatewayId());
        deviceDO.setState(device.state() != null ? device.state().code() : null);
        deviceDO.setOnlineTime(device.onlineTime());
        deviceDO.setOfflineTime(device.offlineTime());
        deviceDO.setActiveTime(device.activeTime());
        deviceDO.setFirmwareId(device.firmwareId());
        deviceDO.setDeviceSecret(device.deviceSecret());
        deviceDO.setConfig(device.config());
        deviceDO.setLatitude(device.latitude());
        deviceDO.setLongitude(device.longitude());
        return deviceDO;
    }

    private IotDevice toDomain(IotDeviceDO deviceDO) {
        return IotDeviceFactory.reconstitute(
                deviceDO.getId(), deviceDO.getDeviceName(), deviceDO.getNickname(),
                deviceDO.getSerialNumber(), deviceDO.getPicUrl(), deviceDO.getGroupIds(),
                deviceDO.getProductId(), deviceDO.getProductKey(), deviceDO.getDeviceType(),
                deviceDO.getGatewayId(), deviceDO.getState(), deviceDO.getOnlineTime(),
                deviceDO.getOfflineTime(), deviceDO.getActiveTime(), deviceDO.getFirmwareId(),
                deviceDO.getDeviceSecret(), deviceDO.getConfig(),
                deviceDO.getLatitude(), deviceDO.getLongitude()
        );
    }
}
