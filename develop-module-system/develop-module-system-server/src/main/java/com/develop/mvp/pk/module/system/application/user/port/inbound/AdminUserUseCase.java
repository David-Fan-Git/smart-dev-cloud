package com.develop.mvp.pk.module.system.application.user.port.inbound;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.controller.admin.auth.vo.AuthRegisterReqVO;
import com.develop.mvp.pk.module.system.controller.admin.user.vo.profile.UserProfileUpdatePasswordReqVO;
import com.develop.mvp.pk.module.system.controller.admin.user.vo.profile.UserProfileUpdateReqVO;
import com.develop.mvp.pk.module.system.controller.admin.user.vo.user.UserImportExcelVO;
import com.develop.mvp.pk.module.system.controller.admin.user.vo.user.UserImportRespVO;
import com.develop.mvp.pk.module.system.controller.admin.user.vo.user.UserPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.user.vo.user.UserSaveReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.user.AdminUserDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * admin user use-case boundary for legacy service compatibility and future adapters.
 */
public interface AdminUserUseCase {

    /**
     * 创建 create User 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @return 处理结果
     */
    Long createUser(UserSaveReqVO createReqVO);

    /**
     * 处理 register User 对应的认证流程。
     *
     * @param registerReqVO registerReqVO 参数
     * @return 处理结果
     */
    Long registerUser(AuthRegisterReqVO registerReqVO);

    /**
     * 更新 update User 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     */
    void updateUser(UserSaveReqVO updateReqVO);

    /**
     * 更新 update User Login 对应的数据。
     *
     * @param id id 参数
     * @param loginIp loginIp 参数
     */
    void updateUserLogin(Long id, String loginIp);

    /**
     * 更新 update User Profile 对应的数据。
     *
     * @param id id 参数
     * @param reqVO reqVO 参数
     */
    void updateUserProfile(Long id, UserProfileUpdateReqVO reqVO);

    /**
     * 更新 update User Password 对应的数据。
     *
     * @param id id 参数
     * @param reqVO reqVO 参数
     */
    void updateUserPassword(Long id, UserProfileUpdatePasswordReqVO reqVO);

    /**
     * 更新 update User Password 对应的数据。
     *
     * @param id id 参数
     * @param password password 参数
     */
    void updateUserPassword(Long id, String password);

    /**
     * 更新 update User Status 对应的数据。
     *
     * @param id id 参数
     * @param status status 参数
     */
    void updateUserStatus(Long id, Integer status);

    /**
     * 删除 delete User 对应的数据。
     *
     * @param id id 参数
     */
    void deleteUser(Long id);

    /**
     * 删除 delete User List 对应的数据。
     *
     * @param ids ids 参数
     */
    void deleteUserList(List<Long> ids);

    /**
     * 查询 get User By Username 对应的数据。
     *
     * @param username username 参数
     * @return 处理结果
     */
    AdminUserDO getUserByUsername(String username);

    /**
     * 查询 get User By Mobile 对应的数据。
     *
     * @param mobile mobile 参数
     * @return 处理结果
     */
    AdminUserDO getUserByMobile(String mobile);

    /**
     * 查询 get User Page 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    PageResult<AdminUserDO> getUserPage(UserPageReqVO reqVO);

    /**
     * 查询 get User 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    AdminUserDO getUser(Long id);

    /**
     * 查询 get User List By Dept Ids 对应的数据。
     *
     * @param deptIds deptIds 参数
     * @return 处理结果
     */
    List<AdminUserDO> getUserListByDeptIds(Collection<Long> deptIds);

    /**
     * 查询 get User List By Post Ids 对应的数据。
     *
     * @param postIds postIds 参数
     * @return 处理结果
     */
    List<AdminUserDO> getUserListByPostIds(Collection<Long> postIds);

    /**
     * 查询 get User List 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    List<AdminUserDO> getUserList(Collection<Long> ids);

    /**
     * 查询 get User Map 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    Map<Long, AdminUserDO> getUserMap(Collection<Long> ids);

    /**
     * 校验 validate User List 对应的业务规则。
     *
     * @param ids ids 参数
     */
    void validateUserList(Collection<Long> ids);

    /**
     * 查询 get User List By Nickname 对应的数据。
     *
     * @param nickname nickname 参数
     * @return 处理结果
     */
    List<AdminUserDO> getUserListByNickname(String nickname);

    /**
     * 查询 get User List By Status 对应的数据。
     *
     * @param status status 参数
     * @return 处理结果
     */
    List<AdminUserDO> getUserListByStatus(Integer status);

    /**
     * 判断 is Password Match 对应的条件是否成立。
     *
     * @param rawPassword rawPassword 参数
     * @param encodedPassword encodedPassword 参数
     * @return 处理结果
     */
    boolean isPasswordMatch(String rawPassword, String encodedPassword);

    /**
     * 执行 import User List 对应的业务操作。
     *
     * @param importUsers importUsers 参数
     * @param isUpdateSupport isUpdateSupport 参数
     * @return 处理结果
     */
    UserImportRespVO importUserList(List<UserImportExcelVO> importUsers, boolean isUpdateSupport);

    /**
     * 校验 validate User Exists 对应的业务规则。
     *
     * @param id id 参数
     * @return 处理结果
     */
    AdminUserDO validateUserExists(Long id);

    /**
     * 校验 validate Username Unique 对应的业务规则。
     *
     * @param id id 参数
     * @param username username 参数
     */
    void validateUsernameUnique(Long id, String username);

    /**
     * 校验 validate Email Unique 对应的业务规则。
     *
     * @param id id 参数
     * @param email email 参数
     */
    void validateEmailUnique(Long id, String email);

    /**
     * 校验 validate Mobile Unique 对应的业务规则。
     *
     * @param id id 参数
     * @param mobile mobile 参数
     */
    void validateMobileUnique(Long id, String mobile);

    /**
     * 校验 validate Old Password 对应的业务规则。
     *
     * @param id id 参数
     * @param oldPassword oldPassword 参数
     */
    void validateOldPassword(Long id, String oldPassword);
}
