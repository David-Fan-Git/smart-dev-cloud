package com.develop.mvp.pk.module.wms.service.inventory;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.wms.controller.admin.inventory.vo.history.WmsInventoryHistoryPageReqVO;
import com.develop.mvp.pk.module.wms.dal.dataobject.inventory.WmsInventoryHistoryDO;

import java.util.List;

/**
 * WMS 库存流水 Service 接口
 *
 * @author David
 */
public interface WmsInventoryHistoryService {

    /**
     * 获得库存流水分页
     *
     * @param pageReqVO 分页查询
     * @return 库存流水分页
     */
    PageResult<WmsInventoryHistoryDO> getInventoryHistoryPage(WmsInventoryHistoryPageReqVO pageReqVO);

    /**
     * 创建库存流水列表
     *
     * @param list 库存流水列表
     */
    void createInventoryHistoryList(List<WmsInventoryHistoryDO> list);

}
