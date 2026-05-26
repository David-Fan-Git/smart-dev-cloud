package com.develop.mvp.pk.module.system.dal.mysql.dept;

import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.system.controller.admin.dept.vo.dept.DeptListReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.dept.DeptDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * Dept Mapper 持久化 Mapper。
 */
@Mapper
public interface DeptMapper extends BaseMapperX<DeptDO> {

    /**
     * 查询 select List 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    default List<DeptDO> selectList(DeptListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<DeptDO>()
                .likeIfPresent(DeptDO::getName, reqVO.getName())
                .eqIfPresent(DeptDO::getStatus, reqVO.getStatus()));
    }

    /**
     * 查询 select By Parent Id And Name 对应的数据。
     *
     * @param parentId parentId 参数
     * @param name name 参数
     * @return 处理结果
     */
    default DeptDO selectByParentIdAndName(Long parentId, String name) {
        return selectOne(DeptDO::getParentId, parentId, DeptDO::getName, name);
    }

    /**
     * 查询 select Count By Parent Id 对应的数据。
     *
     * @param parentId parentId 参数
     * @return 处理结果
     */
    default Long selectCountByParentId(Long parentId) {
        return selectCount(DeptDO::getParentId, parentId);
    }

    /**
     * 查询 select List By Parent Id 对应的数据。
     *
     * @param parentIds parentIds 参数
     * @return 处理结果
     */
    default List<DeptDO> selectListByParentId(Collection<Long> parentIds) {
        return selectList(DeptDO::getParentId, parentIds);
    }

    /**
     * 查询 select List By Leader User Id 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    default List<DeptDO> selectListByLeaderUserId(Long id) {
        return selectList(DeptDO::getLeaderUserId, id);
    }

}
