package com.develop.mvp.pk.module.mes.controller.admin.wm.returnsales.vo.line;

import com.develop.mvp.pk.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理后台 - MES 销售退货相关
 *
 * @author David
 */
@Schema(description = "管理后台 - MES 销售退货单行分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class MesWmReturnSalesLinePageReqVO extends PageParam {

    @Schema(description = "退货单ID", example = "1")
    private Long returnId;

    @Schema(description = "物料ID", example = "1")
    private Long itemId;

}
