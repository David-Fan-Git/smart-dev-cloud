package com.develop.mvp.pk.module.member.controller.app.address;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.module.member.application.address.MemberAddressApplicationService;
import com.develop.mvp.pk.module.member.controller.app.address.vo.AppAddressCreateReqVO;
import com.develop.mvp.pk.module.member.controller.app.address.vo.AppAddressRespVO;
import com.develop.mvp.pk.module.member.controller.app.address.vo.AppAddressUpdateReqVO;
import com.develop.mvp.pk.module.member.convert.address.AddressConvert;
import com.develop.mvp.pk.module.member.domain.address.MemberAddress;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;
import static com.develop.mvp.pk.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 用户收件地址")
@RestController
@RequestMapping("/member/address")
@Validated
public class AppAddressController {

    @Resource
    private MemberAddressApplicationService addressApplicationService;

    @PostMapping("/create")
    @Operation(summary = "创建用户收件地址")
    public CommonResult<Long> createAddress(@Valid @RequestBody AppAddressCreateReqVO createReqVO) {
        return success(addressApplicationService.createAddress(getLoginUserId(),
                createReqVO.getName(), createReqVO.getMobile(), createReqVO.getAreaId(),
                createReqVO.getDetailAddress(), createReqVO.getDefaultStatus()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户收件地址")
    public CommonResult<Boolean> updateAddress(@Valid @RequestBody AppAddressUpdateReqVO updateReqVO) {
        addressApplicationService.updateAddress(getLoginUserId(), updateReqVO.getId(),
                updateReqVO.getName(), updateReqVO.getMobile(), updateReqVO.getAreaId(),
                updateReqVO.getDetailAddress(), updateReqVO.getDefaultStatus());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用户收件地址")
    @Parameter(name = "id", description = "编号", required = true)
    public CommonResult<Boolean> deleteAddress(@RequestParam("id") Long id) {
        addressApplicationService.deleteAddress(getLoginUserId(), id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用户收件地址")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<AppAddressRespVO> getAddress(@RequestParam("id") Long id) {
        MemberAddress address = addressApplicationService.getAddress(getLoginUserId(), id);
        return success(AddressConvert.INSTANCE.convert(address));
    }

    @GetMapping("/get-default")
    @Operation(summary = "获得默认的用户收件地址")
    public CommonResult<AppAddressRespVO> getDefaultUserAddress() {
        MemberAddress address = addressApplicationService.getDefaultUserAddress(getLoginUserId());
        return success(AddressConvert.INSTANCE.convert(address));
    }

    @GetMapping("/list")
    @Operation(summary = "获得用户收件地址列表")
    public CommonResult<List<AppAddressRespVO>> getAddressList() {
        List<MemberAddress> list = addressApplicationService.getAddressList(getLoginUserId());
        return success(AddressConvert.INSTANCE.convertListFromDomain(list));
    }

}
