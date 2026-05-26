package com.develop.mvp.pk.module.iot.application.thingmodel.port.inbound;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.iot.application.thingmodel.command.CreateIotThingModelCommand;
import com.develop.mvp.pk.module.iot.application.thingmodel.command.UpdateIotThingModelCommand;
import com.develop.mvp.pk.module.iot.application.thingmodel.query.IotThingModelListQuery;
import com.develop.mvp.pk.module.iot.application.thingmodel.query.IotThingModelPageQuery;
import com.develop.mvp.pk.module.iot.application.thingmodel.result.IotThingModelResult;
import com.develop.mvp.pk.module.iot.application.thingmodel.result.IotThingModelTslResult;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface IotThingModelUseCase {

    Long createThingModel(CreateIotThingModelCommand command);

    void updateThingModel(UpdateIotThingModelCommand command);

    void deleteThingModel(Long id);

    IotThingModelResult getThingModel(Long id);

    IotThingModelTslResult getTsl(Long productId);

    List<IotThingModelResult> getThingModelList(IotThingModelListQuery query);

    PageResult<IotThingModelResult> getThingModelPage(IotThingModelPageQuery query);

    List<IotThingModelResult> getThingModelListByProductId(Long productId);

    List<IotThingModelResult> getThingModelListByProductIdAndIdentifiers(Long productId, Collection<String> identifiers);

    List<IotThingModelResult> getThingModelListByProductIdAndType(Long productId, Integer type);

    void validateThingModelListExists(Long productId, Set<String> identifiers);

}
