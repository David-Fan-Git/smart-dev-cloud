package com.develop.mvp.pk.module.system.dal.mysql.permission;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.dataobject.BaseDO;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.system.controller.admin.permission.vo.role.RolePageReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.permission.RoleDO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Role Mapper 持久化 Mapper。
 */
@Mapper
public interface RoleMapper extends BaseMapperX<RoleDO> {

    /**
     * 查询 select Page 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    default PageResult<RoleDO> selectPage(RolePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RoleDO>()
                .likeIfPresent(RoleDO::getName, reqVO.getName())
                .likeIfPresent(RoleDO::getCode, reqVO.getCode())
                .eqIfPresent(RoleDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(BaseDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(RoleDO::getSort));
    }

    /**
     * 查询 select By Name 对应的数据。
     *
     * @param name name 参数
     * @return 处理结果
     */
    default RoleDO selectByName(String name) {
        return selectOne(RoleDO::getName, name);
    }

    /**
     * 查询 select By Code 对应的数据。
     *
     * @param code code 参数
     * @return 处理结果
     */
    default RoleDO selectByCode(String code) {
        return selectOne(RoleDO::getCode, code);
    }

    /**
     * 查询 select List By Status 对应的数据。
     *
     * @param statuses statuses 参数
     * @return 处理结果
     */
    default List<RoleDO> selectListByStatus(@Nullable Collection<Integer> statuses) {
        return selectList(RoleDO::getStatus, statuses);
    }

}
