package com.develop.mvp.pk.module.system.controller.admin.notify;

import com.develop.mvp.pk.framework.apilog.core.annotation.ApiAccessLog;
import com.develop.mvp.pk.framework.common.enums.UserTypeEnum;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.system.controller.admin.notify.vo.message.NotifyMessageMyPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.notify.vo.message.NotifyMessagePageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.notify.vo.message.NotifyMessageRespVO;
import com.develop.mvp.pk.module.system.application.notify.port.inbound.NotifyUseCase;
import com.develop.mvp.pk.module.system.dal.dataobject.notify.NotifyMessageDO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;
import static com.develop.mvp.pk.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * Notify Message Controller 控制器。
 */
@Tag(name = "管理后台 - 我的站内信")
@RestController
@RequestMapping("/system/notify-message")
@Validated
public class NotifyMessageController {

    @Resource
    private NotifyUseCase notifyMessageService;

    // ========== 管理所有的站内信 ==========

    /**
     * 查询 get Notify Message 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @GetMapping("/get")
    @Operation(summary = "获得站内信")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:notify-message:query')")
    public CommonResult<NotifyMessageRespVO> getNotifyMessage(@RequestParam("id") Long id) {
        NotifyMessageDO message = notifyMessageService.getNotifyMessage(id);
        return success(BeanUtils.toBean(message, NotifyMessageRespVO.class));
    }

    /**
     * 查询 get Notify Message Page 对应的数据。
     *
     * @param pageVO pageVO 参数
     * @return 处理结果
     */
    @GetMapping("/page")
    @Operation(summary = "获得站内信分页")
    @PreAuthorize("@ss.hasPermission('system:notify-message:query')")
    public CommonResult<PageResult<NotifyMessageRespVO>> getNotifyMessagePage(@Valid NotifyMessagePageReqVO pageVO) {
        PageResult<NotifyMessageDO> pageResult = notifyMessageService.getNotifyMessagePage(pageVO);
        return success(BeanUtils.toBean(pageResult, NotifyMessageRespVO.class));
    }

    // ========== 查看自己的站内信 ==========

    /**
     * 查询 get My My Notify Message Page 对应的数据。
     *
     * @param pageVO pageVO 参数
     * @return 处理结果
     */
    @GetMapping("/my-page")
    @Operation(summary = "获得我的站内信分页")
    public CommonResult<PageResult<NotifyMessageRespVO>> getMyMyNotifyMessagePage(@Valid NotifyMessageMyPageReqVO pageVO) {
        PageResult<NotifyMessageDO> pageResult = notifyMessageService.getMyMyNotifyMessagePage(pageVO,
                getLoginUserId(), UserTypeEnum.ADMIN.getValue());
        return success(BeanUtils.toBean(pageResult, NotifyMessageRespVO.class));
    }

    /**
     * 更新 update Notify Message Read 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    @PutMapping("/update-read")
    @Operation(summary = "标记站内信为已读")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    public CommonResult<Boolean> updateNotifyMessageRead(@RequestParam("ids") List<Long> ids) {
        notifyMessageService.updateNotifyMessageRead(ids, getLoginUserId(), UserTypeEnum.ADMIN.getValue());
        return success(Boolean.TRUE);
    }

    /**
     * 更新 update All Notify Message Read 对应的数据。
     *
     * @return 处理结果
     */
    @PutMapping("/update-all-read")
    @Operation(summary = "标记所有站内信为已读")
    public CommonResult<Boolean> updateAllNotifyMessageRead() {
        notifyMessageService.updateAllNotifyMessageRead(getLoginUserId(), UserTypeEnum.ADMIN.getValue());
        return success(Boolean.TRUE);
    }

    /**
     * 查询 get Unread Notify Message List 对应的数据。
     *
     * @param size size 参数
     * @return 处理结果
     */
    @GetMapping("/get-unread-list")
    @Operation(summary = "获取当前用户的最新站内信列表，默认 10 条")
    @Parameter(name = "size", description = "10")
    public CommonResult<List<NotifyMessageRespVO>> getUnreadNotifyMessageList(
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        List<NotifyMessageDO> list = notifyMessageService.getUnreadNotifyMessageList(
                getLoginUserId(), UserTypeEnum.ADMIN.getValue(), size);
        return success(BeanUtils.toBean(list, NotifyMessageRespVO.class));
    }

    /**
     * 查询 get Unread Notify Message Count 对应的数据。
     *
     * @return 处理结果
     */
    @GetMapping("/get-unread-count")
    @Operation(summary = "获得当前用户的未读站内信数量")
    @ApiAccessLog(enable = false) // 由于前端会不断轮询该接口，记录日志没有意义
    public CommonResult<Long> getUnreadNotifyMessageCount() {
        return success(notifyMessageService.getUnreadNotifyMessageCount(
                getLoginUserId(), UserTypeEnum.ADMIN.getValue()));
    }

}
