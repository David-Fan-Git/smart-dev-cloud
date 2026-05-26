package com.develop.mvp.pk.module.member.convert.level;

import com.develop.mvp.pk.module.member.api.level.dto.MemberLevelRespDTO;
import com.develop.mvp.pk.module.member.controller.admin.level.vo.level.MemberLevelCreateReqVO;
import com.develop.mvp.pk.module.member.controller.admin.level.vo.level.MemberLevelRespVO;
import com.develop.mvp.pk.module.member.controller.admin.level.vo.level.MemberLevelSimpleRespVO;
import com.develop.mvp.pk.module.member.controller.admin.level.vo.level.MemberLevelUpdateReqVO;
import com.develop.mvp.pk.module.member.controller.app.level.vo.level.AppMemberLevelRespVO;
import com.develop.mvp.pk.module.member.dal.dataobject.level.MemberLevelDO;
import com.develop.mvp.pk.module.member.domain.level.MemberLevel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 会员等级 Convert
 *
 * @author David
 */
@Mapper
public interface MemberLevelConvert {

    MemberLevelConvert INSTANCE = Mappers.getMapper(MemberLevelConvert.class);

    MemberLevelDO convert(MemberLevelCreateReqVO bean);

    MemberLevelDO convert(MemberLevelUpdateReqVO bean);

    MemberLevelRespVO convert(MemberLevelDO bean);

    List<MemberLevelRespVO> convertList(List<MemberLevelDO> list);

    List<MemberLevelSimpleRespVO> convertSimpleList(List<MemberLevelDO> list);

    List<AppMemberLevelRespVO> convertList02(List<MemberLevelDO> list);

    MemberLevelRespDTO convert02(MemberLevelDO bean);

    // ── Domain object conversions ──

    default MemberLevelRespVO convert(MemberLevel bean) {
        if (bean == null) return null;
        MemberLevelRespVO vo = new MemberLevelRespVO();
        vo.setId(bean.id());
        vo.setName(bean.name());
        vo.setLevel(bean.level());
        vo.setExperience(bean.experience());
        vo.setDiscountPercent(bean.discountPercent());
        vo.setIcon(bean.icon());
        vo.setBackgroundUrl(bean.backgroundUrl());
        vo.setStatus(bean.status());
        return vo;
    }

    default List<MemberLevelRespVO> convertListFromDomain(List<MemberLevel> list) {
        if (list == null) return null;
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    default List<MemberLevelSimpleRespVO> convertSimpleListFromDomain(List<MemberLevel> list) {
        if (list == null) return null;
        return list.stream().map(l -> {
            MemberLevelSimpleRespVO vo = new MemberLevelSimpleRespVO();
            vo.setId(l.id());
            vo.setName(l.name());
            vo.setIcon(l.icon());
            return vo;
        }).collect(Collectors.toList());
    }

    default List<AppMemberLevelRespVO> convertList02FromDomain(List<MemberLevel> list) {
        if (list == null) return null;
        return list.stream().map(l -> {
            AppMemberLevelRespVO vo = new AppMemberLevelRespVO();
            vo.setName(l.name());
            vo.setLevel(l.level());
            vo.setExperience(l.experience());
            vo.setDiscountPercent(l.discountPercent());
            vo.setIcon(l.icon());
            vo.setBackgroundUrl(l.backgroundUrl());
            return vo;
        }).collect(Collectors.toList());
    }
}
