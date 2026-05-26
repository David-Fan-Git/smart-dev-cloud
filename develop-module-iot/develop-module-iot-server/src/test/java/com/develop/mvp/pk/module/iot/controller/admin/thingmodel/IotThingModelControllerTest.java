package com.develop.mvp.pk.module.iot.controller.admin.thingmodel;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.test.core.ut.BaseMockitoUnitTest;
import com.develop.mvp.pk.module.iot.application.thingmodel.command.CreateIotThingModelCommand;
import com.develop.mvp.pk.module.iot.application.thingmodel.port.inbound.IotThingModelUseCase;
import com.develop.mvp.pk.module.iot.controller.admin.thingmodel.vo.IotThingModelSaveReqVO;
import com.develop.mvp.pk.module.iot.service.product.IotProductService;
import com.develop.mvp.pk.module.iot.service.thingmodel.IotThingModelService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IotThingModelControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private IotThingModelController controller;

    @Mock
    private IotThingModelUseCase thingModelUseCase;
    @Mock
    private IotThingModelService thingModelService;
    @Mock
    private IotProductService productService;

    @Test
    void createThingModelRoutesToThingModelUseCase() {
        IotThingModelSaveReqVO reqVO = new IotThingModelSaveReqVO();
        reqVO.setProductId(1L);
        reqVO.setProductKey("pk-1");
        reqVO.setIdentifier("temperature");
        reqVO.setName("温度");
        reqVO.setDescription("desc");
        reqVO.setType(1);
        CreateIotThingModelCommand command = new CreateIotThingModelCommand(1L, "pk-1", "temperature",
                "温度", "desc", 1, null, null, null);
        when(thingModelUseCase.createThingModel(command)).thenReturn(10L);

        CommonResult<Long> result = controller.createThingModel(reqVO);

        assertEquals(0, result.getCode());
        assertEquals(10L, result.getData());
        verify(thingModelUseCase).createThingModel(command);
    }

}
