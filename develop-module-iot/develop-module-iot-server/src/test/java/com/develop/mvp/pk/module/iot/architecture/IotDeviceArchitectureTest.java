package com.develop.mvp.pk.module.iot.architecture;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class IotDeviceArchitectureTest {

    @Test
    void deviceDddSkeletonUsesStandardPackages() {
        assertPresent("com.develop.mvp.pk.module.iot.application.device.port.inbound.IotDeviceUseCase");
        assertPresent("com.develop.mvp.pk.module.iot.application.device.service.IotDeviceApplicationService");
        assertPresent("com.develop.mvp.pk.module.iot.domain.device.repository.IotDeviceRepository");
        assertPresent("com.develop.mvp.pk.module.iot.infrastructure.device.persistence.IotDeviceRepositoryImpl");
        assertPresent("com.develop.mvp.pk.module.iot.application.device.port.outbound.package-info");
        assertPresent("com.develop.mvp.pk.module.iot.infrastructure.device.external.package-info");
        assertPresent("com.develop.mvp.pk.module.iot.infrastructure.device.rpc.package-info");
        assertPresent("com.develop.mvp.pk.module.iot.infrastructure.device.cache.package-info");
        assertPresent("com.develop.mvp.pk.module.iot.infrastructure.device.messaging.package-info");
    }

    @Test
    void productDddSkeletonUsesStandardPackages() {
        assertPresent("com.develop.mvp.pk.module.iot.domain.product.model.IotProduct");
        assertPresent("com.develop.mvp.pk.module.iot.domain.product.valueobject.IotProductId");
        assertPresent("com.develop.mvp.pk.module.iot.domain.product.valueobject.IotProductKey");
        assertPresent("com.develop.mvp.pk.module.iot.domain.product.valueobject.IotProductSecret");
        assertPresent("com.develop.mvp.pk.module.iot.domain.product.valueobject.IotProductStatus");
        assertPresent("com.develop.mvp.pk.module.iot.domain.product.event.IotProductCreatedEvent");
        assertPresent("com.develop.mvp.pk.module.iot.domain.product.event.IotProductPublishedEvent");
        assertPresent("com.develop.mvp.pk.module.iot.domain.product.service.IotProductPolicy");
        assertPresent("com.develop.mvp.pk.module.iot.domain.product.repository.IotProductRepository");
        assertPresent("com.develop.mvp.pk.module.iot.application.product.command.CreateIotProductCommand");
        assertPresent("com.develop.mvp.pk.module.iot.application.product.command.UpdateIotProductCommand");
        assertPresent("com.develop.mvp.pk.module.iot.application.product.command.UpdateIotProductStatusCommand");
        assertPresent("com.develop.mvp.pk.module.iot.application.product.query.IotProductPageQuery");
        assertPresent("com.develop.mvp.pk.module.iot.application.product.result.IotProductResult");
        assertPresent("com.develop.mvp.pk.module.iot.application.product.port.inbound.IotProductUseCase");
        assertPresent("com.develop.mvp.pk.module.iot.application.product.port.outbound.IotProductPropertyTablePort");
        assertPresent("com.develop.mvp.pk.module.iot.application.product.service.IotProductApplicationService");
        assertPresent("com.develop.mvp.pk.module.iot.infrastructure.product.persistence.IotProductRepositoryImpl");
        assertPresent("com.develop.mvp.pk.module.iot.infrastructure.product.external.package-info");
        assertPresent("com.develop.mvp.pk.module.iot.infrastructure.product.rpc.package-info");
        assertPresent("com.develop.mvp.pk.module.iot.infrastructure.product.cache.package-info");
        assertPresent("com.develop.mvp.pk.module.iot.infrastructure.product.messaging.package-info");
    }

    @Test
    void thingModelDddSkeletonUsesStandardPackages() {
        assertPresent("com.develop.mvp.pk.module.iot.domain.thingmodel.model.IotThingModel");
        assertPresent("com.develop.mvp.pk.module.iot.domain.thingmodel.model.IotThingModelFunction");
        assertPresent("com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelId");
        assertPresent("com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelIdentifier");
        assertPresent("com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelName");
        assertPresent("com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelType");
        assertPresent("com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelPropertyDefinition");
        assertPresent("com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelEventDefinition");
        assertPresent("com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelServiceDefinition");
        assertPresent("com.develop.mvp.pk.module.iot.domain.thingmodel.event.IotThingModelCreatedEvent");
        assertPresent("com.develop.mvp.pk.module.iot.domain.thingmodel.event.IotThingModelUpdatedEvent");
        assertPresent("com.develop.mvp.pk.module.iot.domain.thingmodel.service.IotThingModelPolicy");
        assertPresent("com.develop.mvp.pk.module.iot.domain.thingmodel.repository.IotThingModelRepository");
        assertPresent("com.develop.mvp.pk.module.iot.application.thingmodel.command.CreateIotThingModelCommand");
        assertPresent("com.develop.mvp.pk.module.iot.application.thingmodel.command.UpdateIotThingModelCommand");
        assertPresent("com.develop.mvp.pk.module.iot.application.thingmodel.query.IotThingModelPageQuery");
        assertPresent("com.develop.mvp.pk.module.iot.application.thingmodel.query.IotThingModelListQuery");
        assertPresent("com.develop.mvp.pk.module.iot.application.thingmodel.result.IotThingModelResult");
        assertPresent("com.develop.mvp.pk.module.iot.application.thingmodel.result.IotThingModelTslResult");
        assertPresent("com.develop.mvp.pk.module.iot.application.thingmodel.port.inbound.IotThingModelUseCase");
        assertPresent("com.develop.mvp.pk.module.iot.application.thingmodel.port.outbound.IotThingModelModbusPointPort");
        assertPresent("com.develop.mvp.pk.module.iot.application.thingmodel.service.IotThingModelApplicationService");
        assertPresent("com.develop.mvp.pk.module.iot.infrastructure.thingmodel.persistence.IotThingModelRepositoryImpl");
        assertPresent("com.develop.mvp.pk.module.iot.infrastructure.thingmodel.external.package-info");
        assertPresent("com.develop.mvp.pk.module.iot.infrastructure.thingmodel.rpc.package-info");
        assertPresent("com.develop.mvp.pk.module.iot.infrastructure.thingmodel.cache.package-info");
        assertPresent("com.develop.mvp.pk.module.iot.infrastructure.thingmodel.messaging.package-info");
    }

    @Test
    void commandDddSkeletonUsesStandardPackages() {
        assertPresent("com.develop.mvp.pk.module.iot.domain.command.model.IotDeviceCommand");
        assertPresent("com.develop.mvp.pk.module.iot.domain.command.valueobject.IotCommandId");
        assertPresent("com.develop.mvp.pk.module.iot.domain.command.valueobject.IotCommandRequestId");
        assertPresent("com.develop.mvp.pk.module.iot.domain.command.valueobject.IotCommandStatus");
        assertPresent("com.develop.mvp.pk.module.iot.domain.command.valueobject.IotCommandPayload");
        assertPresent("com.develop.mvp.pk.module.iot.domain.command.event.IotCommandCreatedEvent");
        assertPresent("com.develop.mvp.pk.module.iot.domain.command.event.IotCommandSentEvent");
        assertPresent("com.develop.mvp.pk.module.iot.domain.command.event.IotCommandAckedEvent");
        assertPresent("com.develop.mvp.pk.module.iot.domain.command.event.IotCommandFailedEvent");
        assertPresent("com.develop.mvp.pk.module.iot.domain.command.service.IotCommandStatePolicy");
        assertPresent("com.develop.mvp.pk.module.iot.domain.command.repository.IotDeviceCommandRepository");
        assertPresent("com.develop.mvp.pk.module.iot.application.command.command.CreateIotDeviceCommand");
        assertPresent("com.develop.mvp.pk.module.iot.application.command.command.AckIotDeviceCommand");
        assertPresent("com.develop.mvp.pk.module.iot.application.command.command.MarkIotDeviceCommandTimeout");
        assertPresent("com.develop.mvp.pk.module.iot.application.command.query.IotDeviceCommandQuery");
        assertPresent("com.develop.mvp.pk.module.iot.application.command.result.IotDeviceCommandResult");
        assertPresent("com.develop.mvp.pk.module.iot.application.command.port.inbound.IotDeviceCommandUseCase");
        assertPresent("com.develop.mvp.pk.module.iot.application.command.port.outbound.IotDeviceCommandMessagePort");
        assertPresent("com.develop.mvp.pk.module.iot.application.command.service.IotDeviceCommandApplicationService");
        assertPresent("com.develop.mvp.pk.module.iot.infrastructure.command.persistence.IotDeviceCommandRepositoryImpl");
        assertPresent("com.develop.mvp.pk.module.iot.infrastructure.command.messaging.IotDeviceCommandMessageAdapter");
        assertPresent("com.develop.mvp.pk.module.iot.infrastructure.command.external.package-info");
        assertPresent("com.develop.mvp.pk.module.iot.infrastructure.command.rpc.package-info");
        assertPresent("com.develop.mvp.pk.module.iot.infrastructure.command.cache.package-info");
    }

    @Test
    void propertyApplicationSkeletonUsesStandardPackages() {
        assertPresent("com.develop.mvp.pk.module.iot.application.property.command.PostIotDevicePropertyCommand");
        assertPresent("com.develop.mvp.pk.module.iot.application.property.query.IotDeviceLatestPropertyQuery");
        assertPresent("com.develop.mvp.pk.module.iot.application.property.result.IotDeviceLatestPropertyResult");
        assertPresent("com.develop.mvp.pk.module.iot.application.property.port.inbound.IotDevicePropertyUseCase");
        assertPresent("com.develop.mvp.pk.module.iot.application.property.port.outbound.IotDevicePropertyStoragePort");
        assertPresent("com.develop.mvp.pk.module.iot.application.property.service.IotDevicePropertyApplicationService");
        assertPresent("com.develop.mvp.pk.module.iot.infrastructure.property.persistence.IotDevicePropertyRepositoryImpl");
        assertPresent("com.develop.mvp.pk.module.iot.infrastructure.property.cache.IotDeviceLatestPropertyCacheAdapter");
        assertPresent("com.develop.mvp.pk.module.iot.infrastructure.property.messaging.package-info");
        assertPresent("com.develop.mvp.pk.module.iot.infrastructure.property.external.package-info");
        assertPresent("com.develop.mvp.pk.module.iot.infrastructure.property.rpc.package-info");
    }

    private static void assertPresent(String className) {
        assertDoesNotThrow(() -> Class.forName(className), className + " should exist");
    }

}
