package com.develop.mvp.pk.module.system.dal.mysql.dept;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.system.controller.admin.dept.vo.post.PostPageReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.dept.PostDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * Post Mapper 持久化 Mapper。
 */
@Mapper
public interface PostMapper extends BaseMapperX<PostDO> {

    /**
     * 查询 select List 对应的数据。
     *
     * @param ids ids 参数
     * @param statuses statuses 参数
     * @return 处理结果
     */
    default List<PostDO> selectList(Collection<Long> ids, Collection<Integer> statuses) {
        return selectList(new LambdaQueryWrapperX<PostDO>()
                .inIfPresent(PostDO::getId, ids)
                .inIfPresent(PostDO::getStatus, statuses));
    }

    /**
     * 查询 select Page 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    default PageResult<PostDO> selectPage(PostPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PostDO>()
                .likeIfPresent(PostDO::getCode, reqVO.getCode())
                .likeIfPresent(PostDO::getName, reqVO.getName())
                .eqIfPresent(PostDO::getStatus, reqVO.getStatus())
                .orderByDesc(PostDO::getId));
    }

    /**
     * 查询 select By Name 对应的数据。
     *
     * @param name name 参数
     * @return 处理结果
     */
    default PostDO selectByName(String name) {
        return selectOne(PostDO::getName, name);
    }

    /**
     * 查询 select By Code 对应的数据。
     *
     * @param code code 参数
     * @return 处理结果
     */
    default PostDO selectByCode(String code) {
        return selectOne(PostDO::getCode, code);
    }

}
