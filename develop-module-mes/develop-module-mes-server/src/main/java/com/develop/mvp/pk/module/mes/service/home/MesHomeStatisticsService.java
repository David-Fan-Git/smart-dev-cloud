package com.develop.mvp.pk.module.mes.service.home;

import com.develop.mvp.pk.module.mes.controller.admin.home.vo.MesHomeProductionTrendRespVO;
import com.develop.mvp.pk.module.mes.controller.admin.home.vo.MesHomeSummaryRespVO;
import com.develop.mvp.pk.module.mes.controller.admin.home.vo.MesHomeWorkOrderStatusRespVO;

import java.util.List;

/**
 * MES 首页统计 Service 接口
 *
 * @author David
 */
public interface MesHomeStatisticsService {

    /**
     * 获得首页汇总统计
     *
     * @return 汇总统计数据
     */
    MesHomeSummaryRespVO getHomeSummary();

    /**
     * 获得工单状态分布
     *
     * @return 各状态的工单数量列表
     */
    List<MesHomeWorkOrderStatusRespVO> getWorkOrderStatusDistribution();

    /**
     * 获得生产趋势（近 N 天）
     *
     * @param days 天数
     * @return 每天的产量数据
     */
    List<MesHomeProductionTrendRespVO> getProductionTrend(Integer days);

}
