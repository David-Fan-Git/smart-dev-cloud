package com.develop.mvp.pk.module.member.convert.address;

import com.develop.mvp.pk.framework.ip.core.utils.AreaUtils;
import com.develop.mvp.pk.module.member.api.address.dto.MemberAddressRespDTO;
import com.develop.mvp.pk.module.member.controller.admin.address.vo.AddressRespVO;
import com.develop.mvp.pk.module.member.controller.app.address.vo.AppAddressCreateReqVO;
import com.develop.mvp.pk.module.member.controller.app.address.vo.AppAddressRespVO;
import com.develop.mvp.pk.module.member.controller.app.address.vo.AppAddressUpdateReqVO;
import com.develop.mvp.pk.module.member.dal.dataobject.address.MemberAddressDO;
import com.develop.mvp.pk.module.member.domain.address.MemberAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户收件地址 Convert
 *
 * @author David
 */
@Mapper
public interface AddressConvert {

    AddressConvert INSTANCE = Mappers.getMapper(AddressConvert.class);

    MemberAddressDO convert(AppAddressCreateReqVO bean);

    MemberAddressDO convert(AppAddressUpdateReqVO bean);

    @Mapping(source = "areaId", target = "areaName",  qualifiedByName = "convertAreaIdToAreaName")
    AppAddressRespVO convert(MemberAddressDO bean);

    List<AppAddressRespVO> convertList(List<MemberAddressDO> list);

    MemberAddressRespDTO convert02(MemberAddressDO bean);

    @Named("convertAreaIdToAreaName")
    default String convertAreaIdToAreaName(Integer areaId) {
        return AreaUtils.format(areaId);
    }

    List<AddressRespVO> convertList2(List<MemberAddressDO> list);

    // ── Domain object conversions ──

    default AppAddressRespVO convert(MemberAddress bean) {
        if (bean == null) return null;
        AppAddressRespVO vo = new AppAddressRespVO();
        vo.setId(bean.id());
        vo.setName(bean.name());
        vo.setMobile(bean.mobile());
        vo.setAreaId(bean.areaId());
        vo.setDetailAddress(bean.detailAddress());
        vo.setDefaultStatus(bean.defaultStatus());
        vo.setAreaName(bean.areaId() != null ? AreaUtils.format(bean.areaId().intValue()) : null);
        return vo;
    }

    default List<AppAddressRespVO> convertListFromDomain(List<MemberAddress> list) {
        if (list == null) return null;
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    default AddressRespVO convertAddressRespVO(MemberAddress bean) {
        if (bean == null) return null;
        AddressRespVO vo = new AddressRespVO();
        vo.setId(bean.id());
        vo.setName(bean.name());
        vo.setMobile(bean.mobile());
        vo.setAreaId(bean.areaId());
        vo.setDetailAddress(bean.detailAddress());
        vo.setDefaultStatus(bean.defaultStatus());
        return vo;
    }

    default List<AddressRespVO> convertList2FromDomain(List<MemberAddress> list) {
        if (list == null) return null;
        return list.stream().map(this::convertAddressRespVO).collect(Collectors.toList());
    }
}
