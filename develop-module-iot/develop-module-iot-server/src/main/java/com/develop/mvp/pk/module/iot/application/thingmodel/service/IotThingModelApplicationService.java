package com.develop.mvp.pk.module.iot.application.thingmodel.service;

import cn.hutool.core.collection.CollUtil;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.iot.application.thingmodel.command.CreateIotThingModelCommand;
import com.develop.mvp.pk.module.iot.application.thingmodel.command.UpdateIotThingModelCommand;
import com.develop.mvp.pk.module.iot.application.thingmodel.port.inbound.IotThingModelUseCase;
import com.develop.mvp.pk.module.iot.application.thingmodel.port.outbound.IotThingModelModbusPointPort;
import com.develop.mvp.pk.module.iot.application.thingmodel.query.IotThingModelListQuery;
import com.develop.mvp.pk.module.iot.application.thingmodel.query.IotThingModelPageQuery;
import com.develop.mvp.pk.module.iot.application.thingmodel.result.IotThingModelResult;
import com.develop.mvp.pk.module.iot.application.thingmodel.result.IotThingModelTslResult;
import com.develop.mvp.pk.module.iot.dal.dataobject.product.IotProductDO;
import com.develop.mvp.pk.module.iot.domain.thingmodel.model.IotThingModel;
import com.develop.mvp.pk.module.iot.domain.thingmodel.repository.IotThingModelRepository;
import com.develop.mvp.pk.module.iot.domain.thingmodel.service.IotThingModelPolicy;
import com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelEventDefinition;
import com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelId;
import com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelIdentifier;
import com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelName;
import com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelPropertyDefinition;
import com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelServiceDefinition;
import com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelType;
import com.develop.mvp.pk.module.iot.enums.product.IotProductStatusEnum;
import com.develop.mvp.pk.module.iot.service.product.IotProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertList;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertSet;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.filterList;
import static com.develop.mvp.pk.module.iot.enums.ErrorCodeConstants.*;

@Service
public class IotThingModelApplicationService implements IotThingModelUseCase {

    private final IotThingModelRepository thingModelRepository;
    private final IotProductService productService;
    private final IotThingModelModbusPointPort modbusPointPort;
    private final IotThingModelPolicy thingModelPolicy = new IotThingModelPolicy();

    public IotThingModelApplicationService(IotThingModelRepository thingModelRepository,
                                           IotProductService productService,
                                           IotThingModelModbusPointPort modbusPointPort) {
        this.thingModelRepository = thingModelRepository;
        this.productService = productService;
        this.modbusPointPort = modbusPointPort;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createThingModel(CreateIotThingModelCommand command) {
        validateIdentifierUnique(null, command.productId(), command.identifier());
        validateNameUnique(command.productId(), command.name());
        validateProductStatus(command.productId());
        IotThingModel thingModel = IotThingModel.create(command.productId(), command.productKey(),
                IotThingModelIdentifier.of(command.identifier()), IotThingModelName.of(command.name()),
                command.description(), IotThingModelType.of(command.type()),
                IotThingModelPropertyDefinition.of(command.property()), IotThingModelEventDefinition.of(command.event()),
                IotThingModelServiceDefinition.of(command.service()));
        IotThingModel saved = thingModelRepository.save(thingModel);
        thingModelRepository.evictProductThingModelCache(command.productId());
        return saved.id();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateThingModel(UpdateIotThingModelCommand command) {
        IotThingModel thingModel = findExistingThingModel(command.id());
        validateIdentifierUnique(command.id(), command.productId(), command.identifier());
        validateProductStatus(command.productId());
        thingModel.update(IotThingModelIdentifier.of(command.identifier()), IotThingModelName.of(command.name()),
                command.description(), IotThingModelType.of(command.type()),
                IotThingModelPropertyDefinition.of(command.property()), IotThingModelEventDefinition.of(command.event()),
                IotThingModelServiceDefinition.of(command.service()));
        thingModelRepository.save(thingModel);
        modbusPointPort.updateByThingModel(command.id(), command.identifier(), command.name());
        thingModelRepository.evictProductThingModelCache(command.productId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteThingModel(Long id) {
        IotThingModel thingModel = findExistingThingModel(id);
        validateProductStatus(thingModel.productId());
        thingModelRepository.delete(IotThingModelId.of(id));
        thingModelRepository.evictProductThingModelCache(thingModel.productId());
    }

    @Override
    public IotThingModelResult getThingModel(Long id) {
        IotThingModel thingModel = thingModelRepository.findById(IotThingModelId.of(id));
        return thingModel != null ? toResult(thingModel) : null;
    }

    @Override
    public IotThingModelTslResult getTsl(Long productId) {
        IotProductDO product = productService.getProduct(productId);
        if (product == null) {
            return null;
        }
        List<IotThingModel> thingModels = thingModelRepository.findByProductId(productId);
        return new IotThingModelTslResult(product.getId(), product.getProductKey(),
                convertList(filterList(thingModels, IotThingModel::isProperty), item -> item.property().value()),
                convertList(filterList(thingModels, IotThingModel::isEvent), item -> item.event().value()),
                convertList(filterList(thingModels, IotThingModel::isService), item -> item.service().value()));
    }

    @Override
    public List<IotThingModelResult> getThingModelList(IotThingModelListQuery query) {
        return thingModelRepository.findList(query).stream().map(this::toResult).toList();
    }

    @Override
    public PageResult<IotThingModelResult> getThingModelPage(IotThingModelPageQuery query) {
        PageResult<IotThingModel> page = thingModelRepository.findPage(query);
        return new PageResult<>(page.getList().stream().map(this::toResult).toList(), page.getTotal());
    }

    @Override
    public List<IotThingModelResult> getThingModelListByProductId(Long productId) {
        return thingModelRepository.findByProductId(productId).stream().map(this::toResult).toList();
    }

    @Override
    public List<IotThingModelResult> getThingModelListByProductIdAndIdentifiers(Long productId, Collection<String> identifiers) {
        return thingModelRepository.findByProductIdAndIdentifiers(productId, identifiers).stream().map(this::toResult).toList();
    }

    @Override
    public List<IotThingModelResult> getThingModelListByProductIdAndType(Long productId, Integer type) {
        return thingModelRepository.findByProductIdAndType(productId, type).stream().map(this::toResult).toList();
    }

    @Override
    public void validateThingModelListExists(Long productId, Set<String> identifiers) {
        if (CollUtil.isEmpty(identifiers)) {
            return;
        }
        List<IotThingModel> thingModels = thingModelRepository.findByProductIdAndIdentifiers(productId, identifiers);
        Set<String> foundIdentifiers = convertSet(thingModels, item -> item.identifier().value());
        for (String identifier : identifiers) {
            if (!foundIdentifiers.contains(identifier)) {
                throw exception(THING_MODEL_NOT_EXISTS);
            }
        }
    }

    private IotThingModel findExistingThingModel(Long id) {
        IotThingModel thingModel = thingModelRepository.findById(IotThingModelId.of(id));
        if (thingModel == null) {
            throw exception(THING_MODEL_NOT_EXISTS);
        }
        return thingModel;
    }

    private void validateIdentifierUnique(Long id, Long productId, String identifier) {
        if (id == null && !thingModelPolicy.isValidIdentifier(IotThingModelIdentifier.of(identifier))) {
            throw exception(THING_MODEL_IDENTIFIER_INVALID);
        }
        IotThingModel thingModel = thingModelRepository.findByProductIdAndIdentifier(productId, identifier);
        if (thingModel != null && !java.util.Objects.equals(thingModel.id(), id)) {
            throw exception(THING_MODEL_IDENTIFIER_EXISTS);
        }
    }

    private void validateNameUnique(Long productId, String name) {
        IotThingModel thingModel = thingModelRepository.findByProductIdAndName(productId, name);
        if (thingModel != null) {
            throw exception(THING_MODEL_NAME_EXISTS);
        }
    }

    private void validateProductStatus(Long productId) {
        IotProductDO product = productService.validateProductExists(productId);
        if (java.util.Objects.equals(product.getStatus(), IotProductStatusEnum.PUBLISHED.getStatus())) {
            throw exception(PRODUCT_STATUS_NOT_ALLOW_THING_MODEL);
        }
    }

    private IotThingModelResult toResult(IotThingModel thingModel) {
        return new IotThingModelResult(thingModel.id(), thingModel.productId(), thingModel.productKey(),
                thingModel.identifier().value(), thingModel.name().value(), thingModel.description(),
                thingModel.type().code(), thingModel.property().value(), thingModel.event().value(),
                thingModel.service().value());
    }

}
