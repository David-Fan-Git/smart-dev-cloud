package com.develop.mvp.pk.module.iot.mvp;

import com.develop.mvp.pk.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.iot.application.command.command.AckIotDeviceCommand;
import com.develop.mvp.pk.module.iot.application.command.command.CreateIotDeviceCommand;
import com.develop.mvp.pk.module.iot.application.command.port.outbound.IotDeviceCommandMessagePort;
import com.develop.mvp.pk.module.iot.application.command.query.IotDeviceCommandQuery;
import com.develop.mvp.pk.module.iot.application.command.result.IotDeviceCommandResult;
import com.develop.mvp.pk.module.iot.application.command.service.IotDeviceCommandApplicationService;
import com.develop.mvp.pk.module.iot.application.device.service.IotDeviceApplicationService;
import com.develop.mvp.pk.module.iot.application.product.command.CreateIotProductCommand;
import com.develop.mvp.pk.module.iot.application.product.command.UpdateIotProductStatusCommand;
import com.develop.mvp.pk.module.iot.application.product.port.outbound.IotProductPropertyTablePort;
import com.develop.mvp.pk.module.iot.application.product.result.IotProductResult;
import com.develop.mvp.pk.module.iot.application.product.service.IotProductApplicationService;
import com.develop.mvp.pk.module.iot.application.property.command.PostIotDevicePropertyCommand;
import com.develop.mvp.pk.module.iot.application.property.port.outbound.IotDevicePropertyStoragePort;
import com.develop.mvp.pk.module.iot.application.property.query.IotDeviceLatestPropertyQuery;
import com.develop.mvp.pk.module.iot.application.property.result.IotDeviceLatestPropertyResult;
import com.develop.mvp.pk.module.iot.application.property.service.IotDevicePropertyApplicationService;
import com.develop.mvp.pk.module.iot.application.thingmodel.command.CreateIotThingModelCommand;
import com.develop.mvp.pk.module.iot.application.thingmodel.port.outbound.IotThingModelModbusPointPort;
import com.develop.mvp.pk.module.iot.application.thingmodel.result.IotThingModelTslResult;
import com.develop.mvp.pk.module.iot.application.thingmodel.service.IotThingModelApplicationService;
import com.develop.mvp.pk.module.iot.core.enums.IotDeviceMessageMethodEnum;
import com.develop.mvp.pk.module.iot.core.mq.message.IotDeviceMessage;
import com.develop.mvp.pk.module.iot.dal.dataobject.device.IotDeviceDO;
import com.develop.mvp.pk.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import com.develop.mvp.pk.module.iot.dal.dataobject.product.IotProductDO;
import com.develop.mvp.pk.module.iot.domain.command.model.IotDeviceCommand;
import com.develop.mvp.pk.module.iot.domain.command.repository.IotDeviceCommandRepository;
import com.develop.mvp.pk.module.iot.domain.command.valueobject.IotCommandId;
import com.develop.mvp.pk.module.iot.domain.command.valueobject.IotCommandStatus;
import com.develop.mvp.pk.module.iot.domain.device.IotDevice;
import com.develop.mvp.pk.module.iot.domain.device.event.DomainEventPublisher;
import com.develop.mvp.pk.module.iot.domain.device.repository.IotDevicePageQuery;
import com.develop.mvp.pk.module.iot.domain.device.repository.IotDeviceRepository;
import com.develop.mvp.pk.module.iot.domain.device.valueobject.IotDeviceId;
import com.develop.mvp.pk.module.iot.domain.device.valueobject.IotDeviceState;
import com.develop.mvp.pk.module.iot.domain.product.model.IotProduct;
import com.develop.mvp.pk.module.iot.domain.product.repository.IotProductRepository;
import com.develop.mvp.pk.module.iot.domain.product.valueobject.IotProductId;
import com.develop.mvp.pk.module.iot.domain.product.valueobject.IotProductKey;
import com.develop.mvp.pk.module.iot.domain.thingmodel.model.IotThingModel;
import com.develop.mvp.pk.module.iot.domain.thingmodel.repository.IotThingModelRepository;
import com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelId;
import com.develop.mvp.pk.module.iot.enums.product.IotProductStatusEnum;
import com.develop.mvp.pk.module.iot.enums.thingmodel.IotThingModelTypeEnum;
import com.develop.mvp.pk.module.iot.service.device.IotDeviceService;
import com.develop.mvp.pk.module.iot.service.product.IotProductService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IotMvpFlowIntegrationTest {

    @Test
    void mvpProductionLoopReachesOnlinePropertyAndAckedCommand() {
        FakeProductRepository productRepository = new FakeProductRepository();
        FakeThingModelRepository thingModelRepository = new FakeThingModelRepository();
        FakeDeviceRepository deviceRepository = new FakeDeviceRepository();
        FakePropertyStoragePort propertyStoragePort = new FakePropertyStoragePort();
        FakeCommandRepository commandRepository = new FakeCommandRepository();
        FakeCommandMessagePort commandMessagePort = new FakeCommandMessagePort();

        IotDeviceService deviceService = mock(IotDeviceService.class);
        when(deviceService.getDeviceCountByProductId(1L)).thenReturn(0L);
        IotProductService productService = mock(IotProductService.class);
        when(productService.validateProductExists(1L)).thenAnswer(invocation -> productRepository.toDataObject(productRepository.findById(IotProductId.of(1L))));
        when(productService.getProduct(1L)).thenAnswer(invocation -> productRepository.toDataObject(productRepository.findById(IotProductId.of(1L))));

        IotProductApplicationService productServiceApp = new IotProductApplicationService(productRepository,
                deviceService, productId -> { });
        IotThingModelApplicationService thingModelServiceApp = new IotThingModelApplicationService(thingModelRepository,
                productService, (thingModelId, identifier, name) -> { });
        IotDeviceApplicationService deviceServiceApp = new IotDeviceApplicationService(deviceRepository, event -> { });
        IotDevicePropertyApplicationService propertyServiceApp = new IotDevicePropertyApplicationService(propertyStoragePort);
        IotDeviceCommandApplicationService commandServiceApp = new IotDeviceCommandApplicationService(commandRepository,
                commandMessagePort);

        Long productId = productServiceApp.createProduct(new CreateIotProductCommand("温湿度产品", "pk-1", true,
                10L, "icon", "pic", "desc", 1, 1, "mqtt", "json"));
        Long thingModelId = thingModelServiceApp.createThingModel(new CreateIotThingModelCommand(productId, "pk-1",
                "temperature", "温度", "desc", IotThingModelTypeEnum.PROPERTY.getType(), Map.of("dataType", "double"),
                null, null));
        productServiceApp.updateProductStatus(new UpdateIotProductStatusCommand(productId,
                IotProductStatusEnum.PUBLISHED.getStatus()));
        IotProductResult product = productServiceApp.getProduct(productId);
        IotThingModelTslResult tsl = thingModelServiceApp.getTsl(productId);

        Long deviceId = deviceServiceApp.createDevice("device-1", "一号设备", "sn-1", null, Set.of(), productId,
                "pk-1", 1, null, "secret-1", null, null, null);
        deviceServiceApp.updateDeviceState(deviceId, IotDeviceState.ONLINE.code());
        IotDevice onlineDevice = deviceServiceApp.getDevice(deviceId);

        IotDeviceMessage propertyMessage = IotDeviceMessage.requestOf("property-req-1",
                IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), Map.of("temperature", 26.5));
        propertyServiceApp.postProperty(new PostIotDevicePropertyCommand(IotDeviceDO.builder().id(deviceId)
                .productId(productId).productKey("pk-1").deviceName("device-1").build(), propertyMessage));
        List<IotDeviceLatestPropertyResult> latestProperties = propertyServiceApp.getLatestProperties(
                new IotDeviceLatestPropertyQuery(deviceId));

        IotDeviceCommandResult sentCommand = commandServiceApp.createCommand(new CreateIotDeviceCommand(deviceId,
                "cmd-req-1", IotDeviceMessageMethodEnum.SERVICE_INVOKE.getMethod(), Map.of("switch", true)));
        assertTrue(commandMessagePort.deliveredRequestIds.contains("cmd-req-1"));
        commandServiceApp.handleAck(new AckIotDeviceCommand(deviceId, "cmd-req-1", Map.of("success", true),
                GlobalErrorCodeConstants.SUCCESS.getCode(), GlobalErrorCodeConstants.SUCCESS.getMsg()));
        commandServiceApp.handleAck(new AckIotDeviceCommand(deviceId, "cmd-req-1", Map.of("success", false),
                500, "duplicate failure"));
        IotDeviceCommandResult ackedCommand = commandServiceApp.getCommand(sentCommand.id());

        assertNotNull(product);
        assertEquals(productId, product.id());
        assertEquals(thingModelId, tsl.properties().size() == 1 ? thingModelId : null);
        assertTrue(onlineDevice.state().isOnline());
        assertEquals(1, latestProperties.size());
        assertEquals("temperature", latestProperties.get(0).identifier());
        assertEquals(26.5, latestProperties.get(0).value());
        assertEquals(IotCommandStatus.ACKED_CODE, ackedCommand.status());
        assertEquals(1, commandMessagePort.replyCount);
    }

    private static final class FakeProductRepository implements IotProductRepository {

        private final AtomicLong idGenerator = new AtomicLong(1L);
        private final Map<Long, IotProduct> products = new LinkedHashMap<>();

        @Override
        public IotProduct save(IotProduct product) {
            Long id = product.id() != null ? product.id() : idGenerator.getAndIncrement();
            IotProduct saved = IotProduct.reconstitute(id, product.name(), product.productKey().value(),
                    product.productSecret().value(), product.registerEnabled(), product.categoryId(), product.icon(),
                    product.picUrl(), product.description(), product.status().code(), product.deviceType(),
                    product.netType(), product.protocolType(), product.serializeType());
            products.put(id, saved);
            return saved;
        }

        @Override
        public void delete(IotProductId id) {
            products.remove(id.value());
        }

        @Override
        public IotProduct findById(IotProductId id) {
            return products.get(id.value());
        }

        @Override
        public Optional<IotProduct> findByProductKey(IotProductKey productKey) {
            return products.values().stream().filter(product -> product.productKey().equals(productKey)).findFirst();
        }

        @Override
        public PageResult<IotProduct> findPage(com.develop.mvp.pk.module.iot.application.product.query.IotProductPageQuery query) {
            return new PageResult<>(new ArrayList<>(products.values()), (long) products.size());
        }

        @Override
        public List<IotProduct> findAll() {
            return new ArrayList<>(products.values());
        }

        @Override
        public List<IotProduct> findByIds(Collection<Long> ids) {
            return products.values().stream().filter(product -> ids.contains(product.id())).toList();
        }

        @Override
        public List<IotProduct> findByDeviceType(Integer deviceType) {
            return products.values().stream().filter(product -> java.util.Objects.equals(product.deviceType(), deviceType)).toList();
        }

        @Override
        public List<IotProduct> findByStatus(Integer status) {
            return products.values().stream().filter(product -> java.util.Objects.equals(product.status().code(), status)).toList();
        }

        @Override
        public long countByCreateTime(LocalDateTime createTime) {
            return products.size();
        }

        private IotProductDO toDataObject(IotProduct product) {
            if (product == null) {
                return null;
            }
            return IotProductDO.builder().id(product.id()).name(product.name()).productKey(product.productKey().value())
                    .productSecret(product.productSecret().value()).registerEnabled(product.registerEnabled())
                    .categoryId(product.categoryId()).icon(product.icon()).picUrl(product.picUrl())
                    .description(product.description()).status(product.status().code()).deviceType(product.deviceType())
                    .netType(product.netType()).protocolType(product.protocolType()).serializeType(product.serializeType())
                    .build();
        }

    }

    private static final class FakeThingModelRepository implements IotThingModelRepository {

        private final AtomicLong idGenerator = new AtomicLong(1L);
        private final Map<Long, IotThingModel> thingModels = new LinkedHashMap<>();

        @Override
        public IotThingModel save(IotThingModel thingModel) {
            Long id = thingModel.id() != null ? thingModel.id() : idGenerator.getAndIncrement();
            IotThingModel saved = IotThingModel.reconstitute(id, thingModel.productId(), thingModel.productKey(),
                    thingModel.identifier().value(), thingModel.name().value(), thingModel.description(),
                    thingModel.type().code(), thingModel.property().value(), thingModel.event().value(),
                    thingModel.service().value());
            thingModels.put(id, saved);
            return saved;
        }

        @Override
        public void delete(IotThingModelId id) {
            thingModels.remove(id.value());
        }

        @Override
        public IotThingModel findById(IotThingModelId id) {
            return thingModels.get(id.value());
        }

        @Override
        public IotThingModel findByProductIdAndIdentifier(Long productId, String identifier) {
            return thingModels.values().stream()
                    .filter(thingModel -> java.util.Objects.equals(thingModel.productId(), productId)
                            && java.util.Objects.equals(thingModel.identifier().value(), identifier))
                    .findFirst().orElse(null);
        }

        @Override
        public IotThingModel findByProductIdAndName(Long productId, String name) {
            return thingModels.values().stream()
                    .filter(thingModel -> java.util.Objects.equals(thingModel.productId(), productId)
                            && java.util.Objects.equals(thingModel.name().value(), name))
                    .findFirst().orElse(null);
        }

        @Override
        public List<IotThingModel> findByProductId(Long productId) {
            return thingModels.values().stream()
                    .filter(thingModel -> java.util.Objects.equals(thingModel.productId(), productId)).toList();
        }

        @Override
        public List<IotThingModel> findByProductIdAndIdentifiers(Long productId, Collection<String> identifiers) {
            return thingModels.values().stream()
                    .filter(thingModel -> java.util.Objects.equals(thingModel.productId(), productId)
                            && identifiers.contains(thingModel.identifier().value()))
                    .toList();
        }

        @Override
        public List<IotThingModel> findByProductIdAndType(Long productId, Integer type) {
            return thingModels.values().stream()
                    .filter(thingModel -> java.util.Objects.equals(thingModel.productId(), productId)
                            && java.util.Objects.equals(thingModel.type().code(), type))
                    .toList();
        }

        @Override
        public PageResult<IotThingModel> findPage(com.develop.mvp.pk.module.iot.application.thingmodel.query.IotThingModelPageQuery query) {
            return new PageResult<>(findByProductId(query.productId()), (long) findByProductId(query.productId()).size());
        }

        @Override
        public List<IotThingModel> findList(com.develop.mvp.pk.module.iot.application.thingmodel.query.IotThingModelListQuery query) {
            return findByProductId(query.productId());
        }

        @Override
        public void evictProductThingModelCache(Long productId) {
        }

    }

    private static final class FakeDeviceRepository implements IotDeviceRepository {

        private final AtomicLong idGenerator = new AtomicLong(1L);
        private final Map<Long, IotDevice> devices = new LinkedHashMap<>();

        @Override
        public IotDevice save(IotDevice device) {
            Long id = device.id() != null ? device.id() : idGenerator.getAndIncrement();
            IotDevice saved = com.develop.mvp.pk.module.iot.domain.device.IotDeviceFactory.reconstitute(id,
                    device.deviceName(), device.nickname(), device.serialNumber(), device.picUrl(), device.groupIds(),
                    device.productId(), device.productKey(), device.deviceType(), device.gatewayId(),
                    device.state().code(), device.onlineTime(), device.offlineTime(), device.activeTime(),
                    device.firmwareId(), device.deviceSecret(), device.config(), device.latitude(), device.longitude());
            devices.put(id, saved);
            return saved;
        }

        @Override
        public void delete(IotDeviceId id) {
            devices.remove(id.value());
        }

        @Override
        public IotDevice findById(IotDeviceId id) {
            return devices.get(id.value());
        }

        @Override
        public Optional<IotDevice> findByDeviceName(String deviceName) {
            return devices.values().stream().filter(device -> java.util.Objects.equals(device.deviceName(), deviceName)).findFirst();
        }

        @Override
        public Optional<IotDevice> findByProductKeyAndDeviceName(String productKey, String deviceName) {
            return devices.values().stream().filter(device -> java.util.Objects.equals(device.productKey(), productKey)
                    && java.util.Objects.equals(device.deviceName(), deviceName)).findFirst();
        }

        @Override
        public Optional<IotDevice> findBySerialNumber(String serialNumber) {
            return devices.values().stream().filter(device -> java.util.Objects.equals(device.serialNumber(), serialNumber)).findFirst();
        }

        @Override
        public List<IotDevice> findByState(IotDeviceState state) {
            return devices.values().stream().filter(device -> device.state().equals(state)).toList();
        }

        @Override
        public List<IotDevice> findByProductId(Long productId) {
            return devices.values().stream().filter(device -> java.util.Objects.equals(device.productId(), productId)).toList();
        }

        @Override
        public List<IotDevice> findByCondition(Integer deviceType, Long productId) {
            return devices.values().stream().filter(device -> (deviceType == null || java.util.Objects.equals(device.deviceType(), deviceType))
                    && (productId == null || java.util.Objects.equals(device.productId(), productId))).toList();
        }

        @Override
        public List<IotDevice> findByGatewayId(Long gatewayId) {
            return devices.values().stream().filter(device -> java.util.Objects.equals(device.gatewayId(), gatewayId)).toList();
        }

        @Override
        public List<IotDevice> findByIds(Collection<Long> ids) {
            return devices.values().stream().filter(device -> ids.contains(device.id())).toList();
        }

        @Override
        public PageResult<IotDevice> findPage(IotDevicePageQuery query) {
            return new PageResult<>(new ArrayList<>(devices.values()), (long) devices.size());
        }

        @Override
        public PageResult<IotDevice> findUnboundSubDevicePage(IotDevicePageQuery query) {
            return PageResult.empty();
        }

        @Override
        public long countByProductId(Long productId) {
            return findByProductId(productId).size();
        }

        @Override
        public long countByGatewayId(Long gatewayId) {
            return findByGatewayId(gatewayId).size();
        }

        @Override
        public long countByGroupId(Long groupId) {
            return devices.values().stream().filter(device -> device.groupIds() != null && device.groupIds().contains(groupId)).count();
        }

        @Override
        public long countByCreateTime(LocalDateTime createTime) {
            return devices.size();
        }

        @Override
        public Map<Long, Integer> countDeviceMapByProductId() {
            return Map.of();
        }

        @Override
        public Map<Integer, Long> countDeviceGroupByState() {
            return Map.of();
        }

        @Override
        public List<IotDevice> findByHasLocation() {
            return List.of();
        }

        @Override
        public List<IotDevice> findByProductKeyAndDeviceNames(String productKey, Collection<String> deviceNames) {
            return devices.values().stream().filter(device -> java.util.Objects.equals(device.productKey(), productKey)
                    && deviceNames.contains(device.deviceName())).toList();
        }

        @Override
        public boolean existsBySerialNumber(String serialNumber, Long excludeId) {
            return devices.values().stream().anyMatch(device -> java.util.Objects.equals(device.serialNumber(), serialNumber)
                    && !java.util.Objects.equals(device.id(), excludeId));
        }

        @Override
        public void updateGatewayIdBatch(Collection<Long> ids, Long gatewayId) {
            for (Long id : ids) {
                IotDevice device = devices.get(id);
                if (device != null) {
                    device.bindGateway(gatewayId);
                    save(device);
                }
            }
        }

    }

    private static final class FakePropertyStoragePort implements IotDevicePropertyStoragePort {

        private final Set<String> processedMessages = java.util.concurrent.ConcurrentHashMap.newKeySet();
        private final Map<Long, Map<String, IotDevicePropertyDO>> latestProperties = new LinkedHashMap<>();

        @Override
        public boolean hasProcessedMessage(String messageId, Long productId, Long deviceId) {
            return processedMessages.contains(productId + ":" + deviceId + ":" + messageId);
        }

        @Override
        public void markMessageProcessed(String messageId, Long productId, Long deviceId) {
            processedMessages.add(productId + ":" + deviceId + ":" + messageId);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void saveDeviceProperty(IotDeviceDO device, IotDeviceMessage message) {
            Map<String, Object> params = (Map<String, Object>) message.getParams();
            Map<String, IotDevicePropertyDO> properties = latestProperties.computeIfAbsent(device.getId(), key -> new LinkedHashMap<>());
            params.forEach((identifier, value) -> properties.put(identifier,
                    IotDevicePropertyDO.builder().value(value).updateTime(message.getReportTime()).build()));
        }

        @Override
        public Map<String, IotDevicePropertyDO> getLatestDeviceProperties(Long deviceId) {
            return latestProperties.getOrDefault(deviceId, Map.of());
        }

    }

    private static final class FakeCommandRepository implements IotDeviceCommandRepository {

        private final AtomicLong idGenerator = new AtomicLong(1L);
        private final Map<Long, IotDeviceCommand> commands = new LinkedHashMap<>();

        @Override
        public IotDeviceCommand findById(IotCommandId id) {
            return commands.get(id.value());
        }

        @Override
        public IotDeviceCommand findByRequestId(Long deviceId, String requestId) {
            return commands.values().stream().filter(command -> java.util.Objects.equals(command.deviceId(), deviceId)
                    && java.util.Objects.equals(command.requestId().value(), requestId)).findFirst().orElse(null);
        }

        @Override
        public PageResult<IotDeviceCommand> findPage(IotDeviceCommandQuery query) {
            List<IotDeviceCommand> list = commands.values().stream()
                    .filter(command -> query.deviceId() == null || java.util.Objects.equals(command.deviceId(), query.deviceId()))
                    .filter(command -> query.requestId() == null || java.util.Objects.equals(command.requestId().value(), query.requestId()))
                    .filter(command -> query.status() == null || java.util.Objects.equals(command.status().code(), query.status()))
                    .toList();
            return new PageResult<>(list, (long) list.size());
        }

        @Override
        public IotDeviceCommand save(IotDeviceCommand command) {
            Long id = command.id() != null ? command.id() : idGenerator.getAndIncrement();
            IotDeviceCommand saved = IotDeviceCommand.reconstitute(id, command.deviceId(), command.requestId().value(),
                    command.method(), command.payload().value(), command.status().code(), command.messageId(),
                    command.serverId(), command.data(), command.code(), command.msg());
            commands.put(id, saved);
            return saved;
        }

    }

    private static final class FakeCommandMessagePort implements IotDeviceCommandMessagePort {

        private final List<String> deliveredRequestIds = new ArrayList<>();
        private int replyCount;

        @Override
        public IotDeviceMessage sendToDevice(Long deviceId, String method, Object params, String requestId) {
            deliveredRequestIds.add(requestId);
            return IotDeviceMessage.requestOf(deviceId, 1L, "gateway-1", method, params).setRequestId(requestId);
        }

        @Override
        public void recordReply(Long deviceId, String requestId, String method, Object data, Integer code, String msg) {
            replyCount++;
        }

    }

}
