package com.develop.mvp.pk.module.bpm.controller.admin.definition.vo.form;

import com.develop.mvp.pk.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 动态表单分页 Request VO")
@Data
public class BpmFormPageReqVO extends PageParam {

    @Schema(description = "表单名称", example = "David")
    private String name;

}
