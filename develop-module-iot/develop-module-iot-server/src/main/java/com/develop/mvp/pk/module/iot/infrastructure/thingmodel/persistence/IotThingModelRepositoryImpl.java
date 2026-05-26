package com.develop.mvp.pk.module.iot.infrastructure.thingmodel.persistence;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.iot.application.thingmodel.query.IotThingModelListQuery;
import com.develop.mvp.pk.module.iot.application.thingmodel.query.IotThingModelPageQuery;
import com.develop.mvp.pk.module.iot.controller.admin.thingmodel.vo.IotThingModelListReqVO;
import com.develop.mvp.pk.module.iot.controller.admin.thingmodel.vo.IotThingModelPageReqVO;
import com.develop.mvp.pk.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import com.develop.mvp.pk.module.iot.dal.dataobject.thingmodel.model.ThingModelEvent;
import com.develop.mvp.pk.module.iot.dal.dataobject.thingmodel.model.ThingModelProperty;
import com.develop.mvp.pk.module.iot.dal.dataobject.thingmodel.model.ThingModelService;
import com.develop.mvp.pk.module.iot.dal.mysql.thingmodel.IotThingModelMapper;
import com.develop.mvp.pk.module.iot.domain.thingmodel.model.IotThingModel;
import com.develop.mvp.pk.module.iot.domain.thingmodel.repository.IotThingModelRepository;
import com.develop.mvp.pk.framework.tenant.core.aop.TenantIgnore;
import com.develop.mvp.pk.module.iot.dal.redis.RedisKeyConstants;
import com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelId;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public class IotThingModelRepositoryImpl implements IotThingModelRepository {

    private final IotThingModelMapper thingModelMapper;

    public IotThingModelRepositoryImpl(IotThingModelMapper thingModelMapper) {
        this.thingModelMapper = thingModelMapper;
    }

    @Override
    public IotThingModel save(IotThingModel thingModel) {
        IotThingModelDO thingModelDO = toDataObject(thingModel);
        if (thingModel.id() != null && thingModelMapper.selectById(thingModel.id()) != null) {
            thingModelMapper.updateById(thingModelDO);
        } else {
            thingModelMapper.insert(thingModelDO);
        }
        return toDomain(thingModelDO);
    }

    @Override
    public void delete(IotThingModelId id) {
        thingModelMapper.deleteById(id.value());
    }

    @Override
    public IotThingModel findById(IotThingModelId id) {
        IotThingModelDO thingModelDO = thingModelMapper.selectById(id.value());
        return thingModelDO != null ? toDomain(thingModelDO) : null;
    }

    @Override
    public IotThingModel findByProductIdAndIdentifier(Long productId, String identifier) {
        IotThingModelDO thingModelDO = thingModelMapper.selectByProductIdAndIdentifier(productId, identifier);
        return thingModelDO != null ? toDomain(thingModelDO) : null;
    }

    @Override
    public IotThingModel findByProductIdAndName(Long productId, String name) {
        IotThingModelDO thingModelDO = thingModelMapper.selectByProductIdAndName(productId, name);
        return thingModelDO != null ? toDomain(thingModelDO) : null;
    }

    @Override
    public List<IotThingModel> findByProductId(Long productId) {
        return thingModelMapper.selectListByProductId(productId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<IotThingModel> findByProductIdAndIdentifiers(Long productId, Collection<String> identifiers) {
        return thingModelMapper.selectListByProductIdAndIdentifiers(productId, identifiers).stream().map(this::toDomain).toList();
    }

    @Override
    public List<IotThingModel> findByProductIdAndType(Long productId, Integer type) {
        return thingModelMapper.selectListByProductIdAndType(productId, type).stream().map(this::toDomain).toList();
    }

    @Override
    public PageResult<IotThingModel> findPage(IotThingModelPageQuery query) {
        IotThingModelPageReqVO reqVO = new IotThingModelPageReqVO();
        reqVO.setProductId(query.productId());
        reqVO.setIdentifier(query.identifier());
        reqVO.setName(query.name());
        reqVO.setType(query.type());
        if (query.pageNo() != null) reqVO.setPageNo(query.pageNo());
        if (query.pageSize() != null) reqVO.setPageSize(query.pageSize());
        PageResult<IotThingModelDO> page = thingModelMapper.selectPage(reqVO);
        return new PageResult<>(page.getList().stream().map(this::toDomain).toList(), page.getTotal());
    }

    @Override
    public List<IotThingModel> findList(IotThingModelListQuery query) {
        IotThingModelListReqVO reqVO = new IotThingModelListReqVO();
        reqVO.setProductId(query.productId());
        reqVO.setIdentifier(query.identifier());
        reqVO.setName(query.name());
        reqVO.setType(query.type());
        return thingModelMapper.selectList(reqVO).stream().map(this::toDomain).toList();
    }

    @Override
    @CacheEvict(value = RedisKeyConstants.THING_MODEL_LIST, key = "#productId")
    @TenantIgnore
    public void evictProductThingModelCache(Long productId) {
    }

    private IotThingModelDO toDataObject(IotThingModel thingModel) {
        IotThingModelDO thingModelDO = new IotThingModelDO();
        thingModelDO.setId(thingModel.id());
        thingModelDO.setProductId(thingModel.productId());
        thingModelDO.setProductKey(thingModel.productKey());
        thingModelDO.setIdentifier(thingModel.identifier() != null ? thingModel.identifier().value() : null);
        thingModelDO.setName(thingModel.name() != null ? thingModel.name().value() : null);
        thingModelDO.setDescription(thingModel.description());
        thingModelDO.setType(thingModel.type() != null ? thingModel.type().code() : null);
        thingModelDO.setProperty(thingModel.property() != null ? (ThingModelProperty) thingModel.property().value() : null);
        thingModelDO.setEvent(thingModel.event() != null ? (ThingModelEvent) thingModel.event().value() : null);
        thingModelDO.setService(thingModel.service() != null ? (ThingModelService) thingModel.service().value() : null);
        return thingModelDO;
    }

    private IotThingModel toDomain(IotThingModelDO thingModelDO) {
        return IotThingModel.reconstitute(thingModelDO.getId(), thingModelDO.getProductId(), thingModelDO.getProductKey(),
                thingModelDO.getIdentifier(), thingModelDO.getName(), thingModelDO.getDescription(), thingModelDO.getType(),
                thingModelDO.getProperty(), thingModelDO.getEvent(), thingModelDO.getService());
    }

}
