package com.develop.mvp.pk.module.iot.application.product.port.inbound;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.iot.application.product.command.CreateIotProductCommand;
import com.develop.mvp.pk.module.iot.application.product.command.UpdateIotProductCommand;
import com.develop.mvp.pk.module.iot.application.product.command.UpdateIotProductStatusCommand;
import com.develop.mvp.pk.module.iot.application.product.query.IotProductPageQuery;
import com.develop.mvp.pk.module.iot.application.product.result.IotProductResult;

import java.util.List;

public interface IotProductUseCase {

    Long createProduct(CreateIotProductCommand command);

    void updateProduct(UpdateIotProductCommand command);

    void deleteProduct(Long id);

    void updateProductStatus(UpdateIotProductStatusCommand command);

    IotProductResult getProduct(Long id);

    IotProductResult getProductByProductKey(String productKey);

    PageResult<IotProductResult> getProductPage(IotProductPageQuery query);

    List<IotProductResult> getProductList(Integer deviceType);

    void syncProductPropertyTable();

}
