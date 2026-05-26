package com.develop.mvp.pk.module.system.domain.user.repository;

import com.develop.mvp.pk.module.system.domain.user.User;
import com.develop.mvp.pk.module.system.domain.user.valueobject.*;
import com.develop.mvp.pk.framework.common.pojo.PageResult;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

// Skill: AggregateRoot_User_Validation_Skill — 仓储接口 UserRepository
// DDD 角色：领域层接口，定义聚合根的持久化契约，不依赖任何基础设施
// 验收标准 AC06：定义在 domain.user 包，不 import MyBatis 类

/**
 * User Repository 领域仓储接口。
 */
public interface UserRepository {
    /**
     * 创建 create 对应的数据。
     *
     * @param username username 参数
     * @param encodedPassword encodedPassword 参数
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
    User create(String username, EncodedPassword encodedPassword, Long tenantId, Long deptId,
                String email, String mobile, String nickname, String avatar, Integer sex,
                String remark, java.util.Set<Long> postIds);
    /**
     * 创建 save 对应的数据。
     *
     * @param user user 参数
     */
    void save(User user);
    /**
     * 删除 delete 对应的数据。
     *
     * @param id id 参数
     */
    void delete(UserId id);
    /**
     * 查询 find By Id 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    User findById(UserId id);
    /**
     * 查询 find By Username 对应的数据。
     *
     * @param username username 参数
     * @return 处理结果
     */
    Optional<User> findByUsername(Username username);
    /**
     * 查询 find By Email 对应的数据。
     *
     * @param email email 参数
     * @return 处理结果
     */
    Optional<User> findByEmail(Email email);
    /**
     * 查询 find By Mobile 对应的数据。
     *
     * @param mobile mobile 参数
     * @return 处理结果
     */
    Optional<User> findByMobile(Mobile mobile);
    /**
     * 查询 find By Ids 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    List<User> findByIds(Collection<UserId> ids);
    /**
     * 查询 find By Dept Ids 对应的数据。
     *
     * @param deptIds deptIds 参数
     * @return 处理结果
     */
    List<User> findByDeptIds(Collection<Long> deptIds);
    /**
     * 查询 find By Post Ids 对应的数据。
     *
     * @param postIds postIds 参数
     * @return 处理结果
     */
    List<User> findByPostIds(Collection<Long> postIds);
    /**
     * 查询 find By Nickname 对应的数据。
     *
     * @param nickname nickname 参数
     * @return 处理结果
     */
    List<User> findByNickname(String nickname);
    /**
     * 查询 find By Status 对应的数据。
     *
     * @param status status 参数
     * @return 处理结果
     */
    List<User> findByStatus(UserStatus status);
    /**
     * 查询 find Page 对应的数据。
     *
     * @param query query 参数
     * @return 处理结果
     */
    PageResult<User> findPage(UserPageQuery query);
    /**
     * 执行 exists By Username 对应的业务操作。
     *
     * @param username username 参数
     * @return 处理结果
     */
    boolean existsByUsername(Username username);
    /**
     * 执行 exists By Email 对应的业务操作。
     *
     * @param email email 参数
     * @return 处理结果
     */
    boolean existsByEmail(Email email);
    /**
     * 执行 exists By Mobile 对应的业务操作。
     *
     * @param mobile mobile 参数
     * @return 处理结果
     */
    boolean existsByMobile(Mobile mobile);
    /**
     * 查询 count 对应的数据。
     *
     * @return 处理结果
     */
    long count();
}
