package com.develop.mvp.pk.module.mes.dal.dataobject.md.workstation;

import com.develop.mvp.pk.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * MES 设备资源 DO
 *
 * @author David
 */
@TableName("mes_md_workstation_machine")
@KeySequence("mes_md_workstation_machine_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesMdWorkstationMachineDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 工作站编号
     *
     * 关联 {@link MesMdWorkstationDO#getId()}
     */
    private Long workstationId;
    /**
     * 设备编号
     *
     * TODO @David：等 dv 模块，关联设备表
     */
    private Long machineryId;
    /**
     * 数量
     */
    private Integer quantity;
    /**
     * 备注
     */
    private String remark;

}
