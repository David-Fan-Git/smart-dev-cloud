package com.develop.mvp.pk.module.system.application.logger.query;

import com.develop.mvp.pk.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 登录日志分页查询对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LoginLogPageQuery extends PageParam {

    private String userIp;

    private String username;

    private Boolean status;

    private LocalDateTime[] createTime;

}
