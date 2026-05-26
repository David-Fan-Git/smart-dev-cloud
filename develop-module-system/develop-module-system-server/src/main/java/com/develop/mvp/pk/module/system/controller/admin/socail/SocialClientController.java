package com.develop.mvp.pk.module.system.controller.admin.socail;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.system.api.social.SocialClientApi;
import com.develop.mvp.pk.module.system.api.social.dto.SocialWxaSubscribeMessageSendReqDTO;
import com.develop.mvp.pk.module.system.controller.admin.socail.vo.client.SocialClientPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.socail.vo.client.SocialClientRespVO;
import com.develop.mvp.pk.module.system.controller.admin.socail.vo.client.SocialClientSaveReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.social.SocialClientDO;
import com.develop.mvp.pk.module.system.application.social.port.inbound.SocialUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

/**
 * Social Client Controller 控制器。
 */
@Tag(name = "管理后台 - 社交客户端")
@RestController
@RequestMapping("/system/social-client")
@Validated
public class SocialClientController {

    @Resource
    private SocialUseCase socialUseCase;
    @Resource
    private SocialClientApi socialClientApi;

    /**
     * 创建 create Social Client 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @return 处理结果
     */
    @PostMapping("/create")
    @Operation(summary = "创建社交客户端")
    @PreAuthorize("@ss.hasPermission('system:social-client:create')")
    public CommonResult<Long> createSocialClient(@Valid @RequestBody SocialClientSaveReqVO createReqVO) {
        return success(socialUseCase.createSocialClient(createReqVO));
    }

    /**
     * 更新 update Social Client 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     * @return 处理结果
     */
    @PutMapping("/update")
    @Operation(summary = "更新社交客户端")
    @PreAuthorize("@ss.hasPermission('system:social-client:update')")
    public CommonResult<Boolean> updateSocialClient(@Valid @RequestBody SocialClientSaveReqVO updateReqVO) {
        socialUseCase.updateSocialClient(updateReqVO);
        return success(true);
    }

    /**
     * 删除 delete Social Client 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除社交客户端")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('system:social-client:delete')")
    public CommonResult<Boolean> deleteSocialClient(@RequestParam("id") Long id) {
        socialUseCase.deleteSocialClient(id);
        return success(true);
    }

    /**
     * 删除 delete Social Client List 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @Operation(summary = "批量删除社交客户端")
    @PreAuthorize("@ss.hasPermission('system:social-client:delete')")
    public CommonResult<Boolean> deleteSocialClientList(@RequestParam("ids") List<Long> ids) {
        socialUseCase.deleteSocialClientList(ids);
        return success(true);
    }

    /**
     * 查询 get Social Client 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @GetMapping("/get")
    @Operation(summary = "获得社交客户端")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:social-client:query')")
    public CommonResult<SocialClientRespVO> getSocialClient(@RequestParam("id") Long id) {
        SocialClientDO client = socialUseCase.getSocialClient(id);
        return success(BeanUtils.toBean(client, SocialClientRespVO.class));
    }

    /**
     * 查询 get Social Client Page 对应的数据。
     *
     * @param pageVO pageVO 参数
     * @return 处理结果
     */
    @GetMapping("/page")
    @Operation(summary = "获得社交客户端分页")
    @PreAuthorize("@ss.hasPermission('system:social-client:query')")
    public CommonResult<PageResult<SocialClientRespVO>> getSocialClientPage(@Valid SocialClientPageReqVO pageVO) {
        PageResult<SocialClientDO> pageResult = socialUseCase.getSocialClientPage(pageVO);
        return success(BeanUtils.toBean(pageResult, SocialClientRespVO.class));
    }

    /**
     * 发送 send Subscribe Message 对应的消息。
     *
     * @param reqDTO reqDTO 参数
     */
    @PostMapping("/send-subscribe-message")
    @Operation(summary = "发送订阅消息") // 用于测试
    @PreAuthorize("@ss.hasPermission('system:social-client:query')")
    public void sendSubscribeMessage(@RequestBody SocialWxaSubscribeMessageSendReqDTO reqDTO) {
        socialClientApi.sendWxaSubscribeMessage(reqDTO).checkError();
    }

}
