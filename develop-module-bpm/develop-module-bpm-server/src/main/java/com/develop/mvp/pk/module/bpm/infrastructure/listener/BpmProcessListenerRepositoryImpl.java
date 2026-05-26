package com.develop.mvp.pk.module.bpm.infrastructure.listener;
// DDD 角色：BPM流程监听器仓储实现 - AggregateRoot_Bpm_Skill

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.bpm.controller.admin.definition.vo.listener.BpmProcessListenerPageReqVO;
import com.develop.mvp.pk.module.bpm.dal.dataobject.definition.BpmProcessListenerDO;
import com.develop.mvp.pk.module.bpm.dal.mysql.definition.BpmProcessListenerMapper;
import com.develop.mvp.pk.module.bpm.domain.listener.BpmProcessListener;
import com.develop.mvp.pk.module.bpm.domain.listener.BpmProcessListenerFactory;
import com.develop.mvp.pk.module.bpm.domain.listener.repository.BpmProcessListenerRepository;
import com.develop.mvp.pk.module.bpm.domain.listener.valueobject.ListenerId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Repository
public class BpmProcessListenerRepositoryImpl implements BpmProcessListenerRepository {
    private final BpmProcessListenerMapper mapper;
    public BpmProcessListenerRepositoryImpl(BpmProcessListenerMapper mapper) { this.mapper = mapper; }

    @Override @Transactional
    public void save(BpmProcessListener listener) {
        BpmProcessListenerDO d = toDO(listener);
        if (mapper.selectById(listener.id().value()) == null) mapper.insert(d);
        else mapper.updateById(d);
    }

    @Override @Transactional
    public void delete(ListenerId id) { mapper.deleteById(id.value()); }

    @Override
    public BpmProcessListener findById(ListenerId id) { return fromDO(mapper.selectById(id.value())); }

    @Override
    public PageResult<BpmProcessListener> findPage(String name, String type, String event, Integer status,
                                                    Integer pageNo, Integer pageSize) {
        BpmProcessListenerPageReqVO req = new BpmProcessListenerPageReqVO()
                .setName(name).setType(type).setEvent(event).setStatus(status);
        req.setPageNo(pageNo); req.setPageSize(pageSize);
        PageResult<BpmProcessListenerDO> page = mapper.selectPage(req);
        return new PageResult<>(page.getList().stream().map(this::fromDO).collect(Collectors.toList()), page.getTotal());
    }

    private BpmProcessListenerDO toDO(BpmProcessListener l) {
        BpmProcessListenerDO d = new BpmProcessListenerDO();
        d.setId(l.id().value()); d.setName(l.name().value());
        d.setStatus(l.status().code()); d.setType(l.type());
        d.setEvent(l.event()); d.setValueType(l.valueType()); d.setValue(l.value());
        return d;
    }

    private BpmProcessListener fromDO(BpmProcessListenerDO d) {
        if (d == null) return null;
        return BpmProcessListenerFactory.reconstitute(d.getId(), d.getName(), d.getStatus(),
                d.getType(), d.getEvent(), d.getValueType(), d.getValue());
    }
}
