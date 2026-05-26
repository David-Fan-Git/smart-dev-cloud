package com.develop.mvp.pk.module.mes.dal.mysql.wm.outsourceissue;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.mes.controller.admin.wm.outsourceissue.vo.line.MesWmOutsourceIssueLinePageReqVO;
import com.develop.mvp.pk.module.mes.dal.dataobject.wm.outsourceissue.MesWmOutsourceIssueLineDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 外协发料单行 Mapper
 *
 * @author David
 */
@Mapper
public interface MesWmOutsourceIssueLineMapper extends BaseMapperX<MesWmOutsourceIssueLineDO> {

    default PageResult<MesWmOutsourceIssueLineDO> selectPage(MesWmOutsourceIssueLinePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesWmOutsourceIssueLineDO>()
                .eqIfPresent(MesWmOutsourceIssueLineDO::getIssueId, reqVO.getIssueId())
                .orderByDesc(MesWmOutsourceIssueLineDO::getId));
    }

    default List<MesWmOutsourceIssueLineDO> selectListByIssueId(Long issueId) {
        return selectList(MesWmOutsourceIssueLineDO::getIssueId, issueId);
    }

    default void deleteByIssueId(Long issueId) {
        delete(MesWmOutsourceIssueLineDO::getIssueId, issueId);
    }

}
