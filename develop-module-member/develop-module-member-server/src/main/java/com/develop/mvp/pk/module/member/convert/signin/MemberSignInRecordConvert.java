package com.develop.mvp.pk.module.member.convert.signin;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.collection.MapUtils;
import com.develop.mvp.pk.framework.common.util.date.DateUtils;
import com.develop.mvp.pk.module.member.controller.admin.signin.vo.record.MemberSignInRecordRespVO;
import com.develop.mvp.pk.module.member.controller.app.signin.vo.record.AppMemberSignInRecordRespVO;
import com.develop.mvp.pk.module.member.dal.dataobject.signin.MemberSignInConfigDO;
import com.develop.mvp.pk.module.member.dal.dataobject.signin.MemberSignInRecordDO;
import com.develop.mvp.pk.module.member.dal.dataobject.user.MemberUserDO;
import com.develop.mvp.pk.module.member.domain.signin.MemberSignInRecord;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 签到记录 Convert
 *
 * @author David
 */
@Mapper
public interface MemberSignInRecordConvert {

    MemberSignInRecordConvert INSTANCE = Mappers.getMapper(MemberSignInRecordConvert.class);

    default PageResult<MemberSignInRecordRespVO> convertPage(PageResult<MemberSignInRecordDO> pageResult, List<MemberUserDO> users) {
        PageResult<MemberSignInRecordRespVO> voPageResult = convertPage(pageResult);
        Map<Long, MemberUserDO> userMap = convertMap(users, MemberUserDO::getId);
        voPageResult.getList().forEach(record -> MapUtils.findAndThen(userMap, record.getUserId(),
                memberUserRespDTO -> record.setNickname(memberUserRespDTO.getNickname())));
        return voPageResult;
    }

    PageResult<MemberSignInRecordRespVO> convertPage(PageResult<MemberSignInRecordDO> pageResult);

    PageResult<AppMemberSignInRecordRespVO> convertPage02(PageResult<MemberSignInRecordDO> pageResult);

    AppMemberSignInRecordRespVO coverRecordToAppRecordVo(MemberSignInRecordDO memberSignInRecordDO);

    default MemberSignInRecordDO convert(Long userId, MemberSignInRecordDO lastRecord, List<MemberSignInConfigDO> configs) {
        configs.sort(Comparator.comparing(MemberSignInConfigDO::getDay));
        MemberSignInConfigDO lastConfig = CollUtil.getLast(configs);
        int day = 1;
        if (lastRecord != null && DateUtils.isYesterday(lastRecord.getCreateTime())) {
            day = lastRecord.getDay() + 1;
        }
        if (day > lastConfig.getDay()) {
            day = 1;
        }
        MemberSignInRecordDO record = new MemberSignInRecordDO().setUserId(userId)
                .setDay(day).setPoint(0).setExperience(0);
        MemberSignInConfigDO config = CollUtil.findOne(configs, item -> ObjUtil.equal(item.getDay(), record.getDay()));
        if (config == null) return record;
        record.setPoint(config.getPoint());
        record.setExperience(config.getExperience());
        return record;
    }

    // ── Domain object conversions ──

    default PageResult<MemberSignInRecordRespVO> convertPageFromDomain(PageResult<MemberSignInRecord> pageResult, List<MemberUserDO> users) {
        List<MemberSignInRecordRespVO> list = pageResult.getList().stream().map(r -> {
            MemberSignInRecordRespVO vo = new MemberSignInRecordRespVO();
            vo.setId(r.id());
            vo.setUserId(r.userId());
            vo.setDay(r.day());
            vo.setPoint(r.point());
            vo.setCreateTime(r.createTime());
            return vo;
        }).collect(Collectors.toList());
        PageResult<MemberSignInRecordRespVO> result = new PageResult<>(list, pageResult.getTotal());
        Map<Long, MemberUserDO> userMap = convertMap(users, MemberUserDO::getId);
        result.getList().forEach(record -> MapUtils.findAndThen(userMap, record.getUserId(),
                memberUserRespDTO -> record.setNickname(memberUserRespDTO.getNickname())));
        return result;
    }

    default AppMemberSignInRecordRespVO coverRecordToAppRecordVo(MemberSignInRecord record) {
        if (record == null) return null;
        AppMemberSignInRecordRespVO vo = new AppMemberSignInRecordRespVO();
        vo.setDay(record.day());
        vo.setPoint(record.point());
        vo.setExperience(record.experience());
        vo.setCreateTime(record.createTime());
        return vo;
    }

    default PageResult<AppMemberSignInRecordRespVO> convertPage02FromDomain(PageResult<MemberSignInRecord> pageResult) {
        if (pageResult == null) return null;
        List<AppMemberSignInRecordRespVO> list = pageResult.getList().stream().map(r -> {
            AppMemberSignInRecordRespVO vo = new AppMemberSignInRecordRespVO();
            vo.setDay(r.day());
            vo.setPoint(r.point());
            vo.setExperience(r.experience());
            vo.setCreateTime(r.createTime());
            return vo;
        }).collect(Collectors.toList());
        return new PageResult<>(list, pageResult.getTotal());
    }
}
