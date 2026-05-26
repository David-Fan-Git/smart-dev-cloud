package com.develop.mvp.pk.module.system.convert.user;

import com.develop.mvp.pk.framework.common.util.collection.CollectionUtils;
import com.develop.mvp.pk.framework.common.util.collection.MapUtils;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.system.controller.admin.dept.vo.dept.DeptSimpleRespVO;
import com.develop.mvp.pk.module.system.controller.admin.dept.vo.post.PostSimpleRespVO;
import com.develop.mvp.pk.module.system.controller.admin.permission.vo.role.RoleSimpleRespVO;
import com.develop.mvp.pk.module.system.controller.admin.user.vo.profile.UserProfileRespVO;
import com.develop.mvp.pk.module.system.controller.admin.user.vo.user.UserRespVO;
import com.develop.mvp.pk.module.system.controller.admin.user.vo.user.UserSimpleRespVO;
import com.develop.mvp.pk.module.system.dal.dataobject.dept.DeptDO;
import com.develop.mvp.pk.module.system.dal.dataobject.dept.PostDO;
import com.develop.mvp.pk.module.system.dal.dataobject.permission.RoleDO;
import com.develop.mvp.pk.module.system.dal.dataobject.user.AdminUserDO;
import com.develop.mvp.pk.module.system.domain.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

/**
 * User Convert 对象转换器。
 */
@Mapper
public interface UserConvert {

    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    // ── 旧 API：基于 AdminUserDO（保持兼容） ──

    /**
     * 转换 convert List 对应的数据对象。
     *
     * @param list list 参数
     * @param deptMap deptMap 参数
     * @return 处理结果
     */
    default List<UserRespVO> convertList(List<AdminUserDO> list, Map<Long, DeptDO> deptMap) {
        return CollectionUtils.convertList(list, user -> convert(user, deptMap.get(user.getDeptId())));
    }

    /**
     * 转换 convert 对应的数据对象。
     *
     * @param user user 参数
     * @param dept dept 参数
     * @return 处理结果
     */
    default UserRespVO convert(AdminUserDO user, DeptDO dept) {
        UserRespVO userVO = BeanUtils.toBean(user, UserRespVO.class);
        if (dept != null) {
            userVO.setDeptName(dept.getName());
        }
        return userVO;
    }

    /**
     * 转换 convert Simple List 对应的数据对象。
     *
     * @param list list 参数
     * @param deptMap deptMap 参数
     * @return 处理结果
     */
    default List<UserSimpleRespVO> convertSimpleList(List<AdminUserDO> list, Map<Long, DeptDO> deptMap) {
        return CollectionUtils.convertList(list, user -> {
            UserSimpleRespVO userVO = BeanUtils.toBean(user, UserSimpleRespVO.class);
            MapUtils.findAndThen(deptMap, user.getDeptId(), dept -> userVO.setDeptName(dept.getName()));
            return userVO;
        });
    }

    /**
     * 转换 convert 对应的数据对象。
     *
     * @param user user 参数
     * @param userRoles userRoles 参数
     * @param dept dept 参数
     * @param posts posts 参数
     * @return 处理结果
     */
    default UserProfileRespVO convert(AdminUserDO user, List<RoleDO> userRoles,
                                      DeptDO dept, List<PostDO> posts) {
        UserProfileRespVO userVO = BeanUtils.toBean(user, UserProfileRespVO.class);
        userVO.setRoles(BeanUtils.toBean(userRoles, RoleSimpleRespVO.class));
        userVO.setDept(BeanUtils.toBean(dept, DeptSimpleRespVO.class));
        userVO.setPosts(BeanUtils.toBean(posts, PostSimpleRespVO.class));
        return userVO;
    }

    // ── 新 API：基于 User 领域对象 ──

    /** 将 User 领域对象列表转换为 UserRespVO 列表 */
    default List<UserRespVO> convertUserList(List<User> users, Map<Long, DeptDO> deptMap) {
        return CollectionUtils.convertList(users, user -> convertUser(user, deptMap.get(user.deptId())));
    }

    /** 将单个 User 领域对象转换为 UserRespVO */
    default UserRespVO convertUser(User user, DeptDO dept) {
        UserRespVO vo = new UserRespVO();
        vo.setId(user.id().value());
        vo.setUsername(user.username().value());
        vo.setNickname(user.profile().nickname());
        vo.setDeptId(user.deptId());
        vo.setPostIds(new java.util.HashSet<>(user.postIds()));
        vo.setEmail(user.email().isPresent() ? user.email().value() : null);
        vo.setMobile(user.mobile().isPresent() ? user.mobile().value() : null);
        vo.setSex(user.profile().sex());
        vo.setAvatar(user.profile().avatar());
        vo.setStatus(user.status().code());
        if (user.lastLogin() != null) {
            vo.setLoginIp(user.lastLogin().loginIp());
            vo.setLoginDate(user.lastLogin().loginDate());
        }
        if (dept != null) {
            vo.setDeptName(dept.getName());
        }
        return vo;
    }

    /** 将 User 领域对象列表转换为 UserSimpleRespVO 列表 */
    default List<UserSimpleRespVO> convertUserSimpleList(List<User> users, Map<Long, DeptDO> deptMap) {
        return CollectionUtils.convertList(users, user -> {
            UserSimpleRespVO vo = new UserSimpleRespVO();
            vo.setId(user.id().value());
            vo.setNickname(user.profile().nickname());
            vo.setDeptId(user.deptId());
            MapUtils.findAndThen(deptMap, user.deptId(), dept -> vo.setDeptName(dept.getName()));
            return vo;
        });
    }

    /** 将 User 领域对象转换为 UserProfileRespVO */
    default UserProfileRespVO convertUser(User user, List<RoleDO> userRoles,
                                           DeptDO dept, List<PostDO> posts) {
        UserProfileRespVO vo = new UserProfileRespVO();
        vo.setId(user.id().value());
        vo.setUsername(user.username().value());
        vo.setNickname(user.profile().nickname());
        vo.setEmail(user.email().isPresent() ? user.email().value() : null);
        vo.setMobile(user.mobile().isPresent() ? user.mobile().value() : null);
        vo.setSex(user.profile().sex());
        vo.setAvatar(user.profile().avatar());
        if (user.lastLogin() != null) {
            vo.setLoginIp(user.lastLogin().loginIp());
            vo.setLoginDate(user.lastLogin().loginDate());
        }
        vo.setRoles(BeanUtils.toBean(userRoles, RoleSimpleRespVO.class));
        vo.setDept(BeanUtils.toBean(dept, DeptSimpleRespVO.class));
        vo.setPosts(BeanUtils.toBean(posts, PostSimpleRespVO.class));
        return vo;
    }
}
