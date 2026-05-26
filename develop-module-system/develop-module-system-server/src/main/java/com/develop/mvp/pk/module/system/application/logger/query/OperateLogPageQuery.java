package com.develop.mvp.pk.module.system.application.logger.query;

import com.develop.mvp.pk.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 操作日志分页查询对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OperateLogPageQuery extends PageParam {

    private Long userId;

    private Long bizId;

    private String type;

    private String subType;

    private String action;

    private LocalDateTime[] createTime;

    private boolean exactType;

}
