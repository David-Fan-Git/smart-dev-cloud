package com.develop.mvp.pk.module.system.application.logger.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志应用层数据传输对象。
 */
@Data
public class OperateLogDTO {

    private Long id;

    private String traceId;

    private Long userId;

    private Integer userType;

    private String type;

    private String subType;

    private Long bizId;

    private String action;

    private String extra;

    private String requestMethod;

    private String requestUrl;

    private String userIp;

    private String userAgent;

    private LocalDateTime createTime;

}
