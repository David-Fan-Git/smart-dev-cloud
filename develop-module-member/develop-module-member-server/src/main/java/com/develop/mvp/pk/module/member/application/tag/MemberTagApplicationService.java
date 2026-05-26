package com.develop.mvp.pk.module.member.application.tag;

// Skill: AggregateRoot_MemberTag_Skill — 应用服务 MemberTagApplicationService

import cn.hutool.core.util.StrUtil;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.member.domain.tag.MemberTag;
import com.develop.mvp.pk.module.member.domain.tag.repository.MemberTagRepository;
import com.develop.mvp.pk.module.member.domain.user.repository.MemberUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.member.enums.ErrorCodeConstants.*;

@Service
@RequiredArgsConstructor
public class MemberTagApplicationService {

    private final MemberTagRepository repo;
    private final MemberUserRepository userRepo;

    // ── 创建 ──
    @Transactional
    public Long createTag(String name) {
        validateNameUnique(null, name);
        MemberTag tag = MemberTag.create(name);
        tag = repo.save(tag);
        return tag.id();
    }

    // ── 更新 ──
    @Transactional
    public void updateTag(Long id, String name) {
        validateExists(id);
        validateNameUnique(id, name);
        MemberTag tag = get(id);
        tag.rename(name);
        repo.save(tag);
    }

    // ── 删除 ──
    @Transactional
    public void deleteTag(Long id) {
        validateExists(id);
        validateTagHasUser(id);
        repo.delete(id);
    }

    // ── 查询 ──
    public MemberTag get(Long id) {
        MemberTag t = repo.findById(id);
        if (t == null) throw exception(TAG_NOT_EXISTS);
        return t;
    }

    public List<MemberTag> getList(Collection<Long> ids) { return repo.findByIds(ids); }

    public PageResult<MemberTag> getPage(String name, String createTimeStart, String createTimeEnd,
                                         Integer pageNo, Integer pageSize) {
        return repo.findPage(name, createTimeStart, createTimeEnd, pageNo, pageSize);
    }

    public List<MemberTag> getList() { return repo.findAll(); }

    // ── 校验 ──
    private void validateExists(Long id) {
        if (repo.findById(id) == null) throw exception(TAG_NOT_EXISTS);
    }

    private void validateNameUnique(Long id, String name) {
        if (StrUtil.isBlank(name)) return;
        MemberTag existing = repo.findByName(name);
        if (existing == null) return;
        if (id == null || !existing.id().equals(id)) throw exception(TAG_NAME_EXISTS);
    }

    private void validateTagHasUser(Long id) {
        if (userRepo.countByTagId(id) > 0) throw exception(TAG_HAS_USER);
    }
}
