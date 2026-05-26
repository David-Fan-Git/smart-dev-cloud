package com.develop.mvp.pk.module.iot.application.product.service;

import cn.hutool.core.util.IdUtil;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.iot.application.product.command.CreateIotProductCommand;
import com.develop.mvp.pk.module.iot.application.product.command.UpdateIotProductCommand;
import com.develop.mvp.pk.module.iot.application.product.command.UpdateIotProductStatusCommand;
import com.develop.mvp.pk.module.iot.application.product.port.inbound.IotProductUseCase;
import com.develop.mvp.pk.module.iot.application.product.port.outbound.IotProductPropertyTablePort;
import com.develop.mvp.pk.module.iot.application.product.query.IotProductPageQuery;
import com.develop.mvp.pk.module.iot.application.product.result.IotProductResult;
import com.develop.mvp.pk.module.iot.domain.product.model.IotProduct;
import com.develop.mvp.pk.module.iot.domain.product.repository.IotProductRepository;
import com.develop.mvp.pk.module.iot.domain.product.valueobject.IotProductId;
import com.develop.mvp.pk.module.iot.domain.product.valueobject.IotProductKey;
import com.develop.mvp.pk.module.iot.domain.product.valueobject.IotProductSecret;
import com.develop.mvp.pk.module.iot.domain.product.valueobject.IotProductStatus;
import com.develop.mvp.pk.module.iot.enums.product.IotProductStatusEnum;
import com.develop.mvp.pk.module.iot.service.device.IotDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.iot.enums.ErrorCodeConstants.*;

@Slf4j
@Service
public class IotProductApplicationService implements IotProductUseCase {

    private final IotProductRepository productRepository;
    private final IotDeviceService deviceService;
    private final IotProductPropertyTablePort propertyTablePort;

    public IotProductApplicationService(IotProductRepository productRepository,
                                        IotDeviceService deviceService,
                                        IotProductPropertyTablePort propertyTablePort) {
        this.productRepository = productRepository;
        this.deviceService = deviceService;
        this.propertyTablePort = propertyTablePort;
    }

    @Override
    @Transactional
    public Long createProduct(CreateIotProductCommand command) {
        productRepository.findByProductKey(IotProductKey.of(command.productKey()))
                .ifPresent(product -> {
                    throw exception(PRODUCT_KEY_EXISTS);
                });
        IotProduct product = IotProduct.create(command.name(), IotProductKey.of(command.productKey()),
                IotProductSecret.of(IdUtil.fastSimpleUUID()), command.registerEnabled(), command.categoryId(),
                command.icon(), command.picUrl(), command.description(), command.deviceType(), command.netType(),
                command.protocolType(), command.serializeType());
        return productRepository.save(product).id();
    }

    @Override
    @Transactional
    public void updateProduct(UpdateIotProductCommand command) {
        IotProduct product = findExistingProduct(command.id());
        product.updateProfile(command.name(), command.registerEnabled(), command.categoryId(), command.icon(),
                command.picUrl(), command.description(), command.deviceType(), command.netType(),
                command.protocolType(), command.serializeType());
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        IotProduct product = findExistingProduct(id);
        if (product.isPublished()) {
            throw exception(PRODUCT_STATUS_NOT_DELETE);
        }
        if (deviceService.getDeviceCountByProductId(id) > 0) {
            throw exception(PRODUCT_DELETE_FAIL_HAS_DEVICE);
        }
        productRepository.delete(IotProductId.of(id));
    }

    @Override
    @Transactional
    public void updateProductStatus(UpdateIotProductStatusCommand command) {
        IotProduct product = findExistingProduct(command.id());
        if (IotProductStatusEnum.PUBLISHED.getStatus().equals(command.status())) {
            propertyTablePort.defineProductPropertyTable(command.id());
        }
        product.changeStatus(IotProductStatus.of(command.status()));
        productRepository.save(product);
    }

    @Override
    public IotProductResult getProduct(Long id) {
        IotProduct product = productRepository.findById(IotProductId.of(id));
        return product != null ? toResult(product) : null;
    }

    @Override
    public IotProductResult getProductByProductKey(String productKey) {
        return productRepository.findByProductKey(IotProductKey.of(productKey))
                .map(this::toResult).orElse(null);
    }

    @Override
    public PageResult<IotProductResult> getProductPage(IotProductPageQuery query) {
        PageResult<IotProduct> page = productRepository.findPage(query);
        return new PageResult<>(page.getList().stream().map(this::toResult).toList(), page.getTotal());
    }

    @Override
    public List<IotProductResult> getProductList(Integer deviceType) {
        return productRepository.findByDeviceType(deviceType).stream().map(this::toResult).toList();
    }

    @Override
    public void syncProductPropertyTable() {
        List<IotProduct> products = productRepository.findByStatus(IotProductStatusEnum.PUBLISHED.getStatus());
        for (IotProduct product : products) {
            try {
                propertyTablePort.defineProductPropertyTable(product.id());
            } catch (Exception e) {
                log.error("[syncProductPropertyTable][产品({}/{}) 同步失败]", product.id(), product.name(), e);
            }
        }
    }

    private IotProduct findExistingProduct(Long id) {
        IotProduct product = productRepository.findById(IotProductId.of(id));
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        return product;
    }

    private IotProductResult toResult(IotProduct product) {
        return new IotProductResult(product.id(), product.name(), product.productKey().value(), product.productSecret().value(),
                product.registerEnabled(), product.categoryId(), product.icon(), product.picUrl(), product.description(),
                product.status().code(), product.deviceType(), product.netType(), product.protocolType(), product.serializeType());
    }

}
