package com.develop.mvp.pk.module.bpm.application.usergroup;
// DDD 角色：BPM用户组应用服务 - AggregateRoot_Bpm_Skill

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.bpm.domain.usergroup.BpmUserGroup;
import com.develop.mvp.pk.module.bpm.domain.usergroup.BpmUserGroupFactory;
import com.develop.mvp.pk.module.bpm.domain.usergroup.event.UserGroupDomainEvent;
import com.develop.mvp.pk.module.bpm.domain.usergroup.repository.BpmUserGroupRepository;
import com.develop.mvp.pk.module.bpm.domain.usergroup.valueobject.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.bpm.enums.ErrorCodeConstants.*;

@Service
@RequiredArgsConstructor
public class BpmUserGroupApplicationService {
    private final BpmUserGroupRepository repo;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long create(String name, String description, Integer status, Set<Long> userIds) {
        BpmUserGroup g = BpmUserGroupFactory.create(null, name, description, userIds);
        repo.save(g);
        publishEvents(g);
        return g.id().value();
    }

    @Transactional
    public void update(Long id, String name, String description, Integer status, Set<Long> userIds) {
        findExisting(id);
        BpmUserGroup g = BpmUserGroupFactory.reconstitute(id, name, description, status, userIds);
        repo.save(g);
        publishEvents(g);
    }

    @Transactional
    public void delete(Long id) {
        findExisting(id);
        BpmUserGroup g = BpmUserGroupFactory.reconstitute(id, "", null, 0, null);
        g.markDeleted();
        repo.delete(g.id());
        publishEvents(g);
    }

    public BpmUserGroup get(Long id) { return repo.findById(UserGroupId.of(id)); }
    public List<BpmUserGroup> getByIds(Collection<Long> ids) {
        return repo.findByIds(ids.stream().map(UserGroupId::of).collect(Collectors.toList()));
    }
    public List<BpmUserGroup> getByStatus(Integer status) { return repo.findByStatus(UserGroupStatus.of(status)); }
    public PageResult<BpmUserGroup> getPage(String name, Integer status, Integer pageNo, Integer pageSize) {
        return repo.findPage(name, status, pageNo, pageSize);
    }

    public void validateGroups(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        List<BpmUserGroup> groups = getByIds(ids);
        Map<Long, BpmUserGroup> groupMap = groups.stream().collect(Collectors.toMap(g -> g.id().value(), g -> g));
        ids.forEach(id -> {
            BpmUserGroup g = groupMap.get(id);
            if (g == null) throw exception(USER_GROUP_NOT_EXISTS);
            if (!g.isEnabled()) throw exception(USER_GROUP_IS_DISABLE, g.name().value());
        });
    }

    private BpmUserGroup findExisting(Long id) {
        BpmUserGroup g = repo.findById(UserGroupId.of(id));
        if (g == null) throw exception(USER_GROUP_NOT_EXISTS);
        return g;
    }

    private void publishEvents(BpmUserGroup group) {
        for (UserGroupDomainEvent event : group.pullEvents()) eventPublisher.publishEvent(event);
    }
}
