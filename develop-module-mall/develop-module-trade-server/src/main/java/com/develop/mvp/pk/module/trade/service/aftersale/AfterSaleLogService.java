package com.develop.mvp.pk.module.trade.service.aftersale;


import com.develop.mvp.pk.module.trade.dal.dataobject.aftersale.AfterSaleLogDO;
import com.develop.mvp.pk.module.trade.service.aftersale.bo.AfterSaleLogCreateReqBO;

import java.util.List;

/**
 * 交易售后日志 Service 接口
 *
 * @author David
 * @since 2023/6/12 14:18
 */
public interface AfterSaleLogService {

    /**
     * 创建售后日志
     *
     * @param createReqBO 日志记录
     * @author David
     * @since 2023/6/12 14:18
     */
    void createAfterSaleLog(AfterSaleLogCreateReqBO createReqBO);

    /**
     * 获取售后日志
     *
     * @param afterSaleId 售后编号
     * @return 售后日志
     */
    List<AfterSaleLogDO> getAfterSaleLogList(Long afterSaleId);

}
