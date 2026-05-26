package com.develop.mvp.pk.module.system.application.user.service;

import com.develop.mvp.pk.module.system.application.dept.port.inbound.DeptUseCase;
import com.develop.mvp.pk.module.system.application.user.port.inbound.UserUseCase;
import cn.hutool.core.collection.CollUtil;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.collection.CollectionUtils;
import com.develop.mvp.pk.framework.datapermission.core.util.DataPermissionUtils;
import com.develop.mvp.pk.framework.tenant.core.context.TenantContextHolder;
import com.develop.mvp.pk.module.system.domain.user.User;
import com.develop.mvp.pk.module.system.domain.user.UserFactory;
import com.develop.mvp.pk.module.system.domain.user.event.DomainEvent;
import com.develop.mvp.pk.module.system.domain.user.event.DomainEventPublisher;
import com.develop.mvp.pk.module.system.domain.user.repository.UserPageQuery;
import com.develop.mvp.pk.module.system.domain.user.repository.UserRepository;
import com.develop.mvp.pk.module.system.domain.user.service.PasswordEncoder;
import com.develop.mvp.pk.module.system.domain.user.service.UserUniquenessChecker;
import com.develop.mvp.pk.module.system.domain.user.valueobject.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.*;

/**
 * User Application Service 应用服务。
 */
public class UserApplicationService implements UserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserUniquenessChecker uniquenessChecker;
    private final DomainEventPublisher eventPublisher;
    private final DeptUseCase deptUseCase;
    private final DeptUseCase postUseCase;

    /**
     * 创建 UserApplicationService 实例。
     *
     * @param userRepository userRepository 参数
     * @param passwordEncoder passwordEncoder 参数
     * @param uniquenessChecker uniquenessChecker 参数
     * @param eventPublisher eventPublisher 参数
     * @param deptUseCase deptUseCase 参数
     * @param postUseCase postUseCase 参数
     */
    public UserApplicationService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                   UserUniquenessChecker uniquenessChecker,
                                   DomainEventPublisher eventPublisher,
                                   DeptUseCase deptUseCase, DeptUseCase postUseCase) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.uniquenessChecker = uniquenessChecker;
        this.eventPublisher = eventPublisher;
        this.deptUseCase = deptUseCase;
        this.postUseCase = postUseCase;
    }

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
    @Transactional
    public Long createUser(Long id, String username, String rawPassword, Long tenantId,
                            Long deptId, String email, String mobile,
                            String nickname, String avatar, Integer sex, String remark,
                            Set<Long> postIds) {
        validateDeptAndPosts(deptId, postIds);
        assertUsernameUnique(Username.of(username), null);
        if (email != null) assertEmailUnique(Email.of(email), null);
        if (mobile != null) assertMobileUnique(Mobile.of(mobile), null);

        Long resolvedTenantId = tenantId != null ? tenantId : TenantContextHolder.getRequiredTenantId();
        EncodedPassword encoded = passwordEncoder.encode(RawPassword.of(rawPassword));
        User user = id == null
                ? userRepository.create(username, encoded, resolvedTenantId, deptId,
                        email, mobile, nickname, avatar, sex, remark, postIds)
                : UserFactory.create(id, username, encoded, resolvedTenantId, deptId,
                        email, mobile, nickname, avatar, sex, remark, postIds);
        if (id != null) {
            userRepository.save(user);
        }
        publishEvents(user);
        return user.id().value();
    }

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
    @Transactional
    public void updateUser(Long id, String username, String email, String mobile,
                            String nickname, String avatar, Integer sex, String remark,
                            Long deptId, Set<Long> postIds) {
        User user = findExistingUser(id);
        DataPermissionUtils.executeIgnore(() -> {
            assertUsernameUnique(Username.of(username), UserId.of(id));
            if (email != null) assertEmailUnique(Email.of(email), UserId.of(id));
            if (mobile != null) assertMobileUnique(Mobile.of(mobile), UserId.of(id));
            return null;
        });
        validateDeptAndPosts(deptId, postIds);
        user.updateContact(Email.of(email), Mobile.of(mobile), uniquenessChecker);
        user.updateProfile(UserProfile.of(nickname, avatar, sex, remark));
        user.syncPosts(postIds);
        userRepository.save(user);
        publishEvents(user);
    }

    /**
     * 更新 update User Status 对应的数据。
     *
     * @param id id 参数
     * @param statusCode statusCode 参数
     */
    @Transactional
    public void updateUserStatus(Long id, Integer statusCode) {
        User user = findExistingUser(id);
        UserStatus newStatus = UserStatus.of(statusCode);
        if (newStatus.isDisabled()) {
            user.disable();
        } else {
            user.enable();
        }
        userRepository.save(user);
        publishEvents(user);
    }

    /**
     * 更新 change Password 对应的数据。
     *
     * @param id id 参数
     * @param oldRawPassword oldRawPassword 参数
     * @param newRawPassword newRawPassword 参数
     */
    @Transactional
    public void changePassword(Long id, String oldRawPassword, String newRawPassword) {
        User user = findExistingUser(id);
        user.changePassword(RawPassword.forVerification(oldRawPassword),
                RawPassword.of(newRawPassword), passwordEncoder);
        userRepository.save(user);
        publishEvents(user);
    }

    /**
     * 更新 reset Password 对应的数据。
     *
     * @param id id 参数
     * @param newRawPassword newRawPassword 参数
     */
    @Transactional
    public void resetPassword(Long id, String newRawPassword) {
        User user = findExistingUser(id);
        user.resetPassword(RawPassword.of(newRawPassword), passwordEncoder);
        userRepository.save(user);
        publishEvents(user);
    }

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
    @Transactional
    public void updateProfile(Long id, String email, String mobile,
                               String nickname, String avatar, Integer sex, String remark) {
        User user = findExistingUser(id);
        if (email != null) assertEmailUnique(Email.of(email), UserId.of(id));
        if (mobile != null) assertMobileUnique(Mobile.of(mobile), UserId.of(id));
        user.updateContact(Email.of(email), Mobile.of(mobile), uniquenessChecker);
        user.updateProfile(UserProfile.of(nickname, avatar, sex, remark));
        userRepository.save(user);
        publishEvents(user);
    }

    /**
     * 执行 record Login 对应的业务操作。
     *
     * @param id id 参数
     * @param loginIp loginIp 参数
     */
    @Transactional
    public void recordLogin(Long id, String loginIp) {
        User user = findExistingUser(id);
        user.recordLogin(LoginRecord.of(loginIp));
        userRepository.save(user);
        publishEvents(user);
    }

    /**
     * 删除 delete User 对应的数据。
     *
     * @param id id 参数
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = findExistingUser(id);
        user.markDeleted();
        userRepository.delete(user.id());
        publishEvents(user);
    }

    /**
     * 删除 delete User List 对应的数据。
     *
     * @param ids ids 参数
     */
    @Transactional
    public void deleteUserList(List<Long> ids) {
        for (Long id : ids) {
            User user = findExistingUser(id);
            user.markDeleted();
            userRepository.delete(user.id());
            publishEvents(user);
        }
    }

    // ── 查询 ──

    /**
     * 查询 get User 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    public User getUser(Long id) {
        return userRepository.findById(UserId.of(id));
    }

    /**
     * 查询 get User By Username 对应的数据。
     *
     * @param username username 参数
     * @return 处理结果
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(Username.of(username)).orElse(null);
    }

    /**
     * 查询 get User Page 对应的数据。
     *
     * @param query query 参数
     * @return 处理结果
     */
    public PageResult<User> getUserPage(UserPageQuery query) {
        return userRepository.findPage(query);
    }

    /**
     * 查询 get User List 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    public List<User> getUserList(Collection<Long> ids) {
        return userRepository.findByIds(ids.stream().map(UserId::of).collect(Collectors.toList()));
    }

    /**
     * 查询 get User List By Dept Ids 对应的数据。
     *
     * @param deptIds deptIds 参数
     * @return 处理结果
     */
    public List<User> getUserListByDeptIds(Collection<Long> deptIds) {
        return userRepository.findByDeptIds(deptIds);
    }

    /**
     * 查询 get User List By Post Ids 对应的数据。
     *
     * @param postIds postIds 参数
     * @return 处理结果
     */
    public List<User> getUserListByPostIds(Collection<Long> postIds) {
        return userRepository.findByPostIds(postIds);
    }

    /**
     * 查询 get User List By Status 对应的数据。
     *
     * @param status status 参数
     * @return 处理结果
     */
    public List<User> getUserListByStatus(Integer status) {
        return userRepository.findByStatus(UserStatus.of(status));
    }

    /**
     * 查询 get User List By Nickname 对应的数据。
     *
     * @param nickname nickname 参数
     * @return 处理结果
     */
    public List<User> getUserListByNickname(String nickname) {
        return userRepository.findByNickname(nickname);
    }

    /**
     * 校验 validate User List 对应的业务规则。
     *
     * @param ids ids 参数
     */
    public void validateUserList(Collection<Long> ids) {
        List<User> users = userRepository.findByIds(
                ids.stream().map(UserId::of).collect(Collectors.toList()));
        Map<Long, User> userMap = CollectionUtils.convertMap(users, u -> u.id().value());
        for (Long id : ids) {
            User user = userMap.get(id);
            if (user == null) throw exception(USER_NOT_EXISTS);
            if (user.isDisabled()) throw exception(USER_IS_DISABLE, user.profile().nickname());
        }
    }

    /** 获取部门范围条件 */
    public Set<Long> getDeptCondition(Long deptId) {
        if (deptId == null) return Collections.emptySet();
        Set<Long> deptIds = CollectionUtils.convertSet(
                deptUseCase.getChildDeptList(deptId),
                d -> d.getId());
        deptIds.add(deptId);
        return deptIds;
    }

    /**
     * 查询 find Existing User 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    private User findExistingUser(Long id) {
        User user = userRepository.findById(UserId.of(id));
        if (user == null) throw exception(USER_NOT_EXISTS);
        return user;
    }

    /**
     * 校验 validate Dept And Posts 对应的业务规则。
     *
     * @param deptId deptId 参数
     * @param postIds postIds 参数
     */
    private void validateDeptAndPosts(Long deptId, Set<Long> postIds) {
        deptUseCase.validateDeptList(CollectionUtils.singleton(deptId));
        postUseCase.validatePostList(postIds);
    }

    /**
     * 执行 assert Username Unique 对应的业务操作。
     *
     * @param username username 参数
     * @param excludeId excludeId 参数
     */
    private void assertUsernameUnique(Username username, UserId excludeId) {
        Optional<User> existing = userRepository.findByUsername(username);
        if (existing.isPresent() && (excludeId == null || !existing.get().id().equals(excludeId))) {
            throw exception(USER_USERNAME_EXISTS);
        }
    }

    /**
     * 执行 assert Email Unique 对应的业务操作。
     *
     * @param email email 参数
     * @param excludeId excludeId 参数
     */
    private void assertEmailUnique(Email email, UserId excludeId) {
        if (!email.isPresent()) return;
        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isPresent() && (excludeId == null || !existing.get().id().equals(excludeId))) {
            throw exception(USER_EMAIL_EXISTS);
        }
    }

    /**
     * 执行 assert Mobile Unique 对应的业务操作。
     *
     * @param mobile mobile 参数
     * @param excludeId excludeId 参数
     */
    private void assertMobileUnique(Mobile mobile, UserId excludeId) {
        if (!mobile.isPresent()) return;
        Optional<User> existing = userRepository.findByMobile(mobile);
        if (existing.isPresent() && (excludeId == null || !existing.get().id().equals(excludeId))) {
            throw exception(USER_MOBILE_EXISTS);
        }
    }

    /**
     * 发送 publish Events 对应的消息。
     *
     * @param user user 参数
     */
    private void publishEvents(User user) {
        for (DomainEvent event : user.pullEvents()) {
            eventPublisher.publish(event);
        }
    }
}
