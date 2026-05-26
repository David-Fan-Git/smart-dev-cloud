package com.develop.mvp.pk.module.bpm.infrastructure.leave;
// DDD 角色：BPM OA请假单仓储实现 - AggregateRoot_Bpm_Skill

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.bpm.controller.admin.oa.vo.BpmOALeavePageReqVO;
import com.develop.mvp.pk.module.bpm.dal.dataobject.oa.BpmOALeaveDO;
import com.develop.mvp.pk.module.bpm.dal.mysql.oa.BpmOALeaveMapper;
import com.develop.mvp.pk.module.bpm.domain.leave.BpmOALeave;
import com.develop.mvp.pk.module.bpm.domain.leave.BpmOALeaveFactory;
import com.develop.mvp.pk.module.bpm.domain.leave.repository.BpmOALeaveRepository;
import com.develop.mvp.pk.module.bpm.domain.leave.valueobject.LeaveId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Repository
public class BpmOALeaveRepositoryImpl implements BpmOALeaveRepository {
    private final BpmOALeaveMapper mapper;
    public BpmOALeaveRepositoryImpl(BpmOALeaveMapper mapper) { this.mapper = mapper; }

    @Override @Transactional
    public void save(BpmOALeave leave) {
        BpmOALeaveDO d = toDO(leave);
        if (leave.id().value() == null || mapper.selectById(leave.id().value()) == null) mapper.insert(d);
        else mapper.updateById(d);
    }

    @Override
    public BpmOALeave findById(LeaveId id) { return fromDO(mapper.selectById(id.value())); }

    @Override
    public PageResult<BpmOALeave> findPage(Long userId, Integer status, Integer type, String reason,
                                           Integer pageNo, Integer pageSize) {
        BpmOALeavePageReqVO req = new BpmOALeavePageReqVO().setStatus(status).setType(type).setReason(reason);
        req.setPageNo(pageNo); req.setPageSize(pageSize);
        PageResult<BpmOALeaveDO> page = mapper.selectPage(userId, req);
        return new PageResult<>(page.getList().stream().map(this::fromDO).collect(Collectors.toList()), page.getTotal());
    }

    private BpmOALeaveDO toDO(BpmOALeave l) {
        BpmOALeaveDO d = new BpmOALeaveDO();
        d.setId(l.id().value()); d.setUserId(l.userId()); d.setType(l.type());
        d.setReason(l.reason()); d.setStartTime(l.startTime()); d.setEndTime(l.endTime());
        d.setDay(l.day()); d.setStatus(l.status().code()); d.setProcessInstanceId(l.processInstanceId());
        return d;
    }

    private BpmOALeave fromDO(BpmOALeaveDO d) {
        if (d == null) return null;
        return BpmOALeaveFactory.reconstitute(d.getId(), d.getUserId(), d.getType(), d.getReason(),
                d.getStartTime(), d.getEndTime(), d.getDay(), d.getStatus(), d.getProcessInstanceId());
    }
}
