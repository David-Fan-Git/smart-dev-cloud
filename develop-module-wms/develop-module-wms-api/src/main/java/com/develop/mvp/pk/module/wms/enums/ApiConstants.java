package com.develop.mvp.pk.module.wms.enums;

import com.develop.mvp.pk.framework.common.enums.RpcConstants;

/**
 * API 相关的枚举
 *
 * @author David
 */
public class ApiConstants {

    /**
     * 服务名
     *
     * 注意，需要保证和 spring.application.name 保持一致
     */
    public static final String NAME = "wms-server";

    public static final String PREFIX = RpcConstants.RPC_API_PREFIX + "/wms";

    public static final String VERSION = "1.0.0";

}
