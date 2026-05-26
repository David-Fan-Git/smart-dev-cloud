package com.develop.mvp.pk.module.trade.domain.aftersale.repository;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.trade.domain.aftersale.AfterSale;
import com.develop.mvp.pk.module.trade.domain.aftersale.valueobject.AfterSaleId;

import java.util.List;

public interface AfterSaleRepository {
    AfterSale save(AfterSale afterSale);
    AfterSale findById(AfterSaleId id);
    AfterSale findByNo(String no);
    List<AfterSale> findByUserId(Long userId);
    List<AfterSale> findByOrderId(Long orderId);
    PageResult<AfterSale> findPage(Long userId, Integer status, Integer type, String no,
                                    Integer pageNo, Integer pageSize);
    long count();
}
