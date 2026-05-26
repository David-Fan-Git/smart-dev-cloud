package com.develop.mvp.pk.module.promotion.api.coupon.dto;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import lombok.Data;

/**
 * 优惠券模版 Response DTO
 *
 * @author David
 */
@Data
public class CouponTemplateRespDTO {
    /**
     * 模板编号，自增唯一
     */

    private Long id;
    /**
     * 优惠劵名
     */
    private String name;

    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
