package com.develop.mvp.pk.module.trade.framework.aftersale.core.utils;


import com.develop.mvp.pk.module.trade.enums.aftersale.AfterSaleOperateTypeEnum;
import com.develop.mvp.pk.module.trade.framework.aftersale.core.aop.AfterSaleLogAspect;

import java.util.Map;

/**
 * 操作日志工具类
 * 目前主要的作用，是提供给业务代码，记录操作明细和拓展字段
 *
 * @author David
 */
public class AfterSaleLogUtils {

    public static void setAfterSaleOperateType(AfterSaleOperateTypeEnum operateType) {
        AfterSaleLogAspect.setAfterSaleOperateType(operateType);
    }

    public static void setAfterSaleInfo(Long id, Integer beforeStatus, Integer afterStatus) {
        setAfterSaleInfo(id, beforeStatus, afterStatus, null);
    }

    public static void setAfterSaleInfo(Long id, Integer beforeStatus, Integer afterStatus,
                                        Map<String, Object> exts) {
        AfterSaleLogAspect.setAfterSale(id, beforeStatus, afterStatus, exts);
    }

}
