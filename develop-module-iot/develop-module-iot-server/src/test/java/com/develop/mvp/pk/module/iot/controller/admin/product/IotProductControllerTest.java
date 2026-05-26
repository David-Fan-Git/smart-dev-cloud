package com.develop.mvp.pk.module.iot.controller.admin.product;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.test.core.ut.BaseMockitoUnitTest;
import com.develop.mvp.pk.module.iot.application.product.command.CreateIotProductCommand;
import com.develop.mvp.pk.module.iot.application.product.port.inbound.IotProductUseCase;
import com.develop.mvp.pk.module.iot.controller.admin.product.vo.product.IotProductSaveReqVO;
import com.develop.mvp.pk.module.iot.service.product.IotProductCategoryService;
import com.develop.mvp.pk.module.iot.service.product.IotProductService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IotProductControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private IotProductController controller;

    @Mock
    private IotProductUseCase productUseCase;
    @Mock
    private IotProductService productService;
    @Mock
    private IotProductCategoryService categoryService;

    @Test
    void createProductRoutesToProductUseCase() {
        IotProductSaveReqVO reqVO = new IotProductSaveReqVO();
        reqVO.setName("温湿度");
        reqVO.setProductKey("pk-1");
        reqVO.setRegisterEnabled(true);
        reqVO.setCategoryId(10L);
        reqVO.setIcon("icon");
        reqVO.setPicUrl("pic");
        reqVO.setDescription("desc");
        reqVO.setDeviceType(1);
        reqVO.setNetType(2);
        reqVO.setProtocolType("mqtt");
        reqVO.setSerializeType("json");
        CreateIotProductCommand command = new CreateIotProductCommand("温湿度", "pk-1", true,
                10L, "icon", "pic", "desc", 1, 2, "mqtt", "json");
        when(productUseCase.createProduct(command)).thenReturn(100L);

        CommonResult<Long> result = controller.createProduct(reqVO);

        assertEquals(0, result.getCode());
        assertEquals(100L, result.getData());
        verify(productUseCase).createProduct(command);
    }

}
