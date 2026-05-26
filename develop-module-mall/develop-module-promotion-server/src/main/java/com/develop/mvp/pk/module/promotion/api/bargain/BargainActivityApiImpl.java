package com.develop.mvp.pk.module.promotion.api.bargain;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.module.promotion.service.bargain.BargainActivityService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

/**
 * 砍价活动 Api 接口实现类
 *
 * @author David
 */
@RestController // 提供 RESTful API 接口，给 Feign 调用
@Validated
public class BargainActivityApiImpl implements BargainActivityApi {

    @Resource
    private BargainActivityService bargainActivityService;

    @Override
    public CommonResult<Boolean> updateBargainActivityStock(Long id, Integer count) {
        bargainActivityService.updateBargainActivityStock(id, count);
        return success(true);
    }

}
