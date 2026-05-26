package com.develop.mvp.pk.module.member.convert.config;

import com.develop.mvp.pk.module.member.api.config.dto.MemberConfigRespDTO;
import com.develop.mvp.pk.module.member.controller.admin.config.vo.MemberConfigRespVO;
import com.develop.mvp.pk.module.member.controller.admin.config.vo.MemberConfigSaveReqVO;
import com.develop.mvp.pk.module.member.dal.dataobject.config.MemberConfigDO;
import com.develop.mvp.pk.module.member.domain.config.MemberConfig;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 会员配置 Convert
 *
 * @author David
 */
@Mapper
public interface MemberConfigConvert {

    MemberConfigConvert INSTANCE = Mappers.getMapper(MemberConfigConvert.class);

    MemberConfigRespVO convert(MemberConfigDO bean);

    MemberConfigDO convert(MemberConfigSaveReqVO bean);

    MemberConfigRespDTO convert01(MemberConfigDO config);

    // ── Domain object conversions ──

    default MemberConfigRespVO convert(MemberConfig bean) {
        if (bean == null) return null;
        MemberConfigRespVO vo = new MemberConfigRespVO();
        vo.setId(bean.id());
        vo.setPointTradeDeductEnable(bean.pointTradeDeductEnable());
        vo.setPointTradeDeductUnitPrice(bean.pointTradeDeductUnitPrice());
        vo.setPointTradeDeductMaxPrice(bean.pointTradeDeductMaxPrice());
        vo.setPointTradeGivePoint(bean.pointTradeGivePoint());
        return vo;
    }
}
