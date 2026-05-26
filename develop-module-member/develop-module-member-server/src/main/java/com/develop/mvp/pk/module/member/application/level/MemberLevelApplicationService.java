package com.develop.mvp.pk.module.member.application.level;

// Skill: AggregateRoot_MemberLevel_Skill — 应用服务 MemberLevelApplicationService

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.module.member.dal.dataobject.level.MemberExperienceRecordDO;
import com.develop.mvp.pk.module.member.dal.dataobject.level.MemberLevelRecordDO;
import com.develop.mvp.pk.module.member.dal.mysql.level.MemberExperienceRecordMapper;
import com.develop.mvp.pk.module.member.dal.mysql.level.MemberLevelRecordMapper;
import com.develop.mvp.pk.module.member.domain.level.MemberLevel;
import com.develop.mvp.pk.module.member.domain.level.repository.MemberLevelRepository;
import com.develop.mvp.pk.module.member.domain.user.MemberUser;
import com.develop.mvp.pk.module.member.domain.user.repository.MemberUserRepository;
import com.develop.mvp.pk.module.member.enums.MemberExperienceBizTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.member.enums.ErrorCodeConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberLevelApplicationService {

    private final MemberLevelRepository repo;
    private final MemberUserRepository userRepo;
    private final MemberExperienceRecordMapper experienceRecordMapper;
    private final MemberLevelRecordMapper levelRecordMapper;

    // ── 等级 CRUD ──
    @Transactional
    public Long createLevel(String name, Integer level, Integer experience, Integer discountPercent,
                             String icon, String backgroundUrl, Integer status) {
        List<MemberLevel> all = repo.findAll();
        validateConfigValid(all, null, name, level, experience);
        MemberLevel l = MemberLevel.create(name);
        l.updateConfig(name, level, experience, discountPercent, icon, backgroundUrl, status);
        l = repo.save(l);
        return l.id();
    }

    @Transactional
    public void updateLevel(Long id, String name, Integer level, Integer experience, Integer discountPercent,
                             String icon, String backgroundUrl, Integer status) {
        validateLevelExists(id);
        List<MemberLevel> all = repo.findAll();
        validateConfigValid(all, id, name, level, experience);
        MemberLevel l = get(id);
        l.updateConfig(name, level, experience, discountPercent, icon, backgroundUrl, status);
        repo.save(l);
    }

    @Transactional
    public void deleteLevel(Long id) {
        validateLevelExists(id);
        validateLevelHasUser(id);
        repo.delete(id);
    }

    public MemberLevel get(Long id) {
        if (id == null || id <= 0) return null;
        MemberLevel l = repo.findById(id);
        if (l == null) throw exception(LEVEL_NOT_EXISTS);
        return l;
    }

    public List<MemberLevel> getList(Collection<Long> ids) { return repo.findByIds(ids); }

    public List<MemberLevel> getList(String name, Integer status) {
        if (status != null || name != null) {
            return repo.findAll().stream()
                    .filter(l -> name == null || l.name().contains(name))
                    .filter(l -> status == null || status.equals(l.status()))
                    .toList();
        }
        return repo.findAll();
    }

    public List<MemberLevel> getListByStatus(Integer status) { return repo.findByStatus(status); }

    public List<MemberLevel> getEnableList() { return repo.findByStatus(CommonStatusEnum.ENABLE.getStatus()); }

    // ── 会员等级变更 ──
    @Transactional
    public void updateUserLevel(Long userId, Long newLevelId, String reason) {
        MemberUser user = userRepo.findById(userId);
        if (user == null) throw exception(USER_NOT_EXISTS);
        if (ObjUtil.equal(user.levelId(), newLevelId)) return;

        // 1. 记录等级变动
        MemberLevelRecordDO levelRecord = new MemberLevelRecordDO()
                .setUserId(userId).setRemark(reason);
        MemberLevel memberLevel = null;
        if (newLevelId == null) {
            levelRecord.setExperience(-user.experience());
            levelRecord.setUserExperience(0);
            levelRecord.setDescription("管理员取消了等级");
        } else {
            memberLevel = validateLevelExists(newLevelId);
            copyTo(memberLevel, levelRecord);
            levelRecord.setExperience(memberLevel.experience() - user.experience());
            levelRecord.setUserExperience(memberLevel.experience());
            levelRecord.setDescription("管理员调整为：" + memberLevel.name());
        }
        levelRecordMapper.insert(levelRecord);

        // 2. 记录经验变动
        createExperienceRecord(userId, levelRecord.getExperience(), levelRecord.getUserExperience(),
                MemberExperienceBizTypeEnum.ADMIN, String.valueOf(MemberExperienceBizTypeEnum.ADMIN.getType()));

        // 3. 更新会员等级和经验
        user.updateLevel(newLevelId, levelRecord.getUserExperience());
        userRepo.save(user);

        // 4. 通知
        notifyLevelChange(userId, memberLevel);
    }

    @Transactional
    public void addExperience(Long userId, Integer experience, MemberExperienceBizTypeEnum bizType, String bizId) {
        if (experience == 0) return;
        if (!bizType.isAdd() && experience > 0) experience = -experience;

        MemberUser user = userRepo.findById(userId);
        if (user == null) return;
        int userExperience = NumberUtil.max(user.experience() + experience, 0);

        // 1. 创建经验记录
        MemberLevelRecordDO levelRecord = new MemberLevelRecordDO()
                .setUserId(userId).setExperience(experience).setUserExperience(userExperience)
                .setLevelId(user.levelId());
        createExperienceRecord(userId, experience, userExperience, bizType, bizId);

        // 2. 等级变更
        MemberLevel newLevel = calculateNewLevel(userExperience);
        if (newLevel != null) {
            copyTo(newLevel, levelRecord);
            levelRecordMapper.insert(levelRecord);
            notifyLevelChange(userId, newLevel);
        }

        // 3. 更新会员
        user.updateLevel(levelRecord.getLevelId(), userExperience);
        userRepo.save(user);
    }

    private MemberLevel calculateNewLevel(int userExperience) {
        List<MemberLevel> list = getEnableList();
        if (CollUtil.isEmpty(list)) {
            log.warn("计算会员等级失败：会员等级配置不存在");
            return null;
        }
        return list.stream()
                .filter(l -> userExperience >= l.experience())
                .max(Comparator.comparing(MemberLevel::level))
                .orElse(null);
    }

    private void createExperienceRecord(Long userId, Integer exp, Integer totalExp,
                                         MemberExperienceBizTypeEnum bizType, String bizId) {
        String description = cn.hutool.core.util.StrUtil.format(bizType.getDescription(), exp);
        MemberExperienceRecordDO record = new MemberExperienceRecordDO();
        record.setUserId(userId);
        record.setExperience(exp);
        record.setTotalExperience(totalExp);
        record.setBizId(bizId);
        record.setBizType(bizType.getType());
        record.setTitle(bizType.getTitle());
        record.setDescription(description);
        experienceRecordMapper.insert(record);
    }

    private void notifyLevelChange(Long userId, MemberLevel level) {
        // todo: 给会员发消息
    }

    // ── 校验 ──
    public MemberLevel validateLevelExists(Long id) {
        MemberLevel l = repo.findById(id);
        if (l == null) throw exception(LEVEL_NOT_EXISTS);
        return l;
    }

    void validateLevelHasUser(Long id) {
        // 通过 userRepo 查询关联
        if (userRepo.countByLevelId(id) > 0) {
            throw exception(LEVEL_HAS_USER);
        }
    }

    void validateConfigValid(List<MemberLevel> all, Long id, String name, Integer level, Integer experience) {
        validateNameUnique(all, id, name);
        validateLevelUnique(all, id, level);
        validateExperienceOutRange(all, id, level, experience);
    }

    private void validateNameUnique(List<MemberLevel> list, Long id, String name) {
        for (MemberLevel l : list) {
            if (ObjUtil.notEqual(l.name(), name)) continue;
            if (id == null || !id.equals(l.id())) throw exception(LEVEL_NAME_EXISTS, l.name());
        }
    }

    private void validateLevelUnique(List<MemberLevel> list, Long id, Integer level) {
        for (MemberLevel l : list) {
            if (ObjUtil.notEqual(l.level(), level)) continue;
            if (id == null || !id.equals(l.id())) throw exception(LEVEL_VALUE_EXISTS, l.level(), l.name());
        }
    }

    private void validateExperienceOutRange(List<MemberLevel> list, Long id, Integer level, Integer experience) {
        for (MemberLevel l : list) {
            if (l.id().equals(id)) continue;
            if (l.level() < level && experience <= l.experience()) {
                throw exception(LEVEL_EXPERIENCE_MIN, l.name(), l.experience());
            }
            if (l.level() > level && experience >= l.experience()) {
                throw exception(LEVEL_EXPERIENCE_MAX, l.name(), l.experience());
            }
        }
    }

    private void copyTo(MemberLevel src, MemberLevelRecordDO dest) {
        dest.setLevelId(src.id());
        dest.setLevel(src.level());
        dest.setDiscountPercent(src.discountPercent());
        dest.setExperience(src.experience());
    }
}
