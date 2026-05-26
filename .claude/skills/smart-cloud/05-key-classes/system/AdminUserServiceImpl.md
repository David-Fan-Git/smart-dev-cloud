---
name: AdminUserServiceImpl
description: Admin user CRUD service with validation, password encryption, and role/post association
type: project
---

# AdminUserServiceImpl

## 功能定位

AdminUserServiceImpl 是管理后台用户管理的核心 Service 实现，位于 `develop-module-system` 的 `service.user` 包下，Bean 名为 `"adminUserService"`。

核心职责：
- **用户 CRUD**：创建/更新/删除/分页查询管理员用户
- **唯一性校验**：用户名、邮箱、手机号的全局唯一性保障
- **密码管理**：密码加密（BCrypt）、密码验证、密码修改
- **关联管理**：用户-岗位关联、用户-角色关联（委托 PermissionService）
- **租户约束**：租户账号数量上限控制
- **批量操作**：Excel 导入、批量删除、批量校验
- **操作审计**：通过 `@LogRecord` 记录用户操作的完整审计日志

## 设计模式

| 模式 | 说明 | 代码体现 |
|------|------|----------|
| **Template Method** | CRUD 操作遵循"校验 -> 转换 -> 持久化 -> 关联"固定步骤 | `createUser`, `updateUser`, `deleteUser` |
| **Strategy** | 多维度唯一性校验各成独立方法 | `validateUsernameUnique`, `validateMobileUnique`, `validateEmailUnique` |
| **Mediator** | 通过 Service 组合协调多个模块 | 注入 9 个依赖，协调 Tenant/Dept/Post/Permission/OAuth2 |
| **Decorator** | 操作日志切面 | `@LogRecord` 注解 `@LogRecord` |
| **Protected Variations** | 数据权限隔离 | `DataPermissionUtils.executeIgnore()` |
| **@Lazy Proxy** | 避免循环依赖 | `@Lazy` on `TenantService` + `OAuth2TokenService` |

## 核心逻辑流程

### 创建用户 (createUser)

```
createUser(createReqVO)
  |
  +-- 步骤 1: 校验
  |    +-- 1.1 租户账号数量上限
  |    |    tenantService.handleTenantInfo(tenant -> {
  |    |        if (count >= tenant.getAccountCount()) throw exception(USER_COUNT_MAX);
  |    |    });
  |    |
  |    +-- 1.2 多维度唯一性 + 外键校验
  |         validateUserForCreateOrUpdate(null, username, mobile, email, deptId, postIds)
  |           -> DataPermissionUtils.executeIgnore(() -> {
  |                 // 关闭数据权限，避免查询不到已有数据导致校验失效
  |                 validateUserExists(id);
  |                 validateUsernameUnique(id, username);  // 用户名唯一
  |                 validateMobileUnique(id, mobile);      // 手机号唯一
  |                 validateEmailUnique(id, email);        // 邮箱唯一
  |                 deptService.validateDeptList(deptId);  // 部门存在且启用
  |                 postService.validatePostList(postIds); // 岗位存在且启用
  |              })
  |
  +-- 步骤 2: 持久化
  |    +-- 2.1 BeanUtils.toBean(createReqVO, AdminUserDO.class)
  |    +-- 2.2 user.setStatus(ENABLE) 默认启用
  |    +-- 2.3 user.setPassword(passwordEncoder.encode(password)) BCrypt 加密
  |    +-- 2.4 userMapper.insert(user)
  |    +-- 2.5 插入用户-岗位关联 (userPostMapper.insertBatch)
  |
  +-- 步骤 3: 操作日志上下文
  |    LogRecordContext.putVariable("user", user)
  +-- return user.getId()
```

### 更新用户 (updateUser)

与创建类似，但有两个关键差异：
1. `updateReqVO.setPassword(null)` -- 更新接口不更新密码
2. `updateUserPost()` -- 使用差异算法计算需要新增和删除的岗位

### 岗位差异更新算法

```java
private void updateUserPost(UserSaveReqVO reqVO, AdminUserDO updateObj) {
    Long userId = reqVO.getId();
    Set<Long> dbPostIds = convertSet(userPostMapper.selectListByUserId(userId), UserPostDO::getPostId);
    Set<Long> postIds = CollUtil.emptyIfNull(updateObj.getPostIds());

    Collection<Long> createPostIds = CollUtil.subtract(postIds, dbPostIds); // 需要新增的
    Collection<Long> deletePostIds = CollUtil.subtract(dbPostIds, postIds); // 需要删除的

    if (!CollectionUtil.isEmpty(createPostIds)) {
        userPostMapper.insertBatch(convertList(createPostIds, postId -> new UserPostDO()...));
    }
    if (!CollectionUtil.isEmpty(deletePostIds)) {
        userPostMapper.deleteByUserIdAndPostId(userId, deletePostIds);
    }
}
```

算法：计算 DB 中已有岗位和请求中岗位的差集，批量插入新增、批量删除已移除的。已被保留的岗位不做任何操作。

### 删除用户 (deleteUser)

```
deleteUser(id)
  |
  +-- validateUserExists(id) 校验用户存在
  |
  +-- @Transactional 事务
       +-- userMapper.deleteById(id)               删除用户
       +-- permissionService.processUserDeleted(id) 清理角色关联
       +-- userPostMapper.deleteByUserId(id)        清理岗位关联
       +-- LogRecordContext.putVariable("user", user)
```

**级联删除注意**：不会删除用户日志（LoginLog）、不会删除用户发的流程任务（Flowable Task），只清理角色和岗位关联。

### 禁用用户 (updateUserStatus)

```java
public void updateUserStatus(Long id, Integer status) {
    validateUserExists(id);
    userMapper.updateById(new AdminUserDO().setId(id).setStatus(status));

    // 关键: 禁用用户时，立即作废其所有 Token
    if (CommonStatusEnum.isDisable(status)) {
        oauth2TokenService.removeAccessToken(id, UserTypeEnum.ADMIN.getValue());
    }
}
```

**为什么禁用用户需要删除 Token？**
- 防止用户在禁用状态下仍然通过已有 Token 访问系统
- 删除 Token 后，用户的下次请求会被 TokenAuthenticationFilter 拒绝
- 如果只是修改状态而不删除 Token，已经登录的用户在 Token 过期前仍然可以操作

### 用户分页查询 (getUserPage)

```java
public PageResult<AdminUserDO> getUserPage(UserPageReqVO reqVO) {
    // 如果有角色筛选: 先查角色对应的用户
    Set<Long> userIds = null;
    if (reqVO.getRoleId() != null) {
        userIds = permissionService.getUserRoleIdListByRoleId(singleton(reqVO.getRoleId()));
        if (CollUtil.isEmpty(userIds)) {
            return PageResult.empty(); // 优化: 没有用户拥有该角色，直接返回空
        }
    }

    // 分页查询支持部门筛选 (含子部门)
    return userMapper.selectPage(reqVO, getDeptCondition(reqVO.getDeptId()), userIds);
}
```

`getDeptCondition` 递归查找指定部门及其所有子部门：
```java
private Set<Long> getDeptCondition(Long deptId) {
    if (deptId == null) return Collections.emptySet();
    Set<Long> deptIds = convertSet(deptService.getChildDeptList(deptId), DeptDO::getId);
    deptIds.add(deptId); // 包含自身
    return deptIds;
}
```

## 关键代码剖析

```java
@Service("adminUserService")
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    // 依赖注入: 8 个 Service + 2 个 Mapper + 1 个 Api
    @Resource private AdminUserMapper userMapper;
    @Resource private DeptService deptService;
    @Resource private PostService postService;
    @Resource private PermissionService permissionService;
    @Resource private PasswordEncoder passwordEncoder;
    @Resource @Lazy private TenantService tenantService;
    @Resource @Lazy private OAuth2TokenService oauth2TokenService;
    @Resource private UserPostMapper userPostMapper;
    @Resource private ConfigApi configApi;

    // 导入用户的初始化密码配置键
    static final String USER_INIT_PASSWORD_KEY = "system.user.init-password";
    static final String USER_REGISTER_ENABLED_KEY = "system.user.register-enabled";
}
```

### 批量导入设计 (importUserList)

```
importUserList(importUsers, isUpdateSupport)
  |
  +-- 校验: 导入列表非空 + 初始化密码已配置
  |
  +-- 逐行处理:
       +-- 调用 ValidationUtils.validate() 校验字段 Bean Validation
       +-- 调用 validateUserForCreateOrUpdate() 校验业务规则
       +-- 用户名不存在 -> INSERT (创建)
       +-- 用户名已存在:
            +-- isUpdateSupport=false -> 记录失败
            +-- isUpdateSupport=true  -> UPDATE (更新)
```

使用 `AtomicInteger` 跟踪当前行号，方便错误定位。失败信息收集到 `UserImportRespVO.failureUsernames` (LinkedHashMap 保持顺序)。

## 调用链

```
[Upstream]
  AdminUserController (REST)
    -> AdminUserServiceImpl (本类)
       |
       +-- [校验]  DeptService.validateDeptList()
       +-- [校验]  PostService.validatePostList()
       +-- [校验]  TenantService.handleTenantInfo()
       +-- [角色]  PermissionService.getUserRoleIdListByRoleId()
       +-- [角色]  PermissionService.processUserDeleted()
       +-- [Token] OAuth2TokenService.removeAccessToken()
       +-- [配置]  ConfigApi.getConfigValueByKey()
       |
       +-- [持久化] AdminUserMapper (MyBatis Plus)
       +-- [持久化] UserPostMapper (用户-岗位关联)

[其他调用方]
  AdminAuthService (登录时 getUserByUsername, updateUserLogin)
  OAuth2TokenServiceImpl (Token 创建时查询用户昵称/部门)
```

## 配置与条件

| 配置项 | Key | 说明 |
|--------|-----|------|
| 初始化密码 | `system.user.init-password` | Excel 导入用户的初始密码 |
| 注册开关 | `system.user.register-enabled` | 是否开放自助注册 (`true`/`false`) |
| DataPermission | `@DataPermission(enable = false)` | 校验时关闭数据权限 |

## 生产级关注点

### 1. @Lazy 循环依赖处理

`TenantService` 和 `OAuth2TokenService` 使用了 `@Lazy` 延迟加载。原因：
- `AdminUserServiceImpl` <-- `AdminAuthServiceImpl` -- 通过 `AdminUserService`
- `AdminUserServiceImpl` -- `OAuth2TokenService` --> `OAuth2TokenServiceImpl`
- `AdminAuthServiceImpl` --> `OAuth2TokenService` --> `OAuth2TokenServiceImpl`

如果没有 `@Lazy`，`OAuth2TokenService` 的依赖注入会形成一个循环链。

### 2. 操作日志体系

使用 `@LogRecord` 注解实现操作日志的声明式记录：
```java
@LogRecord(type = SYSTEM_USER_TYPE, subType = SYSTEM_USER_CREATE_SUB_TYPE,
          bizNo = "{{#user.id}}", success = SYSTEM_USER_CREATE_SUCCESS)
```
- `type`: 业务类型（如 `system-user`）
- `subType`: 操作子类型（创建/更新/删除/改密）
- `bizNo`: 业务编号（SpEL 表达式）
- `success`: 成功消息模板（支持 SpEL）
- 配合 `LogRecordContext.putVariable()` 传递上下文变量

### 3. 数据权限隔离

`validateUserForCreateOrUpdate` 中使用 `DataPermissionUtils.executeIgnore()`：
- 原因：数据权限会追加 `dept_id IN (...)` 条件，导致在查询已有用户时可能因为数据权限过滤而查不到
- 后果：查不到已有用户名 -> 校验通过 -> 创建了重复用户名的记录

### 4. 事务边界

- `createUser`：`@Transactional(rollbackFor = Exception.class)`，包括用户创建 + 岗位关联，任何异常回滚
- `deleteUser`：同样绝对事务，用户删除 + 角色清理 + 岗位清理在同一个事务中
- `updateUser`: 事务内更新用户 + 岗位差异更新
- `importUserList`: 注意此处**没有** `@Transactional`！逐条处理，创建失败不影响已导入的用户。这样可以避免一个大 Excel 中一条数据问题导致全部回滚

### 5. 密码安全

- 使用 `PasswordEncoder` 接口（实现为 `BCryptPasswordEncoder`）
- BCrypt 会自动引入 salt，每次加密结果不同
- 无法解密，只能通过 `passwordEncoder.matches(raw, encoded)` 验证
- 密码存储在 `AdminUserDO.password` 字段，BCrypt 编码

### 6. 线程安全

- 无状态 Service（所有依赖通过 `@Resource` 注入），线程安全
- `AtomicInteger` 在 `importUserList` 中是方法内部变量，每次调用新建，无竞态

### 7. 监控建议

- 用户创建/删除/禁用操作已通过 `@LogRecord` 记录操作日志
- 导入操作的失败原因收集到返回 VO 中，前端可展示
- 无自定义 metrics，但可通过操作日志统计用户操作量
