package com.develop.mvp.pk.module.member.convert.signin;

import com.develop.mvp.pk.module.member.controller.admin.signin.vo.config.MemberSignInConfigCreateReqVO;
import com.develop.mvp.pk.module.member.controller.admin.signin.vo.config.MemberSignInConfigRespVO;
import com.develop.mvp.pk.module.member.controller.admin.signin.vo.config.MemberSignInConfigUpdateReqVO;
import com.develop.mvp.pk.module.member.controller.app.signin.vo.config.AppMemberSignInConfigRespVO;
import com.develop.mvp.pk.module.member.dal.dataobject.signin.MemberSignInConfigDO;
import com.develop.mvp.pk.module.member.domain.signin.MemberSignInConfig;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 签到规则 Convert
 *
 * @author David
 */
@Mapper
public interface MemberSignInConfigConvert {

    MemberSignInConfigConvert INSTANCE = Mappers.getMapper(MemberSignInConfigConvert.class);

    MemberSignInConfigDO convert(MemberSignInConfigCreateReqVO bean);

    MemberSignInConfigDO convert(MemberSignInConfigUpdateReqVO bean);

    MemberSignInConfigRespVO convert(MemberSignInConfigDO bean);

    List<MemberSignInConfigRespVO> convertList(List<MemberSignInConfigDO> list);

    List<AppMemberSignInConfigRespVO> convertList02(List<MemberSignInConfigDO> list);

    // ── Domain object conversions ──

    default MemberSignInConfigRespVO convert(MemberSignInConfig bean) {
        if (bean == null) return null;
        MemberSignInConfigRespVO vo = new MemberSignInConfigRespVO();
        vo.setId(bean.id() != null ? bean.id().intValue() : null);
        vo.setDay(bean.day());
        vo.setPoint(bean.point());
        vo.setExperience(bean.experience());
        vo.setStatus(bean.status());
        return vo;
    }

    default List<MemberSignInConfigRespVO> convertListFromDomain(List<MemberSignInConfig> list) {
        if (list == null) return null;
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    default List<AppMemberSignInConfigRespVO> convertList02FromDomain(List<MemberSignInConfig> list) {
        if (list == null) return null;
        return list.stream().map(c -> {
            AppMemberSignInConfigRespVO vo = new AppMemberSignInConfigRespVO();
            vo.setDay(c.day());
            vo.setPoint(c.point());
            return vo;
        }).collect(Collectors.toList());
    }
}
