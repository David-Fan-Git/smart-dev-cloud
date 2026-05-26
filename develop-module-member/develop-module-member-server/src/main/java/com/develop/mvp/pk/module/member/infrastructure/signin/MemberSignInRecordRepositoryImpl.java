package com.develop.mvp.pk.module.member.infrastructure.signin;

// Skill: AggregateRoot_MemberSignInRecord_Skill — 仓储实现 MemberSignInRecordRepositoryImpl

import cn.hutool.core.collection.CollUtil;
import com.develop.mvp.pk.framework.common.pojo.PageParam;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.member.controller.admin.signin.vo.record.MemberSignInRecordPageReqVO;
import com.develop.mvp.pk.module.member.dal.dataobject.signin.MemberSignInRecordDO;
import com.develop.mvp.pk.module.member.dal.dataobject.user.MemberUserDO;
import com.develop.mvp.pk.module.member.dal.mysql.signin.MemberSignInRecordMapper;
import com.develop.mvp.pk.module.member.dal.mysql.user.MemberUserMapper;
import com.develop.mvp.pk.module.member.domain.signin.MemberSignInRecord;
import com.develop.mvp.pk.module.member.domain.signin.repository.MemberSignInRecordRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertSet;

@Repository
public class MemberSignInRecordRepositoryImpl implements MemberSignInRecordRepository {

    private final MemberSignInRecordMapper mapper;
    private final MemberUserMapper userMapper;
    public MemberSignInRecordRepositoryImpl(MemberSignInRecordMapper mapper, MemberUserMapper userMapper) {
        this.mapper = mapper;
        this.userMapper = userMapper;
    }

    @Override @Transactional
    public MemberSignInRecord save(MemberSignInRecord r) {
        MemberSignInRecordDO d = toDO(r);
        if (r.id() == null) { mapper.insert(d); }
        return fromDO(d);
    }

    @Override
    public PageResult<MemberSignInRecord> findPage(String nickname, Long userId, Integer day,
                                                    String createTimeStart, String createTimeEnd,
                                                    Integer pageNo, Integer pageSize) {
        Set<Long> userIds = null;
        if (StringUtils.isNotBlank(nickname)) {
            List<MemberUserDO> users = userMapper.selectListByNicknameLike(nickname);
            if (CollUtil.isEmpty(users)) return PageResult.empty();
            userIds = convertSet(users, MemberUserDO::getId);
        }
        var reqVO = new MemberSignInRecordPageReqVO().setUserId(userId).setDay(day);
        PageResult<MemberSignInRecordDO> result = mapper.selectPage(reqVO, userIds);
        return new PageResult<>(result.getList().stream().map(this::fromDO).collect(Collectors.toList()), result.getTotal());
    }

    @Override
    public PageResult<MemberSignInRecord> findPageByUser(Long userId, Integer pageNo, Integer pageSize) {
        var pageParam = new PageParam().setPageNo(pageNo).setPageSize(pageSize);
        PageResult<MemberSignInRecordDO> result = mapper.selectPage(userId, pageParam);
        return new PageResult<>(result.getList().stream().map(this::fromDO).collect(Collectors.toList()), result.getTotal());
    }

    @Override
    public MemberSignInRecord findLastByUserId(Long userId) {
        return fromDO(mapper.selectLastRecordByUserId(userId));
    }

    @Override
    public Long countByUserId(Long userId) { return mapper.selectCountByUserId(userId); }

    @Override
    public List<MemberSignInRecord> findByUserId(Long userId) {
        return mapper.selectListByUserId(userId).stream().map(this::fromDO).collect(Collectors.toList());
    }

    // ── DO ↔ Domain 映射 ──
    private MemberSignInRecordDO toDO(MemberSignInRecord r) {
        return MemberSignInRecordDO.builder()
                .id(r.id()).userId(r.userId()).day(r.day()).point(r.point()).experience(r.experience())
                .build();
    }

    private MemberSignInRecord fromDO(MemberSignInRecordDO d) {
        if (d == null) return null;
        return MemberSignInRecord.reconstitute(d.getId(), d.getUserId(), d.getDay(), d.getPoint(),
                d.getExperience(), d.getCreateTime());
    }
}
