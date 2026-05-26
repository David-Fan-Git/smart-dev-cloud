package com.develop.mvp.pk.module.member.infrastructure.user;

// Skill: AggregateRoot_MemberUser_Skill — 仓储实现 MemberUserRepositoryImpl
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.member.dal.dataobject.user.MemberUserDO;
import com.develop.mvp.pk.module.member.dal.mysql.user.MemberUserMapper;
import com.develop.mvp.pk.module.member.domain.user.MemberUser;
import com.develop.mvp.pk.module.member.domain.user.repository.MemberUserRepository;
import com.develop.mvp.pk.module.member.domain.user.valueobject.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class MemberUserRepositoryImpl implements MemberUserRepository {

    private final MemberUserMapper mapper;
    public MemberUserRepositoryImpl(MemberUserMapper mapper) { this.mapper = mapper; }

    @Override @Transactional
    public MemberUser save(MemberUser u) {
        MemberUserDO d = toDO(u);
        if (u.id() == null) { mapper.insert(d); } else { mapper.updateById(d); }
        return fromDO(d);
    }

    @Override @Transactional
    public void delete(Long id) { mapper.deleteById(id); }

    @Override
    public MemberUser findById(Long id) { return fromDO(mapper.selectById(id)); }

    @Override
    public Optional<MemberUser> findByMobile(Mobile mobile) {
        return Optional.ofNullable(fromDO(mapper.selectByMobile(mobile.value())));
    }

    @Override
    public List<MemberUser> findByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) return ListUtil.empty();
        return mapper.selectByIds(ids).stream().map(this::fromDO).collect(Collectors.toList());
    }

    @Override
    public List<MemberUser> findByNicknameLike(String keyword) {
        return mapper.selectListByNicknameLike(keyword).stream().map(this::fromDO).collect(Collectors.toList());
    }

    @Override
    public List<MemberUser> findByStatus(Integer status) {
        return mapper.selectList(MemberUserDO::getStatus, status).stream().map(this::fromDO).collect(Collectors.toList());
    }

    @Override
    public PageResult<MemberUser> findPage(String nickname, Mobile mobile, Integer status, Long levelId, Long groupId,
                                            List<Long> tagIds, String loginDateStart, String loginDateEnd,
                                            String createTimeStart, String createTimeEnd, Integer pageNo, Integer pageSize) {
        String tagIdSql = "";
        if (CollUtil.isNotEmpty(tagIds)) {
            tagIdSql = tagIds.stream().map(tid -> "FIND_IN_SET(" + tid + ", tag_ids)").collect(Collectors.joining(" OR "));
        }
        var wrapper = new LambdaQueryWrapperX<MemberUserDO>()
                .likeIfPresent(MemberUserDO::getMobile, mobile != null ? mobile.value() : null)
                .likeIfPresent(MemberUserDO::getNickname, nickname)
                .eqIfPresent(MemberUserDO::getStatus, status)
                .eqIfPresent(MemberUserDO::getLevelId, levelId)
                .eqIfPresent(MemberUserDO::getGroupId, groupId)
                .apply(StrUtil.isNotEmpty(tagIdSql), tagIdSql)
                .orderByDesc(MemberUserDO::getId);
        PageResult<MemberUserDO> result = mapper.selectPage(
                new com.develop.mvp.pk.framework.common.pojo.PageParam().setPageNo(pageNo).setPageSize(pageSize), wrapper);
        return new PageResult<>(result.getList().stream().map(this::fromDO).collect(Collectors.toList()), result.getTotal());
    }

    @Override
    public boolean existsByMobile(Mobile mobile) {
        return mapper.selectByMobile(mobile.value()) != null;
    }

    @Override public long count() { return mapper.selectCount(); }
    @Override public long countByGroupId(Long groupId) { return mapper.selectCountByGroupId(groupId); }
    @Override public long countByLevelId(Long levelId) { return mapper.selectCountByLevelId(levelId); }
    @Override public long countByTagId(Long tagId) { return mapper.selectCountByTagId(tagId); }
    @Override public int updatePointIncr(Long id, Integer incrCount) { mapper.updatePointIncr(id, incrCount); return 1; }
    @Override public int updatePointDecr(Long id, Integer decrCount) { return mapper.updatePointDecr(id, decrCount); }

    // ── DO ↔ Domain 映射 ──
    private MemberUserDO toDO(MemberUser u) {
        return MemberUserDO.builder()
                .id(u.id()).mobile(u.mobile() != null ? u.mobile().value() : null)
                .password(u.password() != null ? u.password().value() : null)
                .status(u.status() != null ? u.status().code() : null)
                .nickname(u.nickname() != null ? u.nickname().value() : null)
                .avatar(u.avatar())
                .loginIp(u.loginIp()).loginDate(u.loginDate())
                .registerIp(u.registerIp()).registerTerminal(u.registerTerminal())
                .levelId(u.levelId()).experience(u.experience()).point(u.point())
                .groupId(u.groupId()).tagIds(u.tagIds())
                .build();
    }

    private MemberUser fromDO(MemberUserDO d) {
        if (d == null) return null;
        return MemberUser.reconstitute(d.getId(),
                new Nickname(d.getNickname() != null ? d.getNickname() : ""),
                d.getMobile() != null ? new Mobile(d.getMobile()) : null,
                d.getPassword() != null ? new EncodedPassword(d.getPassword()) : null,
                d.getStatus() != null ? new UserStatus(d.getStatus()) : UserStatus.enabled(),
                null, d.getAvatar(), d.getTenantId(),
                d.getLoginIp(), d.getLoginDate(), d.getRegisterIp(), d.getRegisterTerminal(),
                d.getLevelId(), d.getExperience(), d.getPoint(), d.getGroupId(), d.getTagIds());
    }
}
