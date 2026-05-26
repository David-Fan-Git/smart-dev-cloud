package com.develop.mvp.pk.module.system.domain.user;

// Skill: AggregateRoot_User_Validation_Skill — 聚合根 User
// DDD 角色：管理后台用户的领域聚合根，封装完整生命周期和业务规则
// 职责边界：参见技能文档 4.1 节 R01-R13
// 依赖倒置：仅在构造时接收领域服务接口，不依赖基础设施层
// 验收标准 AC01/AC02：无 MyBatis/Spring 注解，不注入 Mapper

import com.develop.mvp.pk.module.system.domain.user.event.*;
import com.develop.mvp.pk.module.system.domain.user.service.PasswordEncoder;
import com.develop.mvp.pk.module.system.domain.user.service.UserUniquenessChecker;
import com.develop.mvp.pk.module.system.domain.user.valueobject.*;

import java.util.*;

/**
 * User 领域模型。
 */
public final class User {

    // ── 聚合根标识 ──
    private final UserId id;

    // ── 核心标识值对象 ──
    private final Username username;
    private final Long tenantId;

    // ── 外部聚合 ID 引用（DDD 规范：仅通过 ID 引用外部聚合） ──
    private final Long deptId;

    // ── 可变属性（通过业务方法控制变更） ──
    private EncodedPassword password;
    private Email email;
    private Mobile mobile;
    private UserProfile profile;
    private UserStatus status;
    private LoginRecord lastLogin;

    // ── 聚合内部实体 ──
    private final Set<Long> postIds;

    // ── 领域事件收集 ──
    private final List<DomainEvent> events = new ArrayList<>();

    /**
     * 创建 User 实例。
     *
     * @param id id 参数
     * @param username username 参数
     * @param password password 参数
     * @param tenantId tenantId 参数
     * @param deptId deptId 参数
     * @param email email 参数
     * @param mobile mobile 参数
     * @param profile profile 参数
     * @param status status 参数
     * @param postIds postIds 参数
     */
    User(UserId id, Username username, EncodedPassword password, Long tenantId,
         Long deptId, Email email, Mobile mobile, UserProfile profile,
         UserStatus status, Set<Long> postIds) {
        this.id = Objects.requireNonNull(id, "userId 不能为空");
        this.username = Objects.requireNonNull(username, "username 不能为空");
        this.password = Objects.requireNonNull(password, "password 不能为空");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId 不能为空");
        this.deptId = deptId; // 可为 null
        this.email = email != null ? email : Email.empty();
        this.mobile = mobile != null ? mobile : Mobile.empty();
        this.profile = profile != null ? profile : UserProfile.of(null, null, null, null);
        this.status = status != null ? status : UserStatus.ENABLED; // 规则 R09
        this.postIds = postIds != null ? new HashSet<>(postIds) : new HashSet<>();
        this.lastLogin = null;
    }

    /**
     * 执行 record Created 对应的业务操作。
     */
    void recordCreated() {
        events.add(new UserCreatedEvent(this.id.value(), this.username.value(), this.tenantId));
    }

    /**
     * 更新 disable 对应的数据。
     */
    public void disable() {
        if (this.status.isDisabled()) return;
        this.status = this.status.disable();
        events.add(new UserDisabledEvent(this.id.value()));
    }

    /**
     * 更新 enable 对应的数据。
     */
    public void enable() {
        this.status = this.status.enable();
    }

    /**
     * 更新 change Password 对应的数据。
     *
     * @param oldPassword oldPassword 参数
     * @param newPassword newPassword 参数
     * @param encoder encoder 参数
     */
    public void changePassword(RawPassword oldPassword, RawPassword newPassword,
                                PasswordEncoder encoder) {
        if (!encoder.matches(oldPassword, this.password)) {
            throw new IllegalArgumentException("旧密码不匹配"); // R05
        }
        if (oldPassword.equals(newPassword)) {
            throw new IllegalArgumentException("新密码不能与旧密码相同"); // I11
        }
        this.password = encoder.encode(newPassword); // R06
        events.add(new UserPasswordChangedEvent(this.id.value()));
    }

    /**
     * 更新 reset Password 对应的数据。
     *
     * @param newPassword newPassword 参数
     * @param encoder encoder 参数
     */
    public void resetPassword(RawPassword newPassword, PasswordEncoder encoder) {
        this.password = encoder.encode(newPassword); // R06
        events.add(new UserPasswordChangedEvent(this.id.value()));
    }

    /**
     * 更新 update Profile 对应的数据。
     *
     * @param newProfile newProfile 参数
     */
    public void updateProfile(UserProfile newProfile) {
        this.profile = Objects.requireNonNull(newProfile, "profile 不能为空");
    }

    /**
     * 更新 update Contact 对应的数据。
     *
     * @param newEmail newEmail 参数
     * @param newMobile newMobile 参数
     * @param checker checker 参数
     */
    public void updateContact(Email newEmail, Mobile newMobile,
                               UserUniquenessChecker checker) {
        if (newEmail != null && newEmail.isPresent()
                && !checker.isEmailUnique(newEmail, this.id)) {
            throw new IllegalArgumentException("邮箱已被使用"); // R03
        }
        if (newMobile != null && newMobile.isPresent()
                && !checker.isMobileUnique(newMobile, this.id)) {
            throw new IllegalArgumentException("手机号已被使用"); // R04
        }
        this.email = newEmail != null ? newEmail : this.email;
        this.mobile = newMobile != null ? newMobile : this.mobile;
    }

    /**
     * 执行 record Login 对应的业务操作。
     *
     * @param loginRecord loginRecord 参数
     */
    public void recordLogin(LoginRecord loginRecord) {
        this.lastLogin = Objects.requireNonNull(loginRecord);
        events.add(new UserLoggedInEvent(this.id.value(),
                loginRecord.loginIp(), loginRecord.loginDate()));
    }

    /**
     * 执行 sync Posts 对应的业务操作。
     *
     * @param newPostIds newPostIds 参数
     */
    public void syncPosts(Set<Long> newPostIds) {
        this.postIds.clear();
        if (newPostIds != null) {
            this.postIds.addAll(newPostIds);
        }
    }

    /**
     * 执行 mark Deleted 对应的业务操作。
     */
    public void markDeleted() {
        events.add(new UserDeletedEvent(this.id.value(), this.username.value()));
    }

    // ── 查询方法 ──
    /**
     * 执行 id 对应的业务操作。
     *
     * @return 处理结果
     */
    public UserId id() { return id; }
    /**
     * 执行 username 对应的业务操作。
     *
     * @return 处理结果
     */
    public Username username() { return username; }
    /**
     * 执行 password 对应的业务操作。
     *
     * @return 处理结果
     */
    public EncodedPassword password() { return password; }
    /**
     * 执行 tenant Id 对应的业务操作。
     *
     * @return 处理结果
     */
    public Long tenantId() { return tenantId; }
    /**
     * 执行 dept Id 对应的业务操作。
     *
     * @return 处理结果
     */
    public Long deptId() { return deptId; }
    /**
     * 执行 email 对应的业务操作。
     *
     * @return 处理结果
     */
    public Email email() { return email; }
    /**
     * 执行 mobile 对应的业务操作。
     *
     * @return 处理结果
     */
    public Mobile mobile() { return mobile; }
    /**
     * 执行 profile 对应的业务操作。
     *
     * @return 处理结果
     */
    public UserProfile profile() { return profile; }
    /**
     * 执行 status 对应的业务操作。
     *
     * @return 处理结果
     */
    public UserStatus status() { return status; }
    /**
     * 执行 last Login 对应的业务操作。
     *
     * @return 处理结果
     */
    public LoginRecord lastLogin() { return lastLogin; }
    /**
     * 执行 post Ids 对应的业务操作。
     *
     * @return 处理结果
     */
    public Set<Long> postIds() { return Collections.unmodifiableSet(postIds); }

    /**
     * 判断 is Enabled 对应的条件是否成立。
     *
     * @return 处理结果
     */
    public boolean isEnabled() { return status.isEnabled(); }
    /**
     * 判断 is Disabled 对应的条件是否成立。
     *
     * @return 处理结果
     */
    public boolean isDisabled() { return status.isDisabled(); }

    /**
     * 执行 pull Events 对应的业务操作。
     *
     * @return 处理结果
     */
    public List<DomainEvent> pullEvents() {
        List<DomainEvent> result = new ArrayList<>(events);
        events.clear();
        return result;
    }

    /**
     * 执行 equals 对应的业务操作。
     *
     * @param o o 参数
     * @return 处理结果
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User that)) return false;
        return id.equals(that.id);
    }

    /**
     * 判断 hash Code 对应的条件是否成立。
     *
     * @return 处理结果
     */
    @Override
    public int hashCode() { return Objects.hash(id); }

    /**
     * 执行 to String 对应的业务操作。
     *
     * @return 处理结果
     */
    @Override
    public String toString() {
        return "User{id=" + id + ", username=" + username + '}';
    }
}
