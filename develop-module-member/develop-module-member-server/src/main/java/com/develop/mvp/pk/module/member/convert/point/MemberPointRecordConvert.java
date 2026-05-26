package com.develop.mvp.pk.module.member.convert.point;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.collection.MapUtils;
import com.develop.mvp.pk.module.member.controller.admin.point.vo.recrod.MemberPointRecordRespVO;
import com.develop.mvp.pk.module.member.dal.dataobject.point.MemberPointRecordDO;
import com.develop.mvp.pk.module.member.dal.dataobject.user.MemberUserDO;
import com.develop.mvp.pk.module.member.domain.point.MemberPointRecord;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 用户积分记录 Convert
 *
 * @author David
 */
@Mapper
public interface MemberPointRecordConvert {

    MemberPointRecordConvert INSTANCE = Mappers.getMapper(MemberPointRecordConvert.class);

    default PageResult<MemberPointRecordRespVO> convertPage(PageResult<MemberPointRecordDO> pageResult, List<MemberUserDO> users) {
        PageResult<MemberPointRecordRespVO> voPageResult = convertPage(pageResult);
        Map<Long, MemberUserDO> userMap = convertMap(users, MemberUserDO::getId);
        voPageResult.getList().forEach(record -> MapUtils.findAndThen(userMap, record.getUserId(),
                memberUserRespDTO -> record.setNickname(memberUserRespDTO.getNickname())));
        return voPageResult;
    }

    PageResult<MemberPointRecordRespVO> convertPage(PageResult<MemberPointRecordDO> pageResult);

    // ── Domain object conversions ──

    default PageResult<MemberPointRecordRespVO> convertPageFromDomain(PageResult<MemberPointRecord> pageResult, List<MemberUserDO> users) {
        List<MemberPointRecordRespVO> list = pageResult.getList().stream().map(r -> {
            MemberPointRecordRespVO vo = new MemberPointRecordRespVO();
            vo.setId(r.id());
            vo.setUserId(r.userId());
            vo.setBizId(r.bizId());
            vo.setBizType(r.bizType());
            vo.setTitle(r.title());
            vo.setDescription(r.description());
            vo.setPoint(r.point());
            vo.setTotalPoint(r.totalPoint());
            return vo;
        }).collect(Collectors.toList());
        PageResult<MemberPointRecordRespVO> result = new PageResult<>(list, pageResult.getTotal());
        Map<Long, MemberUserDO> userMap = convertMap(users, MemberUserDO::getId);
        result.getList().forEach(record -> MapUtils.findAndThen(userMap, record.getUserId(),
                memberUserRespDTO -> record.setNickname(memberUserRespDTO.getNickname())));
        return result;
    }
}
