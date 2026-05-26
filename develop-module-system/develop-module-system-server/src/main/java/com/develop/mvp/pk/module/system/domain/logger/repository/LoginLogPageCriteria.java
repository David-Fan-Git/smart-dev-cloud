package com.develop.mvp.pk.module.system.domain.logger.repository;

import com.develop.mvp.pk.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 登录日志仓储分页查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LoginLogPageCriteria extends PageParam {

    private String userIp;

    private String username;

    private Boolean status;

    private LocalDateTime[] createTime;

}
