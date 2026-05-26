package com.develop.mvp.pk.module.member.application.group;

// Skill: AggregateRoot_MemberGroup_Skill — 应用服务 MemberGroupApplicationService

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.member.domain.group.MemberGroup;
import com.develop.mvp.pk.module.member.domain.group.repository.MemberGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.member.enums.ErrorCodeConstants.GROUP_HAS_USER;
import static com.develop.mvp.pk.module.member.enums.ErrorCodeConstants.GROUP_NOT_EXISTS;

@Service
@RequiredArgsConstructor
public class MemberGroupApplicationService {

    private final MemberGroupRepository repo;

    // 需要引用 MemberUserApplicationService 来做校验，通过 setter 或构造器注入
    // 但为避免循环依赖，这里采用方法参数传入计数的方式
    private java.util.function.Function<Long, Long> userCountByGroupId;

    public void setUserCountByGroupId(java.util.function.Function<Long, Long> fn) {
        this.userCountByGroupId = fn;
    }

    // ── 创建 ──
    @Transactional
    public Long createGroup(String name, String remark, Integer status) {
        MemberGroup group = MemberGroup.create(name);
        group.updateInfo(name, remark);
        if (status != null && status.equals(CommonStatusEnum.ENABLE.getStatus())) {
            group.enable();
        } else {
            group.disable();
        }
        group = repo.save(group);
        return group.id();
    }

    // ── 更新 ──
    @Transactional
    public void updateGroup(Long id, String name, String remark, Integer status) {
        MemberGroup group = get(id);
        group.updateInfo(name, remark);
        if (status != null && status.equals(CommonStatusEnum.ENABLE.getStatus())) {
            group.enable();
        } else {
            group.disable();
        }
        repo.save(group);
    }

    // ── 删除 ──
    @Transactional
    public void deleteGroup(Long id) {
        get(id);
        if (userCountByGroupId != null) {
            long count = userCountByGroupId.apply(id);
            if (count > 0) throw exception(GROUP_HAS_USER);
        }
        repo.delete(id);
    }

    // ── 查询 ──
    public MemberGroup get(Long id) {
        MemberGroup g = repo.findById(id);
        if (g == null) throw exception(GROUP_NOT_EXISTS);
        return g;
    }

    public List<MemberGroup> getList(Collection<Long> ids) { return repo.findByIds(ids); }

    public PageResult<MemberGroup> getPage(String name, Integer status, String createTimeStart, String createTimeEnd,
                                           Integer pageNo, Integer pageSize) {
        return repo.findPage(name, status, createTimeStart, createTimeEnd, pageNo, pageSize);
    }

    public List<MemberGroup> getListByStatus(Integer status) { return repo.findByStatus(status); }

    public List<MemberGroup> getEnableList() { return repo.findByStatus(CommonStatusEnum.ENABLE.getStatus()); }
}
