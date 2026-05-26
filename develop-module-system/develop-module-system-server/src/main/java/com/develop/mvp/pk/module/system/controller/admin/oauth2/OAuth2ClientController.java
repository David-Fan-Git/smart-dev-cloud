package com.develop.mvp.pk.module.system.controller.admin.oauth2;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.system.controller.admin.oauth2.vo.client.OAuth2ClientPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.oauth2.vo.client.OAuth2ClientRespVO;
import com.develop.mvp.pk.module.system.controller.admin.oauth2.vo.client.OAuth2ClientSaveReqVO;
import com.develop.mvp.pk.module.system.application.oauth2.port.inbound.OAuth2UseCase;
import com.develop.mvp.pk.module.system.dal.dataobject.oauth2.OAuth2ClientDO;
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
 * OAuth2 Client Controller 控制器。
 */
@Tag(name = "管理后台 - OAuth2 客户端")
@RestController
@RequestMapping("/system/oauth2-client")
@Validated
public class OAuth2ClientController {

    @Resource
    private OAuth2UseCase oAuth2ClientService;

    /**
     * 创建 create OAuth2 Client 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @return 处理结果
     */
    @PostMapping("/create")
    @Operation(summary = "创建 OAuth2 客户端")
    @PreAuthorize("@ss.hasPermission('system:oauth2-client:create')")
    public CommonResult<Long> createOAuth2Client(@Valid @RequestBody OAuth2ClientSaveReqVO createReqVO) {
        return success(oAuth2ClientService.createOAuth2Client(createReqVO));
    }

    /**
     * 更新 update OAuth2 Client 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     * @return 处理结果
     */
    @PutMapping("/update")
    @Operation(summary = "更新 OAuth2 客户端")
    @PreAuthorize("@ss.hasPermission('system:oauth2-client:update')")
    public CommonResult<Boolean> updateOAuth2Client(@Valid @RequestBody OAuth2ClientSaveReqVO updateReqVO) {
        oAuth2ClientService.updateOAuth2Client(updateReqVO);
        return success(true);
    }

    /**
     * 删除 delete OAuth2 Client 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除 OAuth2 客户端")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('system:oauth2-client:delete')")
    public CommonResult<Boolean> deleteOAuth2Client(@RequestParam("id") Long id) {
        oAuth2ClientService.deleteOAuth2Client(id);
        return success(true);
    }

    /**
     * 删除 delete OAuth2 Client List 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @Operation(summary = "批量删除 OAuth2 客户端")
    @PreAuthorize("@ss.hasPermission('system:oauth2-client:delete')")
    public CommonResult<Boolean> deleteOAuth2ClientList(@RequestParam("ids") List<Long> ids) {
        oAuth2ClientService.deleteOAuth2ClientList(ids);
        return success(true);
    }

    /**
     * 查询 get OAuth2 Client 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @GetMapping("/get")
    @Operation(summary = "获得 OAuth2 客户端")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:oauth2-client:query')")
    public CommonResult<OAuth2ClientRespVO> getOAuth2Client(@RequestParam("id") Long id) {
        OAuth2ClientDO client = oAuth2ClientService.getOAuth2Client(id);
        return success(BeanUtils.toBean(client, OAuth2ClientRespVO.class));
    }

    /**
     * 查询 get OAuth2 Client Page 对应的数据。
     *
     * @param pageVO pageVO 参数
     * @return 处理结果
     */
    @GetMapping("/page")
    @Operation(summary = "获得 OAuth2 客户端分页")
    @PreAuthorize("@ss.hasPermission('system:oauth2-client:query')")
    public CommonResult<PageResult<OAuth2ClientRespVO>> getOAuth2ClientPage(@Valid OAuth2ClientPageReqVO pageVO) {
        PageResult<OAuth2ClientDO> pageResult = oAuth2ClientService.getOAuth2ClientPage(pageVO);
        return success(BeanUtils.toBean(pageResult, OAuth2ClientRespVO.class));
    }

}
