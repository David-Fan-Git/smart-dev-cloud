package com.develop.mvp.pk.module.bpm.controller.admin.oa;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.bpm.application.leave.BpmOALeaveApplicationService;
import com.develop.mvp.pk.module.bpm.controller.admin.oa.vo.BpmOALeaveCreateReqVO;
import com.develop.mvp.pk.module.bpm.controller.admin.oa.vo.BpmOALeavePageReqVO;
import com.develop.mvp.pk.module.bpm.controller.admin.oa.vo.BpmOALeaveRespVO;
import com.develop.mvp.pk.module.bpm.domain.leave.BpmOALeave;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;
import static com.develop.mvp.pk.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * OA 请假申请 Controller，用于演示自己存储数据，接入工作流的例子
 *
 * @author David
 * @author David
 */
@Tag(name = "管理后台 - OA 请假申请")
@RestController
@RequestMapping("/bpm/oa/leave")
@Validated
public class BpmOALeaveController {

    @Resource
    private BpmOALeaveApplicationService leaveApplicationService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('bpm:oa-leave:create')")
    @Operation(summary = "创建请求申请")
    public CommonResult<Long> createLeave(@Valid @RequestBody BpmOALeaveCreateReqVO createReqVO) {
        return success(leaveApplicationService.create(getLoginUserId(), createReqVO.getType(),
                createReqVO.getReason(), createReqVO.getStartTime(), createReqVO.getEndTime(),
                createReqVO.getStartUserSelectAssignees()));
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('bpm:oa-leave:query')")
    @Operation(summary = "获得请假申请")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<BpmOALeaveRespVO> getLeave(@RequestParam("id") Long id) {
        BpmOALeave leave = leaveApplicationService.get(id);
        if (leave == null) return success(null);
        return success(toRespVO(leave));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('bpm:oa-leave:query')")
    @Operation(summary = "获得请假申请分页")
    public CommonResult<PageResult<BpmOALeaveRespVO>> getLeavePage(@Valid BpmOALeavePageReqVO pageVO) {
        PageResult<BpmOALeave> pageResult = leaveApplicationService.getPage(
                getLoginUserId(), pageVO.getStatus(), pageVO.getType(), pageVO.getReason(),
                pageVO.getPageNo(), pageVO.getPageSize());
        PageResult<BpmOALeaveRespVO> voPage = new PageResult<>(
                pageResult.getList().stream().map(this::toRespVO).collect(Collectors.toList()),
                pageResult.getTotal());
        return success(voPage);
    }

    private BpmOALeaveRespVO toRespVO(BpmOALeave l) {
        return new BpmOALeaveRespVO().setId(l.id().value()).setType(l.type())
                .setReason(l.reason()).setStartTime(l.startTime()).setEndTime(l.endTime())
                .setStatus(l.status().code()).setProcessInstanceId(l.processInstanceId());
    }
}
