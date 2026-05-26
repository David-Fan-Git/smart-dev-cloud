package com.develop.mvp.pk.module.member.convert.group;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.member.controller.admin.group.vo.MemberGroupCreateReqVO;
import com.develop.mvp.pk.module.member.controller.admin.group.vo.MemberGroupRespVO;
import com.develop.mvp.pk.module.member.controller.admin.group.vo.MemberGroupSimpleRespVO;
import com.develop.mvp.pk.module.member.controller.admin.group.vo.MemberGroupUpdateReqVO;
import com.develop.mvp.pk.module.member.dal.dataobject.group.MemberGroupDO;
import com.develop.mvp.pk.module.member.domain.group.MemberGroup;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户分组 Convert
 *
 * @author David
 */
@Mapper
public interface MemberGroupConvert {

    MemberGroupConvert INSTANCE = Mappers.getMapper(MemberGroupConvert.class);

    MemberGroupDO convert(MemberGroupCreateReqVO bean);

    MemberGroupDO convert(MemberGroupUpdateReqVO bean);

    MemberGroupRespVO convert(MemberGroupDO bean);

    List<MemberGroupRespVO> convertList(List<MemberGroupDO> list);

    PageResult<MemberGroupRespVO> convertPage(PageResult<MemberGroupDO> page);

    List<MemberGroupSimpleRespVO> convertSimpleList(List<MemberGroupDO> list);

    // ── Domain object conversions ──

    default MemberGroupRespVO convert(MemberGroup bean) {
        if (bean == null) return null;
        MemberGroupRespVO vo = new MemberGroupRespVO();
        vo.setId(bean.id());
        vo.setName(bean.name());
        vo.setRemark(bean.remark());
        vo.setStatus(bean.status());
        return vo;
    }

    default List<MemberGroupRespVO> convertListFromDomain(List<MemberGroup> list) {
        if (list == null) return null;
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    default PageResult<MemberGroupRespVO> convertPageFromDomain(PageResult<MemberGroup> page) {
        if (page == null) return null;
        return new PageResult<>(page.getList().stream().map(this::convert).collect(Collectors.toList()), page.getTotal());
    }

    default List<MemberGroupSimpleRespVO> convertSimpleListFromDomain(List<MemberGroup> list) {
        if (list == null) return null;
        return list.stream().map(g -> {
            MemberGroupSimpleRespVO vo = new MemberGroupSimpleRespVO();
            vo.setId(g.id());
            vo.setName(g.name());
            return vo;
        }).collect(Collectors.toList());
    }
}
