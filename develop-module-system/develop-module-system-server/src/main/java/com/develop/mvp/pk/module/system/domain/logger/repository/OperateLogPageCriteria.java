package com.develop.mvp.pk.module.system.domain.logger.repository;

import com.develop.mvp.pk.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 操作日志仓储分页查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OperateLogPageCriteria extends PageParam {

    private Long userId;

    private Long bizId;

    private String type;

    private String subType;

    private String action;

    private LocalDateTime[] createTime;

    private boolean exactType;

}
