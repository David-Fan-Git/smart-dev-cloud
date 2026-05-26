package com.develop.mvp.pk.module.iot.domain.thingmodel.repository;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.iot.application.thingmodel.query.IotThingModelListQuery;
import com.develop.mvp.pk.module.iot.application.thingmodel.query.IotThingModelPageQuery;
import com.develop.mvp.pk.module.iot.domain.thingmodel.model.IotThingModel;
import com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelId;

import java.util.Collection;
import java.util.List;

public interface IotThingModelRepository {

    IotThingModel save(IotThingModel thingModel);

    void delete(IotThingModelId id);

    IotThingModel findById(IotThingModelId id);

    IotThingModel findByProductIdAndIdentifier(Long productId, String identifier);

    IotThingModel findByProductIdAndName(Long productId, String name);

    List<IotThingModel> findByProductId(Long productId);

    List<IotThingModel> findByProductIdAndIdentifiers(Long productId, Collection<String> identifiers);

    List<IotThingModel> findByProductIdAndType(Long productId, Integer type);

    PageResult<IotThingModel> findPage(IotThingModelPageQuery query);

    List<IotThingModel> findList(IotThingModelListQuery query);

    void evictProductThingModelCache(Long productId);

}
