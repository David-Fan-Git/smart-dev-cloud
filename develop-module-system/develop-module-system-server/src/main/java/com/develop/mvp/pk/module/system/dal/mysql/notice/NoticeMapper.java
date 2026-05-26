package com.develop.mvp.pk.module.system.dal.mysql.notice;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.system.controller.admin.notice.vo.NoticePageReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.notice.NoticeDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * Notice Mapper 持久化 Mapper。
 */
@Mapper
public interface NoticeMapper extends BaseMapperX<NoticeDO> {

    /**
     * 查询 select Page 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    default PageResult<NoticeDO> selectPage(NoticePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<NoticeDO>()
                .likeIfPresent(NoticeDO::getTitle, reqVO.getTitle())
                .eqIfPresent(NoticeDO::getStatus, reqVO.getStatus())
                .orderByDesc(NoticeDO::getId));
    }

}
