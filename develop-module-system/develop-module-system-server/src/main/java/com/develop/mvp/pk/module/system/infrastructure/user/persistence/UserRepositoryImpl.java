package com.develop.mvp.pk.module.system.infrastructure.user.persistence;

import cn.hutool.core.collection.CollUtil;
import com.develop.mvp.pk.module.system.dal.dataobject.dept.UserPostDO;
import com.develop.mvp.pk.module.system.dal.dataobject.user.AdminUserDO;
import com.develop.mvp.pk.module.system.dal.mysql.dept.UserPostMapper;
import com.develop.mvp.pk.module.system.dal.mysql.user.AdminUserMapper;
import com.develop.mvp.pk.module.system.domain.user.User;
import com.develop.mvp.pk.module.system.domain.user.UserFactory;
import com.develop.mvp.pk.module.system.domain.user.repository.UserPageQuery;
import com.develop.mvp.pk.module.system.domain.user.repository.UserRepository;
import com.develop.mvp.pk.module.system.domain.user.valueobject.*;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * User Repository Impl 领域仓储实现。
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    private final AdminUserMapper userMapper;
    private final UserPostMapper userPostMapper;

    /**
     * 创建 UserRepositoryImpl 实例。
     *
     * @param userMapper userMapper 参数
     * @param userPostMapper userPostMapper 参数
     */
    public UserRepositoryImpl(AdminUserMapper userMapper, UserPostMapper userPostMapper) {
        this.userMapper = userMapper;
        this.userPostMapper = userPostMapper;
    }

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
    @Override
    @Transactional
    public User create(String username, EncodedPassword encodedPassword, Long tenantId, Long deptId,
                       String email, String mobile, String nickname, String avatar, Integer sex,
                       String remark, Set<Long> postIds) {
        AdminUserDO userDO = new AdminUserDO();
        userDO.setUsername(username);
        userDO.setPassword(encodedPassword.toStoreValue());
        userDO.setTenantId(tenantId);
        userDO.setDeptId(deptId);
        userDO.setEmail(email);
        userDO.setMobile(mobile);
        userDO.setNickname(nickname);
        userDO.setAvatar(avatar);
        userDO.setSex(sex);
        userDO.setRemark(remark);
        userDO.setStatus(UserStatus.ENABLED.code());
        userDO.setPostIds(postIds != null ? new HashSet<>(postIds) : new HashSet<>());
        userMapper.insert(userDO);
        User user = UserFactory.create(userDO.getId(), username, encodedPassword, tenantId, deptId,
                email, mobile, nickname, avatar, sex, remark, postIds);
        syncUserPosts(user);
        return user;
    }

    /**
     * 创建 save 对应的数据。
     *
     * @param user user 参数
     */
    @Override
    @Transactional
    public void save(User user) {
        AdminUserDO userDO = toDataObject(user);
        if (userMapper.selectById(user.id().value()) == null) {
            userMapper.insert(userDO);
        } else {
            userMapper.updateById(userDO);
        }
        syncUserPosts(user);
    }

    /**
     * 删除 delete 对应的数据。
     *
     * @param id id 参数
     */
    @Override
    @Transactional
    public void delete(UserId id) {
        userMapper.deleteById(id.value());
        userPostMapper.deleteByUserId(id.value());
    }

    /**
     * 查询 find By Id 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @Override
    public User findById(UserId id) {
        AdminUserDO userDO = userMapper.selectById(id.value());
        if (userDO == null) return null;
        return toDomain(userDO);
    }

    /**
     * 查询 find By Username 对应的数据。
     *
     * @param username username 参数
     * @return 处理结果
     */
    @Override
    public Optional<User> findByUsername(Username username) {
        AdminUserDO userDO = userMapper.selectByUsername(username.value());
        return Optional.ofNullable(userDO).map(this::toDomain);
    }

    /**
     * 查询 find By Email 对应的数据。
     *
     * @param email email 参数
     * @return 处理结果
     */
    @Override
    public Optional<User> findByEmail(Email email) {
        if (!email.isPresent()) return Optional.empty();
        AdminUserDO userDO = userMapper.selectByEmail(email.value());
        return Optional.ofNullable(userDO).map(this::toDomain);
    }

    /**
     * 查询 find By Mobile 对应的数据。
     *
     * @param mobile mobile 参数
     * @return 处理结果
     */
    @Override
    public Optional<User> findByMobile(Mobile mobile) {
        if (!mobile.isPresent()) return Optional.empty();
        AdminUserDO userDO = userMapper.selectByMobile(mobile.value());
        return Optional.ofNullable(userDO).map(this::toDomain);
    }

    /**
     * 查询 find By Ids 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    @Override
    public List<User> findByIds(Collection<UserId> ids) {
        if (CollUtil.isEmpty(ids)) return Collections.emptyList();
        List<Long> rawIds = ids.stream().map(UserId::value).collect(Collectors.toList());
        return userMapper.selectByIds(rawIds).stream().map(this::toDomain).collect(Collectors.toList());
    }

    /**
     * 查询 find By Dept Ids 对应的数据。
     *
     * @param deptIds deptIds 参数
     * @return 处理结果
     */
    @Override
    public List<User> findByDeptIds(Collection<Long> deptIds) {
        if (CollUtil.isEmpty(deptIds)) return Collections.emptyList();
        return userMapper.selectListByDeptIds(deptIds).stream().map(this::toDomain).collect(Collectors.toList());
    }

    /**
     * 查询 find By Post Ids 对应的数据。
     *
     * @param postIds postIds 参数
     * @return 处理结果
     */
    @Override
    public List<User> findByPostIds(Collection<Long> postIds) {
        if (CollUtil.isEmpty(postIds)) return Collections.emptyList();
        Set<Long> userIds = convertSet(userPostMapper.selectListByPostIds(postIds), UserPostDO::getUserId);
        if (CollUtil.isEmpty(userIds)) return Collections.emptyList();
        return userMapper.selectByIds(userIds).stream().map(this::toDomain).collect(Collectors.toList());
    }

    /**
     * 查询 find By Nickname 对应的数据。
     *
     * @param nickname nickname 参数
     * @return 处理结果
     */
    @Override
    public List<User> findByNickname(String nickname) {
        return userMapper.selectListByNickname(nickname).stream().map(this::toDomain).collect(Collectors.toList());
    }

    /**
     * 查询 find By Status 对应的数据。
     *
     * @param status status 参数
     * @return 处理结果
     */
    @Override
    public List<User> findByStatus(UserStatus status) {
        return userMapper.selectListByStatus(status.code()).stream().map(this::toDomain).collect(Collectors.toList());
    }

    /**
     * 查询 find Page 对应的数据。
     *
     * @param query query 参数
     * @return 处理结果
     */
    @Override
    public PageResult<User> findPage(UserPageQuery query) {
        var reqVO = new com.develop.mvp.pk.module.system.controller.admin.user.vo.user.UserPageReqVO();
        reqVO.setUsername(query.username());
        reqVO.setMobile(query.mobile());
        reqVO.setStatus(query.status());
        reqVO.setPageNo(query.pageNo());
        reqVO.setPageSize(query.pageSize());

        PageResult<AdminUserDO> doPage = userMapper.selectPage(reqVO, query.deptIds(), query.userIds());
        List<User> users = doPage.getList().stream().map(this::toDomain).collect(Collectors.toList());
        return new PageResult<>(users, doPage.getTotal());
    }

    /**
     * 执行 exists By Username 对应的业务操作。
     *
     * @param username username 参数
     * @return 处理结果
     */
    @Override
    public boolean existsByUsername(Username username) {
        return userMapper.selectByUsername(username.value()) != null;
    }

    /**
     * 执行 exists By Email 对应的业务操作。
     *
     * @param email email 参数
     * @return 处理结果
     */
    @Override
    public boolean existsByEmail(Email email) {
        return email.isPresent() && userMapper.selectByEmail(email.value()) != null;
    }

    /**
     * 执行 exists By Mobile 对应的业务操作。
     *
     * @param mobile mobile 参数
     * @return 处理结果
     */
    @Override
    public boolean existsByMobile(Mobile mobile) {
        return mobile.isPresent() && userMapper.selectByMobile(mobile.value()) != null;
    }

    /**
     * 查询 count 对应的数据。
     *
     * @return 处理结果
     */
    @Override
    public long count() {
        return userMapper.selectCount();
    }

    /**
     * 执行 to Data Object 对应的业务操作。
     *
     * @param user user 参数
     * @return 处理结果
     */
    private AdminUserDO toDataObject(User user) {
        AdminUserDO userDO = new AdminUserDO();
        userDO.setId(user.id().value());
        userDO.setUsername(user.username().value());
        userDO.setPassword(user.password().toStoreValue());
        userDO.setTenantId(user.tenantId());
        userDO.setDeptId(user.deptId());
        userDO.setEmail(user.email().isPresent() ? user.email().value() : null);
        userDO.setMobile(user.mobile().isPresent() ? user.mobile().value() : null);
        userDO.setNickname(user.profile().nickname());
        userDO.setAvatar(user.profile().avatar());
        userDO.setSex(user.profile().sex());
        userDO.setRemark(user.profile().remark());
        userDO.setStatus(user.status().code());
        userDO.setPostIds(new HashSet<>(user.postIds()));
        if (user.lastLogin() != null) {
            userDO.setLoginIp(user.lastLogin().loginIp());
            userDO.setLoginDate(user.lastLogin().loginDate());
        }
        return userDO;
    }

    /**
     * 执行 to Domain 对应的业务操作。
     *
     * @param userDO userDO 参数
     * @return 处理结果
     */
    private User toDomain(AdminUserDO userDO) {
        Set<Long> postIds = convertSet(
                userPostMapper.selectListByUserId(userDO.getId()),
                UserPostDO::getPostId
        );
        return UserFactory.reconstitute(
                userDO.getId(),
                userDO.getUsername(),
                userDO.getPassword(),
                userDO.getTenantId(),
                userDO.getDeptId(),
                userDO.getEmail(),
                userDO.getMobile(),
                userDO.getNickname(),
                userDO.getAvatar(),
                userDO.getSex(),
                userDO.getRemark(),
                userDO.getStatus(),
                postIds,
                userDO.getLoginIp(),
                userDO.getLoginDate()
        );
    }

    /**
     * 执行 sync User Posts 对应的业务操作。
     *
     * @param user user 参数
     */
    private void syncUserPosts(User user) {
        Long userId = user.id().value();
        Set<Long> dbPostIds = convertSet(userPostMapper.selectListByUserId(userId), UserPostDO::getPostId);
        Set<Long> newPostIds = user.postIds();
        Collection<Long> createPostIds = CollUtil.subtract(newPostIds, dbPostIds);
        Collection<Long> deletePostIds = CollUtil.subtract(dbPostIds, newPostIds);
        if (!createPostIds.isEmpty()) {
            userPostMapper.insertBatch(createPostIds.stream()
                    .map(postId -> new UserPostDO().setUserId(userId).setPostId(postId))
                    .collect(Collectors.toList()));
        }
        if (!deletePostIds.isEmpty()) {
            userPostMapper.deleteByUserIdAndPostId(userId, deletePostIds);
        }
    }
}
