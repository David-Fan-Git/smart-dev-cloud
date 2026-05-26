package com.develop.mvp.pk.module.bpm.infrastructure.copy;
// DDD 角色：BPM流程抄送仓储实现 - AggregateRoot_Bpm_Skill

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCopyPageReqVO;
import com.develop.mvp.pk.module.bpm.dal.dataobject.task.BpmProcessInstanceCopyDO;
import com.develop.mvp.pk.module.bpm.dal.mysql.task.BpmProcessInstanceCopyMapper;
import com.develop.mvp.pk.module.bpm.domain.copy.BpmProcessInstanceCopy;
import com.develop.mvp.pk.module.bpm.domain.copy.BpmProcessInstanceCopyFactory;
import com.develop.mvp.pk.module.bpm.domain.copy.repository.BpmProcessInstanceCopyRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class BpmProcessInstanceCopyRepositoryImpl implements BpmProcessInstanceCopyRepository {
    private final BpmProcessInstanceCopyMapper mapper;
    public BpmProcessInstanceCopyRepositoryImpl(BpmProcessInstanceCopyMapper mapper) { this.mapper = mapper; }

    @Override @Transactional
    public void save(BpmProcessInstanceCopy copy) {
        mapper.insert(toDO(copy));
    }

    @Override @Transactional
    public void saveBatch(List<BpmProcessInstanceCopy> copies) {
        mapper.insertBatch(copies.stream().map(this::toDO).collect(Collectors.toList()));
    }

    @Override @Transactional
    public void deleteByProcessInstanceId(String processInstanceId) {
        mapper.deleteByProcessInstanceId(processInstanceId);
    }

    @Override
    public PageResult<BpmProcessInstanceCopy> findPage(Long userId, String processInstanceName,
                                                        Integer pageNo, Integer pageSize) {
        BpmProcessInstanceCopyPageReqVO req = new BpmProcessInstanceCopyPageReqVO()
                .setProcessInstanceName(processInstanceName);
        req.setPageNo(pageNo); req.setPageSize(pageSize);
        PageResult<BpmProcessInstanceCopyDO> page = mapper.selectPage(userId, req);
        return new PageResult<>(page.getList().stream().map(this::fromDO).collect(Collectors.toList()), page.getTotal());
    }

    private BpmProcessInstanceCopyDO toDO(BpmProcessInstanceCopy c) {
        BpmProcessInstanceCopyDO d = new BpmProcessInstanceCopyDO();
        d.setId(c.id().value()); d.setStartUserId(c.startUserId());
        d.setProcessInstanceName(c.processInstanceName()); d.setProcessInstanceId(c.processInstanceId());
        d.setProcessDefinitionId(c.processDefinitionId()); d.setCategory(c.category());
        d.setActivityId(c.activityId()); d.setActivityName(c.activityName());
        d.setTaskId(c.taskId()); d.setUserId(c.userId()); d.setReason(c.reason());
        return d;
    }

    private BpmProcessInstanceCopy fromDO(BpmProcessInstanceCopyDO d) {
        if (d == null) return null;
        return BpmProcessInstanceCopyFactory.create(d.getId(), d.getStartUserId(), d.getProcessInstanceName(),
                d.getProcessInstanceId(), d.getProcessDefinitionId(), d.getCategory(),
                d.getActivityId(), d.getActivityName(), d.getTaskId(), d.getUserId(), d.getReason());
    }
}
