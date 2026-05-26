package com.develop.mvp.pk.module.bpm.controller.admin.task;

import cn.hutool.core.collection.CollUtil;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.collection.MapUtils;
import com.develop.mvp.pk.framework.common.util.date.DateUtils;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.bpm.application.copy.BpmProcessInstanceCopyApplicationService;
import com.develop.mvp.pk.module.bpm.controller.admin.base.user.UserSimpleBaseVO;
import com.develop.mvp.pk.module.bpm.controller.admin.task.vo.cc.BpmProcessInstanceCopyRespVO;
import com.develop.mvp.pk.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCopyPageReqVO;
import com.develop.mvp.pk.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import com.develop.mvp.pk.module.bpm.domain.copy.BpmProcessInstanceCopy;
import com.develop.mvp.pk.module.bpm.framework.flowable.core.util.FlowableUtils;
import com.develop.mvp.pk.module.bpm.service.definition.BpmProcessDefinitionService;
import com.develop.mvp.pk.module.bpm.service.task.BpmProcessInstanceService;
import com.develop.mvp.pk.module.system.api.user.AdminUserApi;
import com.develop.mvp.pk.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.flowable.engine.history.HistoricProcessInstance;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Stream;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.*;
import static com.develop.mvp.pk.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 流程实例抄送")
@RestController
@RequestMapping("/bpm/process-instance/copy")
@Validated
public class BpmProcessInstanceCopyController {

    @Resource
    private BpmProcessInstanceCopyApplicationService processInstanceCopyApplicationService;
    @Resource
    private BpmProcessInstanceService processInstanceService;
    @Resource
    private BpmProcessDefinitionService processDefinitionService;

    @Resource
    private AdminUserApi adminUserApi;

    @GetMapping("/page")
    @Operation(summary = "获得抄送流程分页列表")
    @PreAuthorize("@ss.hasPermission('bpm:process-instance-cc:query')")
    public CommonResult<PageResult<BpmProcessInstanceCopyRespVO>> getProcessInstanceCopyPage(
            @Valid BpmProcessInstanceCopyPageReqVO pageReqVO) {
        PageResult<BpmProcessInstanceCopy> pageResult = processInstanceCopyApplicationService.getPage(
                getLoginUserId(), pageReqVO.getProcessInstanceName(),
                pageReqVO.getPageNo(), pageReqVO.getPageSize());
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }

        // 拼接返回
        Map<String, HistoricProcessInstance> processInstanceMap = processInstanceService.getHistoricProcessInstanceMap(
                convertSet(pageResult.getList(), BpmProcessInstanceCopy::processInstanceId));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertListByFlatMap(pageResult.getList(),
                copy -> Stream.of(copy.startUserId(), copy.userId())));
        Map<String, BpmProcessDefinitionInfoDO> processDefinitionInfoMap = processDefinitionService.getProcessDefinitionInfoMap(
                convertSet(pageResult.getList(), BpmProcessInstanceCopy::processDefinitionId));
        return success(convertPage(pageResult, copy -> {
            BpmProcessInstanceCopyRespVO copyVO = toRespVO(copy);
            MapUtils.findAndThen(userMap, copy.userId(),
                    user -> copyVO.setCreateUser(BeanUtils.toBean(user, UserSimpleBaseVO.class)));
            MapUtils.findAndThen(userMap, copy.startUserId(),
                    user -> copyVO.setStartUser(BeanUtils.toBean(user, UserSimpleBaseVO.class)));
            MapUtils.findAndThen(processInstanceMap, copyVO.getProcessInstanceId(),
                    processInstance -> {
                        copyVO.setSummary(FlowableUtils.getSummary(
                                processDefinitionInfoMap.get(processInstance.getProcessDefinitionId()),
                                processInstance.getProcessVariables()));
                        copyVO.setProcessInstanceStartTime(DateUtils.of(processInstance.getStartTime()));
                    });
            return copyVO;
        }));
    }

    private BpmProcessInstanceCopyRespVO toRespVO(BpmProcessInstanceCopy c) {
        return new BpmProcessInstanceCopyRespVO()
                .setId(c.id().value())
                .setProcessInstanceName(c.processInstanceName()).setProcessInstanceId(c.processInstanceId())
                .setActivityId(c.activityId()).setActivityName(c.activityName())
                .setTaskId(c.taskId()).setReason(c.reason());
    }
}
