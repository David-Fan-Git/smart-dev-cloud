package com.develop.mvp.pk.module.member.infrastructure.point;

// Skill: AggregateRoot_MemberPointRecord_Skill — 仓储实现 MemberPointRecordRepositoryImpl

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.member.controller.admin.point.vo.recrod.MemberPointRecordPageReqVO;
import com.develop.mvp.pk.module.member.controller.app.point.vo.AppMemberPointRecordPageReqVO;
import com.develop.mvp.pk.module.member.dal.dataobject.point.MemberPointRecordDO;
import com.develop.mvp.pk.module.member.dal.mysql.point.MemberPointRecordMapper;
import com.develop.mvp.pk.module.member.dal.dataobject.user.MemberUserDO;
import com.develop.mvp.pk.module.member.dal.mysql.user.MemberUserMapper;
import com.develop.mvp.pk.module.member.domain.point.MemberPointRecord;
import com.develop.mvp.pk.module.member.domain.point.repository.MemberPointRecordRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertSet;

@Repository
public class MemberPointRecordRepositoryImpl implements MemberPointRecordRepository {

    private final MemberPointRecordMapper mapper;
    private final MemberUserMapper userMapper;
    public MemberPointRecordRepositoryImpl(MemberPointRecordMapper mapper, MemberUserMapper userMapper) {
        this.mapper = mapper;
        this.userMapper = userMapper;
    }

    @Override @Transactional
    public MemberPointRecord save(MemberPointRecord r) {
        MemberPointRecordDO d = toDO(r);
        if (r.id() == null) { mapper.insert(d); }
        return fromDO(d);
    }

    @Override
    public PageResult<MemberPointRecord> findPage(String nickname, Long userId, Integer bizType, String title,
                                                   Integer pageNo, Integer pageSize) {
        Set<Long> userIds = null;
        if (StringUtils.isNotBlank(nickname)) {
            List<MemberUserDO> users = userMapper.selectListByNicknameLike(nickname);
            if (CollectionUtils.isEmpty(users)) return PageResult.empty();
            userIds = convertSet(users, MemberUserDO::getId);
        }
        var reqVO = new MemberPointRecordPageReqVO().setUserId(userId).setBizType(bizType).setTitle(title);
        PageResult<MemberPointRecordDO> result = mapper.selectPage(reqVO, userIds);
        return new PageResult<>(result.getList().stream().map(this::fromDO).collect(Collectors.toList()), result.getTotal());
    }

    @Override
    public PageResult<MemberPointRecord> findPageByUser(Long userId, LocalDateTime createTimeStart, LocalDateTime createTimeEnd,
                                                         Boolean addStatus, Integer pageNo, Integer pageSize) {
        var reqVO = new AppMemberPointRecordPageReqVO()
                .setCreateTime(new LocalDateTime[]{createTimeStart, createTimeEnd})
                .setAddStatus(addStatus);
        PageResult<MemberPointRecordDO> result = mapper.selectPage(userId, reqVO);
        return new PageResult<>(result.getList().stream().map(this::fromDO).collect(Collectors.toList()), result.getTotal());
    }

    // ── DO ↔ Domain 映射 ──
    private MemberPointRecordDO toDO(MemberPointRecord r) {
        return MemberPointRecordDO.builder()
                .id(r.id()).userId(r.userId()).bizId(r.bizId()).bizType(r.bizType())
                .title(r.title()).description(r.description()).point(r.point()).totalPoint(r.totalPoint())
                .build();
    }

    private MemberPointRecord fromDO(MemberPointRecordDO d) {
        if (d == null) return null;
        return MemberPointRecord.reconstitute(d.getId(), d.getUserId(), d.getBizId(), d.getBizType(),
                d.getTitle(), d.getDescription(), d.getPoint(), d.getTotalPoint(), d.getCreateTime());
    }
}
