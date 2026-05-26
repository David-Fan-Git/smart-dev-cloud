package com.develop.mvp.pk.module.system.application.user.port.inbound;

// DDD 角色：入站端口 — 定义 User 聚合的用例边界，供 Controller/API/跨服务调用
// Hexagonal-Lite：入站端口接口，应用服务实现此接口

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.domain.user.User;
import com.develop.mvp.pk.module.system.domain.user.repository.UserPageQuery;

import java.util.Collection;
import java.util.List;
import java.util.Set;
/**
 * User 聚合的入站用例端口。
 */
public interface UserUseCase {

    /**
     * 创建 create User 对应的数据。
     *
     * @param id id 参数
     * @param username username 参数
     * @param rawPassword rawPassword 参数
     * @param tenantId tenantId 参数
     * @param deptId deptId 参数
     * @param email email 参数
     * @param mobile mobile 参数
     * @param nickname nickname 参数
     * @param avatar avatar 参数
     * @param sex sex 参数
     * @param remark remark 参数
     * @param postIds postIds 参数
     * @return 处理结果
     */
    Long createUser(Long id, String username, String rawPassword, Long tenantId,
                    Long deptId, String email, String mobile,
                    String nickname, String avatar, Integer sex, String remark,
                    Set<Long> postIds);

    /**
     * 更新 update User 对应的数据。
     *
     * @param id id 参数
     * @param username username 参数
     * @param email email 参数
     * @param mobile mobile 参数
     * @param nickname nickname 参数
     * @param avatar avatar 参数
     * @param sex sex 参数
     * @param remark remark 参数
     * @param deptId deptId 参数
     * @param postIds postIds 参数
     */
    void updateUser(Long id, String username, String email, String mobile,
                    String nickname, String avatar, Integer sex, String remark,
                    Long deptId, Set<Long> postIds);

    /**
     * 更新 update User Status 对应的数据。
     *
     * @param id id 参数
     * @param statusCode statusCode 参数
     */
    void updateUserStatus(Long id, Integer statusCode);

    /**
     * 更新 change Password 对应的数据。
     *
     * @param id id 参数
     * @param oldRawPassword oldRawPassword 参数
     * @param newRawPassword newRawPassword 参数
     */
    void changePassword(Long id, String oldRawPassword, String newRawPassword);

    /**
     * 更新 reset Password 对应的数据。
     *
     * @param id id 参数
     * @param newRawPassword newRawPassword 参数
     */
    void resetPassword(Long id, String newRawPassword);

    /**
     * 更新 update Profile 对应的数据。
     *
     * @param id id 参数
     * @param email email 参数
     * @param mobile mobile 参数
     * @param nickname nickname 参数
     * @param avatar avatar 参数
     * @param sex sex 参数
     * @param remark remark 参数
     */
    void updateProfile(Long id, String email, String mobile,
                       String nickname, String avatar, Integer sex, String remark);

    /**
     * 记录 record Login 对应的数据。
     *
     * @param id id 参数
     * @param loginIp loginIp 参数
     */
    void recordLogin(Long id, String loginIp);

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
     * 查询 get User 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    User getUser(Long id);

    /**
     * 查询 get User By Username 对应的数据。
     *
     * @param username username 参数
     * @return 处理结果
     */
    User getUserByUsername(String username);

    /**
     * 查询 get User Page 对应的数据。
     *
     * @param query query 参数
     * @return 处理结果
     */
    PageResult<User> getUserPage(UserPageQuery query);

    /**
     * 查询 get User List 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    List<User> getUserList(Collection<Long> ids);

    /**
     * 查询 get User List By Dept Ids 对应的数据。
     *
     * @param deptIds deptIds 参数
     * @return 处理结果
     */
    List<User> getUserListByDeptIds(Collection<Long> deptIds);

    /**
     * 查询 get User List By Post Ids 对应的数据。
     *
     * @param postIds postIds 参数
     * @return 处理结果
     */
    List<User> getUserListByPostIds(Collection<Long> postIds);

    /**
     * 查询 get User List By Status 对应的数据。
     *
     * @param status status 参数
     * @return 处理结果
     */
    List<User> getUserListByStatus(Integer status);

    /**
     * 查询 get User List By Nickname 对应的数据。
     *
     * @param nickname nickname 参数
     * @return 处理结果
     */
    List<User> getUserListByNickname(String nickname);

    /**
     * 校验 validate User List 对应的业务规则。
     *
     * @param ids ids 参数
     */
    void validateUserList(Collection<Long> ids);

    /**
     * 查询 get Dept Condition 对应的数据。
     *
     * @param deptId deptId 参数
     * @return 处理结果
     */
    Set<Long> getDeptCondition(Long deptId);
}
