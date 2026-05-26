package com.develop.mvp.pk.module.system.controller.admin.oauth2.vo.open;

import com.develop.mvp.pk.framework.common.core.KeyValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * OAuth2 Open Authorize Info Resp VO 接口视图对象。
 */
@Schema(description = "管理后台 - 授权页的信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2OpenAuthorizeInfoRespVO {

    /**
     * 客户端
     */
    private Client client;

    @Schema(description = "scope 的选中信息,使用 List 保证有序性，Key 是 scope，Value 为是否选中", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<KeyValue<String, Boolean>> scopes;

    /**
     * Client 类。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Client {

        @Schema(description = "应用名", requiredMode = Schema.RequiredMode.REQUIRED, example = "土豆")
        private String name;

        @Schema(description = "应用图标", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/xx.png")
        private String logo;

    }

}
