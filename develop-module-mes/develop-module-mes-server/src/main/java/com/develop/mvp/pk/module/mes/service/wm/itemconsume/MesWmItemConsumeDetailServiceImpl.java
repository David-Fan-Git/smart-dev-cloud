package com.develop.mvp.pk.module.mes.service.wm.itemconsume;

import com.develop.mvp.pk.module.mes.dal.dataobject.wm.itemconsume.MesWmItemConsumeDetailDO;
import com.develop.mvp.pk.module.mes.dal.mysql.wm.itemconsume.MesWmItemConsumeDetailMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * MES 物料消耗记录明细 Service 实现类
 *
 * @author David
 */
@Service
@Validated
public class MesWmItemConsumeDetailServiceImpl implements MesWmItemConsumeDetailService {

    @Resource
    private MesWmItemConsumeDetailMapper itemConsumeDetailMapper;

    @Override
    public void createItemConsumeDetailBatch(List<MesWmItemConsumeDetailDO> details) {
        itemConsumeDetailMapper.insertBatch(details);
    }

    @Override
    public List<MesWmItemConsumeDetailDO> getItemConsumeDetailListByConsumeId(Long consumeId) {
        return itemConsumeDetailMapper.selectListByConsumeId(consumeId);
    }

}
