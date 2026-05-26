package com.develop.mvp.pk.module.member.application.signin;

// Skill: AggregateRoot_MemberSignInRecord_Skill — 应用服务 MemberSignInRecordApplicationService

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.date.DateUtils;
import com.develop.mvp.pk.framework.common.util.object.ObjectUtils;
import com.develop.mvp.pk.module.member.application.level.MemberLevelApplicationService;
import com.develop.mvp.pk.module.member.application.point.MemberPointRecordApplicationService;
import com.develop.mvp.pk.module.member.controller.app.signin.vo.record.AppMemberSignInRecordSummaryRespVO;
import com.develop.mvp.pk.module.member.domain.signin.MemberSignInConfig;
import com.develop.mvp.pk.module.member.domain.signin.MemberSignInRecord;
import com.develop.mvp.pk.module.member.domain.signin.repository.MemberSignInConfigRepository;
import com.develop.mvp.pk.module.member.domain.signin.repository.MemberSignInRecordRepository;
import com.develop.mvp.pk.module.member.enums.MemberExperienceBizTypeEnum;
import com.develop.mvp.pk.module.member.enums.point.MemberPointBizTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.member.enums.ErrorCodeConstants.SIGN_IN_RECORD_TODAY_EXISTS;

@Service
@RequiredArgsConstructor
public class MemberSignInRecordApplicationService {

    private final MemberSignInRecordRepository repo;
    private final MemberSignInConfigRepository configRepo;
    private final MemberPointRecordApplicationService pointRecordService;
    private final MemberLevelApplicationService levelService;

    public AppMemberSignInRecordSummaryRespVO getSignInRecordSummary(Long userId) {
        AppMemberSignInRecordSummaryRespVO summary = new AppMemberSignInRecordSummaryRespVO();
        summary.setTotalDay(0);
        summary.setContinuousDay(0);
        summary.setTodaySignIn(false);

        Long signCount = repo.countByUserId(userId);
        if (ObjUtil.equal(signCount, 0L)) return summary;
        summary.setTotalDay(signCount.intValue());

        MemberSignInRecord lastRecord = repo.findLastByUserId(userId);
        if (lastRecord == null) return summary;
        summary.setTodaySignIn(DateUtils.isToday(lastRecord.createTime()));

        if (!summary.getTodaySignIn() && !DateUtils.isYesterday(lastRecord.createTime())) return summary;
        summary.setContinuousDay(lastRecord.day());
        return summary;
    }

    public PageResult<MemberSignInRecord> getSignInRecordPage(String nickname, Long userId, Integer day,
                                                               String createTimeStart, String createTimeEnd,
                                                               Integer pageNo, Integer pageSize) {
        return repo.findPage(nickname, userId, day, createTimeStart, createTimeEnd, pageNo, pageSize);
    }

    public PageResult<MemberSignInRecord> getSignRecordPage(Long userId, Integer pageNo, Integer pageSize) {
        return repo.findPageByUser(userId, pageNo, pageSize);
    }

    @Transactional
    public MemberSignInRecord createSignRecord(Long userId) {
        MemberSignInRecord lastRecord = repo.findLastByUserId(userId);
        validateSigned(lastRecord);

        List<MemberSignInConfig> configs = configRepo.findByStatus(CommonStatusEnum.ENABLE.getStatus());
        MemberSignInRecord record = buildRecord(userId, lastRecord, configs);
        record = repo.save(record);

        // 增加积分
        if (!ObjectUtils.equalsAny(record.point(), null, 0)) {
            pointRecordService.createPointRecord(userId, record.point(),
                    MemberPointBizTypeEnum.SIGN, String.valueOf(record.id()));
        }
        // 增加经验
        if (!ObjectUtils.equalsAny(record.experience(), null, 0)) {
            levelService.addExperience(userId, record.experience(),
                    MemberExperienceBizTypeEnum.SIGN_IN, String.valueOf(record.id()));
        }
        return record;
    }

    private MemberSignInRecord buildRecord(Long userId, MemberSignInRecord lastRecord, List<MemberSignInConfig> configs) {
        int day = 1;
        if (lastRecord != null && DateUtils.isYesterday(lastRecord.createTime())) {
            day = lastRecord.day() + 1;
        }
        final int finalDay = day;
        MemberSignInConfig matched = configs.stream()
                .filter(c -> c.day().equals(finalDay))
                .findFirst().orElse(null);
        int point = matched != null && matched.point() != null ? matched.point() : 0;
        int experience = matched != null && matched.experience() != null ? matched.experience() : 0;
        return MemberSignInRecord.create(userId, day, point, experience);
    }

    private void validateSigned(MemberSignInRecord lastRecord) {
        if (lastRecord != null && DateUtils.isToday(lastRecord.createTime())) {
            throw exception(SIGN_IN_RECORD_TODAY_EXISTS);
        }
    }
}
