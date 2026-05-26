package com.develop.mvp.pk.module.member.convert.tag;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.member.controller.admin.tag.vo.MemberTagCreateReqVO;
import com.develop.mvp.pk.module.member.controller.admin.tag.vo.MemberTagRespVO;
import com.develop.mvp.pk.module.member.controller.admin.tag.vo.MemberTagUpdateReqVO;
import com.develop.mvp.pk.module.member.dal.dataobject.tag.MemberTagDO;
import com.develop.mvp.pk.module.member.domain.tag.MemberTag;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 会员标签 Convert
 *
 * @author David
 */
@Mapper
public interface MemberTagConvert {

    MemberTagConvert INSTANCE = Mappers.getMapper(MemberTagConvert.class);

    MemberTagDO convert(MemberTagCreateReqVO bean);

    MemberTagDO convert(MemberTagUpdateReqVO bean);

    MemberTagRespVO convert(MemberTagDO bean);

    List<MemberTagRespVO> convertList(List<MemberTagDO> list);

    PageResult<MemberTagRespVO> convertPage(PageResult<MemberTagDO> page);

    // ── Domain object conversions ──

    default MemberTagRespVO convert(MemberTag bean) {
        if (bean == null) return null;
        MemberTagRespVO vo = new MemberTagRespVO();
        vo.setId(bean.id());
        vo.setName(bean.name());
        return vo;
    }

    default List<MemberTagRespVO> convertListFromDomain(List<MemberTag> list) {
        if (list == null) return null;
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    default PageResult<MemberTagRespVO> convertPageFromDomain(PageResult<MemberTag> page) {
        if (page == null) return null;
        return new PageResult<>(page.getList().stream().map(this::convert).collect(Collectors.toList()), page.getTotal());
    }
}
