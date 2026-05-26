package com.develop.mvp.pk.module.member.application.point;

// Skill: AggregateRoot_MemberPointRecord_Skill — 应用服务 MemberPointRecordApplicationService

import cn.hutool.core.util.StrUtil;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.member.domain.point.MemberPointRecord;
import com.develop.mvp.pk.module.member.domain.point.repository.MemberPointRecordRepository;
import com.develop.mvp.pk.module.member.domain.user.MemberUser;
import com.develop.mvp.pk.module.member.domain.user.repository.MemberUserRepository;
import com.develop.mvp.pk.module.member.enums.point.MemberPointBizTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.member.enums.ErrorCodeConstants.USER_POINT_NOT_ENOUGH;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberPointRecordApplicationService {

    private final MemberPointRecordRepository repo;
    private final MemberUserRepository userRepo;

    public PageResult<MemberPointRecord> getPointRecordPage(String nickname, Long userId, Integer bizType,
                                                            String title, Integer pageNo, Integer pageSize) {
        return repo.findPage(nickname, userId, bizType, title, pageNo, pageSize);
    }

    public PageResult<MemberPointRecord> getPointRecordPage(Long userId, LocalDateTime createTimeStart, LocalDateTime createTimeEnd,
                                                             Boolean addStatus, Integer pageNo, Integer pageSize) {
        return repo.findPageByUser(userId, createTimeStart, createTimeEnd, addStatus, pageNo, pageSize);
    }

    @Transactional
    public void createPointRecord(Long userId, Integer point, MemberPointBizTypeEnum bizType, String bizId) {
        if (point == 0) return;

        MemberUser user = userRepo.findById(userId);
        if (user == null) return;
        int userPoint = user.point() != null ? user.point() : 0;
        int totalPoint = userPoint + point;
        if (totalPoint < 0) {
            log.error("[createPointRecord][userId({}) point({}) bizType({}) bizId({}) {}]", userId, point, bizType, bizId,
                    USER_POINT_NOT_ENOUGH);
            return;
        }

        // 更新用户积分
        boolean success;
        if (point > 0) {
            userRepo.updatePointIncr(userId, point);
            success = true;
        } else {
            success = userRepo.updatePointDecr(userId, point) > 0;
        }
        if (!success) throw exception(USER_POINT_NOT_ENOUGH);

        // 创建记录
        MemberPointRecord record = MemberPointRecord.create(userId, point, bizId, bizType.getType(),
                bizType.getName(), StrUtil.format(bizType.getDescription(), point), totalPoint);
        repo.save(record);
    }
}
