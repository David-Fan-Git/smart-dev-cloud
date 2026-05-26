---
name: system-module
description: Core system module — auth, RBAC, user/dept/post management, OAuth2, multi-tenant, dictionary, SMS, mail, notice, social login, operation logging, IP
type: project
---

# develop-module-system

## 概述

系统核心模块。负责整个平台的基础管理功能，是平台必不可少的模块。涵盖认证授权、用户管理、组织架构、角色权限、OAuth2 授权、多租户、数据字典、SMS 短信、邮件、通知公告、社交登录、操作日志等。

- **包路径**: `com.develop.mvp.pk.module.system`
- **服务名**: `system-server`
- **错误码区间**: [1-002-000-000 ~ 1-003-000-000)
- **数据库表前缀**: `system_`
- **跨租户实体**: `MenuDO`（标记 `@TenantIgnore`）、`DictTypeDO`、`DictDataDO`、`ConfigDO`（系统配置不隔离租户）

## 模块结构

```
develop-module-system/
├── develop-module-system-api/          # Feign 接口 + DTO + 枚举 + 错误码
└── develop-module-system-server/       # 实现层
    ├── controller/admin/               # 管理后台 API（/admin-api/system/）
    ├── controller/app/                 # 移动端 API（/app-api/system/）
    ├── service/                        # 业务逻辑
    ├── dal/dataobject/                 # DO (Entity)
    ├── dal/mysql/                      # Mapper
    ├── api/                            # Feign API 实现
    ├── convert/                        # MapStruct 转换器
    ├── job/                            # XXL-Job 处理器
    └── framework/                      # 模块本地配置
```

## 核心功能与 Controller 清单

### 1. 认证授权 (Auth)
| Controller | 路由 | 方法 |
|---|---|---|
| `AdminAuthController` | `/system/auth` | `POST /login`(账号密码登录), `POST /logout`, `POST /refresh-token`, `POST /reset-password`, `GET /login/token/:provider`(社交登录), `POST /login/token` |
| `MobileAuthController` | `/system/auth/mobile` | `POST /mobile-login`(短信验证码登录) |
| `OAuth2OpenController` | `/system/oauth2-open` | 授权码模式端点（authorize/token/check_token） |
| `OAuth2ClientController` | `/system/oauth2-client` | OAuth2 客户端 CRUD |
| `OAuth2TokenController` | `/system/oauth2-token` | Token 管理 |
| `OAuth2ApprovalController` | `/system/oauth2-approval` | 用户审批管理 |
| **验证码**: `CaptchaConfiguration` 集成行为验证码 |

### 2. 用户管理
| Controller | 路由 | 方法 |
|---|---|---|
| `UserController` | `/system/user` | CRUD + `/page`, `/export-excel`, `/import`, `/import-template`, 状态变更 |
| `UserProfileController` | `/system/user/profile` | 个人信息、修改密码、修改头像、上传头像 |

**业务流**: 创建用户 → 关联部门(`deptId`) → 关联岗位(`postIds`) → 分配角色。支持 Excel 批量导入。

### 3. 组织架构
| Controller | 路由 | 方法 |
|---|---|---|
| `DeptController` | `/system/dept` | 树形 CRUD + `/list`(列表) |
| `PostController` | `/system/post` | CRUD + `/page`, `/list-all-simple`(下拉) |

Dept 继承 `TenantBaseDO`，树形结构通过 `parentId` 关联。

### 4. 角色与权限
| Controller | 路由 | 方法 |
|---|---|---|
| `RoleController` | `/system/role` | CRUD + `/page`, `/list-all-simple`, `/export-excel` |
| `MenuController` | `/system/menu` | 树形 CRUD + `/list`, `/list-all-simple`, `/delete-list` |
| `PermissionController` | `/system/permission` | `/assign-role-menu`, `/assign-role-data-scope`, `/assign-user-role`, `/list-role-menus`, `/list-user-roles` |

**关键**: `MenuDO` 继承 `BaseDO`（非 `TenantBaseDO`），标注 `@TenantIgnore`。

### 5. 多租户
| Controller | 路由 |
|---|---|
| `TenantController` | `/system/tenant` — CRUD + `/page`, 状态管理 |
| `TenantPackageController` | `/system/tenant-package` — 租户套餐 CRUD + 菜单权限包 |

通过 `develop-spring-boot-starter-biz-tenant` 实现行级隔离。

### 6. 数据字典
| Controller | 路由 |
|---|---|
| `DictTypeController` | `/system/dict-type` — 字典类型 CRUD + `/page` |
| `DictDataController` | `/system/dict-data` — 字典数据 CRUD + `/page` |

### 7. 消息通知
| Controller | 路由 |
|---|---|
| `SmsChannelController` | `/system/sms-channel` — 短信渠道 CRUD |
| `SmsTemplateController` | `/system/sms-template` — 短信模板 CRUD |
| `SmsLogController` | `/system/sms-log` — 发送记录 |
| `MailAccountController` | `/system/mail-account` — 邮箱账户 CRUD |
| `MailTemplateController` | `/system/mail-template` — 邮件模板 CRUD |
| `MailLogController` | `/system/mail-log` — 发送记录 |
| `NoticeController` | `/system/notice` — 通知公告 CRUD |
| `NotifyTemplateController` | `/system/notify-template` — 站内信模板 |
| `NotifyMessageController` | `/system/notify-message` — 用户消息 |

支持多供应商：阿里云、腾讯云、华为云、七牛云。

### 8. 社交登录
| Controller | 路由 |
|---|---|
| `SocialClientController` | `/system/social-client` — 社交客户端配置 |
| `SocialUserController` | `/system/social-user` — 绑定/解绑 |

基于 JustAuth，支持微信、支付宝、钉钉、GitHub、Gitee 等。

### 9. 日志审计
| Controller | 路由 |
|---|---|
| `LoginLogController` | `/system/login-log` — 登录日志 |
| `OperateLogController` | `/system/operate-log` — 操作日志 |

### 10. IP 地理信息
| Controller | 路由 |
|---|---|
| `AreaController` | `/system/area` — 行政区域查询 |
| `AppAreaController` | `/system/area`(app) — 移动端区域查询 |

## 关键 Entities

| DO | 继承 | 表 | 说明 |
|---|---|---|---|
| `AdminUserDO` | `TenantBaseDO` | `system_users` | 系统用户 |
| `DeptDO` | `TenantBaseDO` | `system_dept` | 部门 |
| `PostDO` | `TenantBaseDO` | `system_post` | 岗位 |
| `RoleDO` | `TenantBaseDO` | `system_role` | 角色（含 `@TableField(typeHandler = JacksonTypeHandler.class) Set<Long> dataScopeDeptIds`） |
| `MenuDO` | `BaseDO` (`@TenantIgnore`) | `system_menu` | 菜单/权限 |
| `TenantDO` | `BaseDO` | `system_tenant` | 租户 |
| `DictTypeDO` | `BaseDO` | `system_dict_type` | 字典类型 |
| `DictDataDO` | `BaseDO` | `system_dict_data` | 字典数据 |

## Feign API 清单

所有接口在 `develop-module-system-api` 中定义，`@FeignClient(name = ApiConstants.NAME)` = `"system-server"`：

| API | 主要方法 | 用途 |
|---|---|---|
| `AdminUserApi` | `getUser`, `getUserList`, `getUserListByDeptIds`, `getUserListByPostIds`, `validateUserList` | 用户查询 + Easy-Trans |
| `DeptApi` | `getDept`, `validateDeptList` | 部门查询 |
| `PostApi` | `getPost`, `validatePostList` | 岗位查询 |
| `RoleApi` | `getRoleFromCache`, `hasAnyRoles` | 角色查询/校验 |
| `PermissionApi` | `getUserRoleIdListByRoleIds` | 权限查询 |
| `DictDataApi` | `getDictData`, `getDictDataMap`, `validateDictDataList` | 字典查询 |
| `LoginLogApi` | `createLoginLog` | 登录日志写入 |
| `OperateLogApi` | `createOperateLog` | 操作日志写入 |
| `MailSendApi` | `sendSingleToUser` | 邮件发送 |
| `SmsSendApi` | `sendSingleToUser` | 短信发送 |
| `SmsCodeApi` | `send`, `use`, `validate` | 短信验证码 |
| `SocialClientApi` | `getSocialClientById`, `getSocialClientBySocialType` | 社交客户端 |
| `SocialUserApi` | `bindUser`, `unbindUser`, `getSocialUserList`, `getSocialUserByUserIdAndType` | 社交用户 |
| `NotifyMessageSendApi` | `sendSingleToUser` | 通知消息发送 |

## 依赖的 Starter

- `develop-spring-boot-starter-web` — REST API
- `develop-spring-boot-starter-security` — 认证授权
- `develop-spring-boot-starter-mybatis` — 数据访问
- `develop-spring-boot-starter-redis` — 缓存 + Token 存储
- `develop-spring-boot-starter-mq` — 异步消息
- `develop-spring-boot-starter-biz-tenant` — 多租户
- `develop-spring-boot-starter-biz-data-permission` — 数据权限
- `develop-spring-boot-starter-excel` — Excel 导入导出

## Framework 配置

位于 `com.develop.mvp.pk.module.system.framework`：

- `SecurityConfiguration` — 安全配置（认证过滤器、授权自定义器）
- `CaptchaConfiguration` — 行为验证码配置
- `DataPermissionConfiguration` — 数据权限配置
- `RpcConfiguration` — Feign 客户端扫描

## 关键点

- **跨租户表**：`MenuDO`、`DictTypeDO`、`DictDataDO` 继承 `BaseDO` 而非 `TenantBaseDO`
- **`MenuDO`** 标注 `@TenantIgnore` 确保不拼接租户条件
- **`RoleDO`** 使用 `@TableName(value = "system_role", autoResultMap = true)` 支持 JSON 字段
- **服务名**: `"system-server"`（`ApiConstants.NAME`），非 `"develop-server"`
- **错误码**: system 模块使用 `1-002-XXX-XXX` 段
