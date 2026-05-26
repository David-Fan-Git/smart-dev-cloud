package com.develop.mvp.pk.module.system.application.user.service;

import com.develop.mvp.pk.module.system.application.dept.port.inbound.DeptUseCase;
import com.develop.mvp.pk.module.system.application.user.port.inbound.AdminUserUseCase;
import com.develop.mvp.pk.module.system.application.user.port.inbound.UserUseCase;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.enums.UserTypeEnum;
import com.develop.mvp.pk.framework.common.exception.ServiceException;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.collection.CollectionUtils;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.framework.common.util.validation.ValidationUtils;
import com.develop.mvp.pk.framework.datapermission.core.util.DataPermissionUtils;
import com.develop.mvp.pk.module.infra.api.config.ConfigApi;
import com.develop.mvp.pk.module.system.controller.admin.auth.vo.AuthRegisterReqVO;
import com.develop.mvp.pk.module.system.controller.admin.user.vo.profile.UserProfileUpdatePasswordReqVO;
import com.develop.mvp.pk.module.system.controller.admin.user.vo.profile.UserProfileUpdateReqVO;
import com.develop.mvp.pk.module.system.controller.admin.user.vo.user.UserImportExcelVO;
import com.develop.mvp.pk.module.system.controller.admin.user.vo.user.UserImportRespVO;
import com.develop.mvp.pk.module.system.controller.admin.user.vo.user.UserPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.user.vo.user.UserSaveReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.dept.DeptDO;
import com.develop.mvp.pk.module.system.dal.dataobject.dept.UserPostDO;
import com.develop.mvp.pk.module.system.dal.dataobject.user.AdminUserDO;
import com.develop.mvp.pk.module.system.dal.mysql.dept.UserPostMapper;
import com.develop.mvp.pk.module.system.application.oauth2.port.inbound.OAuth2UseCase;
import com.develop.mvp.pk.module.system.application.tenant.port.inbound.TenantUseCase;
import com.develop.mvp.pk.module.system.dal.mysql.user.AdminUserMapper;
import com.develop.mvp.pk.module.system.application.permission.port.inbound.PermissionUseCase;
import com.google.common.annotations.VisibleForTesting;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.*;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.*;
import static com.develop.mvp.pk.module.system.enums.LogRecordConstants.*;

/**
 * 后台用户 Service 实现类
 *
 * @author David
 */
@Slf4j
public class AdminUserApplicationService implements AdminUserUseCase {

    public static final String USER_INIT_PASSWORD_KEY = "system.user.init-password";
    public static final String USER_REGISTER_ENABLED_KEY = "system.user.register-enabled";

    private final AdminUserMapper userMapper;
    private final UserUseCase userApplicationService;
    private final DeptUseCase deptUseCase;
    private final DeptUseCase postUseCase;
    private final PermissionUseCase permissionService;
    private final PasswordEncoder passwordEncoder;
    private final TenantUseCase tenantService;
    private final OAuth2UseCase oauth2TokenService;
    private final UserPostMapper userPostMapper;
    private final ConfigApi configApi;

    /**
     * 创建 AdminUserApplicationService 实例。
     *
     * @param userMapper userMapper 参数
     * @param userApplicationService userApplicationService 参数
     * @param deptUseCase deptUseCase 参数
     * @param postUseCase postUseCase 参数
     * @param permissionService permissionService 参数
     * @param passwordEncoder passwordEncoder 参数
     * @param tenantService tenantService 参数
     * @param oauth2TokenService oauth2TokenService 参数
     * @param userPostMapper userPostMapper 参数
     * @param configApi configApi 参数
     */
    public AdminUserApplicationService(AdminUserMapper userMapper,
                                       UserUseCase userApplicationService,
                                       DeptUseCase deptUseCase,
                                       DeptUseCase postUseCase,
                                       PermissionUseCase permissionService,
                                       PasswordEncoder passwordEncoder,
                                       @Lazy TenantUseCase tenantService,
                                       @Lazy OAuth2UseCase oauth2TokenService,
                                       UserPostMapper userPostMapper,
                                       ConfigApi configApi) {
        this.userMapper = userMapper;
        this.userApplicationService = userApplicationService;
        this.deptUseCase = deptUseCase;
        this.postUseCase = postUseCase;
        this.permissionService = permissionService;
        this.passwordEncoder = passwordEncoder;
        this.tenantService = tenantService;
        this.oauth2TokenService = oauth2TokenService;
        this.userPostMapper = userPostMapper;
        this.configApi = configApi;
    }

    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = SYSTEM_USER_TYPE, subType = SYSTEM_USER_CREATE_SUB_TYPE, bizNo = "{{#user.id}}",
            success = SYSTEM_USER_CREATE_SUCCESS)
    /**
     * 创建 create User 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @return 处理结果
     */
    public Long createUser(UserSaveReqVO createReqVO) {
        // 1.1 校验账户配合
        tenantService.handleTenantInfo(tenant -> {
            long count = userMapper.selectCount();
            if (count >= tenant.getAccountCount()) {
                throw exception(USER_COUNT_MAX, tenant.getAccountCount());
            }
        });
        // 1.2 校验正确性
        validateUserForCreateOrUpdate(null, createReqVO.getUsername(),
                createReqVO.getMobile(), createReqVO.getEmail(), createReqVO.getDeptId(), createReqVO.getPostIds());
        // 2.1 插入用户
        AdminUserDO user = BeanUtils.toBean(createReqVO, AdminUserDO.class);
        user.setStatus(CommonStatusEnum.ENABLE.getStatus()); // 默认开启
        user.setPassword(encodePassword(createReqVO.getPassword())); // 加密密码
        userMapper.insert(user);
        // 2.2 插入关联岗位
        if (CollectionUtil.isNotEmpty(user.getPostIds())) {
            userPostMapper.insertBatch(convertList(user.getPostIds(),
                    postId -> new UserPostDO().setUserId(user.getId()).setPostId(postId)));
        }

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("user", user);
        return user.getId();
    }

    /**
     * 处理 register User 对应的认证流程。
     *
     * @param registerReqVO registerReqVO 参数
     * @return 处理结果
     */
    public Long registerUser(AuthRegisterReqVO registerReqVO) {
        // 1.1 校验是否开启注册
        if (ObjUtil.notEqual(configApi.getConfigValueByKey(USER_REGISTER_ENABLED_KEY).getCheckedData(), "true")) {
            throw exception(USER_REGISTER_DISABLED);
        }
        // 1.2 校验账户配合
        tenantService.handleTenantInfo(tenant -> {
            long count = userMapper.selectCount();
            if (count >= tenant.getAccountCount()) {
                throw exception(USER_COUNT_MAX, tenant.getAccountCount());
            }
        });
        // 1.3 校验正确性
        validateUserForCreateOrUpdate(null, registerReqVO.getUsername(), null, null, null, null);

        // 2. 插入用户
        AdminUserDO user = BeanUtils.toBean(registerReqVO, AdminUserDO.class);
        user.setStatus(CommonStatusEnum.ENABLE.getStatus()); // 默认开启
        user.setPassword(encodePassword(registerReqVO.getPassword())); // 加密密码
        userMapper.insert(user);
        return user.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = SYSTEM_USER_TYPE, subType = SYSTEM_USER_UPDATE_SUB_TYPE, bizNo = "{{#updateReqVO.id}}",
            success = SYSTEM_USER_UPDATE_SUCCESS)
    /**
     * 更新 update User 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     */
    public void updateUser(UserSaveReqVO updateReqVO) {
        updateReqVO.setPassword(null); // 特殊：此处不更新密码
        // 1. 校验正确性
        AdminUserDO oldUser = validateUserForCreateOrUpdate(updateReqVO.getId(), updateReqVO.getUsername(),
                updateReqVO.getMobile(), updateReqVO.getEmail(), updateReqVO.getDeptId(), updateReqVO.getPostIds());

        // 2.1 更新用户
        AdminUserDO updateObj = BeanUtils.toBean(updateReqVO, AdminUserDO.class);
        userMapper.updateById(updateObj);
        // 2.2 更新岗位
        updateUserPost(updateReqVO, updateObj);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(oldUser, UserSaveReqVO.class));
        LogRecordContext.putVariable("user", oldUser);
    }

    /**
     * 更新 update User Post 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @param updateObj updateObj 参数
     */
    private void updateUserPost(UserSaveReqVO reqVO, AdminUserDO updateObj) {
        Long userId = reqVO.getId();
        Set<Long> dbPostIds = convertSet(userPostMapper.selectListByUserId(userId), UserPostDO::getPostId);
        // 计算新增和删除的岗位编号
        Set<Long> postIds = CollUtil.emptyIfNull(updateObj.getPostIds());
        Collection<Long> createPostIds = CollUtil.subtract(postIds, dbPostIds);
        Collection<Long> deletePostIds = CollUtil.subtract(dbPostIds, postIds);
        // 执行新增和删除。对于已经授权的岗位，不用做任何处理
        if (!CollectionUtil.isEmpty(createPostIds)) {
            userPostMapper.insertBatch(convertList(createPostIds,
                    postId -> new UserPostDO().setUserId(userId).setPostId(postId)));
        }
        if (!CollectionUtil.isEmpty(deletePostIds)) {
            userPostMapper.deleteByUserIdAndPostId(userId, deletePostIds);
        }
    }

    /**
     * 更新 update User Login 对应的数据。
     *
     * @param id id 参数
     * @param loginIp loginIp 参数
     */
    public void updateUserLogin(Long id, String loginIp) {
        userApplicationService.recordLogin(id, loginIp);
    }

    /**
     * 更新 update User Profile 对应的数据。
     *
     * @param id id 参数
     * @param reqVO reqVO 参数
     */
    public void updateUserProfile(Long id, UserProfileUpdateReqVO reqVO) {
        userApplicationService.updateProfile(id, reqVO.getEmail(), reqVO.getMobile(),
                reqVO.getNickname(), reqVO.getAvatar(), reqVO.getSex(), null);
    }

    /**
     * 更新 update User Password 对应的数据。
     *
     * @param id id 参数
     * @param reqVO reqVO 参数
     */
    public void updateUserPassword(Long id, UserProfileUpdatePasswordReqVO reqVO) {
        userApplicationService.changePassword(id, reqVO.getOldPassword(), reqVO.getNewPassword());
    }

    @LogRecord(type = SYSTEM_USER_TYPE, subType = SYSTEM_USER_UPDATE_PASSWORD_SUB_TYPE, bizNo = "{{#id}}",
            success = SYSTEM_USER_UPDATE_PASSWORD_SUCCESS)
    /**
     * 更新 update User Password 对应的数据。
     *
     * @param id id 参数
     * @param password password 参数
     */
    public void updateUserPassword(Long id, String password) {
        AdminUserDO user = validateUserExists(id);
        userApplicationService.resetPassword(id, password);
        LogRecordContext.putVariable("user", user);
        LogRecordContext.putVariable("newPassword", userMapper.selectById(id).getPassword());
    }

    /**
     * 更新 update User Status 对应的数据。
     *
     * @param id id 参数
     * @param status status 参数
     */
    public void updateUserStatus(Long id, Integer status) {
        userApplicationService.updateUserStatus(id, status);
        if (CommonStatusEnum.isDisable(status)) {
            oauth2TokenService.removeAccessToken(id, UserTypeEnum.ADMIN.getValue());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = SYSTEM_USER_TYPE, subType = SYSTEM_USER_DELETE_SUB_TYPE, bizNo = "{{#id}}",
            success = SYSTEM_USER_DELETE_SUCCESS)
    /**
     * 删除 delete User 对应的数据。
     *
     * @param id id 参数
     */
    public void deleteUser(Long id) {
        // 1. 校验用户存在
        AdminUserDO user = validateUserExists(id);

        // 2.1 删除用户
        userMapper.deleteById(id);
        // 2.2 删除用户关联数据
        permissionService.processUserDeleted(id);
        // 2.2 删除用户岗位
        userPostMapper.deleteByUserId(id);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("user", user);
    }

    /**
     * 删除 delete User List 对应的数据。
     *
     * @param ids ids 参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserList(List<Long> ids) {
        // 1. 批量删除用户
        userMapper.deleteByIds(ids);

        // 2. 批量删除用户关联数据
        ids.forEach(id -> {
            permissionService.processUserDeleted(id);
            userPostMapper.deleteByUserId(id);
        });
    }

    /**
     * 查询 get User By Username 对应的数据。
     *
     * @param username username 参数
     * @return 处理结果
     */
    public AdminUserDO getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    /**
     * 查询 get User By Mobile 对应的数据。
     *
     * @param mobile mobile 参数
     * @return 处理结果
     */
    public AdminUserDO getUserByMobile(String mobile) {
        return userMapper.selectByMobile(mobile);
    }

    /**
     * 查询 get User Page 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    public PageResult<AdminUserDO> getUserPage(UserPageReqVO reqVO) {
        // 如果有角色编号，查询角色对应的用户编号
        Set<Long> userIds = null;
        if (reqVO.getRoleId() != null) {
            userIds = permissionService.getUserRoleIdListByRoleId(singleton(reqVO.getRoleId()));
            if (CollUtil.isEmpty(userIds)) {
                return PageResult.empty();
            }
        }

        // 分页查询
        return userMapper.selectPage(reqVO, getDeptCondition(reqVO.getDeptId()), userIds);
    }

    /**
     * 查询 get User 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    public AdminUserDO getUser(Long id) {
        return userMapper.selectById(id);
    }

    /**
     * 查询 get User List By Dept Ids 对应的数据。
     *
     * @param deptIds deptIds 参数
     * @return 处理结果
     */
    public List<AdminUserDO> getUserListByDeptIds(Collection<Long> deptIds) {
        if (CollUtil.isEmpty(deptIds)) {
            return Collections.emptyList();
        }
        return userMapper.selectListByDeptIds(deptIds);
    }

    /**
     * 查询 get User List By Post Ids 对应的数据。
     *
     * @param postIds postIds 参数
     * @return 处理结果
     */
    public List<AdminUserDO> getUserListByPostIds(Collection<Long> postIds) {
        if (CollUtil.isEmpty(postIds)) {
            return Collections.emptyList();
        }
        Set<Long> userIds = convertSet(userPostMapper.selectListByPostIds(postIds), UserPostDO::getUserId);
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        return userMapper.selectByIds(userIds);
    }

    /**
     * 查询 get User List 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    public List<AdminUserDO> getUserList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return userMapper.selectByIds(ids);
    }

    /**
     * 查询 get User Map 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    public Map<Long, AdminUserDO> getUserMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return new HashMap<>();
        }
        return CollectionUtils.convertMap(getUserList(ids), AdminUserDO::getId);
    }

    /**
     * 校验 validate User List 对应的业务规则。
     *
     * @param ids ids 参数
     */
    public void validateUserList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 获得岗位信息
        List<AdminUserDO> users = userMapper.selectByIds(ids);
        Map<Long, AdminUserDO> userMap = CollectionUtils.convertMap(users, AdminUserDO::getId);
        // 校验
        ids.forEach(id -> {
            AdminUserDO user = userMap.get(id);
            if (user == null) {
                throw exception(USER_NOT_EXISTS);
            }
            if (!CommonStatusEnum.ENABLE.getStatus().equals(user.getStatus())) {
                throw exception(USER_IS_DISABLE, user.getNickname());
            }
        });
    }

    /**
     * 查询 get User List By Nickname 对应的数据。
     *
     * @param nickname nickname 参数
     * @return 处理结果
     */
    public List<AdminUserDO> getUserListByNickname(String nickname) {
        return userMapper.selectListByNickname(nickname);
    }

    /**
     * 获得部门条件：查询指定部门的子部门编号们，包括自身
     *
     * @param deptId 部门编号
     * @return 部门编号集合
     */
    private Set<Long> getDeptCondition(Long deptId) {
        if (deptId == null) {
            return Collections.emptySet();
        }
        Set<Long> deptIds = convertSet(deptUseCase.getChildDeptList(deptId), DeptDO::getId);
        deptIds.add(deptId); // 包括自身
        return deptIds;
    }

    /**
     * 校验 validate User For Create Or Update 对应的业务规则。
     *
     * @param id id 参数
     * @param username username 参数
     * @param mobile mobile 参数
     * @param email email 参数
     * @param deptId deptId 参数
     * @param postIds postIds 参数
     * @return 处理结果
     */
    private AdminUserDO validateUserForCreateOrUpdate(Long id, String username, String mobile, String email,
                                               Long deptId, Set<Long> postIds) {
        // 关闭数据权限，避免因为没有数据权限，查询不到数据，进而导致唯一校验不正确
        return DataPermissionUtils.executeIgnore(() -> {
            // 校验用户存在
            AdminUserDO user = validateUserExists(id);
            // 校验用户名唯一
            validateUsernameUnique(id, username);
            // 校验手机号唯一
            validateMobileUnique(id, mobile);
            // 校验邮箱唯一
            validateEmailUnique(id, email);
            // 校验部门处于开启状态
            deptUseCase.validateDeptList(CollectionUtils.singleton(deptId));
            // 校验岗位处于开启状态
            postUseCase.validatePostList(postIds);
            return user;
        });
    }

    /**
     * 校验 validate User Exists 对应的业务规则。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @VisibleForTesting
    public AdminUserDO validateUserExists(Long id) {
        if (id == null) {
            return null;
        }
        AdminUserDO user = userMapper.selectById(id);
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }
        return user;
    }

    /**
     * 校验 validate Username Unique 对应的业务规则。
     *
     * @param id id 参数
     * @param username username 参数
     */
    @VisibleForTesting
    public void validateUsernameUnique(Long id, String username) {
        if (StrUtil.isBlank(username)) {
            return;
        }
        AdminUserDO user = userMapper.selectByUsername(username);
        if (user == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的用户
        if (id == null) {
            throw exception(USER_USERNAME_EXISTS);
        }
        if (!user.getId().equals(id)) {
            throw exception(USER_USERNAME_EXISTS);
        }
    }

    /**
     * 校验 validate Email Unique 对应的业务规则。
     *
     * @param id id 参数
     * @param email email 参数
     */
    @VisibleForTesting
    public void validateEmailUnique(Long id, String email) {
        if (StrUtil.isBlank(email)) {
            return;
        }
        AdminUserDO user = userMapper.selectByEmail(email);
        if (user == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的用户
        if (id == null) {
            throw exception(USER_EMAIL_EXISTS);
        }
        if (!user.getId().equals(id)) {
            throw exception(USER_EMAIL_EXISTS);
        }
    }

    /**
     * 校验 validate Mobile Unique 对应的业务规则。
     *
     * @param id id 参数
     * @param mobile mobile 参数
     */
    @VisibleForTesting
    public void validateMobileUnique(Long id, String mobile) {
        if (StrUtil.isBlank(mobile)) {
            return;
        }
        AdminUserDO user = userMapper.selectByMobile(mobile);
        if (user == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的用户
        if (id == null) {
            throw exception(USER_MOBILE_EXISTS);
        }
        if (!user.getId().equals(id)) {
            throw exception(USER_MOBILE_EXISTS);
        }
    }

    /**
     * 校验旧密码
     * @param id          用户 id
     * @param oldPassword 旧密码
     */
    @VisibleForTesting
    public void validateOldPassword(Long id, String oldPassword) {
        AdminUserDO user = userMapper.selectById(id);
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }
        if (!isPasswordMatch(oldPassword, user.getPassword())) {
            throw exception(USER_PASSWORD_FAILED);
        }
    }

    /**
     * 执行 import User List 对应的业务操作。
     *
     * @param importUsers importUsers 参数
     * @param isUpdateSupport isUpdateSupport 参数
     * @return 处理结果
     */
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public UserImportRespVO importUserList(List<UserImportExcelVO> importUsers, boolean isUpdateSupport) {
        // 1.1 参数校验
        if (CollUtil.isEmpty(importUsers)) {
            throw exception(USER_IMPORT_LIST_IS_EMPTY);
        }
        // 1.2 初始化密码不能为空
        String initPassword = configApi.getConfigValueByKey(USER_INIT_PASSWORD_KEY).getCheckedData();
        if (StrUtil.isEmpty(initPassword)) {
            throw exception(USER_IMPORT_INIT_PASSWORD);
        }

        // 2. 遍历，逐个创建 or 更新
        UserImportRespVO respVO = UserImportRespVO.builder().createUsernames(new ArrayList<>())
                .updateUsernames(new ArrayList<>()).failureUsernames(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importUsers.forEach(importUser -> {
            int currentIndex = index.getAndIncrement();
            // 2.1.1 校验字段是否符合要求
            try {
                ValidationUtils.validate(BeanUtils.toBean(importUser, UserSaveReqVO.class).setPassword(initPassword));
            } catch (ConstraintViolationException ex) {
                String key = StrUtil.blankToDefault(importUser.getUsername(), "第 " + currentIndex + " 行");
                respVO.getFailureUsernames().put(key, ex.getMessage());
                return;
            }
            // 2.1.2 校验，判断是否有不符合的原因
            try {
                validateUserForCreateOrUpdate(null, null, importUser.getMobile(), importUser.getEmail(),
                        importUser.getDeptId(), null);
            } catch (ServiceException ex) {
                respVO.getFailureUsernames().put(importUser.getUsername(), ex.getMessage());
                return;
            }

            // 2.2.1 判断如果不存在，在进行插入
            AdminUserDO existUser = userMapper.selectByUsername(importUser.getUsername());
            if (existUser == null) {
                userMapper.insert(BeanUtils.toBean(importUser, AdminUserDO.class)
                        .setPassword(encodePassword(initPassword)).setPostIds(new HashSet<>())); // 设置默认密码及空岗位编号数组
                respVO.getCreateUsernames().add(importUser.getUsername());
                return;
            }
            // 2.2.2 如果存在，判断是否允许更新
            if (!isUpdateSupport) {
                respVO.getFailureUsernames().put(importUser.getUsername(), USER_USERNAME_EXISTS.getMsg());
                return;
            }
            AdminUserDO updateUser = BeanUtils.toBean(importUser, AdminUserDO.class);
            updateUser.setId(existUser.getId());
            userMapper.updateById(updateUser);
            respVO.getUpdateUsernames().add(importUser.getUsername());
        });
        return respVO;
    }

    /**
     * 查询 get User List By Status 对应的数据。
     *
     * @param status status 参数
     * @return 处理结果
     */
    public List<AdminUserDO> getUserListByStatus(Integer status) {
        return userMapper.selectListByStatus(status);
    }

    /**
     * 判断 is Password Match 对应的条件是否成立。
     *
     * @param rawPassword rawPassword 参数
     * @param encodedPassword encodedPassword 参数
     * @return 处理结果
     */
    public boolean isPasswordMatch(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 对密码进行加密
     *
     * @param password 密码
     * @return 加密后的密码
     */
    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

}
