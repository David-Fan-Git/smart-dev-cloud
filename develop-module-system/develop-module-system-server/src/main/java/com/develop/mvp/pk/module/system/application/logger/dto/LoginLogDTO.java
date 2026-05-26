package com.develop.mvp.pk.module.system.application.logger.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志应用层数据传输对象。
 */
@Data
public class LoginLogDTO {

    private Long id;

    private Integer logType;

    private Long userId;

    private Integer userType;

    private String traceId;

    private String username;

    private Integer result;

    private String userIp;

    private String userAgent;

    private LocalDateTime createTime;

}
