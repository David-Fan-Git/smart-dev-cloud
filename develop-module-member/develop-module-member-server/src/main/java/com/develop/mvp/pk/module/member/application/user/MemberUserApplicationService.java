package com.develop.mvp.pk.module.member.application.user;

// Skill: AggregateRoot_MemberUser_Skill — 应用服务 MemberUserApplicationService
import cn.hutool.core.util.*;
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.member.domain.user.MemberUser;
import com.develop.mvp.pk.module.member.domain.user.MemberUserFactory;
import com.develop.mvp.pk.module.member.domain.user.repository.MemberUserRepository;
import com.develop.mvp.pk.module.member.domain.user.valueobject.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.develop.mvp.pk.module.member.enums.ErrorCodeConstants.*;

@Service
@RequiredArgsConstructor
public class MemberUserApplicationService {

    private final MemberUserRepository repo;
    private final MemberUserFactory factory;

    // ── 创建 ──
    @Transactional
    public MemberUser createIfAbsent(String mobile, String registerIp, Integer terminal) {
        Mobile m = new Mobile(mobile);
        Optional<MemberUser> existing = repo.findByMobile(m);
        if (existing.isPresent()) return existing.get();
        MemberUser u = factory.createQuick(mobile, registerIp, terminal);
        repo.save(u);
        return u;
    }

    @Transactional
    public MemberUser createUser(String nickname, String avatar, String registerIp, Integer terminal) {
        String mobile = IdUtil.fastSimpleUUID(); // 非手机注册时用UUID占位
        Mobile m = new Mobile(mobile);
        Nickname nn = new Nickname(StrUtil.isNotEmpty(nickname) ? nickname : "用户" + RandomUtil.randomNumbers(6));
        String pwd = IdUtil.fastSimpleUUID();
        MemberUser u = factory.create(nn, m, new RawPassword(pwd));
        u = repo.save(u);
        return u;
    }

    // ── 查询 ──
    public MemberUser get(Long id) {
        MemberUser u = repo.findById(id);
        if (u == null) throw com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception(USER_NOT_EXISTS);
        return u;
    }

    public MemberUser getByMobile(String mobile) {
        return repo.findByMobile(new Mobile(mobile)).orElse(null);
    }

    public List<MemberUser> getList(Collection<Long> ids) { return repo.findByIds(ids); }

    public List<MemberUser> getListByNickname(String nickname) { return repo.findByNicknameLike(nickname); }

    public PageResult<MemberUser> getPage(String nickname, String mobile, Integer status,
                                           Long levelId, Long groupId, List<Long> tagIds,
                                           String loginDateStart, String loginDateEnd,
                                           String createTimeStart, String createTimeEnd,
                                           Integer pageNo, Integer pageSize) {
        return repo.findPage(nickname, mobile != null ? new Mobile(mobile) : null, status,
                levelId, groupId, tagIds, loginDateStart, loginDateEnd, createTimeStart, createTimeEnd,
                pageNo, pageSize);
    }

    // ── 更新 ──
    @Transactional
    public void updateProfile(Long userId, String nickname, String avatar) {
        MemberUser u = get(userId);
        u.updateProfile(new Nickname(nickname), avatar);
        repo.save(u);
    }

    @Transactional
    public void updateMobile(Long userId, String newMobile) {
        validateMobileUnique(userId, newMobile);
        MemberUser u = get(userId);
        u.updateMobile(new Mobile(newMobile));
        repo.save(u);
    }

    @Transactional
    public void updatePassword(Long userId, String encodedPassword) {
        MemberUser u = get(userId);
        u.changePassword(new EncodedPassword(encodedPassword));
        repo.save(u);
    }

    @Transactional
    public void updateStatus(Long userId, Integer status) {
        MemberUser u = get(userId);
        if (CommonStatusEnum.ENABLE.getStatus().equals(status)) { u.enable(); }
        else { u.disable(); }
        repo.save(u);
    }

    @Transactional
    public void updateLevel(Long id, Long levelId, Integer experience) {
        MemberUser u = get(id);
        u.updateLevel(levelId, experience);
        repo.save(u);
    }

    @Transactional
    public boolean updatePoint(Long userId, Integer point) {
        if (point > 0) {
            repo.updatePointIncr(userId, point);
            return true;
        } else if (point < 0) {
            return repo.updatePointDecr(userId, point) > 0;
        }
        return true;
    }

    @Transactional
    public void recordLogin(Long id, String loginIp) {
        MemberUser u = get(id);
        u.recordLogin(loginIp);
        repo.save(u);
    }

    @Transactional
    public void delete(Long id) { get(id); repo.delete(id); }

    // ── 校验 ──
    public long countByGroupId(Long groupId) { return repo.countByGroupId(groupId); }
    public long countByLevelId(Long levelId) { return repo.countByLevelId(levelId); }
    public long countByTagId(Long tagId) { return repo.countByTagId(tagId); }
    public long count() { return repo.count(); }

    private void validateMobileUnique(Long userId, String mobile) {
        if (StrUtil.isBlank(mobile)) return;
        Optional<MemberUser> existing = repo.findByMobile(new Mobile(mobile));
        if (existing.isEmpty()) return;
        if (userId == null || !existing.get().id().equals(userId)) {
            throw com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception(USER_MOBILE_USED, mobile);
        }
    }
}
