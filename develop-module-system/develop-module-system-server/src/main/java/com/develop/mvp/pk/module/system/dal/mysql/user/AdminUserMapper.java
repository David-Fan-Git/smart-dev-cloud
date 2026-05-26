package com.develop.mvp.pk.module.system.dal.mysql.user;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.system.controller.admin.user.vo.user.UserPageReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.user.AdminUserDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * Admin User Mapper 持久化 Mapper。
 */
@Mapper
public interface AdminUserMapper extends BaseMapperX<AdminUserDO> {

    /**
     * 查询 select By Username 对应的数据。
     *
     * @param username username 参数
     * @return 处理结果
     */
    default AdminUserDO selectByUsername(String username) {
        return selectOne(AdminUserDO::getUsername, username);
    }

    /**
     * 查询 select By Email 对应的数据。
     *
     * @param email email 参数
     * @return 处理结果
     */
    default AdminUserDO selectByEmail(String email) {
        return selectOne(AdminUserDO::getEmail, email);
    }

    /**
     * 查询 select By Mobile 对应的数据。
     *
     * @param mobile mobile 参数
     * @return 处理结果
     */
    default AdminUserDO selectByMobile(String mobile) {
        return selectOne(AdminUserDO::getMobile, mobile);
    }

    /**
     * 查询 select Page 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @param deptIds deptIds 参数
     * @param userIds userIds 参数
     * @return 处理结果
     */
    default PageResult<AdminUserDO> selectPage(UserPageReqVO reqVO, Collection<Long> deptIds, Collection<Long> userIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AdminUserDO>()
                .likeIfPresent(AdminUserDO::getUsername, reqVO.getUsername())
                .likeIfPresent(AdminUserDO::getMobile, reqVO.getMobile())
                .eqIfPresent(AdminUserDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(AdminUserDO::getCreateTime, reqVO.getCreateTime())
                .inIfPresent(AdminUserDO::getDeptId, deptIds)
                .inIfPresent(AdminUserDO::getId, userIds)
                .orderByDesc(AdminUserDO::getId));
    }

    /**
     * 查询 select List By Nickname 对应的数据。
     *
     * @param nickname nickname 参数
     * @return 处理结果
     */
    default List<AdminUserDO> selectListByNickname(String nickname) {
        return selectList(new LambdaQueryWrapperX<AdminUserDO>().like(AdminUserDO::getNickname, nickname));
    }

    /**
     * 查询 select List By Status 对应的数据。
     *
     * @param status status 参数
     * @return 处理结果
     */
    default List<AdminUserDO> selectListByStatus(Integer status) {
        return selectList(AdminUserDO::getStatus, status);
    }

    /**
     * 查询 select List By Dept Ids 对应的数据。
     *
     * @param deptIds deptIds 参数
     * @return 处理结果
     */
    default List<AdminUserDO> selectListByDeptIds(Collection<Long> deptIds) {
        return selectList(AdminUserDO::getDeptId, deptIds);
    }

}
